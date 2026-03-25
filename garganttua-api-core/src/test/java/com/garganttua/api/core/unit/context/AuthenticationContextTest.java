package com.garganttua.api.core.unit.context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.context.security.AuthenticationContext;
import com.garganttua.api.core.definition.AuthenticationDefinition;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.definition.IAuthenticationDefinition;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequestBuilder;

@DisplayName("AuthenticationContext Tests")
class AuthenticationContextTest {

    private AuthenticationDefinition definition;
    private AuthenticationContext context;

    @BeforeEach
    void setUp() {
        definition = new AuthenticationDefinition(null, null, null, null, null, null, null);
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
    @DisplayName("request()")
    class Request {

        @Test
        @DisplayName("returns a non-null request builder")
        void returnsNonNullBuilder() {
            IAuthenticationRequestBuilder builder = context.request();
            assertNotNull(builder);
        }

        @Test
        @DisplayName("returns a new builder on each call")
        void returnsNewBuilderEachTime() {
            IAuthenticationRequestBuilder b1 = context.request();
            IAuthenticationRequestBuilder b2 = context.request();
            assertNotSame(b1, b2);
        }

        @Test
        @DisplayName("builder produces request with correct values")
        void builderProducesCorrectRequest() {
            byte[] creds = "secret".getBytes(StandardCharsets.UTF_8);

            IAuthenticationRequest request = context.request()
                    .id("user@test.com")
                    .credentials(creds)
                    .tenantId("TENANT_1")
                    .build();

            assertEquals("user@test.com", request.getId());
            assertArrayEquals(creds, request.getCredentials());
            assertEquals("TENANT_1", request.getTenantId());
        }

        @Test
        @DisplayName("tenantId defaults to null when not set")
        void tenantIdDefaultsNull() {
            IAuthenticationRequest request = context.request()
                    .id("user")
                    .credentials("pass".getBytes(StandardCharsets.UTF_8))
                    .build();

            assertEquals("user", request.getId());
            assertNull(request.getTenantId());
        }
    }

    @Nested
    @DisplayName("domainContext")
    class DomainContextTests {

        @Test
        @DisplayName("domainContext is null by default")
        void domainContextNullByDefault() {
            assertNull(context.getDomainContext());
        }

        @Test
        @DisplayName("setDomainContext stores reference")
        void setDomainContextStoresReference() {
            IDomainContext<?> domainCtx = mock(IDomainContext.class);
            context.setDomainContext(domainCtx);
            assertSame(domainCtx, context.getDomainContext());
        }
    }
}
