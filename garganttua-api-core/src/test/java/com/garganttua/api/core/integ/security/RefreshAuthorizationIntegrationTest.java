package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.KeyRealmBuilder;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("Refresh Authorization Integration Tests")
class RefreshAuthorizationIntegrationTest extends AbstractCrudScriptTest {

    /**
     * Token entity that supports both signing and refresh. The refresh
     * sub-config carries its own expiration + revoked fields, distinct from
     * the access-token expiration.
     */
    public static class RefreshableTokenEntity {
        private String id;
        private String uuid;
        private String tenantId;
        private String ownerId;
        private String tokenType;
        private List<String> authorities;
        private Instant createdAt;
        private Instant expiresAt;
        private Boolean revoked;
        private byte[] signature;
        // Refresh-specific fields
        private Instant refreshExpiresAt;
        private Boolean refreshRevoked = false;
        private Boolean superTenant = false;

        public RefreshableTokenEntity() {}

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
        public byte[] getSignature() { return signature; }
        public void setSignature(byte[] signature) { this.signature = signature; }
        public Instant getRefreshExpiresAt() { return refreshExpiresAt; }
        public void setRefreshExpiresAt(Instant v) { this.refreshExpiresAt = v; }
        public Boolean getRefreshRevoked() { return refreshRevoked; }
        public void setRefreshRevoked(Boolean v) { this.refreshRevoked = v; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }

        public byte[] getDataToSign() {
            return (String.valueOf(uuid)
                    + "|" + String.valueOf(ownerId)
                    + "|" + String.valueOf(tenantId)
                    + "|" + String.valueOf(tokenType)).getBytes(StandardCharsets.UTF_8);
        }
    }

    public static class RefreshableTokenDto {
        private String id;
        private String uuid;
        private String tenantId;
        private Boolean superTenant;
        public RefreshableTokenDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    static class FixedKeyRealmSupplierBuilder implements ISupplierBuilder<IKeyRealm, ISupplier<IKeyRealm>> {
        private final IKeyRealm realm;

        FixedKeyRealmSupplierBuilder(IKeyRealm realm) { this.realm = realm; }

        @Override public IClass<IKeyRealm> getSuppliedClass() { return IClass.getClass(IKeyRealm.class); }
        @Override public java.lang.reflect.Type getSuppliedType() { return IKeyRealm.class; }
        @Override public boolean isContextual() { return false; }
        @Override public ISupplier<IKeyRealm> build() {
            return new ISupplier<IKeyRealm>() {
                @Override public Optional<IKeyRealm> supply() { return Optional.of(realm); }
                @Override public java.lang.reflect.Type getSuppliedType() { return IKeyRealm.class; }
                @Override public IClass<IKeyRealm> getSuppliedClass() { return IClass.getClass(IKeyRealm.class); }
            };
        }
    }

    private IApi context;
    private IDomain<?> userCtx;
    private CapturingDao userDao;
    private CapturingDao tokenDao;
    private IKeyRealm keyRealm;

    @BeforeEach
    void setUp() throws Exception {
        userDao = new CapturingDao();
        tokenDao = new CapturingDao();

        keyRealm = KeyRealmBuilder.builder()
                .name("refresh-test-realm")
                .algorithm(KeyAlgorithm.EC_256)
                .signatureAlgorithm(SignatureAlgorithm.SHA256)
                .build();

        StubAuthentication stubAuth = new StubAuthentication();
        IApiBuilder builder = newBuilder();

        var authBuilder = builder.security()
                .authentication(new FixedSupplierBuilder<>(stubAuth, IClass.getClass(StubAuthentication.class)));
        authBuilder.authenticate("authenticate")
                .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
        authBuilder.up();

        // Token domain — signable + refreshable.
        var tokenDomainBuilder = builder.domain(IClass.getClass(RefreshableTokenEntity.class))
                .tenant(true)
                .superTenant("superTenant")
                .owned("ownerId")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(RefreshableTokenDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(tokenDao)
                .up()
                .security()
                    .authorization()
                        .type("tokenType")
                        .authorities("authorities")
                        .expirable("expiresAt")
                        .revokable("revoked")
                        .signable()
                            .signature("signature")
                            .getDataToSign("getDataToSign")
                        .up()
                        .refreshable()
                            .expirable("refreshExpiresAt")
                            .revokable("refreshRevoked")
                        .up()
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

        @SuppressWarnings("rawtypes")
        var userDomainBuilder = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .owner("uuid")
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
                    .alwaysEnabled(true);
        authenticatorBuilder.authentication(authBuilder)
                    .authorization((com.garganttua.api.commons.context.dsl.IDomainBuilder) tokenDomainBuilder)
                        .lifeTime(60, TimeUnit.MINUTES)
                        .refreshLifeTime(120, TimeUnit.MINUTES)
                        .key(new FixedKeyRealmSupplierBuilder(keyRealm));

        userDomainBuilder.up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();

        UserDto existing = new UserDto();
        existing.setId("john@example.com");
        existing.setUuid("user-uuid-1");
        existing.setTenantId("SUPER_TENANT");
        existing.setName("John");
        userDao.save(existing);
    }

    /** Drive a fresh login to obtain a signed authorization the test can replay. */
    private RefreshableTokenEntity loginAndGetToken() throws ApiException {
        AuthenticationRequest req = new AuthenticationRequest(
                "john@example.com", "valid-password".getBytes(StandardCharsets.UTF_8));
        OperationRequest request = superTenantScriptRequest(
                OperationDefinition.authenticate("users", IClass.getClass(User.class)));
        request.arg("entity", req);
        WorkflowResult result = executeScript(userCtx, request);
        assertEquals(0, result.code(), "login should succeed");
        Object output = result.output();
        assertInstanceOf(RefreshableTokenEntity.class, output);
        RefreshableTokenEntity token = (RefreshableTokenEntity) output;
        // CREATE_AUTHORIZATION populates refresh fields itself when refreshable
        // (refreshExpiresAt from .refreshLifeTime config, refreshRevoked=false).
        assertNotNull(token.getRefreshExpiresAt(),
                "refreshable token must have refreshExpiresAt populated by createAuthorizationEntity");
        return token;
    }

    private OperationRequest refreshRequest(RefreshableTokenEntity token) {
        OperationRequest request = superTenantScriptRequest(
                OperationDefinition.refreshAuthorization("users", IClass.getClass(User.class)));
        request.arg("entity", token);
        return request;
    }

    @Nested
    @DisplayName("Happy path")
    class HappyPath {

        @Test
        @DisplayName("refresh produces a brand new signed token for the same principal")
        void refreshIssuesNewToken() throws Exception {
            RefreshableTokenEntity original = loginAndGetToken();
            byte[] originalSig = original.getSignature();

            WorkflowResult result = executeScript(userCtx, refreshRequest(original));
            assertEquals(0, result.code(), "refresh should succeed with code 0");
            assertInstanceOf(RefreshableTokenEntity.class, result.output());

            RefreshableTokenEntity refreshed = (RefreshableTokenEntity) result.output();
            assertNotNull(refreshed.getUuid(), "refreshed token must have its own uuid");
            assertNotEquals(original.getUuid(), refreshed.getUuid(),
                    "refreshed token must be a distinct entity from the source");
            assertEquals(original.getOwnerId(), refreshed.getOwnerId(),
                    "ownerId must be preserved across refresh");
            assertEquals(original.getTenantId(), refreshed.getTenantId(),
                    "tenantId must be preserved across refresh");
            assertNotNull(refreshed.getSignature(), "refreshed token must be signed");
            assertNotEquals(0, refreshed.getSignature().length);
            // Different uuid -> different getDataToSign -> different signature
            assertTrue(!java.util.Arrays.equals(originalSig, refreshed.getSignature()),
                    "refreshed token signature must differ from the original's");
        }

        @Test
        @DisplayName("the refreshed token verifies against the same key realm")
        void refreshedTokenVerifies() throws Exception {
            RefreshableTokenEntity original = loginAndGetToken();
            WorkflowResult result = executeScript(userCtx, refreshRequest(original));
            assertEquals(0, result.code());

            RefreshableTokenEntity refreshed = (RefreshableTokenEntity) result.output();
            assertTrue(keyRealm.getKeyForSignatureVerification()
                    .verifySignature(refreshed.getSignature(), refreshed.getDataToSign()),
                    "refreshed token must verify under the same realm");
        }
    }

    @Nested
    @DisplayName("Refusal paths")
    class Refusals {

        @Test
        @DisplayName("a tampered signature on the incoming token is rejected with 401")
        void tamperedSignatureRefused() throws Exception {
            RefreshableTokenEntity original = loginAndGetToken();
            byte[] tampered = original.getSignature().clone();
            tampered[tampered.length - 1] ^= 0x01;
            original.setSignature(tampered);

            WorkflowResult result = executeScript(userCtx, refreshRequest(original));
            assertEquals(401, result.code(),
                    "tampered token must surface as 401 (not 500)");
        }

        @Test
        @DisplayName("an expired refresh window returns 401")
        void expiredRefreshRefused() throws Exception {
            RefreshableTokenEntity original = loginAndGetToken();
            original.setRefreshExpiresAt(Instant.now().minusSeconds(60));

            WorkflowResult result = executeScript(userCtx, refreshRequest(original));
            assertEquals(401, result.code());
        }

        @Test
        @DisplayName("a revoked refresh returns 401")
        void revokedRefreshRefused() throws Exception {
            RefreshableTokenEntity original = loginAndGetToken();
            original.setRefreshRevoked(true);

            WorkflowResult result = executeScript(userCtx, refreshRequest(original));
            assertEquals(401, result.code());
        }

        @Test
        @DisplayName("an unknown principal (ownerId pointing nowhere) returns 401")
        void unknownPrincipalRefused() throws Exception {
            RefreshableTokenEntity original = loginAndGetToken();
            original.setOwnerId("ghost-uuid-does-not-exist");
            // Re-sign so the signature still matches the new payload —
            // otherwise we'd be testing the signature path, not the lookup.
            byte[] data = original.getDataToSign();
            byte[] freshSig = keyRealm.getKeyForSigning().sign(data);
            original.setSignature(freshSig);

            WorkflowResult result = executeScript(userCtx, refreshRequest(original));
            assertEquals(401, result.code());
        }

        @Test
        @DisplayName("missing entity body returns 401")
        void missingEntityRefused() throws Exception {
            OperationRequest request = superTenantScriptRequest(
                    OperationDefinition.refreshAuthorization("users", IClass.getClass(User.class)));
            // No "entity" arg
            WorkflowResult result = executeScript(userCtx, request);
            assertEquals(401, result.code());
        }
    }
}
