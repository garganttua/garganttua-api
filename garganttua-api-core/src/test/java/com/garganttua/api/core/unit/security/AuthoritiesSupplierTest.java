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
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.security.authentication.AuthoritiesSupplier;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.SupplyException;

@DisplayName("AuthoritiesSupplier Tests")
class AuthoritiesSupplierTest {

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    @SuppressWarnings("rawtypes")
    private AuthoritiesSupplier supplier;
    @SuppressWarnings("rawtypes")
    private IRuntimeContext runtimeContext;
    private IOperationRequest operationRequest;
    private ICaller caller;

    @BeforeEach
    void setUp() {
        supplier = new AuthoritiesSupplier();
        runtimeContext = mock(IRuntimeContext.class);
        operationRequest = mock(IOperationRequest.class);
        caller = mock(ICaller.class);
    }

    @Nested
    @DisplayName("Type metadata")
    class TypeMetadata {

        @Test
        @DisplayName("getSuppliedType returns List type")
        void suppliedTypeIsList() {
            assertEquals(List.class, supplier.getSuppliedType());
        }

        @Test
        @DisplayName("getOwnerContextType wraps IRuntimeContext.class")
        void ownerContextTypeWrapsRuntimeContext() {
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
    }

    @Nested
    @DisplayName("supply() with valid input")
    class SupplyWithValidInput {

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUpVariables() {
            when(runtimeContext.getVariable(eq("request"), any(IClass.class))).thenReturn(Optional.of(operationRequest));
        }

        @Test
        @DisplayName("returns authorities from caller")
        @SuppressWarnings("rawtypes")
        void returnsAuthorities() throws SupplyException {
            List<String> authorities = List.of("ROLE_ADMIN", "ROLE_USER");
            when(operationRequest.caller()).thenReturn(caller);
            when(caller.authorities()).thenReturn(authorities);

            Optional<List> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertEquals(authorities, result.get());
        }

        @Test
        @DisplayName("returns empty when caller is null")
        @SuppressWarnings("rawtypes")
        void returnsEmptyWhenCallerNull() throws SupplyException {
            when(operationRequest.caller()).thenReturn(null);

            Optional<List> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns empty when authorities is null")
        @SuppressWarnings("rawtypes")
        void returnsEmptyWhenAuthoritiesNull() throws SupplyException {
            when(operationRequest.caller()).thenReturn(caller);
            when(caller.authorities()).thenReturn(null);

            Optional<List> result = supplier.supply(runtimeContext);

            assertTrue(result.isEmpty());
        }
    }
}
