package com.garganttua.api.core.integ.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;

@DisplayName("Protocol pipeline stages (protocol/EXTRACT + protocol/RESPONSE)")
class ProtocolIntegrationTest extends AbstractCrudIntegrationTest {

	/** Response record the fake protocol produces. */
	record FakeHttpResponse(int statusCode, byte[] body, String contentType) {}

	/** Records every interaction so tests can assert delegation. */
	static class FakeHttpProtocol implements IProtocol<FakeHttpRequest, FakeHttpResponse> {
		final List<FakeHttpRequest> extracted = new java.util.ArrayList<>();
		final List<Object> buildPayloads = new java.util.ArrayList<>();
		int lastStatusCode;

		@Override public IClass<FakeHttpRequest> requestType() { return IClass.getClass(FakeHttpRequest.class); }

		@Override
		public ICaller getCaller(FakeHttpRequest r) {
			extracted.add(r);
			return new Caller(r.tenantId(), r.tenantId(), r.callerId(), null, true, true, List.of());
		}
		@Override public byte[]              getRawBody(FakeHttpRequest r)         { return r.body(); }
		@Override public String              getAuthorization(FakeHttpRequest r)   { return r.authorization(); }
		@Override public String              getContentType(FakeHttpRequest r)     { return r.contentType(); }
		@Override public String              getAccept(FakeHttpRequest r)          { return r.accept(); }
		@Override public String              getPath(FakeHttpRequest r)            { return r.path(); }
		@Override public String              getMethod(FakeHttpRequest r)          { return r.method(); }
		@Override public Map<String, String> getQueryParameters(FakeHttpRequest r) { return r.queryParameters(); }

		@Override
		public FakeHttpResponse buildResponse(FakeHttpRequest request, Object output, int statusCode) {
			buildPayloads.add(output);
			lastStatusCode = statusCode;
			byte[] body = (output instanceof byte[] b) ? b : (output == null ? new byte[0] : output.toString().getBytes(StandardCharsets.UTF_8));
			return new FakeHttpResponse(statusCode, body, "application/json");
		}
	}

	/** Pipe-format serializer reused from SerializationIntegrationTest's pattern. */
	static class PipeUserSerializer implements ISerializer {
		@Override public MimeType mimeType() { return MimeType.APPLICATION_JSON; }
		@Override public byte[] serialize(Object object) {
			if (object instanceof User u) {
				return (safe(u.getName()) + "|" + safe(u.getEmail())).getBytes(StandardCharsets.UTF_8);
			}
			if (object instanceof UserDto d) {
				return (safe(d.getName()) + "|" + safe(d.getEmail())).getBytes(StandardCharsets.UTF_8);
			}
			return object == null ? new byte[0] : object.toString().getBytes(StandardCharsets.UTF_8);
		}
		@Override
		@SuppressWarnings("unchecked")
		public <T> T deserialize(byte[] data, IClass<T> type) throws ApiException {
			String[] parts = new String(data, StandardCharsets.UTF_8).split("\\|", -1);
			if (parts.length < 2) throw new ApiException("Malformed payload");
			try {
				Object instance = type.getConstructor().newInstance();
				instance.getClass().getMethod("setName", String.class).invoke(instance, parts[0]);
				instance.getClass().getMethod("setEmail", String.class).invoke(instance, parts[1]);
				return (T) instance;
			} catch (Exception e) {
				throw new ApiException("Failed to deserialize", e);
			}
		}
		private static String safe(String s) { return s == null ? "" : s; }
	}

	private IApi context;
	private IDomain<?> userCtx;
	private CapturingDao userDao;
	private FakeHttpProtocol protocol;

	@BeforeEach
	void setUp() throws ApiException {
		userDao = new CapturingDao();
		protocol = new FakeHttpProtocol();

		IApiBuilder builder = newBuilder();
		builder.serializer(new PipeUserSerializer());
		builder.protocol(protocol);
		builder.domain(IClass.getClass(User.class))
				.tenant(true)
				.superTenant("superTenant")
				.entity()
					.id("id").uuid("uuid").tenantId("tenantId")
					.mandatory("name")
				.up()
				.dto(IClass.getClass(UserDto.class))
					.id("id").uuid("uuid").tenantId("tenantId")
					.db(userDao)
				.up()
				.security().disable(true).up()
			.up();

		context = buildAndStart(builder);
		userCtx = context.getDomain("users").orElseThrow();
	}

	private OperationRequest modeARequest(FakeHttpRequest http, OperationDefinition op) {
		OperationRequest req = new OperationRequest(new HashMap<>());
		req.arg(IOperationRequest.OPERATION, op);
		req.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
		req.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
		req.arg(IOperationRequest.SUPER_TENANT, true);
		req.arg(IOperationRequest.SUPER_OWNER, true);
		req.arg("rawRequest", http);
		return req;
	}

	@Nested
	@DisplayName("Mode A — full pipeline")
	class ModeA {

		@Test
		@DisplayName("extracts, deserializes, persists and serializes the response through the protocol")
		void fullFlow() {
			FakeHttpRequest http = new FakeHttpRequest(
					"/users", "POST",
					"Alice|alice@example.com".getBytes(StandardCharsets.UTF_8),
					"application/json", "application/json",
					"Bearer xyz", "SUPER_TENANT", "caller-7",
					Map.of());

			OperationRequest req = modeARequest(http,
					OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));

			IOperationResponse resp = userCtx.invoke(req);

			assertEquals(OperationResponseCode.CREATED, resp.getResponseCode(),
					() -> "Got: " + resp.getResponse());
			assertTrue(resp.getResponse() instanceof FakeHttpResponse,
					"Response should be the transport response, got: " + resp.getResponse());

			FakeHttpResponse httpResp = (FakeHttpResponse) resp.getResponse();
			assertEquals(200, httpResp.statusCode());
			String decoded = new String(httpResp.body(), StandardCharsets.UTF_8);
			assertEquals("Alice|alice@example.com", decoded,
					"Body should be the serialized entity, got: " + decoded);

			// Entity was persisted (mapped through CRUD)
			assertNotNull(userDao.getLastSaved(), "CRUD should have persisted the entity");

			// Protocol was exercised on both sides
			assertEquals(1, protocol.extracted.size(),
					"Protocol should have been called once for extraction");
			assertEquals(1, protocol.buildPayloads.size(),
					"Protocol should have been called once for response");
		}

		@Test
		@DisplayName("registered protocol receives the serialized byte[] payload when Accept was honored")
		void responseReceivesBytesWhenAcceptSet() {
			FakeHttpRequest http = new FakeHttpRequest(
					"/users", "POST",
					"Bob|bob@example.com".getBytes(StandardCharsets.UTF_8),
					"application/json", "application/json",
					null, "SUPER_TENANT", null,
					Map.of());

			userCtx.invoke(modeARequest(http,
					OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class))));

			Object lastPayload = protocol.buildPayloads.get(protocol.buildPayloads.size() - 1);
			assertTrue(lastPayload instanceof byte[],
					"Payload to buildResponse should be byte[] when serialize ran, got: " + lastPayload.getClass());
		}

		@Test
		@DisplayName("when Accept is absent, response receives the raw DTO object (serialize was skipped)")
		void responseReceivesObjectWhenNoAccept() {
			FakeHttpRequest http = new FakeHttpRequest(
					"/users", "POST",
					"Carol|carol@example.com".getBytes(StandardCharsets.UTF_8),
					"application/json", null,  // no Accept → serialize skips
					null, "SUPER_TENANT", null,
					Map.of());

			userCtx.invoke(modeARequest(http,
					OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class))));

			Object lastPayload = protocol.buildPayloads.get(protocol.buildPayloads.size() - 1);
			assertFalse(lastPayload instanceof byte[],
					"Payload should be raw object (not bytes) when serialize is skipped, got: " + lastPayload);
			assertTrue(lastPayload instanceof User,
					"Expected User payload, got: " + (lastPayload == null ? "null" : lastPayload.getClass()));
		}
	}

	@Nested
	@DisplayName("Mode B — bypass")
	class ModeB {

		@Test
		@DisplayName("without rawRequest, protocol stages are skipped entirely — output is the DTO")
		void noRawRequestSkipsProtocol() {
			User user = new User();
			user.setName("Dan");
			user.setEmail("dan@example.com");

			OperationRequest req = new OperationRequest(new HashMap<>());
			req.arg(IOperationRequest.OPERATION,
					OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));
			req.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
			req.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
			req.arg(IOperationRequest.SUPER_TENANT, true);
			req.arg(IOperationRequest.SUPER_OWNER, true);
			req.arg("entity", user);

			IOperationResponse resp = userCtx.invoke(req);

			assertEquals(OperationResponseCode.CREATED, resp.getResponseCode());
			assertTrue(resp.getResponse() instanceof User,
					"Mode B output should be the DTO, not a transport response; got: "
							+ (resp.getResponse() == null ? "null" : resp.getResponse().getClass()));
			assertEquals(0, protocol.extracted.size(),
					"Protocol.extract must not be called in Mode B");
			assertEquals(0, protocol.buildPayloads.size(),
					"Protocol.buildResponse must not be called in Mode B");
		}
	}

	@Nested
	@DisplayName("Discrimination")
	class Discrimination {

		@Test
		@DisplayName("unknown raw request class → 415 and no entity persisted")
		void unknownTransport() {
			Object unknownRawRequest = new Object();  // no registered protocol

			OperationRequest req = new OperationRequest(new HashMap<>());
			req.arg(IOperationRequest.OPERATION,
					OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));
			req.arg(IOperationRequest.TENANT_ID, "SUPER_TENANT");
			req.arg(IOperationRequest.REQUESTED_TENANT_ID, "SUPER_TENANT");
			req.arg(IOperationRequest.SUPER_TENANT, true);
			req.arg("rawRequest", unknownRawRequest);

			IOperationResponse resp = userCtx.invoke(req);

			assertFalse(OperationResponseCode.OK.equals(resp.getResponseCode())
					|| OperationResponseCode.CREATED.equals(resp.getResponseCode()),
					"Unknown raw request class must not yield a success response; got: "
							+ resp.getResponseCode());
			assertNull(userDao.getLastSaved(),
					"Nothing should be persisted when protocol resolution fails");
			assertEquals(0, protocol.buildPayloads.size(),
					"Protocol.buildResponse must not be called when resolveProtocol failed");
		}
	}

	@Nested
	@DisplayName("Registration")
	class Registration {

		@Test
		@DisplayName("IApi.getProtocols() exposes the registered protocol")
		void exposesRegistered() {
			assertEquals(1, context.getProtocols().size());
			assertSame(protocol, context.getProtocols().get(0));
		}
	}
}
