package com.garganttua.api.core.integ.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

        /** Wire encoding: base64-encoded "scheme.uuid.signature". */
        public String toWire() {
            String sig = signature == null ? "" : Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
            return String.valueOf(tokenType) + "." + String.valueOf(uuid) + "." + sig;
        }
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
                        .signable()
                            .signature("signature")
                            .getDataToSign("getDataToSign")
                        .up()
                        .refreshable()
                            .expirable("refreshExpiresAt")
                            .revokable("refreshRevoked")
                            .encode("toWire")
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
                "john@example.com", "valid-password".getBytes(StandardCharsets.UTF_8), "SUPER_TENANT");
        OperationRequest request = superTenantScriptRequest(
                OperationDefinition.authenticate("users", IClass.getClass(User.class)));
        request.arg("entity", req);
        return request;
    }

    @Nested
    @DisplayName("Encode on creation")
    class OnCreate {

        @Test
        @DisplayName("login publishes the encoded form on the request as encodedAuthorization")
        void publishesEncodedForm() throws ApiException {
            OperationRequest request = authenticateRequest();
            WorkflowResult result = executeScript(userCtx, request);

            assertEquals(0, result.code());
            assertInstanceOf(WireEncodableToken.class, result.output());
            WireEncodableToken token = (WireEncodableToken) result.output();

            // The script should have stashed the encoded form on the request
            String encoded = (String) request.arg("encodedAuthorization").orElse(null);
            assertNotNull(encoded, "encodedAuthorization arg must be populated by encodeIfPossible");
            // Sanity: the encoded form should match what calling toWire() produces
            assertEquals(token.toWire(), encoded);
            // And carry the post-sign signature (non-empty Base64 last segment)
            String[] parts = encoded.split("\\.", -1);
            assertEquals(3, parts.length);
            assertEquals("auth-token", parts[0]);
            assertEquals(token.getUuid(), parts[1]);
            assertTrue(parts[2].length() > 0, "encoded form must include the signature segment");
        }
    }

    @Nested
    @DisplayName("Encode on refresh")
    class OnRefresh {

        @Test
        @DisplayName("refresh also publishes the encoded form for the freshly issued token")
        void refreshPublishesEncoded() throws ApiException {
            // First login to obtain a token to replay
            OperationRequest loginReq = authenticateRequest();
            WorkflowResult loginResult = executeScript(userCtx, loginReq);
            assertEquals(0, loginResult.code());
            WireEncodableToken originalToken = (WireEncodableToken) loginResult.output();
            assertNotNull(originalToken.getRefreshExpiresAt(),
                    "refreshable token must have refreshExpiresAt populated at creation");

            // Refresh request
            OperationRequest refreshReq = superTenantScriptRequest(
                    OperationDefinition.refreshAuthorization("users", IClass.getClass(User.class)));
            refreshReq.arg("entity", originalToken);

            WorkflowResult refreshResult = executeScript(userCtx, refreshReq);
            assertEquals(0, refreshResult.code());

            WireEncodableToken refreshed = (WireEncodableToken) refreshResult.output();
            String encoded = (String) refreshReq.arg("encodedAuthorization").orElse(null);

            assertNotNull(encoded, "refresh must publish encodedAuthorization");
            assertEquals(refreshed.toWire(), encoded,
                    "encoded must reflect the freshly minted entity, not the source");
        }
    }

    @Nested
    @DisplayName("Round-trip semantics")
    class RoundTrip {

        @Test
        @DisplayName("the encoded form contains the uuid + signature of the entity returned to the caller")
        void encodedReflectsOutput() throws ApiException {
            OperationRequest request = authenticateRequest();
            WorkflowResult result = executeScript(userCtx, request);
            assertEquals(0, result.code());

            WireEncodableToken token = (WireEncodableToken) result.output();
            String encoded = (String) request.arg("encodedAuthorization").orElse(null);

            // Parse the encoded form and verify each segment matches the token
            String[] parts = encoded.split("\\.", -1);
            assertEquals(3, parts.length);
            assertEquals(token.getTokenType(), parts[0]);
            assertEquals(token.getUuid(), parts[1]);

            byte[] decodedSig = Base64.getUrlDecoder().decode(parts[2]);
            assertNotNull(token.getSignature());
            assertEquals(token.getSignature().length, decodedSig.length);
            for (int i = 0; i < decodedSig.length; i++) {
                assertEquals(token.getSignature()[i], decodedSig[i],
                        "signature byte mismatch at index " + i);
            }
        }
    }
}
