package com.garganttua.api.core.unit.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.core.security.authentication.DomainContextSupplier;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.SupplyException;

@DisplayName("DomainContextSupplier Tests")
class DomainContextSupplierTest {

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .build());
    }

    private DomainContextSupplier supplier;
    @SuppressWarnings("rawtypes")
    private IRuntimeContext runtimeContext;
    private IDomainContext<?> domainContext;

    @BeforeEach
    void setUp() {
        supplier = new DomainContextSupplier();
        runtimeContext = mock(IRuntimeContext.class);
        domainContext = mock(IDomainContext.class);
    }

    @Nested
    @DisplayName("Type metadata")
    class TypeMetadata {

        @Test
        @DisplayName("getSuppliedType returns IDomainContext type")
        void suppliedTypeIsDomainContext() {
            assertEquals(IDomainContext.class, supplier.getSuppliedType());
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
        @DisplayName("throws when domainContext variable is missing")
        void throwsOnMissingDomainContext() {
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
            when(runtimeContext.getVariable(eq("domainContext"), any(IClass.class))).thenReturn(Optional.of(domainContext));
            when(domainContext.getDomainName()).thenReturn("users");
        }

        @Test
        @DisplayName("returns domainContext from runtime context")
        void returnsDomainContext() throws SupplyException {
            @SuppressWarnings("rawtypes")
            Optional<IDomainContext> result = supplier.supply(runtimeContext);

            assertTrue(result.isPresent());
            assertSame(domainContext, result.get());
        }
    }
}
