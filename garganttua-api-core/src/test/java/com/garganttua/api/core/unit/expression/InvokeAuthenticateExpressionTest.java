package com.garganttua.api.core.unit.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.garganttua.api.core.api.Api;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.commons.security.authentication.Authentication;
import com.garganttua.api.core.service.OperationResponse;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;

@DisplayName("SecurityExpressions — authenticate pipeline bridge")
class InvokeAuthenticateExpressionTest {

	static class FixtureEntity {}
	static class OtherEntity {}

	static class FixtureAuth {}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private static IAuthorizationProtocol protocolWithTarget(IClass<?> target) {
		IAuthorizationProtocol p = mock(IAuthorizationProtocol.class);
		when(p.scheme()).thenReturn("Bearer");
		when((IClass) p.targetDomain()).thenReturn(target);
		return p;
	}

	@Nested
	@DisplayName("protocolTargetDomain")
	class ProtocolTargetDomain {

		@Test
		@DisplayName("delegates to IAuthorizationProtocol.targetDomain()")
		void delegates() {
			IClass<FixtureEntity> cls = IClass.getClass(FixtureEntity.class);
			IAuthorizationProtocol p = protocolWithTarget(cls);
			assertSame(cls, SecurityExpressions.protocolTargetDomain(p));
		}

		@Test
		@DisplayName("throws when the protocol is null")
		void rejectsNullProtocol() {
			assertThrows(ApiException.class,
					() -> SecurityExpressions.protocolTargetDomain(null));
		}

		@Test
		@DisplayName("throws when targetDomain() returns null")
		void rejectsNullTarget() {
			IAuthorizationProtocol p = protocolWithTarget(null);
			ApiException ex = assertThrows(ApiException.class,
					() -> SecurityExpressions.protocolTargetDomain(p));
			assertTrue(ex.getMessage().contains("targetDomain"));
		}
	}

	@Nested
	@DisplayName("resolveDomainByEntityClass")
	class ResolveDomainByEntityClass {

		@SuppressWarnings({"rawtypes", "unchecked"})
		private Api apiWithDomain(String domainName, IClass<?> entityClass) {
			Api api = mock(Api.class);
			IDomain domain = mock(IDomain.class);
			when(domain.getEntityClass()).thenReturn(entityClass);
			when(domain.getDomainName()).thenReturn(domainName);
			Map<String, IDomain<?>> domains = new java.util.LinkedHashMap<>();
			domains.put(domainName, domain);
			when(api.getDomains()).thenReturn(domains);
			return api;
		}

		@Test
		@DisplayName("returns the domain whose entity class matches the target")
		void happyMatch() {
			IClass<FixtureEntity> cls = IClass.getClass(FixtureEntity.class);
			Api api = apiWithDomain("fixtures", cls);
			IDomain<?> found = SecurityExpressions.resolveDomainByEntityClass(api, cls);
			assertNotNull(found);
			assertEquals("fixtures", found.getDomainName());
		}

		@Test
		@DisplayName("throws when no registered domain matches the target")
		void noMatch() {
			Api api = apiWithDomain("fixtures", IClass.getClass(FixtureEntity.class));
			ApiException ex = assertThrows(ApiException.class,
					() -> SecurityExpressions.resolveDomainByEntityClass(api, IClass.getClass(OtherEntity.class)));
			assertTrue(ex.getMessage().contains("No domain registered"),
					"Got: " + ex.getMessage());
			assertTrue(ex.getMessage().contains(OtherEntity.class.getName()));
		}

		@Test
		@DisplayName("throws when the API context is null")
		void nullApi() {
			assertThrows(ApiException.class,
					() -> SecurityExpressions.resolveDomainByEntityClass(null, IClass.getClass(FixtureEntity.class)));
		}

		@Test
		@DisplayName("throws when the target class is null")
		void nullTarget() {
			Api api = apiWithDomain("fixtures", IClass.getClass(FixtureEntity.class));
			assertThrows(ApiException.class,
					() -> SecurityExpressions.resolveDomainByEntityClass(api, null));
		}
	}

	@Nested
	@DisplayName("invokeAuthenticate")
	class InvokeAuthenticate {

		@SuppressWarnings({"rawtypes", "unchecked"})
		private IDomain mockDomain(String name, IClass<?> entityClass) {
			IDomain domain = mock(IDomain.class);
			when(domain.getDomainName()).thenReturn(name);
			when(domain.getEntityClass()).thenReturn(entityClass);
			return domain;
		}

		@Test
		@DisplayName("invokes domain.invoke with the authenticate operation and returns the IAuthentication")
		@SuppressWarnings({"rawtypes", "unchecked"})
		void happyPath() {
			IApi api = mock(IApi.class);
			IDomain domain = mockDomain("jwtTokens", IClass.getClass(FixtureEntity.class));
			IAuthentication expected = new Authentication(true, new Object(), null, "jwt", List.of(), null, null, false, false, true, true, true, true);
			when(domain.invoke(any(IOperationRequest.class))).thenReturn(OperationResponse.ok(expected));

			AuthenticationRequest req = new AuthenticationRequest(null, new FixtureAuth());
			IAuthentication result = SecurityExpressions.invokeAuthenticate(api, domain, req, "tenant-1");
			assertSame(expected, result);

			// The invocation carries the authenticate operation and the entity slot
			ArgumentCaptor<IOperationRequest> captor = ArgumentCaptor.forClass(IOperationRequest.class);
			verify(domain, times(1)).invoke(captor.capture());
			IOperationRequest captured = captor.getValue();
			assertSame(req, captured.arg("entity").orElse(null));
			assertEquals("tenant-1", captured.arg(IOperationRequest.TENANT_ID).orElse(null));
			assertEquals("tenant-1", captured.arg(IOperationRequest.REQUESTED_TENANT_ID).orElse(null));
		}

		@Test
		@DisplayName("throws ApiException when domain.invoke returns a non-OK response")
		@SuppressWarnings({"rawtypes", "unchecked"})
		void unauthorizedResponse() {
			IApi api = mock(IApi.class);
			IDomain domain = mockDomain("jwtTokens", IClass.getClass(FixtureEntity.class));
			when(domain.invoke(any(IOperationRequest.class)))
					.thenReturn(OperationResponse.unauthorized("invalid signature"));

			AuthenticationRequest req = new AuthenticationRequest(null, new FixtureAuth());
			ApiException ex = assertThrows(ApiException.class,
					() -> SecurityExpressions.invokeAuthenticate(api, domain, req, "tenant-1"));
			assertTrue(ex.getMessage().contains("UNAUTHORIZED"),
					"Got: " + ex.getMessage());
		}

		@Test
		@DisplayName("throws ApiException when the response body is not an IAuthentication")
		@SuppressWarnings({"rawtypes", "unchecked"})
		void nonAuthenticationBody() {
			IApi api = mock(IApi.class);
			IDomain domain = mockDomain("jwtTokens", IClass.getClass(FixtureEntity.class));
			when(domain.invoke(any(IOperationRequest.class)))
					.thenReturn(OperationResponse.ok("not-an-auth"));

			AuthenticationRequest req = new AuthenticationRequest(null, new FixtureAuth());
			ApiException ex = assertThrows(ApiException.class,
					() -> SecurityExpressions.invokeAuthenticate(api, domain, req, "tenant-1"));
			assertTrue(ex.getMessage().contains("did not return an IAuthentication"),
					"Got: " + ex.getMessage());
		}

		@Test
		@DisplayName("throws when any of api/domain/authRequest is null")
		void rejectsNulls() {
			IApi api = mock(IApi.class);
			@SuppressWarnings("unchecked")
			IDomain<?> domain = mockDomain("jwtTokens", IClass.getClass(FixtureEntity.class));
			AuthenticationRequest req = new AuthenticationRequest(null, new FixtureAuth());
			assertThrows(ApiException.class,
					() -> SecurityExpressions.invokeAuthenticate(null, domain, req, "t"));
			assertThrows(ApiException.class,
					() -> SecurityExpressions.invokeAuthenticate(api, null, req, "t"));
			assertThrows(ApiException.class,
					() -> SecurityExpressions.invokeAuthenticate(api, domain, null, "t"));
		}

		@Test
		@DisplayName("does not propagate TENANT_ID when authRequest has null tenantId")
		@SuppressWarnings({"rawtypes", "unchecked"})
		void noTenantPropagationWhenNull() {
			IApi api = mock(IApi.class);
			IDomain domain = mockDomain("jwtTokens", IClass.getClass(FixtureEntity.class));
			IAuthentication expected = new Authentication(true, new Object(), null, "jwt", List.of(), null, null, false, false, true, true, true, true);
			when(domain.invoke(any(IOperationRequest.class))).thenReturn(OperationResponse.ok(expected));

			AuthenticationRequest req = new AuthenticationRequest(null, new FixtureAuth());
			SecurityExpressions.invokeAuthenticate(api, domain, req, null);

			ArgumentCaptor<IOperationRequest> captor = ArgumentCaptor.forClass(IOperationRequest.class);
			verify(domain, times(1)).invoke(captor.capture());
			IOperationRequest captured = captor.getValue();
			assertNull(captured.arg(IOperationRequest.TENANT_ID).orElse(null));
		}
	}
}
