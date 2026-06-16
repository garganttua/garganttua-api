package com.garganttua.api.binding.javalin;

import java.util.List;
import java.util.Objects;

import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.api.commons.endpoint.Interface;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.ArgKey;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.expression.SerializationExpressions;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleStatus;
import com.garganttua.core.reflection.IClass;

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

	/**
	 * Response header carrying the minted authorization (the encoded token) after a
	 * successful {@code authenticate} / {@code refreshAuthorization}. The body is then a
	 * minimal {@code ok} — the token travels in the header, never the body.
	 */
	public static final String AUTHORIZATION_RESPONSE_HEADER = "X-Authorization";

	/** The request arg under which the pipeline publishes the encoded token (set by CREATE/REFRESH_AUTHORIZATION). */
	private static final ArgKey<Object> ENCODED_AUTHORIZATION =
			ArgKey.of("encodedAuthorization", IClass.getClass(Object.class));

	/**
	 * The request arg under which the pipeline publishes the sanitized {@code IAuthentication}
	 * (the login security context — tenant/owner/super/authorities, never credentials/principal)
	 * after authenticate / refreshAuthorization. Rendered as the response body.
	 */
	private static final ArgKey<Object> AUTHENTICATION =
			ArgKey.of("authentication", IClass.getClass(Object.class));

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
		// Access.authenticated/authority=true regardless, and the verify stages would reject an
		// anonymous HTTP caller — silently skipping the business stage. A route is
		// registered only when its operation is actually enabled on the domain.
		List<OperationDefinition> configured = domain.getDomainDefinition().operations();

		// Use cases first: each declared use case is a routable operation carrying its own verb
		// (read→GET, create→POST, update→PUT, delete→DELETE), its own path (defaulting to
		// /{domain}/{name}) and a {uuid} segment when scoped to a single entity. They are
		// registered BEFORE the CRUD table so a literal use-case path (e.g. /users/greet) wins
		// over readOne's /users/{uuid} — Javalin resolves a collision by registration order, so
		// the literal route must come first or readOne would swallow it as uuid="greet". The
		// dispatched operation is the domain's own, carrying the access/authority the use case
		// declared via .security().
		for (OperationDefinition op : configured) {
			if (op.getBusinessOperation() != BusinessOperation.useCase) {
				continue;
			}
			String path = toJavalinPath(op);
			boolean hasUuid = path.contains("{uuid}");
			Handler handler = ctx -> dispatch(domain, op, ctx, hasUuid ? ctx.pathParam("uuid") : null);
			register(server, verbOf(op.technicalOperation()), path, handler);
		}

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

	/** The HTTP verb a use case's technical operation maps to. */
	private static HttpVerb verbOf(com.garganttua.api.commons.operation.TechnicalOperation op) {
		if (op == null) {
			return HttpVerb.GET;
		}
		return switch (op) {
			case create -> HttpVerb.POST;
			case update -> HttpVerb.PUT;
			case delete -> HttpVerb.DELETE;
			case read -> HttpVerb.GET;
		};
	}

	/**
	 * The Javalin route path for an operation: its declared {@link OperationDefinition#getPath()}
	 * with the framework's {@code ${uuid}} placeholder rewritten to Javalin's {@code {uuid}}.
	 */
	private static String toJavalinPath(OperationDefinition op) {
		String path = op.getPath() != null ? op.getPath().path() : null;
		if (path == null || path.isBlank()) {
			return "/";
		}
		return path.replace("${uuid}", "{uuid}");
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
		register(server, verb, path, handler);
	}

	/** Binds a handler to a Javalin route for the given verb. */
	private void register(Javalin server, HttpVerb verb, String path, Handler handler) {
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
			IOperationResponse response = domain.invoke(request);

			// A token-minting op (authenticate / refreshAuthorization) that produced an
			// encoded authorization returns it in the X-Authorization response header; the
			// body is the sanitized IAuthentication (login security context — tenant/owner/
			// super/authorities, never credentials/principal). The token travels in the header,
			// never the body. The failure path is unchanged (applyOutcome surfaces the 4xx).
			Object encoded = request.arg(ENCODED_AUTHORIZATION).orElse(null);
			if (encoded != null && isSuccess(response)) {
				ctx.header(AUTHORIZATION_RESPONSE_HEADER, asTokenString(encoded));
				int status = httpStatus(response.getResponseCode());
				// Rendered in the client's negotiated media (JSON, XML, …) via the serializer
				// registry; degrades to plain "ok" only when no registered serializer satisfies
				// Accept, or when the pipeline published no authentication.
				Object authentication = request.arg(AUTHENTICATION).orElse(null);
				Object body = authentication != null ? authentication : new StatusEnvelope("ok");
				writeEnvelope(ctx, domain, status, body, "ok");
				return;
			}
			applyOutcome(ctx, domain, response);
		} catch (RuntimeException e) {
			// Defensive: the pipeline returns error codes rather than throwing, but a
			// transport-level failure (e.g. no protocol resolved) must still answer.
			ctx.status(500).result("Internal error: " + e.getMessage());
		}
	}

	/** A successful outcome carries a payload, not a {@link Throwable}. */
	private static boolean isSuccess(IOperationResponse response) {
		return response != null && !(response.getResponse() instanceof Throwable);
	}

	/** Renders the encoded token as a header string (it may be a String or a byte[]/Byte[] wire form). */
	private static String asTokenString(Object encoded) {
		if (encoded instanceof String s) {
			return s;
		}
		if (encoded instanceof byte[] b) {
			return new String(b, java.nio.charset.StandardCharsets.UTF_8);
		}
		if (encoded instanceof Byte[] boxed) {
			byte[] out = new byte[boxed.length];
			for (int i = 0; i < boxed.length; i++) {
				out[i] = boxed[i];
			}
			return new String(out, java.nio.charset.StandardCharsets.UTF_8);
		}
		return String.valueOf(encoded);
	}

	/**
	 * Reconciles the HTTP response with the pipeline's {@link IOperationResponse} so the
	 * wire reflects the operation, not the always-200 default. On failure (the response
	 * carries a {@link Throwable}) the status comes from the response code; on success
	 * the RESPONSE stage already serialized the body, so only the status is corrected.
	 * <p>
	 * The error body is rendered in the client's negotiated media (JSON, XML, …) via
	 * the serializer registry. It degrades to {@code text/plain} (the raw message) only
	 * when no registered serializer satisfies {@code Accept} — the very situation a
	 * {@code 406} reports, where answering in a served media would repeat the
	 * content-negotiation violation being signalled. {@code text/plain} every client accepts.
	 */
	private void applyOutcome(Context ctx, IDomain<?> domain, IOperationResponse response) {
		if (response == null) {
			return;
		}
		int status = httpStatus(response.getResponseCode());
		Object payload = response.getResponse();
		if (payload instanceof Throwable t) {
			String message = (t.getMessage() != null && !t.getMessage().isBlank())
					? t.getMessage() : t.getClass().getSimpleName();
			writeEnvelope(ctx, domain, status, new ErrorEnvelope(message), message);
		} else {
			ctx.status(status);
		}
	}

	/**
	 * Writes a small envelope object as the response body in the client's negotiated
	 * media. Reuses the framework's RFC 7231 negotiation ({@link SerializationExpressions#negotiateSerializer})
	 * over the API's serializer registry, labels the response with the chosen media type,
	 * and falls back to {@code text/plain} (the supplied raw text) only when the API has
	 * no serializer or none satisfies {@code Accept}.
	 */
	private void writeEnvelope(Context ctx, IDomain<?> domain, int status, Object envelope, String fallbackText) {
		IApi api = apiOf(domain);
		if (api != null) {
			try {
				ISerializer serializer = SerializationExpressions.negotiateSerializer(api, ctx.header("Accept"));
				byte[] body = serializer.serialize(envelope);
				ctx.status(status);
				if (serializer.mimeType() != null) {
					ctx.contentType(serializer.mimeType().toString());
				}
				ctx.result(body);
				return;
			} catch (Exception negotiationOrSerializationFailed) {
				// No serializer satisfies Accept (or serialization failed) — degrade to plain text.
			}
		}
		ctx.status(status).contentType("text/plain").result(fallbackText);
	}

	/** The API context backing a domain (the serializer registry lives on it), or null. */
	private static IApi apiOf(IDomain<?> domain) {
		return domain != null ? domain.getApiContext() : null;
	}

	/** Minimal success envelope: serializes to {@code {"status":"ok"}} (JSON) / {@code <StatusEnvelope><status>ok</status></StatusEnvelope>} (XML). */
	public static final class StatusEnvelope {
		private final String status;
		public StatusEnvelope(String status) { this.status = status; }
		public String getStatus() { return this.status; }
	}

	/** Minimal error envelope: serializes to {@code {"error":"…"}} (JSON) / {@code <ErrorEnvelope><error>…</error></ErrorEnvelope>} (XML). */
	public static final class ErrorEnvelope {
		private final String error;
		public ErrorEnvelope(String error) { this.error = error; }
		public String getError() { return this.error; }
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
			case NOT_ACCEPTABLE -> 406;
			case CONFLICT -> 409;
			case UNSUPPORTED_MEDIA_TYPE -> 415;
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
