package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.workflow.WorkflowResult;

/**
 * End-to-end test for {@code .key(IDomainBuilder)} (persisted-mode key).
 *
 * <p>Three authentications fire against the configured authenticator; for
 * each {@code AuthenticatorKeyUsage} the test asserts that the right
 * number of {@code CryptoKey} entities materialize in the key DAO and
 * that subsequent authentications reuse the same key (lookup hit).
 */
@DisplayName("@Key entity domain — lookup-or-create per AuthenticatorKeyUsage")
class KeyAutoCreationIntegrationTest extends AbstractCrudScriptTest {

    public static class CryptoKey {
        private String id;
        private String uuid;
        private String tenantId;
        private String ownerId;
        private String realmName;
        private String algorithm;
        private String signatureAlgorithm;
        // IKey-typed key-material fields — the entity acts as an IKeyRealm
        // shape, not a raw byte container. Stockage côté DTO assure le
        // mapping IKey ↔ bytes pour les couches qui en ont besoin.
        private com.garganttua.core.crypto.IKey publicMaterial;
        private com.garganttua.core.crypto.IKey privateMaterial;
        private Instant expiration;
        private boolean revoked;

        public CryptoKey() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getRealmName() { return realmName; }
        public void setRealmName(String realmName) { this.realmName = realmName; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public String getSignatureAlgorithm() { return signatureAlgorithm; }
        public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
        public com.garganttua.core.crypto.IKey getPublicMaterial() { return publicMaterial; }
        public void setPublicMaterial(com.garganttua.core.crypto.IKey publicMaterial) { this.publicMaterial = publicMaterial; }
        public com.garganttua.core.crypto.IKey getPrivateMaterial() { return privateMaterial; }
        public void setPrivateMaterial(com.garganttua.core.crypto.IKey privateMaterial) { this.privateMaterial = privateMaterial; }
        public Instant getExpiration() { return expiration; }
        public void setExpiration(Instant expiration) { this.expiration = expiration; }
        public boolean isRevoked() { return revoked; }
        public void setRevoked(boolean revoked) { this.revoked = revoked; }
    }

    /**
     * The DTO must mirror every entity field that the framework reads or
     * writes through the repository — otherwise the entity→DTO→entity round
     * trip silently drops key material. In particular, realmName,
     * publicMaterial and privateMaterial must round-trip intact.
     */
    public static class CryptoKeyDto {
        private String id;
        private String uuid;
        private String tenantId;
        private String ownerId;
        private String realmName;
        private String algorithm;
        private String signatureAlgorithm;
        // IKey-typed: the in-memory test mapper copies the IKey reference
        // through the entity ↔ DTO conversion. Production DBs would store
        // the JDK-encoded bytes via a custom DTO/(de)serializer.
        private com.garganttua.core.crypto.IKey publicMaterial;
        private com.garganttua.core.crypto.IKey privateMaterial;
        private Instant expiration;
        private boolean revoked;

        public CryptoKeyDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getRealmName() { return realmName; }
        public void setRealmName(String realmName) { this.realmName = realmName; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public String getSignatureAlgorithm() { return signatureAlgorithm; }
        public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
        public com.garganttua.core.crypto.IKey getPublicMaterial() { return publicMaterial; }
        public void setPublicMaterial(com.garganttua.core.crypto.IKey publicMaterial) { this.publicMaterial = publicMaterial; }
        public com.garganttua.core.crypto.IKey getPrivateMaterial() { return privateMaterial; }
        public void setPrivateMaterial(com.garganttua.core.crypto.IKey privateMaterial) { this.privateMaterial = privateMaterial; }
        public Instant getExpiration() { return expiration; }
        public void setExpiration(Instant expiration) { this.expiration = expiration; }
        public boolean isRevoked() { return revoked; }
        public void setRevoked(boolean revoked) { this.revoked = revoked; }
    }

    /** Signable token entity (reused from the sign integration test pattern). */
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
        private byte[] signature;

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

        public byte[] getDataToSign() {
            String payload = String.valueOf(uuid)
                    + "|" + String.valueOf(ownerId)
                    + "|" + String.valueOf(tenantId)
                    + "|" + String.valueOf(tokenType);
            return payload.getBytes(StandardCharsets.UTF_8);
        }
    }

    public static class TokenDto {
        private String id;
        private String uuid;
        private String tenantId;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }

    /** Wires an API for a given AuthenticatorKeyUsage and returns its handles. */
    static class Wired {
        IApi api;
        IDomain<?> userCtx;
        CapturingDao userDao;
        CapturingDao tokenDao;
        CapturingDao keyDao;
    }

    private Wired buildApi(AuthenticatorKeyUsage usage) throws ApiException {
        return buildApi(usage, true, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Wired buildApi(AuthenticatorKeyUsage usage, boolean autoGenerate, boolean autoRotate) throws ApiException {
        Wired w = new Wired();
        w.userDao = new CapturingDao();
        w.tokenDao = new CapturingDao();
        w.keyDao = new CapturingDao();

        IApiBuilder builder = newBuilder();

        StubAuthentication stubAuth = new StubAuthentication();
        var authBuilder = builder.security()
                .authentication(new FixedSupplierBuilder<>(stubAuth, IClass.getClass(StubAuthentication.class)));
        authBuilder.authenticate("authenticate")
                .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
        authBuilder.up();

        // ─── Token domain — signable authorization ───
        var tokenBuilder = builder.domain(IClass.getClass(TokenEntity.class))
                .tenant(true)
                .owned("ownerId")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(TokenDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(w.tokenDao)
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
                    .up()
                .up();

        // ─── @Key entity domain — declared via the .key() sub-builder ───
        // Marked owned("ownerId") because oneForEach keys are scoped per
        // caller — the framework stamps caller.ownerId() onto this field.
        var keyBuilder = builder.domain(IClass.getClass(CryptoKey.class))
                .tenant(true)
                .owned("ownerId")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(CryptoKeyDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(w.keyDao)
                .up();
        keyBuilder.key()
                .name("realmName")
                .keyAlgorithm("algorithm")
                .signatureAlgorithm("signatureAlgorithm")
                .keyForSignatureVerification("publicMaterial")
                .keyForSigning("privateMaterial")
                .expiration("expiration")
                .revoked("revoked")
                .up();
        keyBuilder.up();

        // ─── Authenticator (User) wires authorization → token + key → CryptoKey ───
        var userBuilder = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .owner("uuid")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(w.userDao)
                .up();
        var authzDsl = userBuilder.security()
                .authenticator()
                    .login("id")
                    .scope(AuthenticatorScope.tenant)
                    .alwaysEnabled(true)
                    .authentication(authBuilder)
                .authorization((IDomainBuilder) tokenBuilder)
                    .lifeTime(60, java.util.concurrent.TimeUnit.MINUTES);
        authzDsl.key((IDomainBuilder) keyBuilder)
                .usage(usage)
                .algorithm(KeyAlgorithm.EC_256)
                .signatureAlgorithm(SignatureAlgorithm.SHA256)
                .lifeTime(1, java.util.concurrent.TimeUnit.HOURS)
                .autoGenerate(autoGenerate)
                .autoRotate(autoRotate)
                .up();
        userBuilder.up();

        w.api = buildAndStart(builder);
        w.userCtx = w.api.getDomain("users").orElseThrow();
        return w;
    }

    private static UserDto seedUser(CapturingDao userDao, String id, String uuid, String tenantId) throws ApiException {
        UserDto u = new UserDto();
        u.setId(id);
        u.setUuid(uuid);
        u.setTenantId(tenantId);
        userDao.save(u);
        return u;
    }

    private OperationRequest authenticateRequest(String login, String tenantId) {
        AuthenticationRequest authReq = new AuthenticationRequest(
                login, "valid-password".getBytes(StandardCharsets.UTF_8), tenantId);
        OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(authOp);
        request.arg("entity", authReq);
        return request;
    }

    /**
     * Bare OperationRequest carrying just the caller fields (tenantId, ownerId)
     * — used to test the persisted-key resolver directly, bypassing the
     * authenticate workflow. The realmName scoping for oneForEach depends on
     * the caller's ownerId, which the authenticate flow itself does not
     * provide (the caller is anonymous at authentication time). Tests that
     * want to assert per-caller scoping must therefore drive resolveKeyRealm
     * with a request that has an owner set.
     */
    private static OperationRequest callerRequest(String tenantId, String ownerId) {
        OperationRequest request = new OperationRequest(new java.util.HashMap<>());
        if (tenantId != null) {
            request.arg(com.garganttua.api.commons.service.IOperationRequest.TENANT_ID, tenantId);
            request.arg(com.garganttua.api.commons.service.IOperationRequest.REQUESTED_TENANT_ID, tenantId);
        }
        if (ownerId != null) {
            request.arg(com.garganttua.api.commons.service.IOperationRequest.OWNER_ID, ownerId);
        }
        return request;
    }

    @Nested
    @DisplayName("oneForAll — a single global key shared across all callers")
    class OneForAll {

        @Test
        @DisplayName("first authentication generates one CryptoKey entity, second one reuses it")
        void lookupOrCreate() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll);
            seedUser(w.userDao, "alice@example.com", "uuid-alice", "SUPER_TENANT");

            assertEquals(0, w.keyDao.getStorage().size(), "key DAO must start empty");

            WorkflowResult first = executeScript(w.userCtx,
                    authenticateRequest("alice@example.com", "SUPER_TENANT"));
            assertEquals(0, first.code(), "first authentication must succeed");

            assertEquals(1, w.keyDao.getStorage().size(),
                    "the first sign must materialize exactly one CryptoKey entity");
            CryptoKeyDto created = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            assertEquals("cryptokeys:global", created.getRealmName(),
                    "oneForAll realmName must be scoped to ':global', regardless of caller tenant");
            assertEquals("EC-256", created.getAlgorithm());
            assertEquals("SHA256", created.getSignatureAlgorithm());
            assertNotNull(created.getPublicMaterial());
            assertNotNull(created.getPrivateMaterial());
            assertFalse(created.isRevoked());

            // Second authentication — should hit the lookup branch, no new key
            WorkflowResult second = executeScript(w.userCtx,
                    authenticateRequest("alice@example.com", "SUPER_TENANT"));
            assertEquals(0, second.code());
            assertEquals(1, w.keyDao.getStorage().size(),
                    "second authentication must reuse the existing key — no new row");
        }

        @Test
        @DisplayName("two different tenants share the same global key")
        void sharedAcrossTenants() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll);
            seedUser(w.userDao, "alice@example.com", "uuid-alice", "SUPER_TENANT");
            seedUser(w.userDao, "bob@example.com", "uuid-bob", "OTHER_TENANT");

            executeScript(w.userCtx, authenticateRequest("alice@example.com", "SUPER_TENANT"));
            executeScript(w.userCtx, authenticateRequest("bob@example.com", "OTHER_TENANT"));

            assertEquals(1, w.keyDao.getStorage().size(),
                    "with oneForAll, both tenants share the single global key");
        }

        @Test
        @DisplayName("sign produced via the persisted key actually verifies cryptographically")
        void cryptographicallySound() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll);
            seedUser(w.userDao, "alice@example.com", "uuid-alice", "SUPER_TENANT");

            WorkflowResult result = executeScript(w.userCtx,
                    authenticateRequest("alice@example.com", "SUPER_TENANT"));
            assertEquals(0, result.code());
            assertInstanceOf(TokenEntity.class, result.output());
            TokenEntity token = (TokenEntity) result.output();
            assertNotNull(token.getSignature(), "signature must be populated");
            assertTrue(token.getSignature().length > 0);

            // Independently verify via JDK Signature, reusing the materialized public key bytes
            // pulled out of the persisted IKey via its underlying JDK key encoding.
            CryptoKeyDto storedKey = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            byte[] publicBytes = storedKey.getPublicMaterial().getKey().getEncoded();
            java.security.PublicKey publicKey = java.security.KeyFactory.getInstance("EC")
                    .generatePublic(new java.security.spec.X509EncodedKeySpec(publicBytes));
            java.security.Signature verifier = java.security.Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(publicKey);
            verifier.update(token.getDataToSign());
            assertTrue(verifier.verify(token.getSignature()),
                    "the persisted public material must verify the signature stamped onto the token");
        }
    }

    @Nested
    @DisplayName("oneForTenant — one key per tenant")
    class OneForTenant {

        @Test
        @DisplayName("two tenants each get their own key (2 entities)")
        void splitsByTenant() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForTenant);
            seedUser(w.userDao, "alice@example.com", "uuid-alice", "SUPER_TENANT");
            seedUser(w.userDao, "bob@example.com", "uuid-bob", "OTHER_TENANT");

            executeScript(w.userCtx, authenticateRequest("alice@example.com", "SUPER_TENANT"));
            executeScript(w.userCtx, authenticateRequest("bob@example.com", "OTHER_TENANT"));

            assertEquals(2, w.keyDao.getStorage().size(),
                    "oneForTenant must create one key per distinct tenantId");

            CryptoKeyDto k0 = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            CryptoKeyDto k1 = (CryptoKeyDto) w.keyDao.getStorage().get(1);

            // realmName is scoped to ':tenant:<id>' so we can assert by inspection
            assertTrue(k0.getRealmName().startsWith("cryptokeys:tenant:")
                    && k1.getRealmName().startsWith("cryptokeys:tenant:"),
                    "both realm names must follow the per-tenant convention");
            assertNotEquals(k0.getRealmName(), k1.getRealmName(),
                    "the two tenant keys must have distinct realmNames");
            assertNotEquals(k0.getPrivateMaterial(), k1.getPrivateMaterial(),
                    "per-tenant keys must have distinct private material (IKey.equals compares raw key bytes)");
        }

        @Test
        @DisplayName("two authentications from the same tenant reuse the same key")
        void reusesPerTenant() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForTenant);
            seedUser(w.userDao, "alice@example.com", "uuid-alice", "SUPER_TENANT");
            seedUser(w.userDao, "alice2@example.com", "uuid-alice2", "SUPER_TENANT");

            executeScript(w.userCtx, authenticateRequest("alice@example.com", "SUPER_TENANT"));
            executeScript(w.userCtx, authenticateRequest("alice2@example.com", "SUPER_TENANT"));

            assertEquals(1, w.keyDao.getStorage().size(),
                    "two callers from the same tenant must share the per-tenant key");
        }

        @Test
        @DisplayName("the persisted key's tenantId field is stamped with the original caller's tenant")
        void tenantIdStamped() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForTenant);
            seedUser(w.userDao, "alice@example.com", "uuid-alice", "SUPER_TENANT");

            executeScript(w.userCtx, authenticateRequest("alice@example.com", "SUPER_TENANT"));
            CryptoKeyDto key = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            assertEquals("SUPER_TENANT", key.getTenantId(),
                    "oneForTenant must stamp the key entity's tenantId with the caller's tenant");
        }
    }

    @Nested
    @DisplayName("Cross-usage behaviour")
    class CrossUsage {

        @Test
        @DisplayName("oneForAll stamps no tenantId — global keys are not tenant-scoped")
        void oneForAllNoTenantStamp() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll);
            seedUser(w.userDao, "alice@example.com", "uuid-alice", "SUPER_TENANT");

            executeScript(w.userCtx, authenticateRequest("alice@example.com", "SUPER_TENANT"));
            CryptoKeyDto key = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            assertNull(key.getTenantId(),
                    "oneForAll keys must leave tenantId null — they are global by design");
        }

        @Test
        @DisplayName("the materialized realm uses the persisted bytes verbatim — round-trip identity")
        void persistedBytesAreUsedVerbatim() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll);
            seedUser(w.userDao, "alice@example.com", "uuid-alice", "SUPER_TENANT");

            // First call generates and persists
            WorkflowResult first = executeScript(w.userCtx,
                    authenticateRequest("alice@example.com", "SUPER_TENANT"));
            assertEquals(0, first.code());
            CryptoKeyDto persisted = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            // Snapshot the underlying JDK-encoded bytes (the IKey itself is
            // mutable-shaped via lazy JDK key reconstruction; we want a
            // byte-level identity check).
            byte[] originalPrivate = persisted.getPrivateMaterial().getKey().getEncoded();
            byte[] originalPublic = persisted.getPublicMaterial().getKey().getEncoded();

            // Second call hits the lookup branch — the persisted bytes must remain identical
            WorkflowResult second = executeScript(w.userCtx,
                    authenticateRequest("alice@example.com", "SUPER_TENANT"));
            assertEquals(0, second.code());
            assertEquals(1, w.keyDao.getStorage().size());
            CryptoKeyDto afterLookup = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            assertArrayEquals(originalPrivate, afterLookup.getPrivateMaterial().getKey().getEncoded(),
                    "lookup must not rewrite persisted private material");
            assertArrayEquals(originalPublic, afterLookup.getPublicMaterial().getKey().getEncoded(),
                    "lookup must not rewrite persisted public material");
        }
    }

    @Nested
    @DisplayName("oneForEach — one key per caller (per tenant + ownerId)")
    class OneForEach {

        @Test
        @DisplayName("two callers in the same tenant with distinct ownerIds get distinct keys")
        void distinctCallersDistinctKeys() throws Exception {
            // oneForEach scoping uses caller.ownerId() to differentiate keys
            // within a tenant. The authenticate workflow runs with an
            // anonymous caller (by design — login is the entry point), so we
            // drive resolveKeyRealm directly with caller-bearing requests.
            Wired w = buildApi(AuthenticatorKeyUsage.oneForEach);

            com.garganttua.core.crypto.IKeyRealm realm1 =
                    com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", "owner-1"));
            com.garganttua.core.crypto.IKeyRealm realm2 =
                    com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", "owner-2"));

            assertEquals(2, w.keyDao.getStorage().size(),
                    "oneForEach must create a distinct key per ownerId, even within the same tenant");
            assertNotEquals(realm1.getName(), realm2.getName(),
                    "the two realms must have distinct names");
            assertTrue(realm1.getName().endsWith(":owner-1"),
                    "first realm name must end with the caller's ownerId — got: " + realm1.getName());
            assertTrue(realm2.getName().endsWith(":owner-2"),
                    "second realm name must end with the caller's ownerId — got: " + realm2.getName());
        }

        @Test
        @DisplayName("same caller twice reuses its dedicated key")
        void sameCallerSameKey() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForEach);

            com.garganttua.core.crypto.IKeyRealm first =
                    com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", "owner-1"));
            com.garganttua.core.crypto.IKeyRealm second =
                    com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", "owner-1"));

            assertEquals(1, w.keyDao.getStorage().size(),
                    "second call from the same caller must reuse the existing key");
            assertEquals(first.getName(), second.getName());
        }

        @Test
        @DisplayName("the persisted key carries the caller's tenantId AND ownerId")
        void stampingFromCaller() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForEach);
            com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                    w.userCtx, callerRequest("TENANT_X", "owner-99"));

            CryptoKeyDto stored = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            assertEquals("TENANT_X", stored.getTenantId(),
                    "oneForEach stamps the caller's tenant onto the key entity");
            assertEquals("owner-99", stored.getOwnerId(),
                    "oneForEach stamps the caller's owner onto the key entity");
        }
    }

    @Nested
    @DisplayName("Expiration — autoRotate=true rotates expired/revoked keys silently")
    class Expiration {

        @Test
        @DisplayName("autoRotate=true: an expired key is skipped and a fresh one is generated")
        void expiredKeySkipped() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll, true, true);

            // First call: generates and persists a key
            com.garganttua.core.crypto.IKeyRealm initial =
                    com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", null));
            assertEquals(1, w.keyDao.getStorage().size());
            CryptoKeyDto stored = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            byte[] originalPrivate = stored.getPrivateMaterial().getKey().getEncoded();

            // Force the persisted key to be in the past — emulates the key
            // outliving its configured lifeTime.
            stored.setExpiration(Instant.now().minusSeconds(60));

            // Second call: with autoRotate=true the resolver skips the expired
            // entry and generates a fresh one. The old entity stays in storage
            // (its public material remains useful for verifying tokens signed
            // before rotation).
            com.garganttua.core.crypto.IKeyRealm refreshed =
                    com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", null));

            assertEquals(2, w.keyDao.getStorage().size(),
                    "an expired key in storage must not be reused — the resolver must materialize a fresh key");
            CryptoKeyDto fresh = (CryptoKeyDto) w.keyDao.getStorage().get(1);
            assertFalse(java.util.Arrays.equals(originalPrivate, fresh.getPrivateMaterial().getKey().getEncoded()),
                    "the freshly generated key must have distinct private material from the expired one");
            assertTrue(fresh.getExpiration().isAfter(Instant.now()),
                    "the freshly generated key must have a future expiration");
            assertNotNull(refreshed);
        }

        @Test
        @DisplayName("autoRotate=true: a revoked key is skipped and a fresh one is generated")
        void revokedKeySkipped() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll, true, true);

            com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                    w.userCtx, callerRequest("TENANT_A", null));
            assertEquals(1, w.keyDao.getStorage().size());
            CryptoKeyDto stored = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            stored.setRevoked(true);

            com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                    w.userCtx, callerRequest("TENANT_A", null));

            assertEquals(2, w.keyDao.getStorage().size(),
                    "a revoked key in storage must not be reused");
            CryptoKeyDto fresh = (CryptoKeyDto) w.keyDao.getStorage().get(1);
            assertFalse(fresh.isRevoked(),
                    "the freshly generated key must not carry the revoked flag");
        }
    }

    @Nested
    @DisplayName("Lifecycle flags — autoGenerate / autoRotate are opt-out / opt-in respectively")
    class LifecycleFlags {

        @Test
        @DisplayName("autoGenerate=false + missing key in storage: resolver throws with a parlant message")
        void autoGenerateFalseMissingKeyThrows() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll, false, false);

            assertEquals(0, w.keyDao.getStorage().size(), "key DAO must start empty");

            ApiException ex = assertThrows(ApiException.class,
                    () -> com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", null)));
            assertTrue(ex.getMessage().contains("autoGenerate(false)"),
                    "error must mention the autoGenerate flag explicitly — got: " + ex.getMessage());
            assertTrue(ex.getMessage().contains("realmName 'cryptokeys:global'")
                            || ex.getMessage().contains("cryptokeys:global"),
                    "error must mention the realmName that was sought — got: " + ex.getMessage());
            assertEquals(0, w.keyDao.getStorage().size(),
                    "no key must be created when autoGenerate=false");
        }

        @Test
        @DisplayName("autoGenerate=false + seeded key: resolver finds it (no generation needed)")
        void autoGenerateFalseSeededWorks() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll, false, false);

            // Seed a key out of band — emulates an admin import / HSM operator.
            java.security.KeyPair pair = java.security.KeyPairGenerator.getInstance("EC")
                    .generateKeyPair();
            CryptoKeyDto seed = new CryptoKeyDto();
            seed.setUuid("seed-uuid");
            seed.setRealmName("cryptokeys:global");
            seed.setAlgorithm("EC-256");
            seed.setSignatureAlgorithm("SHA256");
            seed.setPublicMaterial(com.garganttua.core.crypto.Key.fromSigningMaterial(
                    com.garganttua.core.crypto.KeyType.PUBLIC,
                    com.garganttua.core.crypto.KeyAlgorithm.EC_256,
                    com.garganttua.core.crypto.SignatureAlgorithm.SHA256,
                    pair.getPublic().getEncoded()));
            seed.setPrivateMaterial(com.garganttua.core.crypto.Key.fromSigningMaterial(
                    com.garganttua.core.crypto.KeyType.PRIVATE,
                    com.garganttua.core.crypto.KeyAlgorithm.EC_256,
                    com.garganttua.core.crypto.SignatureAlgorithm.SHA256,
                    pair.getPrivate().getEncoded()));
            seed.setExpiration(Instant.now().plusSeconds(3600));
            seed.setRevoked(false);
            w.keyDao.save(seed);

            com.garganttua.core.crypto.IKeyRealm realm =
                    com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", null));
            assertEquals("cryptokeys:global", realm.getName(),
                    "the seeded key must be returned verbatim");
            assertEquals(1, w.keyDao.getStorage().size(),
                    "no new key must be created — the seed is reused");
        }

        @Test
        @DisplayName("autoRotate=false + expired key in storage: resolver throws with a parlant message")
        void autoRotateFalseExpiredKeyThrows() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll, true, false);

            // First call seeds a usable key
            com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                    w.userCtx, callerRequest("TENANT_A", null));
            assertEquals(1, w.keyDao.getStorage().size());
            CryptoKeyDto stored = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            stored.setExpiration(Instant.now().minusSeconds(60));

            // Second call: expired key + autoRotate=false → throw
            ApiException ex = assertThrows(ApiException.class,
                    () -> com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", null)));
            assertTrue(ex.getMessage().contains("autoRotate(false)"),
                    "error must mention the autoRotate flag explicitly — got: " + ex.getMessage());
            assertTrue(ex.getMessage().contains("expired or revoked"),
                    "error must explain why the existing key was rejected — got: " + ex.getMessage());
            assertEquals(1, w.keyDao.getStorage().size(),
                    "no second key must be generated when autoRotate=false");
        }

        @Test
        @DisplayName("autoRotate=false + revoked key in storage: resolver throws (same path as expired)")
        void autoRotateFalseRevokedKeyThrows() throws Exception {
            Wired w = buildApi(AuthenticatorKeyUsage.oneForAll, true, false);

            com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                    w.userCtx, callerRequest("TENANT_A", null));
            CryptoKeyDto stored = (CryptoKeyDto) w.keyDao.getStorage().get(0);
            stored.setRevoked(true);

            ApiException ex = assertThrows(ApiException.class,
                    () -> com.garganttua.api.core.expression.SecurityExpressions.resolveKeyRealm(
                            w.userCtx, callerRequest("TENANT_A", null)));
            assertTrue(ex.getMessage().contains("autoRotate(false)"));
        }

        @Test
        @DisplayName("autoRotate=true + autoGenerate=false: ApiBuilder.build refuses the inconsistent combo")
        void rotateWithoutGenerateRefusedAtBuild() {
            // Build with autoRotate=true, autoGenerate=false → invalid.
            ApiException ex = assertThrows(ApiException.class,
                    () -> buildApi(AuthenticatorKeyUsage.oneForAll, false, true));
            String unwrapped = unwrap(ex);
            assertTrue(unwrapped.contains("autoRotate(true)"),
                    "build error must mention the autoRotate flag — got: " + unwrapped);
            assertTrue(unwrapped.contains("autoGenerate(false)"),
                    "build error must mention the autoGenerate flag — got: " + unwrapped);
        }

        private static String unwrap(Throwable t) {
            StringBuilder sb = new StringBuilder();
            for (Throwable cur = t; cur != null; cur = cur.getCause()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(cur.getMessage());
            }
            return sb.toString();
        }
    }
}
