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

import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.api.core.security.authentication.PrincipalSupplier;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.SupplyException;

@DisplayName("PrincipalSupplier Tests")
class PrincipalSupplierTest {

    private static final String DOMAIN_NAME = "users";

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .build());
    }

    private PrincipalSupplier supplier;
    @SuppressWarnings("rawtypes")
    private IRuntimeContext runtimeContext;
    private IDomainContext domainContext;
    private IEntityDefinition entityDefinition;
    private IOperationRequest operationRequest;
    private ICaller caller;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        supplier = new PrincipalSupplier();
        runtimeContext = mock(IRuntimeContext.class);
        domainContext = mock(IDomainContext.class);
        entityDefinition = mock(IEntityDefinition.class);
        operationRequest = mock(IOperationRequest.class);
        caller = mock(ICaller.class);

        when(domainContext.getDomainName()).thenReturn(DOMAIN_NAME);
        when(domainContext.getEntityDefinition()).thenReturn(entityDefinition);
        when(entityDefinition.id()).thenReturn(new ObjectAddress("id"));
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
        void throwsOnMissingDomainContext() {
            when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.of(operationRequest));
            when(runtimeContext.getVariable(eq("domainContext"), any(IClass.class))).thenReturn(Optional.empty());
            assertThrows(SupplyException.class, () -> supplier.supply(runtimeContext));
        }
    }

    @Nested
    @DisplayName("supply() with valid input")
    class SupplyWithValidInput {

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUpVariables() {
            when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.of(operationRequest));
            when(runtimeContext.getVariable(eq("domainContext"), any(IClass.class))).thenReturn(Optional.of(domainContext));
        }

        @Test
        @DisplayName("returns empty when id field address is null")
        void returnsEmptyWhenNoIdField() throws SupplyException {
            when(entityDefinition.id()).thenReturn(null);
            when(operationRequest.caller()).thenReturn(caller);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when caller is null")
        void returnsEmptyWhenCallerIsNull() throws SupplyException {
            when(operationRequest.caller()).thenReturn(null);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when callerId is null")
        void returnsEmptyWhenCallerIdIsNull() throws SupplyException {
            when(operationRequest.caller()).thenReturn(caller);
            when(caller.callerId()).thenReturn(null);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when response is null")
        void returnsEmptyWhenResponseNull() throws SupplyException {
            when(operationRequest.caller()).thenReturn(caller);
            when(caller.callerId()).thenReturn("user-123");
            when(caller.tenantId()).thenReturn("TENANT_1");
            when(domainContext.readAll(any(), isNull(), isNull(), any())).thenReturn(null);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when response code is not OK")
        void returnsEmptyWhenResponseNotOk() throws SupplyException {
            when(operationRequest.caller()).thenReturn(caller);
            when(caller.callerId()).thenReturn("user-123");
            when(caller.tenantId()).thenReturn("TENANT_1");

            IOperationResponse response = mock(IOperationResponse.class);
            when(response.getResponseCode()).thenReturn(OperationResponseCode.NOT_FOUND);
            when(domainContext.readAll(any(), isNull(), isNull(), any())).thenReturn(response);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when response contains empty list")
        void returnsEmptyWhenEmptyList() throws SupplyException {
            when(operationRequest.caller()).thenReturn(caller);
            when(caller.callerId()).thenReturn("user-123");
            when(caller.tenantId()).thenReturn("TENANT_1");

            IOperationResponse response = mock(IOperationResponse.class);
            when(response.getResponseCode()).thenReturn(OperationResponseCode.OK);
            when(response.getResponse()).thenReturn(List.of());
            when(domainContext.readAll(any(), isNull(), isNull(), any())).thenReturn(response);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns first entity when principal found with tenant caller")
        void returnsPrincipalWithTenantCaller() throws SupplyException {
            Object expectedEntity = new Object();

            when(operationRequest.caller()).thenReturn(caller);
            when(caller.callerId()).thenReturn("user-123");
            when(caller.tenantId()).thenReturn("TENANT_1");

            IOperationResponse response = mock(IOperationResponse.class);
            when(response.getResponseCode()).thenReturn(OperationResponseCode.OK);
            when(response.getResponse()).thenReturn(List.of(expectedEntity));
            when(domainContext.readAll(any(), isNull(), isNull(), any())).thenReturn(response);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(expectedEntity, result.get());
        }

        @Test
        @DisplayName("returns first entity when principal found with super caller (null tenantId)")
        void returnsPrincipalWithSuperCaller() throws SupplyException {
            Object expectedEntity = new Object();

            when(operationRequest.caller()).thenReturn(caller);
            when(caller.callerId()).thenReturn("user-123");
            when(caller.tenantId()).thenReturn(null);

            IOperationResponse response = mock(IOperationResponse.class);
            when(response.getResponseCode()).thenReturn(OperationResponseCode.OK);
            when(response.getResponse()).thenReturn(List.of(expectedEntity));
            when(domainContext.readAll(any(), isNull(), isNull(), any())).thenReturn(response);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(expectedEntity, result.get());
        }

        @Test
        @DisplayName("returns first entity when multiple results returned")
        void returnsFirstOfMultipleResults() throws SupplyException {
            Object first = new Object();
            Object second = new Object();

            when(operationRequest.caller()).thenReturn(caller);
            when(caller.callerId()).thenReturn("user-123");
            when(caller.tenantId()).thenReturn("TENANT_1");

            IOperationResponse response = mock(IOperationResponse.class);
            when(response.getResponseCode()).thenReturn(OperationResponseCode.OK);
            when(response.getResponse()).thenReturn(List.of(first, second));
            when(domainContext.readAll(any(), isNull(), isNull(), any())).thenReturn(response);

            Optional<Object> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(first, result.get());
        }
    }
}
