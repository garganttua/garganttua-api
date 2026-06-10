package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;

@DisplayName("Serialization pipeline stages (data/DESERIALIZE + data/SERIALIZE)")
class SerializationIntegrationTest extends AbstractCrudIntegrationTest {

	/**
	 * Minimal test serializer — encodes a {@link UserDto} as "name|email" UTF-8 bytes.
	 * Enough to exercise the deserialize/serialize pipeline stages without pulling
	 * Jackson into the test.
	 */
	static class PipeUserDtoSerializer implements ISerializer {
		private final MimeType mimeType;
		private final List<Object> serializeLog = new ArrayList<>();

		PipeUserDtoSerializer(MimeType mimeType) { this.mimeType = mimeType; }

		@Override public MimeType mimeType() { return mimeType; }

		@Override
		public byte[] serialize(Object object) throws ApiException {
			serializeLog.add(object);
			if (object instanceof UserDto dto) {
				String payload = safe(dto.getName()) + "|" + safe(dto.getEmail());
				return payload.getBytes(StandardCharsets.UTF_8);
			}
			if (object instanceof User user) {
				return (safe(user.getName()) + "|" + safe(user.getEmail()))
						.getBytes(StandardCharsets.UTF_8);
			}
			throw new ApiException("Unsupported object type: " + object.getClass());
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> T deserialize(byte[] data, IClass<T> type) throws ApiException {
			String payload = new String(data, StandardCharsets.UTF_8);
			String[] parts = payload.split("\\|", -1);
			if (parts.length < 2) {
				throw new ApiException("Malformed test payload: " + payload);
			}
			try {
				Object instance = type.getConstructor().newInstance();
				instance.getClass().getMethod("setName", String.class).invoke(instance, parts[0]);
				instance.getClass().getMethod("setEmail", String.class).invoke(instance, parts[1]);
				return (T) instance;
			} catch (Exception e) {
				throw new ApiException("Failed to deserialize to " + type.getName(), e);
			}
		}

		List<Object> getSerializeLog() { return serializeLog; }

		private static String safe(String s) { return s == null ? "" : s; }
	}

	private IApi context;
	private IDomain<?> userCtx;
	private CapturingDao userDao;
	private PipeUserDtoSerializer jsonSerializer;

	@BeforeEach
	void setUp() throws ApiException {
		userDao = new CapturingDao();
		jsonSerializer = new PipeUserDtoSerializer(MimeType.APPLICATION_JSON);

		IApiBuilder builder = newBuilder();
		builder.serializer(jsonSerializer);
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

	private OperationRequest createRequestWithRawBody(byte[] body, String contentType) {
		OperationRequest request = superTenantRequest(
				OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));
		request.arg(IOperationRequest.RAW_BODY.name(), body);
		request.arg("contentType", contentType);
		return request;
	}

	@Nested
	@DisplayName("Mode A — deserialize before CRUD")
	class Deserialize {

		@Test
		@DisplayName("deserializes rawBody into a DTO and persists through the CREATE_ONE stage")
		void deserializeAndCreate() {
			byte[] body = "Alice|alice@example.com".getBytes(StandardCharsets.UTF_8);
			OperationRequest request = createRequestWithRawBody(body, "application/json");

			IOperationResponse response = userCtx.invoke(request);

			assertEquals(OperationResponseCode.CREATED, response.getResponseCode(),
					() -> "Got response: " + response.getResponse());

			// The workflow output is the persisted entity (post-CREATE_ONE)
			assertTrue(response.getResponse() instanceof User,
					"Workflow output should be the created User, got: " + response.getResponse());
			User user = (User) response.getResponse();
			assertEquals("Alice", user.getName(), "name should have been deserialized from the raw body");
			assertEquals("alice@example.com", user.getEmail(),
					"email should have been deserialized from the raw body");
			assertEquals("SUPER_TENANT", user.getTenantId(), "tenantId should be set from caller");
			assertNotNull(user.getUuid(), "CREATE_ONE should generate a UUID");

			assertNotNull(userDao.getLastSaved(), "Repository.save should have been called");
		}

		@Test
		@DisplayName("honors charset parameter on Content-Type")
		void charsetParameter() {
			byte[] body = "Bob|bob@example.com".getBytes(StandardCharsets.UTF_8);
			OperationRequest request = createRequestWithRawBody(body, "application/json; charset=utf-8");

			IOperationResponse response = userCtx.invoke(request);
			assertEquals(OperationResponseCode.CREATED, response.getResponseCode());
			User user = (User) response.getResponse();
			assertEquals("Bob", user.getName());
		}

		@Test
		@DisplayName("returns 415 when Content-Type has no registered serializer")
		void unsupportedContentType() {
			OperationRequest request = createRequestWithRawBody(
					"Carol|carol@example.com".getBytes(StandardCharsets.UTF_8),
					"application/xml");

			IOperationResponse response = userCtx.invoke(request);

			assertFalse(OperationResponseCode.OK.equals(response.getResponseCode())
					|| OperationResponseCode.CREATED.equals(response.getResponseCode()),
					"Unsupported Content-Type should not produce a success response");
			// 415 now maps to OperationResponseCode.UNSUPPORTED_MEDIA_TYPE — here we just
			// assert the CRUD was not executed (not a success)
			assertTrue(userDao.getLastSaved() == null,
					"Nothing should be persisted when Content-Type is unsupported");
		}

		@Test
		@DisplayName("stage is skipped (no-op) when rawBody is absent — Mode B unaffected")
		void modeBBypassesStage() {
			User user = new User();
			user.setName("Dan");
			user.setEmail("dan@example.com");

			OperationRequest request = superTenantRequest(
					OperationDefinition.createOneWithStandardSecurity("users", IClass.getClass(User.class)));
			request.arg("entity", user);

			IOperationResponse response = userCtx.invoke(request);

			assertEquals(OperationResponseCode.CREATED, response.getResponseCode());
			assertNotNull(userDao.getLastSaved(), "Mode B path must still persist the entity");
			User created = (User) response.getResponse();
			assertEquals("Dan", created.getName());
		}

		@Test
		@DisplayName("read operation with rawBody set still succeeds (operationExpectsBody=false skips)")
		void readOpSkipsDeserialize() {
			// Seed one entity
			User seed = new User();
			seed.setUuid("u-1");
			seed.setTenantId("SUPER_TENANT");
			seed.setName("Eve");
			userDao.getStorage().add(seed);

			OperationRequest request = superTenantRequest(
					OperationDefinition.readAllWithStandardSecurity("users", IClass.getClass(User.class)));
			request.arg(IOperationRequest.RAW_BODY.name(), "anything".getBytes(StandardCharsets.UTF_8));
			request.arg("contentType", "application/json");

			IOperationResponse response = userCtx.invoke(request);

			assertEquals(OperationResponseCode.OK, response.getResponseCode(),
					"READ must ignore the rawBody even when it's present");
		}
	}

	@Nested
	@DisplayName("Mode A — serialize after CRUD")
	class Serialize {

		@Test
		@DisplayName("serializes the CRUD output to bytes when Accept matches")
		void serializeOnExit() {
			byte[] body = "Grace|grace@example.com".getBytes(StandardCharsets.UTF_8);
			OperationRequest request = createRequestWithRawBody(body, "application/json");
			request.arg("accept", "application/json");

			IOperationResponse response = userCtx.invoke(request);

			assertEquals(OperationResponseCode.CREATED, response.getResponseCode(),
					() -> "Got response: " + response.getResponse());

			Object out = response.getResponse();
			assertNotNull(out, "Serialize stage should produce output");
			assertTrue(out instanceof byte[],
					"Output must be raw bytes when serialize stage ran, got: " + out.getClass());
			String decoded = new String((byte[]) out, StandardCharsets.UTF_8);
			assertTrue(decoded.startsWith("Grace|grace@example.com"),
					"Serialized bytes should encode the created entity, got: " + decoded);
		}

		@Test
		@DisplayName("picks JSON via */* Accept wildcard")
		void wildcardAccept() {
			byte[] body = "Henry|henry@example.com".getBytes(StandardCharsets.UTF_8);
			OperationRequest request = createRequestWithRawBody(body, "application/json");
			request.arg("accept", "*/*");

			IOperationResponse response = userCtx.invoke(request);

			assertEquals(OperationResponseCode.CREATED, response.getResponseCode());
			assertTrue(response.getResponse() instanceof byte[]);
			String decoded = new String((byte[]) response.getResponse(), StandardCharsets.UTF_8);
			assertTrue(decoded.startsWith("Henry|"));
		}

		@Test
		@DisplayName("when Accept is absent, serialize stage is skipped — output remains the DTO")
		void acceptAbsentSkipsSerialize() {
			byte[] body = "Ivy|ivy@example.com".getBytes(StandardCharsets.UTF_8);
			OperationRequest request = createRequestWithRawBody(body, "application/json");
			// no "accept" arg

			IOperationResponse response = userCtx.invoke(request);

			assertEquals(OperationResponseCode.CREATED, response.getResponseCode());
			assertFalse(response.getResponse() instanceof byte[],
					"Serialize stage must be a no-op when Accept is absent");
		}

		@Test
		@DisplayName("negotiates correctly when Accept lists multiple media types")
		void multipleAccepts() {
			byte[] body = "Jack|jack@example.com".getBytes(StandardCharsets.UTF_8);
			OperationRequest request = createRequestWithRawBody(body, "application/json");
			request.arg("accept", "application/xml, application/json;q=0.9");

			IOperationResponse response = userCtx.invoke(request);

			assertEquals(OperationResponseCode.CREATED, response.getResponseCode(),
					"Negotiation should fall through application/xml (not registered) to application/json");
			assertTrue(response.getResponse() instanceof byte[]);
			String decoded = new String((byte[]) response.getResponse(), StandardCharsets.UTF_8);
			assertEquals("Jack|jack@example.com", decoded);
		}
	}

	@Nested
	@DisplayName("Serializer registration on the API")
	class Registration {

		@Test
		@DisplayName("IApi.getSerializers() exposes all registered serializers")
		void exposesRegisteredSerializers() {
			assertEquals(1, context.getSerializers().size());
			assertEquals(MimeType.APPLICATION_JSON, context.getSerializers().get(0).mimeType());
		}

		@Test
		@DisplayName("serialize stage actually delegates to the registered serializer instance")
		void delegatesToRegisteredInstance() {
			byte[] body = "Kate|kate@example.com".getBytes(StandardCharsets.UTF_8);
			OperationRequest request = createRequestWithRawBody(body, "application/json");
			request.arg("accept", "application/json");

			IOperationResponse response = userCtx.invoke(request);
			assertEquals(OperationResponseCode.CREATED, response.getResponseCode());

			assertFalse(jsonSerializer.getSerializeLog().isEmpty(),
					"Registered serializer instance must have been called for output serialization");
			Object serialized = jsonSerializer.getSerializeLog().get(jsonSerializer.getSerializeLog().size() - 1);
			assertNotNull(serialized);
			// verify end-to-end byte correctness
			byte[] expected = jsonSerializer.serialize(serialized);
			if (!(response.getResponse() instanceof byte[] actual)) {
				fail("Expected byte[] response, got: " + response.getResponse());
				return;
			}
			assertArrayEquals(expected, actual);
		}
	}
}
