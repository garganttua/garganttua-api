package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
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
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.KeyRealmBuilder;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("Encode Authorization Integration Tests (Phase 3)")
class EncodeAuthorizationIntegrationTest extends AbstractCrudScriptTest {

    /**
     * Token entity that knows how to encode itself to a transport-friendly
     * "scheme.uuid.signatureBase64" form. The decode side uses simple split.
     */
    public static class WireEncodableToken {
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
        private Instant refreshExpiresAt;
        private Boolean refreshRevoked = false;
        private Boolean superTenant = false;

        public WireEncodableToken() {}

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
        public void setAuthorities(List<String> a) { this.authorities = a; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant v) { this.createdAt = v; }
        public Instant getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Instant v) { this.expiresAt = v; }
        public Boolean getRevoked() { return revoked; }
        public void setRevoked(Boolean v) { this.revoked = v; }
        public byte[] getSignature() { return signature; }
        public void setSignature(byte[] v) { this.signature = v; }
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

        /**
         * Lossless wire encoding, JWT-shaped: {@code tokenType.base64(payload).base64(signature)}.
         * The payload carries every field needed to reconstruct the entity on the way back
         * (refresh re-verifies the signature and resolves the principal by ownerId).
         */
        public String toWire() {
            String payload = String.join(";",
                    nz(tokenType), nz(uuid), nz(ownerId), nz(tenantId),
                    authorities == null ? "" : String.join(",", authorities),
                    createdAt == null ? "" : Long.toString(createdAt.getEpochSecond()),
                    expiresAt == null ? "" : Long.toString(expiresAt.getEpochSecond()),
                    refreshExpiresAt == null ? "" : Long.toString(refreshExpiresAt.getEpochSecond()),
                    refreshRevoked == null ? "false" : refreshRevoked.toString());
            String p = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            String sig = signature == null ? "" : Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
            return nz(tokenType) + "." + p + "." + sig;
        }

        /** Decode side: populates this entity from {@link #toWire()}'s output. */
        public void fromWire(byte[] raw) {
            String[] parts = new String(raw, StandardCharsets.UTF_8).split("\\.", -1);
            this.tokenType = emptyToNull(parts[0]);
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String[] f = payload.split(";", -1);
            this.uuid = emptyToNull(f[1]);
            this.ownerId = emptyToNull(f[2]);
            this.tenantId = emptyToNull(f[3]);
            this.authorities = f[4].isEmpty() ? null : java.util.Arrays.asList(f[4].split(","));
            this.createdAt = f[5].isEmpty() ? null : Instant.ofEpochSecond(Long.parseLong(f[5]));
            this.expiresAt = f[6].isEmpty() ? null : Instant.ofEpochSecond(Long.parseLong(f[6]));
            this.refreshExpiresAt = f[7].isEmpty() ? null : Instant.ofEpochSecond(Long.parseLong(f[7]));
            this.refreshRevoked = Boolean.parseBoolean(f[8]);
            this.signature = parts[2].isEmpty() ? null : Base64.getUrlDecoder().decode(parts[2]);
        }

        private static String nz(String s) { return s == null ? "" : s; }
        private static String emptyToNull(String s) { return s == null || s.isEmpty() ? null : s; }
    }

    public static class WireTokenDto {
        private String id;
        private String uuid;
        private String tenantId;
        private Boolean superTenant;
        public WireTokenDto() {}
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
                .name("encode-test-realm")
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

        // Token: signable + refreshable + carries an encode method (toWire).
        var tokenDomainBuilder = builder.domain(IClass.getClass(WireEncodableToken.class))
                .tenant(true)
                .superTenant("superTenant")
                .owned("ownerId")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(WireTokenDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(tokenDao)
                .up()
                .security()
                    .authorization()
                        .type("tokenType")
                        .authorities("authorities")
                        .expirable("expiresAt")
                        .revokable("revoked")
                        .encode("toWire")
                        .decode("fromWire")
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

        userDomainBuilder.security()
                .authenticator()
                    .login("id")
                    .scope(AuthenticatorScope.tenant)
                    .alwaysEnabled(true)
                    .authentication(authBuilder)
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

    private OperationRequest authenticateRequest() {
        AuthenticationRequest req = new AuthenticationRequest(
                "john@example.com", "valid-password".getBytes(StandardCharsets.UTF_8));
        OperationRequest request = superTenantScriptRequest(
                OperationDefinition.authenticate("users", IClass.getClass(User.class)));
        request.arg("entity", req);
        return request;
    }

    @Nested
    @DisplayName("Encode on creation")
    class OnCreate {

        @Test
        @DisplayName("login returns the ENCODED transport form (the JWT-like wire string), not the entity")
        void loginReturnsEncodedForm() throws ApiException {
            OperationRequest request = authenticateRequest();
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code(), () -> "login failed; vars=" + result.variables());
            // The operation output is now the encoded wire form — the whole point.
            assertInstanceOf(String.class, result.output());
            String encoded = (String) result.output();

            // The same wire form is also published on the request for custom protocols.
            assertEquals(encoded, request.arg("encodedAuthorization").orElse(null));

            String[] parts = encoded.split("\\.", -1);
            assertEquals(3, parts.length, "JWT-shaped tokenType.payload.signature; got: " + encoded);
            assertEquals("auth-token", parts[0]);
            assertTrue(parts[2].length() > 0, "encoded form must include the signature segment");
        }
    }

    @Nested
    @DisplayName("Round-trip on refresh (decode → reissue)")
    class OnRefresh {

        @Test
        @DisplayName("refresh DECODES the presented JWT, verifies its signature, and reissues a fresh JWT")
        void refreshDecodesAndReissues() throws ApiException {
            // Login → the client now holds the encoded JWT, not the entity.
            OperationRequest loginReq = authenticateRequest();
            WorkflowResult loginResult = executeScript(userCtx, loginReq);
            assertEquals(0, loginResult.code(), () -> "login failed; vars=" + loginResult.variables());
            assertInstanceOf(String.class, loginResult.output());
            String loginJwt = (String) loginResult.output();

            // The client presents the SAME wire form (the JWT string) to refresh.
            OperationRequest refreshReq = superTenantScriptRequest(
                    OperationDefinition.refreshAuthorization("users", IClass.getClass(User.class)));
            refreshReq.arg("entity", loginJwt);

            WorkflowResult refreshResult = executeScript(userCtx, refreshReq);
            assertEquals(0, refreshResult.code(),
                    () -> "refresh must decode the presented JWT (signature verified) and reissue; vars="
                            + refreshResult.variables());
            assertInstanceOf(String.class, refreshResult.output());
            String refreshedJwt = (String) refreshResult.output();

            assertEquals(3, refreshedJwt.split("\\.", -1).length, "reissued token is a JWT wire form");
            assertNotEquals(loginJwt, refreshedJwt, "refresh mints a NEW token (fresh uuid)");
        }
    }

    @Nested
    @DisplayName("Round-trip semantics")
    class RoundTrip {

        @Test
        @DisplayName("the encoded output decodes back to an entity that re-encodes identically (lossless round-trip)")
        void encodedRoundTripsLosslessly() throws ApiException {
            OperationRequest request = authenticateRequest();
            WorkflowResult result = executeScript(userCtx, request);
            assertEquals(0, result.code(), () -> "login failed; vars=" + result.variables());

            String encoded = (String) result.output();
            String[] parts = encoded.split("\\.", -1);
            assertEquals(3, parts.length);
            assertEquals("auth-token", parts[0]);

            // Decode back to an entity and re-encode — must reproduce the wire form,
            // and carry a non-empty post-sign signature.
            WireEncodableToken decoded = new WireEncodableToken();
            decoded.fromWire(encoded.getBytes(StandardCharsets.UTF_8));
            assertNotNull(decoded.getSignature());
            assertTrue(decoded.getSignature().length > 0);
            assertEquals(encoded, decoded.toWire(), "decode → re-encode must round-trip identically");
        }
    }

    @Nested
    @DisplayName("Verify Mode A (Bearer JWT in the raw Authorization header)")
    class VerifyModeA {

        private String login() throws ApiException {
            WorkflowResult login = executeScript(userCtx, authenticateRequest());
            assertEquals(0, login.code(), () -> "login failed; vars=" + login.variables());
            return (String) login.output();
        }

        private OperationRequest bearer(String jwt) {
            OperationRequest req = superTenantScriptRequest(
                    OperationDefinition.authenticate("users", IClass.getClass(User.class)));
            byte[] b = ("Bearer " + jwt).getBytes(StandardCharsets.UTF_8);
            Byte[] boxed = new Byte[b.length];
            for (int i = 0; i < b.length; i++) boxed[i] = b[i];
            req.arg(IOperationRequest.RAW_AUTHORIZATION, boxed);
            return req;
        }

        @Test
        @DisplayName("a Bearer JWT is pre-decoded to the entity and its signature verifies")
        void bearerJwtDecodesAndVerifies() throws ApiException {
            String jwt = login();
            OperationRequest req = bearer(jwt);

            boolean decoded = SecurityExpressions.predecodeRawAuthorization(req, userCtx);
            assertTrue(decoded, "a Bearer JWT with a configured decode method must be pre-decoded");

            Object authz = req.arg(IOperationRequest.AUTHORIZATION).orElse(null);
            assertInstanceOf(WireEncodableToken.class, authz, "the raw header must decode to the entity");

            boolean sigOk = (Boolean) SecurityExpressions.verifyIfSignable(authz, userCtx, req);
            assertTrue(sigOk, "the decoded Bearer JWT's signature must verify against the signing key");
        }

        @Test
        @DisplayName("a TAMPERED Bearer JWT decodes but FAILS signature verification")
        void tamperedJwtRejected() throws ApiException {
            String jwt = login();
            String[] parts = jwt.split("\\.", -1);
            // Tamper a decoded signature BYTE, not a trailing base64 char: the
            // ECDSA/DER signature length varies run-to-run, and when it is not a
            // multiple of 3 the last base64url char carries non-significant
            // padding bits the decoder ignores — flipping those would leave the
            // bytes (and thus the signature) unchanged, making the test flaky.
            byte[] sigBytes = Base64.getUrlDecoder().decode(parts[2]);
            sigBytes[sigBytes.length / 2] ^= 0x01; // flip a guaranteed-significant bit
            String tamperedSig = Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);
            assertNotEquals(parts[2], tamperedSig, "the tamper must actually change the signature bytes");
            String tampered = parts[0] + "." + parts[1] + "." + tamperedSig;

            OperationRequest req = bearer(tampered);
            SecurityExpressions.predecodeRawAuthorization(req, userCtx);
            Object authz = req.arg(IOperationRequest.AUTHORIZATION).orElse(null);
            assertInstanceOf(WireEncodableToken.class, authz);

            boolean sigOk = (Boolean) SecurityExpressions.verifyIfSignable(authz, userCtx, req);
            assertFalse(sigOk, "a tampered JWT signature must NOT verify");
        }
    }
}
