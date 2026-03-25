package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
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

@DisplayName("Authentication Cascade Integration Tests")
class AuthenticationCascadeTest {

	/**
	 * Simple entity with an authenticate method that always succeeds.
	 */
	public static class AlwaysAuthEntity {
		public boolean authenticate(String id, byte[] credentials) {
			return true;
		}
	}

	private ApiSecurityContext apiSecurityContext;

	@BeforeEach
	void setUp() {
		apiSecurityContext = new ApiSecurityContext(false);
	}

	private AuthenticationContext createAuthContext(
			IDomainContext<?> domainContext,
			String authenticateMethodName) {
		AuthenticationDefinition definition = new AuthenticationDefinition(
				null, authenticateMethodName, null, null, null, null, null);

		AuthenticationContext ctx = new AuthenticationContext(definition);
		ctx.setDomainContext(domainContext);
		return ctx;
	}

	@SuppressWarnings("unchecked")
	private IDomainContext<?> createDomainContextWithEntity(Object entity) {
		IDomainContext<?> domainContext = mock(IDomainContext.class);
		IEntityDefinition<?> entityDef = mock(IEntityDefinition.class);
		when(entityDef.id()).thenReturn(new ObjectAddress("id"));
		when(((IDomainContext<Object>) domainContext).getDomainDefinition()).thenReturn(null);
		when(domainContext.getEntityDefinition()).thenReturn((IEntityDefinition) entityDef);
		when(domainContext.getDomainName()).thenReturn("users");

		if (entity != null) {
			when(domainContext.readAll(any(), isNull(), isNull(), any()))
					.thenReturn(OperationResponse.ok(List.of(entity)));
		} else {
			when(domainContext.readAll(any(), isNull(), isNull(), any()))
					.thenReturn(OperationResponse.ok(List.of()));
		}
		return domainContext;
	}

	private IDomainContext<?> createEmptyDomainContext() {
		return createDomainContextWithEntity(null);
	}

	@Nested
	@DisplayName("Single authentication context")
	class SingleContext {

		@Test
		@DisplayName("succeeds when principal found and authenticate method returns true")
		void singleContextSuccess() {
			IDomainContext<?> domainCtx = createDomainContextWithEntity(new AlwaysAuthEntity());
			AuthenticationContext auth = createAuthContext(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("users", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("users")
					.id("john@example.com")
					.credentials("password".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertNotNull(response);
			assertEquals(OperationResponseCode.OK, response.getResponseCode());
		}

		@Test
		@DisplayName("returns unauthorized when principal not found")
		void principalNotFound() {
			IDomainContext<?> domainCtx = createEmptyDomainContext();
			AuthenticationContext auth = createAuthContext(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("users", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("users")
					.id("unknown@example.com")
					.credentials("wrong".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertNotNull(response);
			assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode());
		}

		@Test
		@DisplayName("returns unauthorized when no method binder configured")
		void noMethodBinder() {
			IDomainContext<?> domainCtx = createDomainContextWithEntity(new AlwaysAuthEntity());
			AuthenticationContext auth = createAuthContext(domainCtx, null);
			apiSecurityContext.registerAuthenticationContexts("users", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("users")
					.id("user@example.com")
					.credentials("pass".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode());
		}
	}

	@Nested
	@DisplayName("Cascade with multiple contexts")
	class CascadeMultiple {

		@Test
		@DisplayName("first context fails (no principal), second succeeds")
		void firstFailsSecondSucceeds() {
			IDomainContext<?> emptyDomain = createEmptyDomainContext();
			AuthenticationContext fail = createAuthContext(emptyDomain, "authenticate");

			IDomainContext<?> fullDomain = createDomainContextWithEntity(new AlwaysAuthEntity());
			AuthenticationContext success = createAuthContext(fullDomain, "authenticate");

			apiSecurityContext.registerAuthenticationContexts("users", List.of(fail, success));

			IOperationResponse response = apiSecurityContext.request("users")
					.id("user1")
					.credentials("credentials".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertNotNull(response);
			assertEquals(OperationResponseCode.OK, response.getResponseCode());
		}

		@Test
		@DisplayName("all contexts fail returns unauthorized")
		void allContextsFail() {
			IDomainContext<?> emptyDomain = createEmptyDomainContext();
			AuthenticationContext fail1 = createAuthContext(emptyDomain, "authenticate");
			AuthenticationContext fail2 = createAuthContext(emptyDomain, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("users", List.of(fail1, fail2));

			IOperationResponse response = apiSecurityContext.request("users")
					.id("user1")
					.credentials("credentials".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode());
			assertEquals("Authentication failed", response.getResponse());
		}
	}

	@Nested
	@DisplayName("Domain isolation")
	class DomainIsolation {

		@Test
		@DisplayName("different domains have independent auth contexts")
		void independentDomains() {
			IDomainContext<?> userDomain = createDomainContextWithEntity(new AlwaysAuthEntity());
			AuthenticationContext userAuth = createAuthContext(userDomain, "authenticate");

			IDomainContext<?> deviceDomain = createDomainContextWithEntity(new AlwaysAuthEntity());
			when(deviceDomain.getDomainName()).thenReturn("devices");
			AuthenticationContext deviceAuth = createAuthContext(deviceDomain, "authenticate");

			apiSecurityContext.registerAuthenticationContexts("users", List.of(userAuth));
			apiSecurityContext.registerAuthenticationContexts("devices", List.of(deviceAuth));

			IOperationResponse userResp = apiSecurityContext.request("users")
					.id("user1").credentials("credentials".getBytes(StandardCharsets.UTF_8)).execute();
			IOperationResponse deviceResp = apiSecurityContext.request("devices")
					.id("device1").credentials("key".getBytes(StandardCharsets.UTF_8)).execute();

			assertEquals(OperationResponseCode.OK, userResp.getResponseCode());
			assertEquals(OperationResponseCode.OK, deviceResp.getResponseCode());
		}

		@Test
		@DisplayName("unknown domain returns unauthorized")
		void unknownDomain() {
			IOperationResponse response = apiSecurityContext.request("unknown")
					.id("user1")
					.credentials("credentials".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode());
		}
	}

	@Nested
	@DisplayName("Tenant scoping")
	class TenantScoping {

		@Test
		@DisplayName("tenantId creates tenant caller for repository lookup")
		void tenantIdUsedForLookup() {
			IDomainContext<?> domainCtx = createDomainContextWithEntity(new AlwaysAuthEntity());
			AuthenticationContext auth = createAuthContext(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("users", List.of(auth));

			apiSecurityContext.request("users")
					.id("user1")
					.credentials("credentials".getBytes(StandardCharsets.UTF_8))
					.tenantId("TENANT_42")
					.execute();

			verify(domainCtx).readAll(any(), isNull(), isNull(), argThat(caller ->
					"TENANT_42".equals(caller.tenantId()) && !caller.superTenant()));
		}

		@Test
		@DisplayName("no tenantId creates super caller for repository lookup")
		void noTenantIdUsesSuperCaller() {
			IDomainContext<?> domainCtx = createDomainContextWithEntity(new AlwaysAuthEntity());
			AuthenticationContext auth = createAuthContext(domainCtx, "authenticate");
			apiSecurityContext.registerAuthenticationContexts("users", List.of(auth));

			apiSecurityContext.request("users")
					.id("user1")
					.credentials("credentials".getBytes(StandardCharsets.UTF_8))
					.execute();

			verify(domainCtx).readAll(any(), isNull(), isNull(), argThat(caller ->
					caller.superTenant() && caller.superOwner()));
		}
	}

	@Nested
	@DisplayName("No authenticate method configured")
	class NoAuthenticateMethod {

		@Test
		@DisplayName("returns unauthorized when no authenticate method name")
		void returnsUnauthorizedWithNoMethod() {
			IDomainContext<?> domainCtx = createDomainContextWithEntity(new AlwaysAuthEntity());
			AuthenticationContext auth = createAuthContext(domainCtx, null);
			apiSecurityContext.registerAuthenticationContexts("users", List.of(auth));

			IOperationResponse response = apiSecurityContext.request("users")
					.id("user1")
					.credentials("credentials".getBytes(StandardCharsets.UTF_8))
					.execute();

			assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode());
		}
	}
}
