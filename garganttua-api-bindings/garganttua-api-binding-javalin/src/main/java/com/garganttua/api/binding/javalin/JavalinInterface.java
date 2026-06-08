package com.garganttua.api.binding.javalin;

import java.util.List;
import java.util.Objects;

import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.api.commons.endpoint.Interface;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleStatus;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;

/**
 * A Javalin-backed {@link IInterface} — the HTTP transport entry point for a domain.
 * <p>
 * It owns a Javalin server and, on {@link #handle(IDomain)}, wires the standard CRUD
 * route table for the domain it is attached to:
 * <pre>
 *   POST    /{domain}            → createOne
 *   GET     /{domain}            → readAll
 *   GET     /{domain}/{uuid}     → readOne
 *   PUT     /{domain}/{uuid}     → updateOne
 *   DELETE  /{domain}/{uuid}     → deleteOne
 *   DELETE  /{domain}            → deleteAll
 * </pre>
 * Each handler hands the live Javalin {@link Context} to the pipeline as
 * {@code rawRequest} and invokes the domain. Transport extraction and response
 * writing are delegated to the companion {@link JavalinProtocol} (Mode A): this
 * interface decides <em>which</em> operation (routing, and the {@code uuid} path
 * parameter), the protocol adapts the <em>how</em> (body, caller, headers, response).
 * <p>
 * Register the protocol once on the API and attach this interface to the domain:
 * <pre>{@code
 *   ApiBuilder.builder()
 *       .protocol(new JavalinProtocol())
 *       .domain(User.class)
 *           .interfasse(new JavalinInterface(7000))
 *           .entity()...
 *       .up()
 *       .build();
 * }</pre>
 *
 * <h2>Scope</h2>
 * Per the per-domain interface model, one {@code JavalinInterface} owns one Javalin
 * server. Two domains each carrying their own instance must use distinct ports.
 * The lifecycle guards ({@link #onStart}/{@link #onStop}) are idempotent, so a single
 * instance shared across domains via a supplier starts/stops its server exactly once
 * while registering every domain's routes.
 * <p>
 * <b>External server.</b> Pass a {@link Javalin} via {@link #JavalinInterface(Javalin)}
 * to attach to a caller-provided server (a shared one, or a Spring Boot-managed one):
 * the interface registers its routes on it but never starts or stops it — the owner
 * keeps full control of the lifecycle. Several domains can share one server this way.
 */
@Interface
public class JavalinInterface implements IInterface {

	/** Default HTTP port when none is supplied. */
	public static final int DEFAULT_PORT = 7000;

	private final int port;
	/** Whether this interface owns (creates + starts + stops) its Javalin server. */
	private final boolean ownsServer;
	private Javalin app;
	private boolean started;
	private LifecycleStatus status = LifecycleStatus.NEW;

	/** Binds an owned server to {@link #DEFAULT_PORT}. Required no-arg form for {@code .interfasse(IClass)}. */
	public JavalinInterface() {
		this(DEFAULT_PORT);
	}

	/** Owns a Javalin server bound to {@code port}; this interface starts and stops it. */
	public JavalinInterface(int port) {
		this.port = port;
		this.ownsServer = true;
	}

	/**
	 * Attaches to a caller-provided Javalin server (e.g. a shared server or a
	 * Spring Boot-managed one). The interface registers its routes on it but does
	 * <strong>not</strong> start or stop it — the owner manages the lifecycle.
	 * Multiple domains can pass the same instance to share one server.
	 */
	public JavalinInterface(Javalin app) {
		this.app = Objects.requireNonNull(app, "Javalin app cannot be null");
		this.ownsServer = false;
		this.port = -1;
	}

	/** The bound port for an owned server, or {@code -1} when the server is provided externally. */
	public int getPort() {
		return this.port;
	}

	/** {@code true} when this interface owns (and manages the lifecycle of) its Javalin server. */
	public boolean ownsServer() {
		return this.ownsServer;
	}

	/** Whether the owned server is currently bound. Always {@code false} in external-server mode. */
	public boolean isStarted() {
		return this.started;
	}

	/** Returns the Javalin server, lazily creating an owned one (no port binding until {@link #onStart}). */
	private Javalin app() {
		if (this.app == null) {
			this.app = Javalin.create();
		}
		return this.app;
	}

	@Override
	public void handle(IDomain<?> domain) {
		String base = "/" + domain.getDomainName();
		String one = base + "/{uuid}";
		Javalin server = app();

		// Resolve each route's operation from the domain's CONFIGURED operations so the
		// access/authority the request carries matches what the domain declared (e.g.
		// readAllAccess(anonymous)). Hardcoding *WithStandardSecurity would send
		// Access.tenant/authority=true regardless, and the verify stages would reject an
		// anonymous HTTP caller — silently skipping the business stage. A route is
		// registered only when its operation is actually enabled on the domain.
		List<OperationDefinition> configured = domain.getDomainDefinition().operations();

		route(server, HttpVerb.POST,   base, domain, configured, BusinessOperation.create,    false);
		route(server, HttpVerb.GET,    base, domain, configured, BusinessOperation.readAll,   false);
		route(server, HttpVerb.GET,    one,  domain, configured, BusinessOperation.readOne,   true);
		route(server, HttpVerb.PUT,    one,  domain, configured, BusinessOperation.update,    true);
		route(server, HttpVerb.DELETE, one,  domain, configured, BusinessOperation.deleteOne, true);
		route(server, HttpVerb.DELETE, base, domain, configured, BusinessOperation.deleteAll, false);

		// Authentication entry point (anonymous): the credentials travel in the body as
		// an AuthenticationRequest. Registered only when the domain has an authenticator
		// (its authenticate operation is then present in the configured operations).
		route(server, HttpVerb.POST, base + "/authenticate", domain, configured,
				BusinessOperation.authenticate, false);
	}

	private enum HttpVerb { GET, POST, PUT, DELETE }

	/**
	 * Registers one route, but only when the domain actually exposes {@code bo} (the
	 * matching {@link OperationDefinition} is present in its configured operations).
	 * The dispatched operation is the domain's own — carrying its declared
	 * access/authority — never a synthesized standard-security one.
	 */
	private void route(Javalin server, HttpVerb verb, String path, IDomain<?> domain,
			List<OperationDefinition> configured, BusinessOperation bo, boolean hasUuid) {
		OperationDefinition operation = findOperation(configured, bo);
		if (operation == null) {
			return; // operation not enabled on this domain — no route
		}
		Handler handler = ctx -> dispatch(domain, operation, ctx, hasUuid ? ctx.pathParam("uuid") : null);
		switch (verb) {
			case GET -> server.get(path, handler);
			case POST -> server.post(path, handler);
			case PUT -> server.put(path, handler);
			case DELETE -> server.delete(path, handler);
		}
	}

	private static OperationDefinition findOperation(List<OperationDefinition> operations, BusinessOperation bo) {
		if (operations == null) {
			return null;
		}
		for (OperationDefinition op : operations) {
			if (op.getBusinessOperation() == bo) {
				return op;
			}
		}
		return null;
	}

	/**
	 * Builds the operation request, hands the {@link Context} to the pipeline as
	 * {@code rawRequest}, invokes the domain, and reconciles the HTTP response with
	 * the operation's outcome.
	 * <p>
	 * The Mode-A RESPONSE stage serializes the body onto the {@code Context}, but it
	 * cannot set the status (the pipeline does not yet write an {@code exitCode}) and
	 * leaves a stale/empty body on failures — so the wire response would otherwise
	 * always read 200 regardless of the pipeline outcome. We therefore make the
	 * transport authoritative: the status follows {@link IOperationResponse#getResponseCode()},
	 * and on failure the carried {@link Throwable}'s message becomes the body.
	 */
	private void dispatch(IDomain<?> domain, OperationDefinition operation, Context ctx, String uuid) {
		try {
			IOperationRequest request = IOperationRequest.create();
			request.arg(IOperationRequest.OPERATION, operation);
			request.arg(IOperationRequest.RAW_REQUEST, ctx);
			if (uuid != null) {
				request.arg(IOperationRequest.ENTITY_UUID, uuid);
			}
			applyOutcome(ctx, domain.invoke(request));
		} catch (RuntimeException e) {
			// Defensive: the pipeline returns error codes rather than throwing, but a
			// transport-level failure (e.g. no protocol resolved) must still answer.
			ctx.status(500).result("Internal error: " + e.getMessage());
		}
	}

	/**
	 * Reconciles the HTTP response with the pipeline's {@link IOperationResponse} so the
	 * wire reflects the operation, not the always-200 default. On failure (the response
	 * carries a {@link Throwable}) the status comes from the response code and the body
	 * is the error message; on success the RESPONSE stage already serialized the body,
	 * so only the status is corrected.
	 */
	private void applyOutcome(Context ctx, IOperationResponse response) {
		if (response == null) {
			return;
		}
		int status = httpStatus(response.getResponseCode());
		Object payload = response.getResponse();
		if (payload instanceof Throwable t) {
			String message = (t.getMessage() != null && !t.getMessage().isBlank())
					? t.getMessage() : t.getClass().getSimpleName();
			ctx.status(status).result(message);
		} else {
			ctx.status(status);
		}
	}

	/** Maps the framework's response code to an HTTP status. */
	private static int httpStatus(OperationResponseCode code) {
		if (code == null) {
			return 200;
		}
		return switch (code) {
			case OK, UPDATED, DELETED -> 200;
			case CREATED -> 201;
			case CLIENT_ERROR -> 400;
			case UNAUTHORIZED -> 401;
			case FORBIDDEN -> 403;
			case NOT_FOUND -> 404;
			case NOT_AVAILABLE -> 503;
			case SERVER_ERROR -> 500;
		};
	}

	@Override
	public ILifecycle onInit() {
		this.status = LifecycleStatus.INITIALIZED;
		return this;
	}

	@Override
	public ILifecycle onStart() {
		// Only an owned server is started here; an externally-provided server is
		// started by its owner (the interface merely registered its routes on it).
		if (this.ownsServer && !this.started) {
			app().start(this.port);
			this.started = true;
		}
		this.status = LifecycleStatus.STARTED;
		return this;
	}

	@Override
	public ILifecycle onStop() {
		if (this.ownsServer && this.started) {
			app().stop();
			this.started = false;
		}
		this.status = LifecycleStatus.STOPPED;
		return this;
	}

	@Override
	public ILifecycle onFlush() {
		this.status = LifecycleStatus.FLUSHED;
		return this;
	}

	@Override
	public ILifecycle onReload() {
		return this;
	}

	@Override
	public LifecycleStatus status() {
		return this.status;
	}
}
