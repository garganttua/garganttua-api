package com.garganttua.api.core.unit.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.expression.SerializationExpressions;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.MimeType;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationType;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;

@DisplayName("SerializationExpressions")
class SerializationExpressionsTest {

	static class Dto {}

	private static OperationDefinition opWith(TechnicalOperation tech) {
		return new OperationDefinition("users", tech, IClass.getClass(Dto.class), Scope.oneEntity,
				OperationType.standard, false, null, Access.anonymous);
	}

	private static ISerializer fakeSerializer(MimeType mime, byte[] serialized, Object deserialized) {
		return new ISerializer() {
			@Override public MimeType mimeType() { return mime; }
			@Override public byte[] serialize(Object object) { return serialized; }
			@SuppressWarnings("unchecked")
			@Override public <T> T deserialize(byte[] data, IClass<T> type) { return (T) deserialized; }
		};
	}

	private static IApi apiWithSerializers(ISerializer... serializers) {
		IApi api = mock(IApi.class);
		when(api.getSerializers()).thenReturn(List.of(serializers));
		return api;
	}

	@Nested
	@DisplayName("operationExpectsBody")
	class OperationExpectsBody {

		@Test
		@DisplayName("returns true for CREATE")
		void createTrue() {
			assertTrue(SerializationExpressions.operationExpectsBody(opWith(TechnicalOperation.create)));
		}

		@Test
		@DisplayName("returns true for UPDATE")
		void updateTrue() {
			assertTrue(SerializationExpressions.operationExpectsBody(opWith(TechnicalOperation.update)));
		}

		@Test
		@DisplayName("returns false for READ")
		void readFalse() {
			assertFalse(SerializationExpressions.operationExpectsBody(opWith(TechnicalOperation.read)));
		}

		@Test
		@DisplayName("returns false for DELETE")
		void deleteFalse() {
			assertFalse(SerializationExpressions.operationExpectsBody(opWith(TechnicalOperation.delete)));
		}

		@Test
		@DisplayName("returns false for null operation")
		void nullFalse() {
			assertFalse(SerializationExpressions.operationExpectsBody(null));
		}

		@Test
		@DisplayName("unwraps Optional argument")
		void unwrapsOptional() {
			assertTrue(SerializationExpressions.operationExpectsBody(Optional.of(opWith(TechnicalOperation.create))));
		}
	}

	@Nested
	@DisplayName("resolveSerializer (strict, for Content-Type)")
	class ResolveSerializer {

		@Test
		@DisplayName("defaults to JSON when contentType is null")
		void defaultsToJson() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			IApi api = apiWithSerializers(json);
			ISerializer found = SerializationExpressions.resolveSerializer(api, null);
			assertSame(json, found);
		}

		@Test
		@DisplayName("matches exact Content-Type")
		void matchesExact() {
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(xml);
			ISerializer found = SerializationExpressions.resolveSerializer(api, "application/xml");
			assertSame(xml, found);
		}

		@Test
		@DisplayName("strips charset parameter before matching")
		void stripsCharset() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			IApi api = apiWithSerializers(json);
			ISerializer found = SerializationExpressions.resolveSerializer(api, "application/json; charset=utf-8");
			assertSame(json, found);
		}

		@Test
		@DisplayName("is case-insensitive on media type")
		void caseInsensitive() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			IApi api = apiWithSerializers(json);
			ISerializer found = SerializationExpressions.resolveSerializer(api, "Application/JSON");
			assertSame(json, found);
		}

		@Test
		@DisplayName("throws when MIME type is unknown (→ 415)")
		void unknownMime() {
			IApi api = apiWithSerializers(fakeSerializer(MimeType.APPLICATION_JSON, null, null));
			ApiException ex = assertThrows(ApiException.class,
					() -> SerializationExpressions.resolveSerializer(api, "foo/bar"));
			assertTrue(ex.getMessage().contains("Unsupported Content-Type"));
		}

		@Test
		@DisplayName("throws when no serializer registered for the MIME type (→ 415)")
		void noneMatching() {
			IApi api = apiWithSerializers(fakeSerializer(MimeType.APPLICATION_JSON, null, null));
			ApiException ex = assertThrows(ApiException.class,
					() -> SerializationExpressions.resolveSerializer(api, "application/xml"));
			assertTrue(ex.getMessage().contains("No serializer registered"));
		}
	}

	@Nested
	@DisplayName("negotiateSerializer (Accept)")
	class NegotiateSerializer {

		@Test
		@DisplayName("picks JSON when Accept is */*")
		void wildcard() {
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			IApi api = apiWithSerializers(xml, json);
			assertSame(json, SerializationExpressions.negotiateSerializer(api, "*/*"));
		}

		@Test
		@DisplayName("picks JSON when Accept is null")
		void nullAccept() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			IApi api = apiWithSerializers(json);
			assertSame(json, SerializationExpressions.negotiateSerializer(api, null));
		}

		@Test
		@DisplayName("picks first when Accept is blank")
		void blankAccept() {
			ISerializer first = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(first);
			assertSame(first, SerializationExpressions.negotiateSerializer(api, "   "));
		}

		@Test
		@DisplayName("picks first matching media type from comma-separated list")
		void firstMatch() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(json, xml);
			ISerializer picked = SerializationExpressions.negotiateSerializer(api,
					"text/html, application/xml, application/json");
			assertSame(xml, picked);
		}

		@Test
		@DisplayName("honors q-order: a higher-q type wins over an earlier lower-q one")
		void qOrdering() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(json, xml);
			// json listed first but lower q — xml must win.
			assertSame(xml, SerializationExpressions.negotiateSerializer(api,
					"application/json;q=0.8, application/xml;q=0.9"));
		}

		@Test
		@DisplayName("browser Accept (xml;q=0.9, */*;q=0.8) resolves to XML, not the JSON default")
		void browserAcceptPrefersXml() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(json, xml);
			// The */* must NOT short-circuit to JSON: application/xml has the higher q.
			assertSame(xml, SerializationExpressions.negotiateSerializer(api,
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
		}

		@Test
		@DisplayName("*/* with a low q still yields the default when no concrete type matches")
		void wildcardLowQDefault() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(json, xml);
			// No serializer for image/png; */* (even at q=0.1) yields the JSON default.
			assertSame(json, SerializationExpressions.negotiateSerializer(api, "image/png, */*;q=0.1"));
		}

		@Test
		@DisplayName("q=0 marks a type as not acceptable and is skipped")
		void qZeroRefused() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(json, xml);
			assertSame(xml, SerializationExpressions.negotiateSerializer(api,
					"application/json;q=0, application/xml"));
		}

		@Test
		@DisplayName("a subtype wildcard (application/*) matches a serializer of that type")
		void subtypeWildcard() {
			ISerializer json = fakeSerializer(MimeType.APPLICATION_JSON, null, null);
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(xml, json);
			// application/* → the first registered application/* serializer (xml here).
			assertSame(xml, SerializationExpressions.negotiateSerializer(api, "application/*"));
		}

		@Test
		@DisplayName("throws 406 when no Accept entry matches (no JSON fallback)")
		void noMatch406() {
			ISerializer xml = fakeSerializer(MimeType.APPLICATION_XML, null, null);
			IApi api = apiWithSerializers(xml);
			ApiException ex = assertThrows(ApiException.class,
					() -> SerializationExpressions.negotiateSerializer(api, "application/pdf, text/csv"));
			assertTrue(ex.getMessage().contains("No acceptable serializer"));
		}

		@Test
		@DisplayName("throws when no serializers registered")
		void noSerializers() {
			IApi api = apiWithSerializers();
			assertThrows(ApiException.class,
					() -> SerializationExpressions.negotiateSerializer(api, "*/*"));
		}
	}

	@Nested
	@DisplayName("serialize / deserialize")
	class SerializeDeserialize {

		@Test
		@DisplayName("serialize delegates to serializer and returns the bytes unchanged")
		void serializeDelegates() {
			byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
			ISerializer s = fakeSerializer(MimeType.APPLICATION_JSON, bytes, null);
			byte[] out = SerializationExpressions.serialize(s, new Object());
			assertArrayEquals(bytes, out);
		}

		@Test
		@DisplayName("serialize throws when serializer is null")
		void serializeNullSerializer() {
			assertThrows(ApiException.class, () -> SerializationExpressions.serialize(null, "x"));
		}

		@Test
		@DisplayName("deserialize delegates and returns the object from the serializer")
		void deserializeDelegates() {
			Dto dto = new Dto();
			ISerializer s = fakeSerializer(MimeType.APPLICATION_JSON, null, dto);
			Object out = SerializationExpressions.deserialize(s, "body".getBytes(StandardCharsets.UTF_8),
					IClass.getClass(Dto.class));
			assertSame(dto, out);
		}

		@Test
		@DisplayName("deserialize accepts boxed Byte[]")
		void deserializeBoxedBytes() {
			Dto dto = new Dto();
			ISerializer s = fakeSerializer(MimeType.APPLICATION_JSON, null, dto);
			Byte[] boxed = new Byte[] { (byte) 'a', (byte) 'b' };
			Object out = SerializationExpressions.deserialize(s, boxed, IClass.getClass(Dto.class));
			assertSame(dto, out);
		}

		@Test
		@DisplayName("deserialize throws on non-byte payload")
		void deserializeWrongType() {
			ISerializer s = fakeSerializer(MimeType.APPLICATION_JSON, null, new Dto());
			assertThrows(ApiException.class,
					() -> SerializationExpressions.deserialize(s, "a string", IClass.getClass(Dto.class)));
		}

		@Test
		@DisplayName("deserialize throws when any argument is null")
		void deserializeNullArg() {
			ISerializer s = fakeSerializer(MimeType.APPLICATION_JSON, null, new Dto());
			assertThrows(ApiException.class,
					() -> SerializationExpressions.deserialize(null, new byte[]{1}, IClass.getClass(Dto.class)));
			assertThrows(ApiException.class,
					() -> SerializationExpressions.deserialize(s, null, IClass.getClass(Dto.class)));
			assertThrows(ApiException.class,
					() -> SerializationExpressions.deserialize(s, new byte[]{1}, null));
		}
	}

	@Nested
	@DisplayName("setRequestArg")
	class SetRequestArg {

		@Test
		@DisplayName("writes value into the request arg map and returns it")
		void writesAndReturns() {
			IOperationRequest req = new OperationRequest(new java.util.HashMap<>());
			Dto dto = new Dto();
			Object ret = SerializationExpressions.setRequestArg(req, "body", dto);
			assertSame(dto, ret);
			assertSame(dto, req.arg("body").orElse(null));
		}

		@Test
		@DisplayName("unwraps Optional values before storing them")
		void unwrapsOptional() {
			IOperationRequest req = new OperationRequest(new java.util.HashMap<>());
			Dto dto = new Dto();
			SerializationExpressions.setRequestArg(req, "body", Optional.of(dto));
			assertSame(dto, req.arg("body").orElse(null));
		}

		@Test
		@DisplayName("throws when request or key is null")
		void rejectsNulls() {
			IOperationRequest req = new OperationRequest(new java.util.HashMap<>());
			assertThrows(ApiException.class, () -> SerializationExpressions.setRequestArg(null, "body", "v"));
			assertThrows(ApiException.class, () -> SerializationExpressions.setRequestArg(req, null, "v"));
		}
	}

	@Nested
	@DisplayName("resolveBodyType")
	class ResolveBodyType {

		@SuppressWarnings({"unchecked", "rawtypes"})
		private IApi apiWithDomain(String domainName, IClass<?> dtoClass) {
			IApi api = mock(IApi.class);
			IDomain domain = mock(IDomain.class);
			IDomainDefinition def = mock(IDomainDefinition.class);
			IDtoDefinition dtoDef = mock(IDtoDefinition.class);

			when(dtoDef.dtoClass()).thenReturn(dtoClass);
			when(def.dtoDefinitions()).thenReturn(List.of(dtoDef));
			when(domain.getDomainDefinition()).thenReturn(def);
			when(api.getDomain(domainName)).thenReturn(Optional.of(domain));
			return api;
		}

		private OperationDefinition opWithoutEntity() {
			return new OperationDefinition("users", TechnicalOperation.create, null, Scope.oneEntity,
					OperationType.standard, false, null, Access.anonymous);
		}

		@Test
		@DisplayName("returns the operation's entity class by default (CRUD scripts operate on the entity)")
		void returnsEntityClass() {
			OperationDefinition op = opWith(TechnicalOperation.create);
			IClass<?> resolved = SerializationExpressions.resolveBodyType(op, null);
			assertNotNull(resolved);
			assertEquals(IClass.getClass(Dto.class), resolved);
		}

		@Test
		@DisplayName("falls back to the domain's first DTO class when the operation has no entity class")
		void fallsBackToDtoWhenEntityNull() {
			IClass<Dto> dtoClass = IClass.getClass(Dto.class);
			IApi api = apiWithDomain("users", dtoClass);
			IClass<?> resolved = SerializationExpressions.resolveBodyType(opWithoutEntity(), api);
			assertEquals(dtoClass, resolved);
		}

		@Test
		@DisplayName("throws when entity is null and API context is null")
		void nullEntityAndNullApi() {
			ApiException ex = assertThrows(ApiException.class,
					() -> SerializationExpressions.resolveBodyType(opWithoutEntity(), null));
			assertTrue(ex.getMessage().contains("apiContext is null"));
		}

		@Test
		@DisplayName("throws when operation is null")
		void nullOperation() {
			ApiException ex = assertThrows(ApiException.class,
					() -> SerializationExpressions.resolveBodyType(null, null));
			assertTrue(ex.getMessage().contains("operation is null"));
		}

		@Test
		@DisplayName("throws when falling back and the domain is not registered in the API")
		void throwsOnUnknownDomain() {
			IApi api = mock(IApi.class);
			when(api.getDomain("users")).thenReturn(Optional.empty());
			ApiException ex = assertThrows(ApiException.class,
					() -> SerializationExpressions.resolveBodyType(opWithoutEntity(), api));
			assertTrue(ex.getMessage().contains("Unknown domain"));
		}
	}
}
