package com.garganttua.api.core.unit.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.context.security.ApiSecurityContext;
import com.garganttua.api.core.context.security.AuthenticationContext;
import com.garganttua.api.core.definition.AuthenticationDefinition;
import com.garganttua.api.core.security.authentication.AuthenticationRequestBuilder;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequestBuilder;

@DisplayName("AuthenticationRequestBuilder Tests")
class AuthenticationRequestBuilderTest {

    private ApiSecurityContext apiSecurityContext;

    @BeforeEach
    void setUp() {
        apiSecurityContext = new ApiSecurityContext(false);
    }

    @Nested
    @DisplayName("Fluent builder API")
    class FluentApi {

        @Test
        @DisplayName("id() returns same builder for chaining")
        void idReturnsSameBuilder() {
            IAuthenticationRequestBuilder builder = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList());
            assertSame(builder, builder.id("user@test.com"));
        }

        @Test
        @DisplayName("credentials() returns same builder for chaining")
        void credentialsReturnsSameBuilder() {
            IAuthenticationRequestBuilder builder = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList());
            assertSame(builder, builder.credentials("pass".getBytes(StandardCharsets.UTF_8)));
        }

        @Test
        @DisplayName("tenantId() returns same builder for chaining")
        void tenantIdReturnsSameBuilder() {
            IAuthenticationRequestBuilder builder = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList());
            assertSame(builder, builder.tenantId("TENANT_1"));
        }

        @Test
        @DisplayName("full chaining works")
        void fullChaining() {
            IAuthenticationRequestBuilder builder = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList());
            IAuthenticationRequestBuilder result = builder
                    .id("john@example.com")
                    .credentials("secret".getBytes(StandardCharsets.UTF_8))
                    .tenantId("TENANT_1");
            assertSame(builder, result);
        }
    }

    @Nested
    @DisplayName("build()")
    class Build {

        @Test
        @DisplayName("creates request with correct id")
        void requestHasCorrectId() {
            IAuthenticationRequest request = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList())
                    .id("john@example.com")
                    .credentials("pass".getBytes(StandardCharsets.UTF_8))
                    .build();

            assertEquals("john@example.com", request.getId());
        }

        @Test
        @DisplayName("creates request with correct credentials")
        void requestHasCorrectCredentials() {
            byte[] creds = "password123".getBytes(StandardCharsets.UTF_8);
            IAuthenticationRequest request = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList())
                    .id("user")
                    .credentials(creds)
                    .build();

            assertArrayEquals(creds, request.getCredentials());
        }

        @Test
        @DisplayName("creates request with correct tenantId")
        void requestHasCorrectTenantId() {
            IAuthenticationRequest request = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList())
                    .id("user")
                    .credentials("pass".getBytes(StandardCharsets.UTF_8))
                    .tenantId("TENANT_42")
                    .build();

            assertEquals("TENANT_42", request.getTenantId());
        }

        @Test
        @DisplayName("tenantId is null when not set")
        void tenantIdNullByDefault() {
            IAuthenticationRequest request = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList())
                    .id("user")
                    .credentials("pass".getBytes(StandardCharsets.UTF_8))
                    .build();

            assertNull(request.getTenantId());
        }

        @Test
        @DisplayName("request setTenantId mutates tenantId")
        void setTenantIdMutates() {
            IAuthenticationRequest request = new AuthenticationRequestBuilder(
                    apiSecurityContext, Collections.emptyList())
                    .id("user")
                    .credentials("pass".getBytes(StandardCharsets.UTF_8))
                    .build();

            request.setTenantId("NEW_TENANT");
            assertEquals("NEW_TENANT", request.getTenantId());
        }
    }

    @Nested
    @DisplayName("ApiSecurityContext.request()")
    class ApiSecurityContextRequest {

        @Test
        @DisplayName("returns builder for registered domain")
        void returnsBuilderForRegisteredDomain() {
            AuthenticationContext authCtx = new AuthenticationContext(new AuthenticationDefinition(null, null, null, null, null, null));
            apiSecurityContext.registerAuthenticationContexts("users", List.of(authCtx));

            IAuthenticationRequestBuilder builder = apiSecurityContext.request("users");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("returns builder for unknown domain with empty contexts")
        void returnsBuilderForUnknownDomain() {
            IAuthenticationRequestBuilder builder = apiSecurityContext.request("unknown");
            assertNotNull(builder);
        }

        @Test
        @DisplayName("built request can be constructed from domain lookup")
        void builtRequestFromDomainLookup() {
            AuthenticationContext authCtx = new AuthenticationContext(new AuthenticationDefinition(null, null, null, null, null, null));
            apiSecurityContext.registerAuthenticationContexts("users", List.of(authCtx));

            IAuthenticationRequest request = apiSecurityContext.request("users")
                    .id("admin@test.com")
                    .credentials("token".getBytes(StandardCharsets.UTF_8))
                    .tenantId("T1")
                    .build();

            assertEquals("admin@test.com", request.getId());
            assertEquals("T1", request.getTenantId());
        }
    }

    @Nested
    @DisplayName("AuthenticationContext.request()")
    class AuthenticationContextRequest {

        @Test
        @DisplayName("returns builder scoped to single context")
        void returnsScopedBuilder() {
            AuthenticationContext authCtx = new AuthenticationContext(new AuthenticationDefinition(null, null, null, null, null, null));
            authCtx.setApiSecurityContext(apiSecurityContext);

            IAuthenticationRequestBuilder builder = authCtx.request();
            assertNotNull(builder);

            IAuthenticationRequest request = builder
                    .id("user@test.com")
                    .credentials("cred".getBytes(StandardCharsets.UTF_8))
                    .build();

            assertEquals("user@test.com", request.getId());
        }
    }
}
