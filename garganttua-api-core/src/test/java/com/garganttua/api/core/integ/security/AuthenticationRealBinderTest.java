package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.context.security.ApiSecurityContext;
import com.garganttua.api.core.context.security.AuthenticationContext;
import com.garganttua.api.core.definition.AuthenticationDefinition;
import com.garganttua.api.core.service.OperationResponse;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.reflection.ObjectAddress;

@DisplayName("Authentication with real AuthenticationMethodBinderBuilder")
class AuthenticationRealBinderTest {

	/**
	 * Simple authenticator entity with a real authenticate method.
	 */
	public static class TestAuthenticator {
		private final String id;
		private final byte[] password;

		public TestAuthenticator(String id, byte[] password) {
			this.id = id;
			this.password = password;
		}

		public String getId() {
			return id;
		}

		/**
		 * Authenticate method matching signature: boolean authenticate(String id, byte[] credentials)
		 */
		public boolean authenticate(String id, byte[] credentials) {
			return this.id.equals(id) && Arrays.equals(this.password, credentials);
		}
	}

	/**
	 * Authenticator entity that returns IOperationResponse instead of boolean.
	 */
	public static class TestAuthenticatorWithResponse {
		private final String id;
		private final byte[] password;

		public TestAuthenticatorWithResponse(String id, byte[] password) {
			this.id = id;
			this.password = password;
		}

		public String getId() {
			return id;
		}

		public IOperationResponse authenticate(byte[] credentials) {
			if (Arrays.equals(this.password, credentials)) {
				return OperationResponse.ok("auth-token-for-" + id);
			}
			return OperationResponse.unauthorized("Bad credentials");
		}
	}

	private ApiSecurityContext apiSecurityContext;

	@BeforeEach
	void setUp() {
		apiSecurityContext = new ApiSecurityContext(false);
	}

	@SuppressWarnings("unchecked")
	private IDomainContext<?> createDomainContextReturning(Object entity) {
		IDomainContext<?> domainContext = mock(IDomainContext.class);
		IEntityDefinition<?> entityDef = mock(IEntityDefinition.class);
		when(entityDef.id()).thenReturn(new ObjectAddress("id"));
		when(domainContext.getEntityDefinition()).thenReturn((IEntityDefinition) entityDef);
		when(domainContext.getDomainName()).thenReturn("authenticators");

		if (entity != null) {
			when(domainContext.readAll(any(), isNull(), isNull(), any()))
					.thenReturn(OperationResponse.ok(List.of(entity)));
		} else {
			when(domainContext.readAll(any(), isNull(), isNull(), any()))
					.thenReturn(OperationResponse.ok(List.of()));
		}
		return domainContext;
	}

	private AuthenticationContext createAuthContextWithRealBinder(
			IDomainContext<?> domainContext, String methodName) {
		AuthenticationDefinition definition = new AuthenticationDefinition(
				null, methodName, null, null, null, null, null);

		AuthenticationContext ctx = new AuthenticationContext(definition);
		ctx.setDomainContext(domainContext);
		return ctx;
	}

	@Nested
	@DisplayName("Boolean authenticate method")
	class BooleanAuthenticate {

		@Test
		@DisplayName("succeeds when credentials match")
		void successfulAuthentication() {
			byte[] password = "secret123".getBytes(StandardCharsets.UTF_8);
			TestAuthenticator entity = new TestAuthenticator("john@example.com", password);

			IDomainContext<?> domainCtx = createDomainContextReturning(entity);
			AuthenticationContext auth = createAuthContextWithRealBinder(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("authenticators", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("authenticators")
					.id("john@example.com")
					.credentials(password)
					.execute();

			assertNotNull(response);
			assertEquals(OperationResponseCode.OK, response.getResponseCode());
			assertSame(entity, response.getResponse());
		}

		@Test
		@DisplayName("fails when credentials do not match")
		void failedAuthentication() {
			byte[] password = "secret123".getBytes(StandardCharsets.UTF_8);
			TestAuthenticator entity = new TestAuthenticator("john@example.com", password);

			IDomainContext<?> domainCtx = createDomainContextReturning(entity);
			AuthenticationContext auth = createAuthContextWithRealBinder(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("authenticators", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("authenticators")
					.id("john@example.com")
					.credentials("wrong-password".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode());
		}
	}

	@Nested
	@DisplayName("IOperationResponse authenticate method")
	class ResponseAuthenticate {

		@Test
		@DisplayName("returns response from authenticate method on success")
		void successReturnsToken() {
			byte[] password = "pin1234".getBytes(StandardCharsets.UTF_8);
			TestAuthenticatorWithResponse entity = new TestAuthenticatorWithResponse("device-01", password);

			IDomainContext<?> domainCtx = createDomainContextReturning(entity);
			AuthenticationContext auth = createAuthContextWithRealBinder(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("authenticators", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("authenticators")
					.id("device-01")
					.credentials(password)
					.execute();

			assertNotNull(response);
			assertEquals(OperationResponseCode.OK, response.getResponseCode());
			assertEquals("auth-token-for-device-01", response.getResponse());
		}

		@Test
		@DisplayName("returns unauthorized from authenticate method on failure")
		void failureReturnsUnauthorized() {
			byte[] password = "pin1234".getBytes(StandardCharsets.UTF_8);
			TestAuthenticatorWithResponse entity = new TestAuthenticatorWithResponse("device-01", password);

			IDomainContext<?> domainCtx = createDomainContextReturning(entity);
			AuthenticationContext auth = createAuthContextWithRealBinder(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("authenticators", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("authenticators")
					.id("device-01")
					.credentials("wrong".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode());
		}
	}

	@Nested
	@DisplayName("Principal not found in repository")
	class PrincipalNotFound {

		@Test
		@DisplayName("returns unauthorized when principal does not exist")
		void unknownPrincipal() {
			IDomainContext<?> domainCtx = createDomainContextReturning(null);
			AuthenticationContext auth = createAuthContextWithRealBinder(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("authenticators", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("authenticators")
					.id("unknown@example.com")
					.credentials("pass".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode());
		}
	}

	@Nested
	@DisplayName("Cascade with real binders")
	class CascadeWithRealBinders {

		@Test
		@DisplayName("first auth type fails, second succeeds")
		void cascadeFallthrough() {
			byte[] password = "secret".getBytes(StandardCharsets.UTF_8);

			// First context: entity exists but wrong credentials type (returns false)
			TestAuthenticator wrongEntity = new TestAuthenticator("user1", "other".getBytes(StandardCharsets.UTF_8));
			IDomainContext<?> domain1 = createDomainContextReturning(wrongEntity);
			AuthenticationContext auth1 = createAuthContextWithRealBinder(domain1, "authenticate");

			// Second context: entity with matching credentials
			TestAuthenticator rightEntity = new TestAuthenticator("user1", password);
			IDomainContext<?> domain2 = createDomainContextReturning(rightEntity);
			AuthenticationContext auth2 = createAuthContextWithRealBinder(domain2, "authenticate");

			apiSecurityContext.registerAuthenticationContexts("authenticators", List.of(auth1, auth2));

			IOperationResponse response = apiSecurityContext.request("authenticators")
					.id("user1")
					.credentials(password)
					.execute();

			assertNotNull(response);
			assertEquals(OperationResponseCode.OK, response.getResponseCode());
			assertSame(rightEntity, response.getResponse());
		}
	}
}
