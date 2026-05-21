package com.garganttua.api.core.unit.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.garganttua.api.core.expression.ApiExpressions;
import com.garganttua.api.core.expression.AuthorizationProtocolExpressions;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.core.reflection.IClass;

@DisplayName("AuthorizationProtocolExpressions")
class AuthorizationProtocolExpressionsTest {

	/** Plain authorization entity — no interface contract required. */
	static class FakeAuth {
		final String marker;
		FakeAuth(String marker) { this.marker = marker; }
	}

	/** Records the last decode invocation arguments. */
	static class StubProtocol implements IAuthorizationProtocol {
		final String scheme;
		String lastValue;
		IApi lastApi;
		Object canned;
		ApiException toThrow;

		StubProtocol(String scheme) { this.scheme = scheme; }

		@Override public String scheme() { return scheme; }
		@Override public IClass<?> targetDomain() { return IClass.getClass(Object.class); }

		@Override
		public Object decode(String rawAuthorizationValue, IApi api) throws ApiException {
			this.lastValue = rawAuthorizationValue;
			this.lastApi = api;
			if (toThrow != null) throw toThrow;
			return canned;
		}
	}

	private static IApi apiWithProtocols(IAuthorizationProtocol... protocols) {
		IApi api = mock(IApi.class);
		when(api.getAuthorizationProtocols()).thenReturn(List.of(protocols));
		return api;
	}

	@Nested
	@DisplayName("rawAuthorizationAsString")
	class RawToString {

		@Test
		@DisplayName("returns String unchanged")
		void stringPassThrough() {
			assertEquals("Bearer xyz",
					AuthorizationProtocolExpressions.rawAuthorizationAsString("Bearer xyz"));
		}

		@Test
		@DisplayName("decodes byte[] as UTF-8")
		void bytesUtf8() {
			byte[] bytes = "Bearer caf\u00e9".getBytes(StandardCharsets.UTF_8);
			assertEquals("Bearer café",
					AuthorizationProtocolExpressions.rawAuthorizationAsString(bytes));
		}

		@Test
		@DisplayName("unboxes Byte[] and decodes as UTF-8")
		void boxedBytes() {
			Byte[] boxed = new Byte[] {(byte) 'B', (byte) 'a', (byte) 's', (byte) 'i', (byte) 'c', (byte) ' ', (byte) 'x'};
			assertEquals("Basic x",
					AuthorizationProtocolExpressions.rawAuthorizationAsString(boxed));
		}

		@Test
		@DisplayName("returns null for null input")
		void nullInput() {
			assertNull(AuthorizationProtocolExpressions.rawAuthorizationAsString(null));
		}

		@Test
		@DisplayName("unwraps Optional inputs")
		void unwrapsOptional() {
			assertEquals("Bearer xyz",
					AuthorizationProtocolExpressions.rawAuthorizationAsString(Optional.of("Bearer xyz")));
			assertNull(AuthorizationProtocolExpressions.rawAuthorizationAsString(Optional.empty()));
		}
	}

	@Nested
	@DisplayName("parseAuthorizationScheme")
	class ParseScheme {

		@Test
		@DisplayName("returns lowercased scheme token")
		void happy() {
			assertEquals("bearer", AuthorizationProtocolExpressions.parseAuthorizationScheme("Bearer xyz"));
			assertEquals("basic",  AuthorizationProtocolExpressions.parseAuthorizationScheme("BASIC abc"));
			assertEquals("apikey", AuthorizationProtocolExpressions.parseAuthorizationScheme("ApiKey k1"));
		}

		@Test
		@DisplayName("handles multiple whitespace between scheme and value")
		void multiWhitespace() {
			assertEquals("bearer",
					AuthorizationProtocolExpressions.parseAuthorizationScheme("Bearer    xyz"));
		}

		@Test
		@DisplayName("trims leading/trailing whitespace from header")
		void trimsHeader() {
			assertEquals("bearer",
					AuthorizationProtocolExpressions.parseAuthorizationScheme("   Bearer xyz   "));
		}

		@Test
		@DisplayName("throws for null header")
		void nullHeader() {
			ApiException ex = assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.parseAuthorizationScheme(null));
			assertTrue(ex.getMessage().contains("null"));
		}

		@Test
		@DisplayName("throws for blank header")
		void blank() {
			assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.parseAuthorizationScheme("   "));
		}

		@Test
		@DisplayName("throws when no whitespace separator")
		void noSeparator() {
			ApiException ex = assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.parseAuthorizationScheme("BearerXyzNoSpace"));
			assertTrue(ex.getMessage().contains("scheme/value separator"));
		}
	}

	@Nested
	@DisplayName("parseAuthorizationValue")
	class ParseValue {

		@Test
		@DisplayName("returns the value after the scheme")
		void happy() {
			assertEquals("xyz.abc.def",
					AuthorizationProtocolExpressions.parseAuthorizationValue("Bearer xyz.abc.def"));
		}

		@Test
		@DisplayName("preserves whitespace inside the value")
		void preservesInternalWs() {
			assertEquals("a b c",
					AuthorizationProtocolExpressions.parseAuthorizationValue("Basic a b c"));
		}

		@Test
		@DisplayName("strips leading whitespace between scheme and value")
		void stripsLeadingWs() {
			assertEquals("xyz",
					AuthorizationProtocolExpressions.parseAuthorizationValue("Bearer    xyz"));
		}

		@Test
		@DisplayName("throws when value is missing")
		void missingValue() {
			ApiException ex = assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.parseAuthorizationValue("Bearer "));
			assertTrue(ex.getMessage().contains("no value"));
		}

		@Test
		@DisplayName("throws when no whitespace separator")
		void noSeparator() {
			assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.parseAuthorizationValue("Bearer"));
		}

		@Test
		@DisplayName("throws for null header")
		void nullHeader() {
			assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.parseAuthorizationValue(null));
		}
	}

	@Nested
	@DisplayName("resolveAuthorizationProtocol")
	class ResolveProtocol {

		@Test
		@DisplayName("matches scheme case-insensitively")
		void caseInsensitive() {
			StubProtocol p = new StubProtocol("Bearer");
			IApi api = apiWithProtocols(p);
			assertSame(p, AuthorizationProtocolExpressions.resolveAuthorizationProtocol(api, "bearer"));
			assertSame(p, AuthorizationProtocolExpressions.resolveAuthorizationProtocol(api, "BEARER"));
			assertSame(p, AuthorizationProtocolExpressions.resolveAuthorizationProtocol(api, "BeArEr"));
		}

		@Test
		@DisplayName("first registered wins (registration-order priority)")
		void firstWins() {
			StubProtocol p1 = new StubProtocol("Bearer");
			StubProtocol p2 = new StubProtocol("Bearer");
			IApi api = apiWithProtocols(p1, p2);
			assertSame(p1, AuthorizationProtocolExpressions.resolveAuthorizationProtocol(api, "bearer"));
		}

		@Test
		@DisplayName("throws when no protocol matches the scheme")
		void noMatch() {
			IApi api = apiWithProtocols(new StubProtocol("Bearer"));
			ApiException ex = assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.resolveAuthorizationProtocol(api, "Basic"));
			assertTrue(ex.getMessage().contains("No authorization protocol"));
			assertTrue(ex.getMessage().contains("Basic"));
		}

		@Test
		@DisplayName("throws when API context is null")
		void nullApi() {
			assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.resolveAuthorizationProtocol(null, "Bearer"));
		}

		@Test
		@DisplayName("throws when scheme is null")
		void nullScheme() {
			IApi api = apiWithProtocols();
			assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.resolveAuthorizationProtocol(api, null));
		}

		@Test
		@DisplayName("unwraps Optional inputs")
		void unwrapsOptional() {
			StubProtocol p = new StubProtocol("Bearer");
			IApi api = apiWithProtocols(p);
			assertSame(p, AuthorizationProtocolExpressions.resolveAuthorizationProtocol(
					Optional.of(api), Optional.of("bearer")));
		}
	}

	@Nested
	@DisplayName("decodeAuthorization")
	class Decode {

		@Test
		@DisplayName("delegates to protocol.decode and returns the typed authorization")
		void delegates() {
			StubProtocol p = new StubProtocol("Bearer");
			FakeAuth auth = new FakeAuth("xyz");
			p.canned = auth;
			IApi api = mock(IApi.class);

			Object result = AuthorizationProtocolExpressions.decodeAuthorization(p, "xyz.abc", api);
			assertSame(auth, result);
			assertEquals("xyz.abc", p.lastValue);
			assertSame(api, p.lastApi);
		}

		@Test
		@DisplayName("propagates ApiException unchanged")
		void propagatesApiException() {
			StubProtocol p = new StubProtocol("Bearer");
			ApiException original = new ApiException("invalid signature");
			p.toThrow = original;

			ApiException ex = assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.decodeAuthorization(p, "x", null));
			assertSame(original, ex);
		}

		@Test
		@DisplayName("wraps RuntimeException into ApiException")
		void wrapsRuntime() {
			IAuthorizationProtocol p = new IAuthorizationProtocol() {
				@Override public String scheme() { return "Bearer"; }
				@Override public com.garganttua.core.reflection.IClass<?> targetDomain() {
					return com.garganttua.core.reflection.IClass.getClass(Object.class);
				}
				@Override public Object decode(String v, IApi api) {
					throw new IllegalStateException("boom");
				}
			};
			ApiException ex = assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.decodeAuthorization(p, "x", null));
			assertTrue(ex.getMessage().contains("Authorization decoding failed"));
		}

		@Test
		@DisplayName("throws when protocol is null")
		void nullProtocol() {
			assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.decodeAuthorization(null, "x", null));
		}

		@Test
		@DisplayName("throws when value is null")
		void nullValue() {
			StubProtocol p = new StubProtocol("Bearer");
			assertThrows(ApiException.class,
					() -> AuthorizationProtocolExpressions.decodeAuthorization(p, null, null));
		}
	}

	@Nested
	@DisplayName("ApiExpressions.isNull")
	class IsNullExpression {

		@Test
		@DisplayName("returns true for null")
		void trueForNull() {
			assertTrue(ApiExpressions.isNull(null));
		}

		@Test
		@DisplayName("returns true for Optional.empty()")
		void trueForEmptyOptional() {
			assertTrue(ApiExpressions.isNull(Optional.empty()));
		}

		@Test
		@DisplayName("returns false for non-null value")
		void falseForValue() {
			assertFalse(ApiExpressions.isNull("x"));
			assertFalse(ApiExpressions.isNull(0));
			assertFalse(ApiExpressions.isNull(Boolean.FALSE));
		}

		@Test
		@DisplayName("returns false for Optional.of(value)")
		void falseForPresentOptional() {
			assertFalse(ApiExpressions.isNull(Optional.of("x")));
		}
	}
}
