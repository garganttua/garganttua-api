package com.garganttua.api.core.unit.context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.authentication.AuthenticationContext;
import com.garganttua.api.core.security.authentication.AuthenticationDefinition;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IAuthenticationDefinition;

@DisplayName("AuthenticationContext Tests")
class AuthenticationContextTest {

    private AuthenticationDefinition definition;
    private AuthenticationContext context;

    @BeforeEach
    void setUp() {
        definition = new AuthenticationDefinition(null, null, null, null, null);
        context = new AuthenticationContext(definition);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("rejects null definition")
        void rejectsNullDefinition() {
            assertThrows(NullPointerException.class, () -> new AuthenticationContext(null));
        }

        @Test
        @DisplayName("null definition has correct error message")
        void nullDefinitionMessage() {
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> new AuthenticationContext(null));
            assertEquals("Authentication definition is mandatory to create an authentication context",
                    ex.getMessage());
        }
    }

    @Nested
    @DisplayName("getAuthenticationDefinition()")
    class GetDefinition {

        @Test
        @DisplayName("returns the definition passed to constructor")
        void returnsDefinition() {
            IAuthenticationDefinition result = context.getAuthenticationDefinition();
            assertSame(definition, result);
        }

        @Test
        @DisplayName("definition supplier is null when constructed with null")
        void definitionSupplierIsNull() {
            AuthenticationDefinition def = (AuthenticationDefinition) context.getAuthenticationDefinition();
            assertNull(def.supplier());
        }
    }

    @Nested
    @DisplayName("domainContext")
    class DomainTests {

        @Test
        @DisplayName("domainContext is null by default")
        void domainContextNullByDefault() {
            assertNull(context.getDomain());
        }

        @Test
        @DisplayName("setDomain stores reference")
        void setDomainStoresReference() {
            IDomain<?> domainCtx = mock(IDomain.class);
            context.setDomain(domainCtx);
            assertSame(domainCtx, context.getDomain());
        }
    }
}
