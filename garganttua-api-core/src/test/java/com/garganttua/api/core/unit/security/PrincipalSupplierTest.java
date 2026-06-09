package com.garganttua.api.core.unit.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.security.authentication.PrincipalSupplier;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.SupplyException;

/**
 * Unit tests for {@link PrincipalSupplier}.
 *
 * <p>The supplier resolves the principal by reading the authenticator entity
 * through the <strong>domain pipeline</strong> ({@code readAll} + login filter),
 * <em>not</em> by hitting the repository directly. These tests therefore stub
 * {@link IDomain#invoke(IOperationRequest)} — the bridge that
 * {@code SecurityExpressions.invokeReadAll} calls — rather than a repository.
 */
@DisplayName("PrincipalSupplier Tests")
class PrincipalSupplierTest {

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    private PrincipalSupplier supplier;
    @SuppressWarnings("rawtypes")
    private IRuntimeContext runtimeContext;
    @SuppressWarnings("rawtypes")
    private IDomain domainContext;
    @SuppressWarnings("rawtypes")
    private DomainDefinition domainDefinition;
    private IDomainSecurityDefinition securityDefinition;
    private IAuthenticatorDefinition authenticatorDefinition;
    private IOperationRequest operationRequest;
    private IAuthenticationRequest authenticationRequest;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        supplier = new PrincipalSupplier();
        runtimeContext = mock(IRuntimeContext.class);
        domainContext = mock(IDomain.class);
        domainDefinition = mock(DomainDefinition.class);
        securityDefinition = mock(IDomainSecurityDefinition.class);
        authenticatorDefinition = mock(IAuthenticatorDefinition.class);
        operationRequest = mock(IOperationRequest.class);
        authenticationRequest = mock(IAuthenticationRequest.class);

        when(domainContext.getDomainDefinition()).thenReturn(domainDefinition);
        when(domainContext.getDomainName()).thenReturn("users");
        when(domainContext.getEntityClass()).thenReturn(IClass.getClass(Object.class));
        when(domainDefinition.domainSecurityDefinition()).thenReturn(securityDefinition);
        when(securityDefinition.authenticatorDefinition()).thenReturn(authenticatorDefinition);
        when(authenticatorDefinition.login()).thenReturn(new ObjectAddress("id"));
        when(authenticatorDefinition.alwaysEnabled()).thenReturn(true);
        when(authenticationRequest.login()).thenReturn("john@example.com");
        doReturn(Optional.of(authenticationRequest)).when(operationRequest).arg("entity");
    }

    @SuppressWarnings("unchecked")
    private void setupRuntimeContext() {
        when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.of(operationRequest));
        when(runtimeContext.getVariable(eq("domainContext"), any(IClass.class))).thenReturn(Optional.of(domainContext));
    }

    /**
     * Stubs the domain pipeline's {@code readAll} (reached via
     * {@code SecurityExpressions.invokeReadAll}) to return {@code body} as the
     * successful response payload.
     */
    @SuppressWarnings("unchecked")
    private void stubPipelineReadAll(Object body) {
        IOperationResponse response = mock(IOperationResponse.class);
        when(response.getResponseCode()).thenReturn(OperationResponseCode.OK);
        when(response.getException()).thenReturn(Optional.empty());
        doReturn(body).when(response).getResponse();
        when(domainContext.invoke(any(IOperationRequest.class))).thenReturn(response);
    }

    @Nested
    @DisplayName("Type metadata")
    class TypeMetadata {

        @Test
        @DisplayName("getSuppliedType returns Object type")
        void suppliedTypeIsObject() {
            assertEquals(Object.class, supplier.getSuppliedType());
        }

        @Test
        @DisplayName("getSuppliedClass wraps Object.class")
        void suppliedClassWrapsObject() {
            assertNotNull(supplier.getSuppliedClass());
            assertEquals(Object.class, supplier.getSuppliedClass().getType());
        }

        @Test
        @DisplayName("getOwnerContextType wraps IRuntimeContext.class")
        void ownerContextTypeWrapsRuntimeContext() {
            assertNotNull(supplier.getOwnerContextType());
            assertEquals(IRuntimeContext.class, supplier.getOwnerContextType().getType());
        }
    }

    @Nested
    @DisplayName("supply() validation")
    class SupplyValidation {

        @Test
        @DisplayName("throws when context is null")
        void throwsOnNullContext() {
            assertThrows(SupplyException.class, () -> supplier.supply(null));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("throws when request variable is missing")
        void throwsOnMissingRequest() {
            when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.empty());
            assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("throws when domainContext variable is missing")
        void throwsOnMissingDomain() {
            when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.of(operationRequest));
            when(runtimeContext.getVariable(eq("domainContext"), any(IClass.class))).thenReturn(Optional.empty());
            assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
        }

        @Test
        @DisplayName("throws when security definition is null")
        void throwsOnNoSecurityDefinition() {
            setupRuntimeContext();
            when(domainDefinition.domainSecurityDefinition()).thenReturn(null);
            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("security definition"));
        }

        @Test
        @DisplayName("throws when authenticator definition is null")
        void throwsOnNoAuthenticatorDefinition() {
            setupRuntimeContext();
            when(securityDefinition.authenticatorDefinition()).thenReturn(null);
            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("authenticator definition"));
        }

        @Test
        @DisplayName("throws when entity is missing from request")
        void throwsOnMissingEntity() {
            setupRuntimeContext();
            when(operationRequest.arg("entity")).thenReturn(Optional.empty());
            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("entity"));
        }

        @Test
        @DisplayName("throws when login field is null on authenticator")
        void throwsOnNoLoginField() {
            setupRuntimeContext();
            when(authenticatorDefinition.login()).thenReturn(null);
            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("login field"));
        }
    }

    @Nested
    @DisplayName("findByLogin (via the domain pipeline)")
    class FindByLogin {

        @Test
        @DisplayName("throws when the pipeline returns no entity for the login")
        void throwsWhenUserNotFound() {
            setupRuntimeContext();
            stubPipelineReadAll(List.of());
            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("not found"));
            assertTrue(ex.getMessage().contains("john@example.com"));
        }

        @Test
        @DisplayName("throws when the pipeline returns a null body")
        void throwsWhenPipelineReturnsNull() {
            setupRuntimeContext();
            stubPipelineReadAll(null);
            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("surfaces a pipeline failure as a SupplyException naming the domain and login")
        void wrapsPipelineFailure() {
            setupRuntimeContext();
            when(domainContext.invoke(any(IOperationRequest.class)))
                    .thenThrow(new RuntimeException("boom from pipeline"));
            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("users"), "message names the domain");
            assertTrue(ex.getMessage().contains("john@example.com"), "message names the login");
            assertTrue(ex.getMessage().contains("boom from pipeline"), "message carries the cause text");
        }

        @Test
        @DisplayName("returns the first entity when the pipeline finds one")
        void returnsFirstEntity() throws SupplyException {
            setupRuntimeContext();
            Object expectedPrincipal = new Object();
            stubPipelineReadAll(List.of(expectedPrincipal));

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(expectedPrincipal, result.get());
        }

        @Test
        @DisplayName("returns the first entity when the pipeline returns several")
        void returnsFirstOfMultiple() throws SupplyException {
            setupRuntimeContext();
            Object first = new Object();
            Object second = new Object();
            stubPipelineReadAll(List.of(first, second));

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(first, result.get());
        }

        @Test
        @DisplayName("queries the pipeline with a filter on the authenticator's login field")
        void usesLoginFieldFromAuthDef() throws SupplyException {
            setupRuntimeContext();
            when(authenticatorDefinition.login()).thenReturn(new ObjectAddress("email"));
            stubPipelineReadAll(List.of(new Object()));

            supplier.supply(runtimeContext);

            ArgumentCaptor<IOperationRequest> reqCaptor = ArgumentCaptor.forClass(IOperationRequest.class);
            verify(domainContext).invoke(reqCaptor.capture());
            Optional<?> filterArg = reqCaptor.getValue().arg(IOperationRequest.FILTER);
            assertTrue(filterArg.isPresent(), "the readAll request must carry a filter");
            // Filter.eq("email", login) renders as a $field/$eq tree:
            //   Filter{name='$field', value=email, literals=[Filter{name='$eq', value=<login>, ...}]}
            String filterStr = filterArg.get().toString();
            assertTrue(filterStr.contains("value=email"),
                    "the filter must target the configured login field 'email'; got " + filterStr);
            assertFalse(filterStr.contains("value=id"),
                    "the filter must NOT fall back to the default 'id' field once 'email' is configured; got " + filterStr);
            assertTrue(filterStr.contains("value=john@example.com"),
                    "the filter must match on the requested login value; got " + filterStr);
        }
    }

    @Nested
    @DisplayName("checkAccountStatus")
    class CheckAccountStatus {

        // Use a real POJO so DefaultMapper.reflection() can read fields
        public static class FakeUser {
            public boolean enabled = true;
            public boolean accountNonLocked = true;
            public boolean accountNonExpired = true;
            public boolean credentialsNonExpired = true;
        }

        @BeforeEach
        void setUpForAccountChecks() {
            when(authenticatorDefinition.alwaysEnabled()).thenReturn(false);
        }

        @Test
        @DisplayName("skips all checks when alwaysEnabled is true")
        void skipsChecksWhenAlwaysEnabled() throws SupplyException {
            setupRuntimeContext();
            when(authenticatorDefinition.alwaysEnabled()).thenReturn(true);
            when(authenticatorDefinition.enabled()).thenReturn(new ObjectAddress("enabled"));
            FakeUser user = new FakeUser();
            user.enabled = false; // would fail if checked
            stubPipelineReadAll(List.of(user));

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(user, result.get());
        }

        @Test
        @DisplayName("throws when account is disabled")
        void throwsWhenDisabled() {
            setupRuntimeContext();
            when(authenticatorDefinition.enabled()).thenReturn(new ObjectAddress("enabled"));
            FakeUser user = new FakeUser();
            user.enabled = false;
            stubPipelineReadAll(List.of(user));

            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("disabled"));
        }

        @Test
        @DisplayName("throws when account is locked")
        void throwsWhenLocked() {
            setupRuntimeContext();
            when(authenticatorDefinition.accountNonLocked()).thenReturn(new ObjectAddress("accountNonLocked"));
            FakeUser user = new FakeUser();
            user.accountNonLocked = false;
            stubPipelineReadAll(List.of(user));

            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("locked"));
        }

        @Test
        @DisplayName("throws when account is expired")
        void throwsWhenExpired() {
            setupRuntimeContext();
            when(authenticatorDefinition.accountNonExpired()).thenReturn(new ObjectAddress("accountNonExpired"));
            FakeUser user = new FakeUser();
            user.accountNonExpired = false;
            stubPipelineReadAll(List.of(user));

            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("expired"));
        }

        @Test
        @DisplayName("throws when credentials are expired")
        void throwsWhenCredentialsExpired() {
            setupRuntimeContext();
            when(authenticatorDefinition.credentialsNonExpired()).thenReturn(new ObjectAddress("credentialsNonExpired"));
            FakeUser user = new FakeUser();
            user.credentialsNonExpired = false;
            stubPipelineReadAll(List.of(user));

            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("expired"));
        }

        @Test
        @DisplayName("passes when all account status checks are positive")
        void passesWhenAllChecksOk() throws SupplyException {
            setupRuntimeContext();
            when(authenticatorDefinition.enabled()).thenReturn(new ObjectAddress("enabled"));
            when(authenticatorDefinition.accountNonLocked()).thenReturn(new ObjectAddress("accountNonLocked"));
            when(authenticatorDefinition.accountNonExpired()).thenReturn(new ObjectAddress("accountNonExpired"));
            when(authenticatorDefinition.credentialsNonExpired()).thenReturn(new ObjectAddress("credentialsNonExpired"));
            FakeUser user = new FakeUser();
            stubPipelineReadAll(List.of(user));

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(user, result.get());
        }

        @Test
        @DisplayName("skips individual checks when field address is null")
        void skipsNullFieldAddresses() throws SupplyException {
            setupRuntimeContext();
            // All field addresses are null by default (mock returns null)
            // So no checks should be performed even though alwaysEnabled=false
            FakeUser user = new FakeUser();
            user.enabled = false; // would fail if checked
            stubPipelineReadAll(List.of(user));

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(user, result.get());
        }
    }
}
