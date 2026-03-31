package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IApi;
import com.garganttua.api.spec.context.IDomain;
import com.garganttua.api.spec.context.dsl.IApiBuilder;
import com.garganttua.api.spec.definition.IAuthenticatorDefinition;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.security.authentication.Authentication;
import com.garganttua.api.spec.security.authentication.IAuthentication;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("Create Authorization Integration Tests")
class CreateAuthorizationIntegrationTest extends AbstractCrudScriptTest {

    // --- Token entity (authorization domain) ---

    public static class TokenEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private String ownerId;
        private String tokenType;
        private List<String> authorities;
        private Instant createdAt;
        private Instant expiresAt;
        private Boolean revoked;

        public TokenEntity() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        public List<String> getAuthorities() { return authorities; }
        public void setAuthorities(List<String> authorities) { this.authorities = authorities; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
        public Instant getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
        public Boolean getRevoked() { return revoked; }
        public void setRevoked(Boolean revoked) { this.revoked = revoked; }
    }

    public static class TokenDto {
        private String id;
        private String uuid;
        private String tenantId;

        public TokenDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }


    private IApi context;
    private IDomain<?> userCtx;
    private CapturingDao userDao;
    private CapturingDao tokenDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();
        tokenDao = new CapturingDao();

        StubAuthentication stubAuth = new StubAuthentication();

        IApiBuilder builder = newBuilder();

        // Register authentication method with parameter suppliers
        var authBuilder = builder.security()
                .authentication(new FixedSupplierBuilder<>(stubAuth, IClass.getClass(StubAuthentication.class)));
        authBuilder.authenticate("authenticate")
                .withParam(0, new com.garganttua.api.core.security.authentication.AuthenticatePrincipalSupplierBuilder())
                .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
        authBuilder.up();

        // Register Token domain (authorization entity) — must be owned
        var tokenDomainBuilder = builder.domain(IClass.getClass(TokenEntity.class))
                .tenant(true)
                .owned("ownerId")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(TokenDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(tokenDao)
                .up()
                .security()
                    .authorization()
                        .type("tokenType")
                        .authorities("authorities")
                        .expirable("expiresAt")
                        .revokable("revoked")
                    .up()
                .up();

        // Register User domain (authenticator entity) with authorization link
        @SuppressWarnings("rawtypes")
        var userDomainBuilder = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .owner("uuid")  // User is owner of authorizations
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up();

        var authenticatorBuilder = userDomainBuilder.security()
                .authenticator()
                    .login("id")
                    .scope(AuthenticatorScope.tenant)
                    .alwaysEnabled(true)
                    .authentication(authBuilder);
        authenticatorBuilder
                    .authorization((com.garganttua.api.spec.context.dsl.IDomainBuilder) tokenDomainBuilder)
                        .lifeTime(60, java.util.concurrent.TimeUnit.MINUTES);

        userDomainBuilder.up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();

        // Pre-populate a user DTO in the DAO (the pipeline works with DTOs, not entities)
        UserDto existingUser = new UserDto();
        existingUser.setId("john@example.com");
        existingUser.setUuid("user-uuid-1");
        existingUser.setTenantId("SUPER_TENANT");
        existingUser.setName("John");
        userDao.save(existingUser);
    }

    private OperationRequest authenticateRequest(String login, String password, String tenantId) {
        AuthenticationRequest authReq = new AuthenticationRequest(
                login, password.getBytes(StandardCharsets.UTF_8), tenantId);
        OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(authOp);
        request.arg("entity", authReq);
        return request;
    }

    @Nested
    @DisplayName("Authorization creation in workflow")
    class WorkflowIntegration {

        @Test
        @DisplayName("successful authentication returns code 0 with IAuthentication result")
        void successfulAuthenticationReturnsCode0() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code(), "workflow should succeed with code 0");
            assertNotNull(result.output(), "workflow should produce an output");
        }

        @Test
        @DisplayName("successful authentication result contains authorities")
        void authenticationResultContainsAuthorities() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code(), "workflow should succeed");
            assertInstanceOf(com.garganttua.api.spec.security.authentication.IAuthentication.class, result.output());
            com.garganttua.api.spec.security.authentication.IAuthentication auth =
                    (com.garganttua.api.spec.security.authentication.IAuthentication) result.output();
            assertTrue(auth.authenticated(), "should be authenticated");
            assertNotNull(auth.authorities(), "authorities should be set");
            assertTrue(auth.authorities().contains("ROLE_USER"), "authorities should contain ROLE_USER");
        }

        @Test
        @DisplayName("failed authentication returns 401")
        void failedAuthenticationReturns401() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "wrong-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(401, result.code(), "wrong password should return 401");
        }

        @Test
        @DisplayName("unknown login returns 401")
        void unknownLoginReturns401() throws ApiException {
            OperationRequest request = authenticateRequest("unknown@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(401, result.code(), "unknown login should return 401");
        }
    }

    @Nested
    @DisplayName("Authorization entity validation")
    class AuthorizationValidation {

        @Test
        @DisplayName("authorization domain must be owned — build throws if not")
        void authorizationDomainMustBeOwned() {
            assertThrows(ApiException.class, () -> {
                CapturingDao dao1 = new CapturingDao();
                CapturingDao dao2 = new CapturingDao();

                IApiBuilder bldr = newBuilder();

                var ab = bldr.security()
                        .authentication(new FixedSupplierBuilder<>(new StubAuthentication(), IClass.getClass(StubAuthentication.class)));
                ab.authenticate("authenticate");
                ab.up();

                // Token domain WITHOUT owned
                var tb = bldr.domain(IClass.getClass(TokenEntity.class))
                        .tenant(true)
                        .entity()
                            .id("id").uuid("uuid").tenantId("tenantId")
                        .up()
                        .dto(IClass.getClass(TokenDto.class))
                            .id("id").uuid("uuid").tenantId("tenantId")
                            .db(dao1)
                        .up()
                        .security()
                            .authorization()
                                .type("tokenType")
                            .up()
                        .up();

                var ub = bldr.domain(IClass.getClass(User.class))
                        .tenant(true)
                        .entity()
                            .id("id").uuid("uuid").tenantId("tenantId")
                        .up()
                        .dto(IClass.getClass(UserDto.class))
                            .id("id").uuid("uuid").tenantId("tenantId")
                            .db(dao2)
                        .up();
                ub.security()
                        .authenticator()
                            .login("id")
                            .scope(AuthenticatorScope.tenant)
                            .alwaysEnabled(true)
                            .authentication(ab)
                            .authorization((com.garganttua.api.spec.context.dsl.IDomainBuilder) tb);
                ub.up();

                buildAndStart(bldr);
            }, "Should throw because authorization domain is not owned");
        }

        @Test
        @DisplayName("authenticator with authorization must be owner — build throws if not")
        void authenticatorWithAuthorizationMustBeOwner() {
            assertThrows(ApiException.class, () -> {
                CapturingDao dao1 = new CapturingDao();
                CapturingDao dao2 = new CapturingDao();

                IApiBuilder bldr = newBuilder();

                var ab = bldr.security()
                        .authentication(new FixedSupplierBuilder<>(new StubAuthentication(), IClass.getClass(StubAuthentication.class)));
                ab.authenticate("authenticate");
                ab.up();

                // Token domain WITH owned (correct)
                var tb = bldr.domain(IClass.getClass(TokenEntity.class))
                        .tenant(true)
                        .owned("ownerId")
                        .entity()
                            .id("id").uuid("uuid").tenantId("tenantId")
                        .up()
                        .dto(IClass.getClass(TokenDto.class))
                            .id("id").uuid("uuid").tenantId("tenantId")
                            .db(dao1)
                        .up()
                        .security()
                            .authorization()
                                .type("tokenType")
                            .up()
                        .up();

                // User domain WITHOUT owner — should fail
                var ub = bldr.domain(IClass.getClass(User.class))
                        .tenant(true)
                        // NO .owner("uuid") — this should cause the error
                        .entity()
                            .id("id").uuid("uuid").tenantId("tenantId")
                        .up()
                        .dto(IClass.getClass(UserDto.class))
                            .id("id").uuid("uuid").tenantId("tenantId")
                            .db(dao2)
                        .up();
                ub.security()
                        .authenticator()
                            .login("id")
                            .scope(AuthenticatorScope.tenant)
                            .alwaysEnabled(true)
                            .authentication(ab)
                            .authorization((com.garganttua.api.spec.context.dsl.IDomainBuilder) tb);
                ub.up();

                buildAndStart(bldr);
            }, "Should throw because authenticator domain is not owner");
        }
    }
}
