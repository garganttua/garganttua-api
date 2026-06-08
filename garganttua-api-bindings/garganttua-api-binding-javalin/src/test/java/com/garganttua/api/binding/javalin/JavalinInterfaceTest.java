package com.garganttua.api.binding.javalin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.BusinessOperation;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.IRequestBuilder;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.service.OperationResponse;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleStatus;
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservableEvent;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.IWorkflow;
import com.garganttua.core.workflow.WorkflowExecutionOptions;

import io.javalin.Javalin;
import io.javalin.http.Context;

/**
 * End-to-end test of {@link JavalinInterface} over a real Javalin server with a real
 * HTTP client. The interface is attached to a {@link CapturingDomain} stand-in that
 * records exactly what the route dispatch handed the pipeline, then drives
 * {@link JavalinProtocol} to write the response back onto the live {@code Context}.
 * <p>
 * This proves the interface's own contract — the CRUD route table maps each HTTP
 * verb+path to the right {@link OperationDefinition}, extracts the {@code uuid} path
 * parameter, and hands the {@code Context} through as {@code rawRequest} — without
 * needing the full API engine.
 */
@DisplayName("JavalinInterface — HTTP transport entry point")
class JavalinInterfaceTest {

	/** Entity stand-in — only its class identity matters to the operation definitions. */
	public static class FakeEntity {}

	/**
	 * Records the dispatched request and answers via the protocol. Every method not
	 * exercised by the interface is a harmless stub.
	 */
	static IClass<?> fakeEntityClass() {
		return IClass.getClass(FakeEntity.class);
	}

	/** The full standard CRUD operation set a domain would expose by default. */
	static List<OperationDefinition> standardOperations() {
		IClass<?> e = fakeEntityClass();
		return List.of(
				OperationDefinition.createOneWithStandardSecurity("users", e),
				OperationDefinition.readAllWithStandardSecurity("users", e),
				OperationDefinition.readOneWithStandardSecurity("users", e),
				OperationDefinition.updateOneWithStandardSecurity("users", e),
				OperationDefinition.deleteOneWithStandardSecurity("users", e),
				OperationDefinition.deleteAllWithStandardSecurity("users", e));
	}

	static class CapturingDomain implements IDomain<Object> {
		final JavalinProtocol protocol = new JavalinProtocol();
		final IDomainDefinition<Object> definition;
		volatile OperationDefinition lastOperation;
		volatile String lastUuid;
		volatile Object lastRawRequest;
		volatile String lastMethod;
		volatile String lastPath;
		volatile byte[] lastBody;
		volatile ICaller lastCaller;
		volatile int invokeCount;
		/** When set, invoke() returns this (so applyOutcome reconciles the response). */
		volatile IOperationResponse responseOverride;

		@SuppressWarnings("unchecked")
		CapturingDomain(List<OperationDefinition> operations) {
			this.definition = mock(IDomainDefinition.class);
			when(this.definition.operations()).thenReturn(operations);
		}

		@Override public String getDomainName() { return "users"; }
		@Override @SuppressWarnings("unchecked")
		public IClass<Object> getEntityClass() { return (IClass<Object>) (IClass<?>) IClass.getClass(FakeEntity.class); }

		@Override
		public IOperationResponse invoke(IOperationRequest request) {
			this.invokeCount++;
			this.lastOperation = request.arg(IOperationRequest.OPERATION).orElse(null);
			this.lastUuid = request.arg(IOperationRequest.ENTITY_UUID).orElse(null);
			Object raw = request.arg(IOperationRequest.RAW_REQUEST).orElse(null);
			this.lastRawRequest = raw;
			try {
				if (raw instanceof Context ctx) {
					this.lastMethod = protocol.getMethod(ctx);
					this.lastPath = protocol.getPath(ctx);
					this.lastBody = protocol.getRawBody(ctx);
					this.lastCaller = protocol.getCaller(ctx);
					protocol.buildResponse(ctx,
							("ok:" + this.lastOperation.getBusinessOperation()).getBytes(StandardCharsets.UTF_8), 200);
				}
			} catch (ApiException e) {
				throw new RuntimeException(e);
			}
			return this.responseOverride;
		}

		@Override public IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options) { return invoke(request); }

		@Override public IDomainDefinition<Object> getDomainDefinition() { return this.definition; }

		// --- unused stubs ---
		@Override public IRepository getRepository() { return null; }
		@Override public IWorkflow getWorkflow() { return null; }
		@Override public IRequestBuilder request() { return null; }
		@Override public void addObserver(IObserver<ObservableEvent> observer) { }
		@Override public void removeObserver(IObserver<ObservableEvent> observer) { }
		@Override public ILifecycle onInit() { return this; }
		@Override public ILifecycle onStart() { return this; }
		@Override public ILifecycle onStop() { return this; }
		@Override public ILifecycle onFlush() { return this; }
		@Override public ILifecycle onReload() { return this; }
		@Override public LifecycleStatus status() { return LifecycleStatus.NEW; }
	}

	private static int freePort() {
		try (ServerSocket s = new ServerSocket(0)) {
			return s.getLocalPort();
		} catch (IOException e) {
			throw new RuntimeException("Could not allocate a free port", e);
		}
	}

	@BeforeAll
	static void installReflection() {
		// Cold-start garganttua-core's ServiceLoader so IClass.getClass(...) has an
		// IReflection installed (mirrors core's ReflectionTestBootstrap).
		com.garganttua.core.bootstrap.dsl.Bootstrap.builder();
	}

	private int port;
	private JavalinInterface iface;
	private CapturingDomain domain;
	private HttpClient http;

	@BeforeEach
	void setUp() {
		startWith(standardOperations());
	}

	/** (Re)starts the interface attached to a domain exposing the given configured operations. */
	private void startWith(List<OperationDefinition> operations) {
		if (iface != null && iface.isStarted()) {
			iface.onStop();
		}
		port = freePort();
		domain = new CapturingDomain(operations);
		iface = new JavalinInterface(port);
		iface.handle(domain);
		iface.onInit();
		iface.onStart();
		http = HttpClient.newHttpClient();
	}

	@AfterEach
	void tearDown() {
		iface.onStop();
	}

	private HttpResponse<String> send(String method, String path, String body) throws Exception {
		HttpRequest.BodyPublisher pub = body == null
				? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofString(body);
		HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + path))
				.method(method, pub)
				.build();
		return http.send(req, HttpResponse.BodyHandlers.ofString());
	}

	@Nested
	@DisplayName("Lifecycle")
	class Lifecycle {
		@Test
		@DisplayName("onStart binds the port and marks STARTED")
		void started() {
			assertTrue(iface.isStarted(), "interface must report started");
			assertEquals(LifecycleStatus.STARTED, iface.status());
			assertEquals(port, iface.getPort());
		}

		@Test
		@DisplayName("onStop releases the server and marks STOPPED")
		void stopped() {
			iface.onStop();
			assertFalse(iface.isStarted(), "interface must report stopped");
			assertEquals(LifecycleStatus.STOPPED, iface.status());
		}

		@Test
		@DisplayName("onStart is idempotent — a second call does not rebind or throw")
		void startIdempotent() {
			assertDoesNotThrow(() -> iface.onStart());
			assertTrue(iface.isStarted());
		}
	}

	@Nested
	@DisplayName("CRUD route table → OperationDefinition")
	class RouteTable {

		@Test
		@DisplayName("POST /users → create, no uuid, Context handed as rawRequest")
		void postCreate() throws Exception {
			HttpResponse<String> resp = send("POST", "/users", "Alice|alice@x.io");

			assertEquals(200, resp.statusCode());
			assertEquals("ok:create", resp.body(), "response must come from the protocol round-trip");
			assertEquals(1, domain.invokeCount);
			assertEquals(BusinessOperation.create, domain.lastOperation.getBusinessOperation());
			assertNull(domain.lastUuid, "collection POST carries no uuid");
			assertInstanceOf(Context.class, domain.lastRawRequest, "rawRequest must be the live Javalin Context");
			assertEquals("POST", domain.lastMethod);
			assertEquals("/users", domain.lastPath);
			assertArrayEquals("Alice|alice@x.io".getBytes(StandardCharsets.UTF_8), domain.lastBody,
					"the POST body must reach the protocol verbatim");
		}

		@Test
		@DisplayName("GET /users → readAll, no uuid")
		void getReadAll() throws Exception {
			HttpResponse<String> resp = send("GET", "/users", null);

			assertEquals(200, resp.statusCode());
			assertEquals("ok:readAll", resp.body());
			assertEquals(BusinessOperation.readAll, domain.lastOperation.getBusinessOperation());
			assertNull(domain.lastUuid);
			assertNull(domain.lastBody, "a GET has no body");
		}

		@Test
		@DisplayName("GET /users/{uuid} → readOne, uuid captured from the path")
		void getReadOne() throws Exception {
			HttpResponse<String> resp = send("GET", "/users/abc-123", null);

			assertEquals(200, resp.statusCode());
			assertEquals("ok:readOne", resp.body());
			assertEquals(BusinessOperation.readOne, domain.lastOperation.getBusinessOperation());
			assertEquals("abc-123", domain.lastUuid, "the {uuid} path param must be threaded as ENTITY_UUID");
			assertEquals("/users/abc-123", domain.lastPath);
		}

		@Test
		@DisplayName("PUT /users/{uuid} → update, uuid + body captured")
		void putUpdate() throws Exception {
			HttpResponse<String> resp = send("PUT", "/users/u-9", "Bob|bob@x.io");

			assertEquals(200, resp.statusCode());
			assertEquals("ok:update", resp.body());
			assertEquals(BusinessOperation.update, domain.lastOperation.getBusinessOperation());
			assertEquals("u-9", domain.lastUuid);
			assertArrayEquals("Bob|bob@x.io".getBytes(StandardCharsets.UTF_8), domain.lastBody);
		}

		@Test
		@DisplayName("DELETE /users/{uuid} → deleteOne, uuid captured")
		void deleteOne() throws Exception {
			HttpResponse<String> resp = send("DELETE", "/users/u-7", null);

			assertEquals(200, resp.statusCode());
			assertEquals("ok:deleteOne", resp.body());
			assertEquals(BusinessOperation.deleteOne, domain.lastOperation.getBusinessOperation());
			assertEquals("u-7", domain.lastUuid);
		}

		@Test
		@DisplayName("DELETE /users → deleteAll, no uuid")
		void deleteAll() throws Exception {
			HttpResponse<String> resp = send("DELETE", "/users", null);

			assertEquals(200, resp.statusCode());
			assertEquals("ok:deleteAll", resp.body());
			assertEquals(BusinessOperation.deleteAll, domain.lastOperation.getBusinessOperation());
			assertNull(domain.lastUuid);
		}
	}

	@Nested
	@DisplayName("Outcome reconciliation (response matches the pipeline, not always 200)")
	class OutcomeReconciliation {

		@Test
		@DisplayName("a failure response sets the error status and the message body")
		void failureSurfacesStatusAndMessage() throws Exception {
			domain.responseOverride = new OperationResponse(
					OperationResponseCode.NOT_FOUND, new ApiException("entity not found: u-404"));

			HttpResponse<String> resp = send("GET", "/users/u-404", null);

			assertEquals(404, resp.statusCode(),
					"the HTTP status must follow the pipeline's response code, not default to 200");
			assertEquals("entity not found: u-404", resp.body(),
					"the body must be the error message, not the stale pipeline output");
		}

		@Test
		@DisplayName("UNAUTHORIZED → 401 with the message")
		void unauthorizedMaps401() throws Exception {
			domain.responseOverride = new OperationResponse(
					OperationResponseCode.UNAUTHORIZED, new ApiException("missing authorization"));

			HttpResponse<String> resp = send("GET", "/users", null);

			assertEquals(401, resp.statusCode());
			assertEquals("missing authorization", resp.body());
		}

		@Test
		@DisplayName("CREATED → 201 (status corrected, serialized body kept)")
		void createdMaps201() throws Exception {
			domain.responseOverride = new OperationResponse(OperationResponseCode.CREATED, "ignored-non-throwable");

			HttpResponse<String> resp = send("POST", "/users", "body");

			assertEquals(201, resp.statusCode(),
					"a CREATED outcome must surface as 201, not the always-200 default");
			assertEquals("ok:create", resp.body(),
					"on success the serialized body from the pipeline is kept");
		}
	}

	@Nested
	@DisplayName("External server (Javalin provided from outside)")
	class ExternalServer {

		@Test
		@DisplayName("constructed with a Javalin app: does not own the server, no port")
		void doesNotOwnServer() {
			JavalinInterface ext = new JavalinInterface(Javalin.create());
			assertFalse(ext.ownsServer(), "an externally-provided server is not owned");
			assertEquals(-1, ext.getPort(), "no bound port in external-server mode");
		}

		@Test
		@DisplayName("rejects a null external app")
		void rejectsNull() {
			assertThrows(NullPointerException.class, () -> new JavalinInterface((Javalin) null));
		}

		@Test
		@DisplayName("registers routes on the external app but never starts or stops it")
		void registersWithoutManagingLifecycle() throws Exception {
			int p = freePort();
			Javalin external = Javalin.create();
			CapturingDomain dom = new CapturingDomain(standardOperations());
			JavalinInterface ext = new JavalinInterface(external);
			ext.handle(dom);
			ext.onInit();
			ext.onStart();
			assertFalse(ext.isStarted(), "external-server mode must bind nothing on onStart");

			external.start(p); // the OWNER starts the shared server
			HttpClient client = HttpClient.newHttpClient();
			try {
				HttpResponse<String> served = client.send(
						HttpRequest.newBuilder().uri(URI.create("http://localhost:" + p + "/users")).GET().build(),
						HttpResponse.BodyHandlers.ofString());
				assertEquals(200, served.statusCode(), "routes registered on the external app must serve");
				assertEquals(BusinessOperation.readAll, dom.lastOperation.getBusinessOperation());

				ext.onStop(); // must NOT stop the externally-owned server
				HttpResponse<String> stillServing = client.send(
						HttpRequest.newBuilder().uri(URI.create("http://localhost:" + p + "/users")).GET().build(),
						HttpResponse.BodyHandlers.ofString());
				assertEquals(200, stillServing.statusCode(),
						"onStop() must not stop a server the interface does not own");
			} finally {
				external.stop();
			}
		}
	}

	@Nested
	@DisplayName("Caller seeding")
	class CallerSeeding {
		@Test
		@DisplayName("no identity header → anonymous caller reaches the pipeline")
		void anonymous() throws Exception {
			send("GET", "/users", null);
			assertNotNull(domain.lastCaller);
			assertTrue(domain.lastCaller.anonymous(), "header-less request must seed an anonymous caller");
		}

		@Test
		@DisplayName("X-Tenant-Id / X-Caller-Id headers are carried into the caller")
		void withHeaders() throws Exception {
			HttpRequest req = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:" + port + "/users"))
					.header(JavalinProtocol.TENANT_HEADER, "T-42")
					.header(JavalinProtocol.CALLER_HEADER, "C-7")
					.GET()
					.build();
			http.send(req, HttpResponse.BodyHandlers.ofString());

			assertNotNull(domain.lastCaller);
			assertFalse(domain.lastCaller.anonymous());
			assertEquals("T-42", domain.lastCaller.tenantId());
			assertEquals("C-7", domain.lastCaller.callerId());
			assertFalse(domain.lastCaller.superTenant(), "transport must never assert superTenant");
		}
	}

	@Nested
	@DisplayName("Configured operations (regression: HTTP reads must use the domain's own op)")
	class ConfiguredOperations {

		@Test
		@DisplayName("dispatches the domain's CONFIGURED operation — its access, not a hardcoded standard one")
		void dispatchesConfiguredOperation() throws Exception {
			IClass<?> e = fakeEntityClass();
			// readAll configured as ANONYMOUS (as garganttua-api-example does); everything
			// else standard. The interface must dispatch THIS op, not readAllWithStandardSecurity.
			OperationDefinition anonymousReadAll =
					OperationDefinition.readAll("users", e, false, null, Access.anonymous);
			startWith(List.of(
					OperationDefinition.createOneWithStandardSecurity("users", e),
					anonymousReadAll));

			send("GET", "/users", null);

			assertNotNull(domain.lastOperation, "the read must reach the domain");
			assertEquals(BusinessOperation.readAll, domain.lastOperation.getBusinessOperation());
			assertEquals(Access.anonymous, domain.lastOperation.access(),
					"the dispatched op must carry the domain's configured access, not Access.tenant");
			assertFalse(domain.lastOperation.authority(),
					"the dispatched op must carry the domain's configured authority flag");
			assertSame(anonymousReadAll, domain.lastOperation,
					"the interface must hand through the very configured OperationDefinition");
		}

		@Test
		@DisplayName("POST /{domain}/authenticate routes to the authenticate op when the domain exposes it")
		void authenticateRouteExposed() throws Exception {
			IClass<?> e = fakeEntityClass();
			startWith(List.of(
					OperationDefinition.readAllWithStandardSecurity("users", e),
					OperationDefinition.authenticate("users", e)));

			HttpResponse<String> resp = send("POST", "/users/authenticate", "john|pw|T1");

			assertEquals(200, resp.statusCode());
			assertEquals(BusinessOperation.authenticate, domain.lastOperation.getBusinessOperation(),
					"the login route must dispatch the authenticate operation");
			assertArrayEquals("john|pw|T1".getBytes(StandardCharsets.UTF_8), domain.lastBody,
					"the credentials body must reach the protocol");
		}

		@Test
		@DisplayName("no authenticate route when the domain has no authenticator (404)")
		void noAuthenticateRouteWhenAbsent() throws Exception {
			IClass<?> e = fakeEntityClass();
			startWith(List.of(OperationDefinition.readAllWithStandardSecurity("users", e)));

			assertEquals(404, send("POST", "/users/authenticate", "x").statusCode(),
					"an authenticator-less domain must expose no login route");
		}

		@Test
		@DisplayName("a CRUD operation the domain does not expose gets no route (404)")
		void unconfiguredOperationHasNoRoute() throws Exception {
			IClass<?> e = fakeEntityClass();
			// Only readAll is enabled — readOne / create / … are not configured.
			startWith(List.of(OperationDefinition.readAllWithStandardSecurity("users", e)));

			assertEquals(200, send("GET", "/users", null).statusCode(),
					"the enabled readAll route must exist");

			HttpResponse<String> readOne = send("GET", "/users/x-1", null);
			assertEquals(404, readOne.statusCode(),
					"an unconfigured readOne must have no route at all");
			HttpResponse<String> create = send("POST", "/users", "body");
			assertEquals(404, create.statusCode(),
					"an unconfigured create must have no route at all");
		}
	}
}
