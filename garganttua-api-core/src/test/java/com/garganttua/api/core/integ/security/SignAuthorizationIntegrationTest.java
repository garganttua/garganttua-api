package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.expression.SecurityExpressions;
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

@DisplayName("Sign Authorization Integration Tests")
class SignAuthorizationIntegrationTest extends AbstractCrudScriptTest {

    /**
     * Authorization entity with a signature field and a deterministic
     * getDataToSign() method. The framework will call getDataToSign(), sign the
     * result via the user-provided IKeyRealm, and write the bytes back into
     * {@code signature}.
     */
    public static class SignedTokenEntity {
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
        private String signedBy;
        private Boolean superTenant = false;

        public SignedTokenEntity() {}

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
        public String getSignedBy() { return signedBy; }
        public void setSignedBy(String signedBy) { this.signedBy = signedBy; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }

        /**
         * Stable byte payload representing the token's identity. Same fields
         * end up signed regardless of whether the entity is being created
         * (sign) or replayed (verify).
         */
        public byte[] getDataToSign() {
            String payload = String.valueOf(uuid)
                    + "|" + String.valueOf(ownerId)
                    + "|" + String.valueOf(tenantId)
                    + "|" + String.valueOf(tokenType);
            return payload.getBytes(StandardCharsets.UTF_8);
        }
    }

    public static class SignedTokenDto {
        private String id;
        private String uuid;
        private String tenantId;
        private Boolean superTenant;

        public SignedTokenDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    /**
     * Minimal {@link ISupplierBuilder} adapter for an already-built
     * {@link IKeyRealm}. The api ships no IKeyRealm impl, so we instantiate
     * one from {@code garganttua-crypto} (test scope only) and inject it.
     */
    static class FixedKeyRealmSupplierBuilder implements ISupplierBuilder<IKeyRealm, ISupplier<IKeyRealm>> {
        private final IKeyRealm realm;

        FixedKeyRealmSupplierBuilder(IKeyRealm realm) {
            this.realm = realm;
        }

        @Override
        public IClass<IKeyRealm> getSuppliedClass() {
            return IClass.getClass(IKeyRealm.class);
        }

        @Override
        public java.lang.reflect.Type getSuppliedType() {
            return IKeyRealm.class;
        }

        @Override
        public boolean isContextual() {
            return false;
        }

        @Override
        public ISupplier<IKeyRealm> build() {
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

        // Build a real IKeyRealm from garganttua-crypto. EC_256 is fast at key
        // generation and supports asymmetric signing — the IKey impl in
        // garganttua-crypto requires a private key for sign().
        keyRealm = KeyRealmBuilder.builder()
                .name("test-key-realm")
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

        // Token domain — declares a signable authorization with signature field
        // and getDataToSign method.
        var tokenDomainBuilder = builder.domain(IClass.getClass(SignedTokenEntity.class))
                .tenant(true)
                .superTenant("superTenant")
                .owned("ownerId")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(SignedTokenDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(tokenDao)
                .up()
                .security()
                    .authorization()
                        .type("tokenType")
                        .authorities("authorities")
                        .expirable("expiresAt")
                        .revokable("revoked")
                        .signedBy("signedBy")
                        .signable()
                            .signature("signature")
                            .getDataToSign("getDataToSign")
                        .up()
                    .up()
                .up();

        // Every authorization (token) domain must also be an authenticator: a
        // token verifies itself (login = token uuid).
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
                        .lifeTime(60, java.util.concurrent.TimeUnit.MINUTES)
                        .key(new FixedKeyRealmSupplierBuilder(keyRealm));

        userDomainBuilder.up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();

        UserDto existingUser = new UserDto();
        existingUser.setId("john@example.com");
        existingUser.setUuid("user-uuid-1");
        existingUser.setTenantId("SUPER_TENANT");
        existingUser.setName("John");
        userDao.save(existingUser);
    }

    private OperationRequest authenticateRequest(String login, String password, String tenantId) {
        AuthenticationRequest authReq = new AuthenticationRequest(
                login, password.getBytes(StandardCharsets.UTF_8));
        OperationDefinition authOp = OperationDefinition.authenticate("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(authOp);
        request.arg("entity", authReq);
        return request;
    }

    @Nested
    @DisplayName("Sign on creation (CREATE_AUTHORIZATION pipeline)")
    class SignOnCreation {

        @Test
        @DisplayName("successful authentication produces a signed token (signature field populated)")
        void successfulAuthSignsToken() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code(), "workflow should succeed with code 0");
            assertInstanceOf(SignedTokenEntity.class, result.output());

            SignedTokenEntity token = (SignedTokenEntity) result.output();
            assertNotNull(token.getSignature(), "signature must be populated by signIfSignable");
            assertTrue(token.getSignature().length > 0, "signature must be non-empty");
        }

        @Test
        @DisplayName("signing stamps signedBy with the key realm id (supplier mode → realm name)")
        void signingStampsSignedBy() throws ApiException {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            SignedTokenEntity token = (SignedTokenEntity) result.output();
            assertEquals("test-key-realm", token.getSignedBy(),
                    "signedBy must carry the signing realm's id — the supplier realm's name");
        }

        @Test
        @DisplayName("the produced signature actually verifies against the same key realm")
        void producedSignatureRoundTrips() throws Exception {
            OperationRequest request = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            WorkflowResult result = executeScript(userCtx, request);
            assertEquals(0, result.code());

            SignedTokenEntity token = (SignedTokenEntity) result.output();

            // Verify directly through the same realm: the bytes signed by the
            // pipeline must match what verifySignature returns true on.
            byte[] data = token.getDataToSign();
            assertTrue(keyRealm.getKeyForSignatureVerification()
                    .verifySignature(token.getSignature(), data),
                    "freshly signed token must verify successfully");
        }

        @Test
        @DisplayName("two consecutive authentications produce different signatures (different uuids)")
        void differentTokensDifferentSignatures() throws ApiException {
            OperationRequest first = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");
            OperationRequest second = authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT");

            WorkflowResult firstResult = executeScript(userCtx, first);
            WorkflowResult secondResult = executeScript(userCtx, second);

            assertEquals(0, firstResult.code());
            assertEquals(0, secondResult.code());

            SignedTokenEntity t1 = (SignedTokenEntity) firstResult.output();
            SignedTokenEntity t2 = (SignedTokenEntity) secondResult.output();

            assertFalse(java.util.Arrays.equals(t1.getSignature(), t2.getSignature()),
                    "two tokens with different uuids must have different signatures");
        }
    }

    @Nested
    @DisplayName("Verify on incoming authorization")
    class VerifyOnIncoming {

        @Test
        @DisplayName("a freshly signed token verifies via verifyIfSignable on the token domain")
        void verifyAcceptsFreshlySignedToken() throws ApiException {
            // First sign one
            WorkflowResult result = executeScript(userCtx,
                    authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT"));
            assertEquals(0, result.code());
            SignedTokenEntity token = (SignedTokenEntity) result.output();

            // Now verify on the token domain (which holds the signable contract)
            // — same code path that VERIFY_AUTHORIZATION.gs takes for a decoded token.
            assertTrue(SecurityExpressions.verifyIfSignable(token, userCtx, null),
                    "freshly signed token must pass verifyIfSignable on the authenticator domain");
        }

        @Test
        @DisplayName("tampering the signature bytes makes verifyIfSignable return false")
        void verifyRejectsTamperedSignature() throws ApiException {
            WorkflowResult result = executeScript(userCtx,
                    authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT"));
            assertEquals(0, result.code());
            SignedTokenEntity token = (SignedTokenEntity) result.output();

            // Flip a byte in the signature
            byte[] tampered = token.getSignature().clone();
            tampered[0] ^= 0x55;
            token.setSignature(tampered);

            assertFalse(SecurityExpressions.verifyIfSignable(token, userCtx, null),
                    "a token with mutated signature must not verify");
        }

        @Test
        @DisplayName("tampering the payload (changing ownerId after sign) makes verifyIfSignable return false")
        void verifyRejectsTamperedPayload() throws ApiException {
            WorkflowResult result = executeScript(userCtx,
                    authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT"));
            assertEquals(0, result.code());
            SignedTokenEntity token = (SignedTokenEntity) result.output();
            byte[] originalSignature = token.getSignature().clone();

            // Mutate a field that's part of getDataToSign — signature now refers
            // to a different payload.
            token.setOwnerId("attacker-uuid");

            assertFalse(SecurityExpressions.verifyIfSignable(token, userCtx, null),
                    "mutated payload must not verify against the original signature");
            assertArrayEquals(originalSignature, token.getSignature(),
                    "signature itself was untouched — it's the payload that changed");
        }
    }

    @Nested
    @DisplayName("Misconfiguration: signable but no key wired")
    class MissingKeyRealm {

        @Test
        @DisplayName("ApiBuilder.build() refuses to build when the authenticator declares a signable authorization but no .key(supplier) nor .key(domain) is wired")
        void noKeyRefusedAtBuildTime() throws Exception {
            CapturingDao localUserDao = new CapturingDao();
            CapturingDao localTokenDao = new CapturingDao();

            StubAuthentication stubAuth = new StubAuthentication();
            IApiBuilder builder = newBuilder();

            var authBuilder = builder.security()
                    .authentication(new FixedSupplierBuilder<>(stubAuth, IClass.getClass(StubAuthentication.class)));
            authBuilder.authenticate("authenticate")
                    .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                    .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                    .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
            authBuilder.up();

            var tb = builder.domain(IClass.getClass(SignedTokenEntity.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .owned("ownerId")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                    .up()
                    .dto(IClass.getClass(SignedTokenDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(localTokenDao)
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

            // Token domain must also be an authenticator (login = token uuid).
            StubTokenAuthentication stubTokenAuthNoKey = new StubTokenAuthentication();
            var tokenAuthBuilderNoKey = builder.security()
                    .authentication(new FixedSupplierBuilder<>(stubTokenAuthNoKey, IClass.getClass(StubTokenAuthentication.class)));
            tokenAuthBuilderNoKey.authenticate("authenticate")
                    .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                    .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                    .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
            tokenAuthBuilderNoKey.up();

            tb.security()
                    .authenticator()
                        .login("uuid")
                        .scope(AuthenticatorScope.tenant)
                        .alwaysEnabled(true)
                        .authentication(tokenAuthBuilderNoKey);

            @SuppressWarnings("rawtypes")
            var ub = builder.domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .owner("uuid")
                    .superOwner("superOwner")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                    .up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(localUserDao)
                    .up();
            // Authenticator with linked authorization but NO .key(supplier) nor .key(domain)
            ub.security()
                    .authenticator()
                        .login("id")
                        .scope(AuthenticatorScope.tenant)
                        .alwaysEnabled(true)
                        .authentication(authBuilder)
                    .authorization((com.garganttua.api.commons.context.dsl.IDomainBuilder) tb)
                        .lifeTime(60, java.util.concurrent.TimeUnit.MINUTES);
            ub.up();

            // The build itself must fail — we never get to the runtime pipeline. This
            // matches the policy that misconfiguration is caught at .build() rather
            // than at the first sign call.
            ApiException ex = assertThrows(ApiException.class,
                    () -> buildAndStart(builder),
                    "ApiBuilder must refuse a signable authorization with no .key(...) configured");
            String fullMessage = unwrap(ex);
            assertTrue(fullMessage.contains("neither .key(supplier) nor .key(domain)"),
                    "build error must explain the missing key configuration — got: " + fullMessage);
            assertTrue(fullMessage.contains("signable authorization"),
                    "build error must mention the signable authorization that triggered the check — got: " + fullMessage);
        }

        private static String unwrap(Throwable t) {
            StringBuilder sb = new StringBuilder();
            for (Throwable cur = t; cur != null; cur = cur.getCause()) {
                if (sb.length() > 0) sb.append(" | ");
                sb.append(cur.getMessage());
            }
            return sb.toString();
        }

        @Test
        @DisplayName("non-signable authorization runs the pipeline normally without requiring a keyRealm (no-op signing)")
        void nonSignableNoKeyRealmStillWorks() throws Exception {
            CapturingDao localUserDao = new CapturingDao();
            CapturingDao localTokenDao = new CapturingDao();

            StubAuthentication stubAuth = new StubAuthentication();
            IApiBuilder builder = newBuilder();

            var authBuilder = builder.security()
                    .authentication(new FixedSupplierBuilder<>(stubAuth, IClass.getClass(StubAuthentication.class)));
            authBuilder.authenticate("authenticate")
                    .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                    .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                    .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
            authBuilder.up();

            // Token domain WITHOUT .signable(...)
            var tb = builder.domain(IClass.getClass(SignedTokenEntity.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .owned("ownerId")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                    .up()
                    .dto(IClass.getClass(SignedTokenDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(localTokenDao)
                    .up()
                    .security()
                        .authorization()
                            .type("tokenType")
                            .authorities("authorities")
                            .expirable("expiresAt")
                            .revokable("revoked")
                        .up()
                    .up();

            // Token domain must also be an authenticator (login = token uuid).
            StubTokenAuthentication stubTokenAuthNonSignable = new StubTokenAuthentication();
            var tokenAuthBuilderNonSignable = builder.security()
                    .authentication(new FixedSupplierBuilder<>(stubTokenAuthNonSignable, IClass.getClass(StubTokenAuthentication.class)));
            tokenAuthBuilderNonSignable.authenticate("authenticate")
                    .withParam(0, new com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder())
                    .withParam(1, new com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder())
                    .withParam(2, new com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder());
            tokenAuthBuilderNonSignable.up();

            tb.security()
                    .authenticator()
                        .login("uuid")
                        .scope(AuthenticatorScope.tenant)
                        .alwaysEnabled(true)
                        .authentication(tokenAuthBuilderNonSignable);

            @SuppressWarnings("rawtypes")
            var ub = builder.domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .owner("uuid")
                    .superOwner("superOwner")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                    .up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(localUserDao)
                    .up();
            ub.security()
                    .authenticator()
                        .login("id")
                        .scope(AuthenticatorScope.tenant)
                        .alwaysEnabled(true)
                        .authentication(authBuilder)
                    .authorization((com.garganttua.api.commons.context.dsl.IDomainBuilder) tb)
                        .lifeTime(60, java.util.concurrent.TimeUnit.MINUTES);
            ub.up();

            IApi api = buildAndStart(builder);
            IDomain<?> userCtxLocal = api.getDomain("users").orElseThrow();

            UserDto u = new UserDto();
            u.setId("john@example.com");
            u.setUuid("user-uuid-1");
            u.setTenantId("SUPER_TENANT");
            localUserDao.save(u);

            WorkflowResult result = executeScript(userCtxLocal,
                    authenticateRequest("john@example.com", "valid-password", "SUPER_TENANT"));

            assertEquals(0, result.code(), "non-signable authorization must not require a keyRealm");
            SignedTokenEntity token = (SignedTokenEntity) result.output();
            assertNull(token.getSignature(), "non-signable authorization must leave signature null");
        }
    }
}
