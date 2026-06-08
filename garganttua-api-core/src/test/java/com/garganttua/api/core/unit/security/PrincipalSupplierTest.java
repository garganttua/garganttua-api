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

import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.security.authentication.PrincipalSupplier;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.SupplyException;

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
    private IDomain domainContext;
    private DomainDefinition domainDefinition;
    private IDomainSecurityDefinition securityDefinition;
    private IAuthenticatorDefinition authenticatorDefinition;
    private IOperationRequest operationRequest;
    private IAuthenticationRequest authenticationRequest;
    private IRepository repository;

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
        repository = mock(IRepository.class);

        when(domainContext.getDomainDefinition()).thenReturn(domainDefinition);
        when(domainDefinition.domainSecurityDefinition()).thenReturn(securityDefinition);
        when(securityDefinition.authenticatorDefinition()).thenReturn(authenticatorDefinition);
        when(authenticatorDefinition.login()).thenReturn(new ObjectAddress("id"));
        when(authenticatorDefinition.alwaysEnabled()).thenReturn(true);
        when(domainContext.getRepository()).thenReturn(repository);
        when(authenticationRequest.login()).thenReturn("john@example.com");
        doReturn(Optional.of(authenticationRequest)).when(operationRequest).arg("entity");
    }

    @SuppressWarnings("unchecked")
    private void setupRuntimeContext() {
        when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.of(operationRequest));
        when(runtimeContext.getVariable(eq("domainContext"), any(IClass.class))).thenReturn(Optional.of(domainContext));
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
    @DisplayName("findByLogin")
    class FindByLogin {

        @Test
        @DisplayName("throws when no entity found for login")
        void throwsWhenUserNotFound() {
            setupRuntimeContext();
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of());
            SupplyException ex = assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
            assertTrue(ex.getMessage().contains("not found"));
            assertTrue(ex.getMessage().contains("john@example.com"));
        }

        @Test
        @DisplayName("throws when repository returns null")
        void throwsWhenRepositoryReturnsNull() {
            setupRuntimeContext();
            when(repository.getEntities(any(), any(), any())).thenReturn(null);
            assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
        }

        @Test
        @DisplayName("returns first entity when found by login")
        void returnsFirstEntity() throws SupplyException {
            setupRuntimeContext();
            Object expectedPrincipal = new Object();
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(expectedPrincipal));

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(expectedPrincipal, result.get());
        }

        @Test
        @DisplayName("returns first entity when multiple results")
        void returnsFirstOfMultiple() throws SupplyException {
            setupRuntimeContext();
            Object first = new Object();
            Object second = new Object();
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(first, second));

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(first, result.get());
        }

        @Test
        @DisplayName("uses login field from authenticator definition as filter")
        void usesLoginFieldFromAuthDef() throws SupplyException {
            setupRuntimeContext();
            when(authenticatorDefinition.login()).thenReturn(new ObjectAddress("email"));
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(new Object()));

            supplier.supply(runtimeContext);

            verify(repository).getEntities(eq(Optional.empty()), argThat(opt -> {
                assertTrue(opt.isPresent());
                return opt.get().toString().contains("email");
            }), eq(Optional.empty()));
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
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(user));

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
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(user));

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
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(user));

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
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(user));

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
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(user));

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
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(user));

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
            when(repository.getEntities(any(), any(), any())).thenReturn(List.of(user));

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(user, result.get());
        }
    }
}
