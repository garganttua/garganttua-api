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

import com.garganttua.api.binding.jackson.JacksonJsonSerializer;
import com.garganttua.api.binding.jackson.JacksonXmlSerializer;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.serialization.ISerializer;
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
		/** When set, invoke() publishes it on the request as 'encodedAuthorization' (a token-minting op). */
		volatile Object encodedToPublish;
		/** Serializer registry surfaced via getApiContext() — JSON by default; tests may add XML. */
		final List<ISerializer> serializers = new java.util.ArrayList<>(List.of(new JacksonJsonSerializer()));
		private final IApi api;

		@SuppressWarnings("unchecked")
		CapturingDomain(List<OperationDefinition> operations) {
			this.definition = mock(IDomainDefinition.class);
			when(this.definition.operations()).thenReturn(operations);
			this.api = mock(IApi.class);
			when(this.api.getSerializers()).thenAnswer(inv -> this.serializers);
		}

		@Override public IApi getApiContext() { return this.api; }

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
			if (this.encodedToPublish != null) {
				request.arg(com.garganttua.api.commons.service.ArgKey.of(
						"encodedAuthorization", IClass.getClass(Object.class)), this.encodedToPublish);
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
		return send(method, path, body, null);
	}

	private HttpResponse<String> send(String method, String path, String body, String accept) throws Exception {
		HttpRequest.BodyPublisher pub = body == null
				? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofString(body);
		HttpRequest.Builder b = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + path))
				.method(method, pub);
		if (accept != null) {
			b.header("Accept", accept);
		}
		return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
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
		@DisplayName("a failure response sets the error status and a JSON error body")
		void failureSurfacesStatusAndMessage() throws Exception {
			domain.responseOverride = new OperationResponse(
					OperationResponseCode.NOT_FOUND, new ApiException("entity not found: u-404"));

			HttpResponse<String> resp = send("GET", "/users/u-404", null);

			assertEquals(404, resp.statusCode(),
					"the HTTP status must follow the pipeline's response code, not default to 200");
			assertEquals("{\"error\":\"entity not found: u-404\"}", resp.body(),
					"the error message must be wrapped in a JSON object, not the stale pipeline output");
			assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("application/json"),
					"an error body must be labelled application/json");
		}

		@Test
		@DisplayName("UNAUTHORIZED → 401 with a JSON error body")
		void unauthorizedMaps401() throws Exception {
			domain.responseOverride = new OperationResponse(
					OperationResponseCode.UNAUTHORIZED, new ApiException("missing authorization"));

			HttpResponse<String> resp = send("GET", "/users", null);

			assertEquals(401, resp.statusCode());
			assertEquals("{\"error\":\"missing authorization\"}", resp.body());
		}

		@Test
		@DisplayName("error messages with quotes/backslashes are JSON-escaped")
		void escapesJsonSpecials() throws Exception {
			domain.responseOverride = new OperationResponse(
					OperationResponseCode.CLIENT_ERROR, new ApiException("bad \"value\" \\ here"));

			HttpResponse<String> resp = send("GET", "/users", null);

			assertEquals(400, resp.statusCode());
			assertEquals("{\"error\":\"bad \\\"value\\\" \\\\ here\"}", resp.body(),
					"quotes and backslashes in the message must be escaped to keep the JSON valid");
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

		@Test
		@DisplayName("Accept: application/xml on a 406 → text/plain raw message, NOT a JSON envelope")
		void xmlOnlyAcceptDegradesToPlainText() throws Exception {
			// The very contradiction: the client asked for application/xml, the pipeline
			// could not produce it (406), so the error must NOT come back as JSON — the
			// format the client just refused. It degrades to text/plain.
			domain.responseOverride = new OperationResponse(OperationResponseCode.NOT_ACCEPTABLE,
					new ApiException("No acceptable serializer for: application/xml"));

			HttpResponse<String> resp = send("GET", "/users", null, "application/xml");

			assertEquals(406, resp.statusCode(), "an unmet Accept must surface as 406");
			assertEquals("No acceptable serializer for: application/xml", resp.body(),
					"the body must be the raw message, not wrapped in a JSON object");
			String ct = resp.headers().firstValue("Content-Type").orElse("");
			assertTrue(ct.contains("text/plain"),
					"a client that refused JSON must not receive an application/json error body; got: " + ct);
			assertFalse(ct.contains("application/json"),
					"the JSON content-type must not be sent to an xml-only client; got: " + ct);
		}

		@Test
		@DisplayName("Accept: application/json → JSON error envelope is kept")
		void jsonAcceptKeepsJsonEnvelope() throws Exception {
			domain.responseOverride = new OperationResponse(OperationResponseCode.CLIENT_ERROR,
					new ApiException("bad request"));

			HttpResponse<String> resp = send("GET", "/users", null, "application/json");

			assertEquals(400, resp.statusCode());
			assertEquals("{\"error\":\"bad request\"}", resp.body(),
					"a client that accepts JSON still receives the JSON error envelope");
			assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("application/json"));
		}

		@Test
		@DisplayName("Accept: */* (wildcard) → JSON error envelope is kept")
		void wildcardAcceptKeepsJsonEnvelope() throws Exception {
			domain.responseOverride = new OperationResponse(OperationResponseCode.NOT_ACCEPTABLE,
					new ApiException("No acceptable serializer for: application/xml"));

			HttpResponse<String> resp = send("GET", "/users", null, "application/xml, */*;q=0.1");

			assertEquals(406, resp.statusCode());
			assertEquals("{\"error\":\"No acceptable serializer for: application/xml\"}", resp.body(),
					"a wildcard Accept admits JSON, so the JSON envelope is kept");
			assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("application/json"));
		}

		@Test
		@DisplayName("Accept: application/xml with an XML serializer → the error envelope is XML, not text/plain")
		void xmlAcceptYieldsXmlErrorEnvelope() throws Exception {
			domain.serializers.add(new JacksonXmlSerializer()); // XML is now negotiable
			domain.responseOverride = new OperationResponse(OperationResponseCode.UNAUTHORIZED,
					new ApiException("missing authorization"));

			HttpResponse<String> resp = send("GET", "/users", null, "application/xml");

			assertEquals(401, resp.statusCode());
			assertTrue(resp.body().contains("<error>missing authorization</error>"),
					"the error must be rendered in XML; got: " + resp.body());
			assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("application/xml"),
					"the error body must be labelled application/xml; got: "
							+ resp.headers().firstValue("Content-Type").orElse(""));
		}
	}

	@Nested
	@DisplayName("Authorization returned in the X-Authorization header (body is ok)")
	class AuthorizationHeader {

		@Test
		@DisplayName("a minted token is returned in X-Authorization and the body is a structured {\"status\":\"ok\"}")
		void tokenInHeaderBodyOk() throws Exception {
			domain.encodedToPublish = "eyJhbGciOiJFUzI1NiJ9.payload.signature";
			domain.responseOverride = new OperationResponse(OperationResponseCode.OK, "the-authentication-result");

			HttpResponse<String> resp = send("POST", "/users", "credentials");

			assertEquals(200, resp.statusCode());
			assertEquals("eyJhbGciOiJFUzI1NiJ9.payload.signature",
					resp.headers().firstValue("X-Authorization").orElse(null),
					"the minted token must travel in the X-Authorization header");
			assertEquals("{\"status\":\"ok\"}", resp.body(),
					"the body must be a structured envelope, symmetric to {\"error\":\"…\"}");
			assertTrue(resp.headers().firstValue("Content-Type").orElse("").contains("application/json"));
		}

		@Test
		@DisplayName("on failure the token is NOT headered and the 4xx + parlant error stands")
		void failureKeepsErrorDetail() throws Exception {
			domain.encodedToPublish = "should-not-leak";
			domain.responseOverride = new OperationResponse(
					OperationResponseCode.UNAUTHORIZED, new ApiException("Account is disabled"));

			HttpResponse<String> resp = send("POST", "/users", "credentials");

			assertEquals(401, resp.statusCode());
			assertTrue(resp.headers().firstValue("X-Authorization").isEmpty(),
					"no token header on a failed authentication");
			assertEquals("{\"error\":\"Account is disabled\"}", resp.body(),
					"failure keeps the detailed parlant error, not a bare ko");
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

	/** Builds a use-case {@link OperationDefinition} carrying a minimal definition (verb, scope, path). */
	static OperationDefinition useCaseOp(String name,
			com.garganttua.api.commons.operation.TechnicalOperation verb,
			com.garganttua.api.commons.operation.Scope scope, String path) {
		com.garganttua.api.commons.definition.IUseCaseDefinition uc =
				new com.garganttua.api.commons.definition.IUseCaseDefinition() {
			@Override public String name() { return name; }
			@Override public com.garganttua.api.commons.operation.OperationPath path() {
				return new com.garganttua.api.commons.operation.OperationPath(path);
			}
			@Override public IClass<?> inputType() { return null; }
			@Override public IClass<?> outputType() { return null; }
			@Override public com.garganttua.core.reflection.binders.IMethodBinder<?> binder() { return null; }
			@Override public com.garganttua.api.commons.operation.Scope scope() { return scope; }
			@Override public com.garganttua.api.commons.operation.TechnicalOperation operation() { return verb; }
			@Override public Access access() { return Access.anonymous; }
			@Override public boolean authority() { return false; }
			@Override public String authorityName() { return null; }
		};
		return OperationDefinition.useCase("users", fakeEntityClass(), uc);
	}

	@Nested
	@DisplayName("Use case routes → use-case OperationDefinition")
	class UseCaseRoutes {

		@Test
		@DisplayName("GET /users/greet → the read-scoped use case, no uuid, Context handed through")
		void getReadUseCase() throws Exception {
			startWith(List.of(useCaseOp("greet",
					com.garganttua.api.commons.operation.TechnicalOperation.read,
					com.garganttua.api.commons.operation.Scope.allEntities, "/users/greet")));

			HttpResponse<String> resp = send("GET", "/users/greet", null);

			assertEquals(200, resp.statusCode());
			assertEquals("ok:useCase", resp.body(), "the use-case stage's output round-trips through the protocol");
			assertEquals(BusinessOperation.useCase, domain.lastOperation.getBusinessOperation());
			assertEquals("greet", domain.lastOperation.useCaseName(), "the dispatched op must be the greet use case");
			assertNull(domain.lastUuid, "an allEntities use case carries no uuid");
			assertInstanceOf(Context.class, domain.lastRawRequest);
			assertEquals("/users/greet", domain.lastPath);
		}

		@Test
		@DisplayName("POST /users/import → a create-scoped use case carries its body")
		void postCreateUseCase() throws Exception {
			startWith(List.of(useCaseOp("import",
					com.garganttua.api.commons.operation.TechnicalOperation.create,
					com.garganttua.api.commons.operation.Scope.allEntities, "/users/import")));

			HttpResponse<String> resp = send("POST", "/users/import", "row1,row2");

			assertEquals(200, resp.statusCode());
			assertEquals(BusinessOperation.useCase, domain.lastOperation.getBusinessOperation());
			assertEquals("import", domain.lastOperation.useCaseName());
			assertArrayEquals("row1,row2".getBytes(StandardCharsets.UTF_8), domain.lastBody,
					"the POST body must reach the protocol verbatim");
		}

		@Test
		@DisplayName("PUT /users/activate/{uuid} → a one-entity use case captures the uuid")
		void putOneEntityUseCase() throws Exception {
			startWith(List.of(useCaseOp("activate",
					com.garganttua.api.commons.operation.TechnicalOperation.update,
					com.garganttua.api.commons.operation.Scope.oneEntity, "/users/activate/${uuid}")));

			HttpResponse<String> resp = send("PUT", "/users/activate/u-42", "body");

			assertEquals(200, resp.statusCode());
			assertEquals(BusinessOperation.useCase, domain.lastOperation.getBusinessOperation());
			assertEquals("activate", domain.lastOperation.useCaseName());
			assertEquals("u-42", domain.lastUuid, "the ${uuid} segment must be threaded as ENTITY_UUID");
			assertEquals("/users/activate/u-42", domain.lastPath);
		}

		@Test
		@DisplayName("a use case the domain does not expose gets no route (404)")
		void unconfiguredUseCaseHasNoRoute() throws Exception {
			// readAll only — no readOne (/users/{uuid}) to shadow /users/greet, and no greet use case.
			startWith(List.of(OperationDefinition.readAllWithStandardSecurity("users", fakeEntityClass())));

			assertEquals(404, send("GET", "/users/greet", null).statusCode(),
					"a domain without the greet use case must expose no /users/greet route");
		}

		@Test
		@DisplayName("a literal use-case path wins over readOne's /{uuid} when both are registered")
		void useCasePathWinsOverReadOne() throws Exception {
			IClass<?> e = fakeEntityClass();
			startWith(List.of(
					OperationDefinition.readOneWithStandardSecurity("users", e),
					useCaseOp("greet", com.garganttua.api.commons.operation.TechnicalOperation.read,
							com.garganttua.api.commons.operation.Scope.allEntities, "/users/greet")));

			assertEquals(200, send("GET", "/users/greet", null).statusCode());
			assertEquals(BusinessOperation.useCase, domain.lastOperation.getBusinessOperation(),
					"the literal /users/greet must route to the use case, not readOne with uuid=greet");
			assertEquals("greet", domain.lastOperation.useCaseName());

			// A real uuid still reaches readOne.
			assertEquals(200, send("GET", "/users/u-1", null).statusCode());
			assertEquals(BusinessOperation.readOne, domain.lastOperation.getBusinessOperation());
			assertEquals("u-1", domain.lastUuid);
		}

		@Test
		@DisplayName("two use cases on one domain route to distinct paths")
		void twoUseCasesDistinctPaths() throws Exception {
			startWith(List.of(
					useCaseOp("greet", com.garganttua.api.commons.operation.TechnicalOperation.read,
							com.garganttua.api.commons.operation.Scope.allEntities, "/users/greet"),
					useCaseOp("stats", com.garganttua.api.commons.operation.TechnicalOperation.read,
							com.garganttua.api.commons.operation.Scope.allEntities, "/users/stats")));

			assertEquals(200, send("GET", "/users/greet", null).statusCode());
			assertEquals("greet", domain.lastOperation.useCaseName());

			assertEquals(200, send("GET", "/users/stats", null).statusCode());
			assertEquals("stats", domain.lastOperation.useCaseName(),
					"the second use case must route to its own path");
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
					"the dispatched op must carry the domain's configured access, not Access.authenticated");
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
