package com.garganttua.api.binding.javalin;

import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.api.commons.endpoint.Interface;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleStatus;
import com.garganttua.core.reflection.IClass;

import io.javalin.Javalin;
import io.javalin.http.Context;

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
 */
@Interface
public class JavalinInterface implements IInterface {

	/** Default HTTP port when none is supplied. */
	public static final int DEFAULT_PORT = 7000;

	private final int port;
	private Javalin app;
	private boolean started;
	private LifecycleStatus status = LifecycleStatus.NEW;

	/** Binds the server to {@link #DEFAULT_PORT}. Required no-arg form for {@code .interfasse(IClass)}. */
	public JavalinInterface() {
		this(DEFAULT_PORT);
	}

	public JavalinInterface(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	public boolean isStarted() {
		return this.started;
	}

	/** Lazily materialises the Javalin server (no port binding happens until {@link #onStart}). */
	private Javalin app() {
		if (this.app == null) {
			this.app = Javalin.create();
		}
		return this.app;
	}

	@Override
	public void handle(IDomain<?> domain) {
		String name = domain.getDomainName();
		IClass<?> entity = domain.getEntityClass();
		String base = "/" + name;
		String one = base + "/{uuid}";
		Javalin server = app();

		server.post(base, ctx ->
				dispatch(domain, OperationDefinition.createOneWithStandardSecurity(name, entity), ctx, null));
		server.get(base, ctx ->
				dispatch(domain, OperationDefinition.readAllWithStandardSecurity(name, entity), ctx, null));
		server.get(one, ctx ->
				dispatch(domain, OperationDefinition.readOneWithStandardSecurity(name, entity), ctx, ctx.pathParam("uuid")));
		server.put(one, ctx ->
				dispatch(domain, OperationDefinition.updateOneWithStandardSecurity(name, entity), ctx, ctx.pathParam("uuid")));
		server.delete(one, ctx ->
				dispatch(domain, OperationDefinition.deleteOneWithStandardSecurity(name, entity), ctx, ctx.pathParam("uuid")));
		server.delete(base, ctx ->
				dispatch(domain, OperationDefinition.deleteAllWithStandardSecurity(name, entity), ctx, null));
	}

	/**
	 * Builds the operation request, hands the {@link Context} to the pipeline as
	 * {@code rawRequest}, and invokes the domain. The pipeline's RESPONSE stage drives
	 * {@link JavalinProtocol#buildResponse} which writes status + body onto the very
	 * {@code Context} — so there is nothing to write back here on the happy path.
	 */
	private void dispatch(IDomain<?> domain, OperationDefinition operation, Context ctx, String uuid) {
		try {
			IOperationRequest request = IOperationRequest.create();
			request.arg(IOperationRequest.OPERATION, operation);
			request.arg(IOperationRequest.RAW_REQUEST, ctx);
			if (uuid != null) {
				request.arg(IOperationRequest.ENTITY_UUID, uuid);
			}
			domain.invoke(request);
		} catch (RuntimeException e) {
			// Defensive: the pipeline returns error codes rather than throwing, but a
			// transport-level failure (e.g. no protocol resolved) must still answer.
			ctx.status(500).result("Internal error: " + e.getMessage());
		}
	}

	@Override
	public ILifecycle onInit() {
		this.status = LifecycleStatus.INITIALIZED;
		return this;
	}

	@Override
	public ILifecycle onStart() {
		if (!this.started) {
			app().start(this.port);
			this.started = true;
		}
		this.status = LifecycleStatus.STARTED;
		return this;
	}

	@Override
	public ILifecycle onStop() {
		if (this.started) {
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
