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
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
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
        private Boolean superTenant = false;

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
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    public static class TokenDto {
        private String id;
        private String uuid;
        private String tenantId;
        // Mirror the entity-side fields the storable-authz lookup filters
        // on (ownerId, expiresAt, revoked). Without these the repository's
        // filter mapper would drop the predicates that scope the lookup
        // and the reuse-path tests would never match.
        private String ownerId;
        private Instant expiresAt;
        private Boolean revoked;
        private Boolean superTenant;

        public TokenDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public Instant getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
        public Boolean getRevoked() { return revoked; }
        public void setRevoked(Boolean revoked) { this.revoked = revoked; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
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
                .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
        authBuilder.up();

        // Register Token domain (authorization entity) — must be owned
        var tokenDomainBuilder = builder.domain(IClass.getClass(TokenEntity.class))
                .tenant(true)
                .superTenant("superTenant")
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
                        .creation("createdAt")
                        .revokable("revoked")
                    .up()
                .up();

        // A token verifies itself: its domain is also an authenticator (login =
        // token uuid). Register the token authentication strategy and wire it.
        StubTokenAuthentication stubTokenAuth = new StubTokenAuthentication();
        var tokenAuthBuilder = builder.security()
                .authentication(new FixedSupplierBuilder<>(stubTokenAuth, IClass.getClass(StubTokenAuthentication.class)));
        tokenAuthBuilder.authenticate("authenticate")
                .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
        tokenAuthBuilder.up();

        tokenDomainBuilder.security()
                .authenticator()
                    .login("uuid")
                    .scope(AuthenticatorScope.tenant)
                    .alwaysEnabled(true)
                    .authentication(tokenAuthBuilder);

        // Register User domain (authenticator entity) with authorization link
        @SuppressWarnings("rawtypes")
        var userDomainBuilder = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .owner("uuid")  // User is owner of authorizations
                .superOwner("superOwner")
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
                    .authorization((com.garganttua.api.commons.context.dsl.IDomainBuilder) tokenDomainBuilder)
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
    @DisplayName("Authorization entity creation")
    class AuthorizationEntityCreation {

        @Test
        @DisplayName("successful authentication returns a TokenEntity, not an IAuthentication")
        void successfulAuthReturnsTokenEntity() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code(), "workflow should succeed with code 0");
            assertNotNull(result.output(), "workflow should produce an output");
            assertInstanceOf(TokenEntity.class, result.output(),
                    "output should be a TokenEntity (authorization), not an IAuthentication");
        }

        @Test
        @DisplayName("authorization entity has a generated uuid")
        void authorizationEntityHasUuid() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            TokenEntity token = (TokenEntity) result.output();
            assertNotNull(token.getUuid(), "authorization entity must have a generated uuid");
            assertFalse(token.getUuid().isBlank(), "uuid must not be blank");
        }

        @Test
        @DisplayName("authorization entity has correct ownerId (principal uuid)")
        void authorizationEntityHasCorrectOwnerId() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            TokenEntity token = (TokenEntity) result.output();
            assertEquals("users:user-uuid-1", token.getOwnerId(),
                    "ownerId should be the qualified principal id (${domainName}:${uuid})");
        }

        @Test
        @DisplayName("authorization entity has createdAt stamped (~now)")
        void authorizationEntityHasCreatedAt() throws ApiException {
            Instant before = Instant.now().minusSeconds(5);
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            TokenEntity token = (TokenEntity) result.output();
            assertNotNull(token.getCreatedAt(),
                    "createdAt must be stamped by createAuthorizationEntity now that .creation(...) wires the field");
            assertFalse(token.getCreatedAt().isBefore(before), "createdAt should be ~now, not in the past");
            assertFalse(token.getCreatedAt().isAfter(Instant.now().plusSeconds(5)), "createdAt should be ~now, not in the future");
        }

        @Test
        @DisplayName("authorization entity has correct tenantId")
        void authorizationEntityHasCorrectTenantId() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            TokenEntity token = (TokenEntity) result.output();
            assertEquals("SUPER_TENANT", token.getTenantId(),
                    "tenantId should be propagated from the authentication request");
        }

        @Test
        @DisplayName("authorization entity has authorities from authentication result")
        void authorizationEntityHasAuthorities() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            TokenEntity token = (TokenEntity) result.output();
            assertNotNull(token.getAuthorities(), "authorities should be set");
            assertTrue(token.getAuthorities().contains("ROLE_USER"),
                    "authorities should contain ROLE_USER from StubAuthentication");
        }

        @Test
        @DisplayName("authorization entity has token type from authentication result")
        void authorizationEntityHasTokenType() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            TokenEntity token = (TokenEntity) result.output();
            assertEquals("auth-token", token.getTokenType(),
                    "tokenType should come from the authentication result authorization field");
        }

        @Test
        @DisplayName("authorization entity has expiration set based on lifeTime config")
        void authorizationEntityHasExpiration() throws ApiException {
            Instant before = Instant.now();
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);
            Instant after = Instant.now();

            assertEquals(0, result.code());
            TokenEntity token = (TokenEntity) result.output();
            assertNotNull(token.getExpiresAt(), "expiration should be set");
            // lifeTime is 60 minutes
            Instant expectedMin = before.plusSeconds(60 * 60 - 1);
            Instant expectedMax = after.plusSeconds(60 * 60 + 1);
            assertTrue(token.getExpiresAt().isAfter(expectedMin),
                    "expiration should be ~60 minutes in the future");
            assertTrue(token.getExpiresAt().isBefore(expectedMax),
                    "expiration should be ~60 minutes in the future");
        }

        @Test
        @DisplayName("authorization entity has revoked set to false")
        void authorizationEntityHasRevokedFalse() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            TokenEntity token = (TokenEntity) result.output();
            assertEquals(false, token.getRevoked(), "newly created authorization should not be revoked");
        }
    }

    @Nested
    @DisplayName("Authentication failures do not create authorization")
    class AuthenticationFailures {

        @Test
        @DisplayName("wrong password returns 401, no authorization created")
        void wrongPasswordReturns401() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "wrong-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(401, result.code(), "wrong password should return 401");
        }

        @Test
        @DisplayName("unknown login returns 401, no authorization created")
        void unknownLoginReturns401() throws ApiException {
            OperationRequest request = authenticateRequest("unknown@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(401, result.code(), "unknown login should return 401");
        }

        @Test
        @DisplayName("missing tenantId on tenant-scoped authenticator returns 400")
        void missingTenantIdReturns400() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", null);
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(400, result.code(), "tenant scope without tenantId should return 400");
        }
    }

    @Nested
    @DisplayName("Authorization domain structural validation")
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
                        .superTenant("superTenant")
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
                        .superTenant("superTenant")
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
                            .authorization((com.garganttua.api.commons.context.dsl.IDomainBuilder) tb);
                ub.up();

                buildAndStart(bldr);
            }, "Should throw because authorization domain is not owned");
        }

        @Test
        @DisplayName("authenticator with authorization must be owner — build throws if not")
        void authenticatorWithAuthorizationMustBeOwner() {
            ApiException ex = assertThrows(ApiException.class, () -> {
                CapturingDao dao1 = new CapturingDao();
                CapturingDao dao2 = new CapturingDao();

                IApiBuilder bldr = newBuilder();

                var ab = bldr.security()
                        .authentication(new FixedSupplierBuilder<>(new StubAuthentication(), IClass.getClass(StubAuthentication.class)));
                ab.authenticate("authenticate");
                ab.up();

                // Token auth strategy so the token can be a self-verifying authenticator.
                var tokenAb = bldr.security()
                        .authentication(new FixedSupplierBuilder<>(new StubTokenAuthentication(), IClass.getClass(StubTokenAuthentication.class)));
                tokenAb.authenticate("authenticate")
                        .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                        .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                        .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
                tokenAb.up();

                // Token domain WITH owned + authenticator (both correct) so the
                // ONLY remaining error is the User missing its owner role.
                var tb = bldr.domain(IClass.getClass(TokenEntity.class))
                        .tenant(true)
                        .superTenant("superTenant")
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
                tb.security()
                        .authenticator()
                            .login("uuid")
                            .scope(AuthenticatorScope.tenant)
                            .alwaysEnabled(true)
                            .authentication(tokenAb);

                // User domain WITHOUT owner — should fail
                var ub = bldr.domain(IClass.getClass(User.class))
                        .tenant(true)
                        .superTenant("superTenant")
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
                            .authorization((com.garganttua.api.commons.context.dsl.IDomainBuilder) tb);
                ub.up();

                buildAndStart(bldr);
            }, "Should throw because authenticator domain is not owner");
            assertTrue(ex.getMessage().contains("is not an owner"),
                    "rejection must name the missing owner role (not the token authenticator rule); got: "
                            + ex.getMessage());
        }

        @Test
        @DisplayName("authorization domain must ALSO be an authenticator — build throws if not")
        void authorizationDomainMustBeAuthenticator() {
            ApiException ex = assertThrows(ApiException.class, () -> {
                CapturingDao dao1 = new CapturingDao();
                CapturingDao dao2 = new CapturingDao();

                IApiBuilder bldr = newBuilder();

                var ab = bldr.security()
                        .authentication(new FixedSupplierBuilder<>(new StubAuthentication(), IClass.getClass(StubAuthentication.class)));
                ab.authenticate("authenticate")
                        .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                        .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                        .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
                ab.up();

                // Token domain: owned + authorization but NOT an authenticator → must fail.
                bldr.domain(IClass.getClass(TokenEntity.class))
                        .tenant(true)
                        .superTenant("superTenant")
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

                // A fully-valid User authenticator (owner) so only the token can fail.
                var ub = bldr.domain(IClass.getClass(User.class))
                        .tenant(true)
                        .superTenant("superTenant")
                        .owner("uuid")
                        .superOwner("superOwner")
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
                            .authentication(ab);
                ub.up();

                buildAndStart(bldr);
            }, "Should throw because the authorization domain is not an authenticator");
            assertTrue(ex.getMessage().contains("not an authenticator"),
                    "rejection must name the missing authenticator role; got: " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Token reuse — when the authorization is storable, an existing non-expired non-revoked token is reused")
    class TokenReuse {

        @Test
        @DisplayName("two authentications in a row return the SAME token uuid (reuse path short-circuits create + persist)")
        void twoAuthsReuseSameToken() throws ApiException {
            // First auth: creates & persists a fresh token.
            OperationRequest first = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult firstResult = executeScript(userCtx, first);
            assertEquals(0, firstResult.code());
            TokenEntity firstToken = (TokenEntity) firstResult.output();
            String firstUuid = firstToken.getUuid();
            assertNotNull(firstUuid);
            assertEquals(1, tokenDao.getStorage().size(),
                    "first authentication must persist exactly one authorization in the token domain");

            // Second auth, same caller: storable + non-expired non-revoked authz exists
            // → reuse path returns the existing token entity unchanged, no second persist.
            OperationRequest second = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult secondResult = executeScript(userCtx, second);
            assertEquals(0, secondResult.code());
            TokenEntity secondToken = (TokenEntity) secondResult.output();
            assertEquals(firstUuid, secondToken.getUuid(),
                    "reuse path must return the same token entity (same uuid), not mint a new one");
            assertEquals(firstToken.getOwnerId(), secondToken.getOwnerId(),
                    "the reused token must carry the same ownerId — the repository round-tripped it through the DTO");
            assertEquals(firstToken.getTenantId(), secondToken.getTenantId(),
                    "the reused token must carry the same tenantId");
            assertEquals(1, tokenDao.getStorage().size(),
                    "reuse must not persist a second entity — the storable lookup short-circuited create + sign + persist");
        }

        @Test
        @DisplayName("an expired authorization in the DB is ignored → a fresh token is minted and persisted")
        void expiredTokenForcesFreshCreate() throws ApiException {
            // Seed: a token-DTO that EXPIRED a minute ago, owned by the future authenticator.
            // We seed at the DAO level — the storage is the DTO row that the
            // repository would have produced via the normal persist path.
            TokenDto expired = new TokenDto();
            expired.setId("expired-token-id");
            expired.setUuid("expired-token-uuid");
            expired.setOwnerId("users:user-uuid-1");
            expired.setTenantId("SUPER_TENANT");
            expired.setRevoked(false);
            expired.setExpiresAt(Instant.now().minusSeconds(60));
            tokenDao.save(expired);
            assertEquals(1, tokenDao.getStorage().size(), "seeded the expired token");

            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);
            assertEquals(0, result.code());
            TokenEntity fresh = (TokenEntity) result.output();
            assertNotEquals("expired-token-uuid", fresh.getUuid(),
                    "must NOT reuse an expired authorization");
            assertNotNull(fresh.getExpiresAt());
            assertTrue(fresh.getExpiresAt().isAfter(Instant.now()),
                    "the freshly-minted token must have a non-expired expiration");
            assertEquals(2, tokenDao.getStorage().size(),
                    "expired entity is left in place; a NEW entity is persisted alongside it");
        }

        @Test
        @DisplayName("a revoked authorization in the DB is ignored → a fresh token is minted and persisted")
        void revokedTokenForcesFreshCreate() throws ApiException {
            // Seed: a still-unexpired token-DTO but REVOKED.
            TokenDto revoked = new TokenDto();
            revoked.setId("revoked-token-id");
            revoked.setUuid("revoked-token-uuid");
            revoked.setOwnerId("users:user-uuid-1");
            revoked.setTenantId("SUPER_TENANT");
            revoked.setRevoked(true);
            revoked.setExpiresAt(Instant.now().plusSeconds(3600));
            tokenDao.save(revoked);
            assertEquals(1, tokenDao.getStorage().size(), "seeded the revoked token");

            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);
            assertEquals(0, result.code());
            TokenEntity fresh = (TokenEntity) result.output();
            assertNotEquals("revoked-token-uuid", fresh.getUuid(),
                    "must NOT reuse a revoked authorization, even if not yet expired");
            assertEquals(Boolean.FALSE, fresh.getRevoked(),
                    "the freshly-minted token starts un-revoked");
            assertEquals(2, tokenDao.getStorage().size(),
                    "revoked entity is left in place; a NEW entity is persisted alongside it");
        }

        @Test
        @DisplayName("an authorization for a DIFFERENT owner is ignored → a fresh token is minted for the caller")
        void differentOwnerTokenForcesFreshCreate() throws ApiException {
            // Seed: a valid token-DTO for a DIFFERENT user (not user-uuid-1).
            TokenDto other = new TokenDto();
            other.setId("other-token-id");
            other.setUuid("other-owner-token");
            other.setOwnerId("users:other-user-uuid");
            other.setTenantId("SUPER_TENANT");
            other.setRevoked(false);
            other.setExpiresAt(Instant.now().plusSeconds(3600));
            tokenDao.save(other);

            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);
            assertEquals(0, result.code());
            TokenEntity fresh = (TokenEntity) result.output();
            assertNotEquals("other-owner-token", fresh.getUuid(),
                    "must NOT reuse a token owned by a different principal");
            assertEquals("users:user-uuid-1", fresh.getOwnerId(),
                    "the fresh token is owned by the authenticated principal (qualified id)");
            assertEquals(2, tokenDao.getStorage().size(),
                    "other-owner entity is left in place; a NEW entity is persisted for the caller");
        }
    }
}
