package com.garganttua.api.core.unit.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.authorization.AuthorizationBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.security.IDomainSecurityBuilder;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.security.context.IAuthorizationContext;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("AuthorizationBuilder Tests")
class AuthorizationBuilderTest {

    // Test entity with authorization fields
    public static class TokenEntity {
        private String tokenType;
        private List<String> authorities;
        private Instant expiresAt;
        private Instant createdAt;
        private Boolean revoked;
        private byte[] signature;
        private Instant refreshExpiresAt;
        private Boolean refreshRevoked;
    }

    @SuppressWarnings("rawtypes")
    private IDomainSecurityBuilder parentLink;
    private AuthorizationBuilder<TokenEntity> builder;

    @BeforeAll
    static void initReflection() {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        parentLink = mock(IDomainSecurityBuilder.class);
        builder = new AuthorizationBuilder<>(parentLink, IClass.getClass(TokenEntity.class));
    }

    @Nested
    @DisplayName("Minimal configuration")
    class MinimalConfig {

        @Test
        @DisplayName("builds with no configuration — all optional")
        void buildsWithNoConfig() throws ApiException {
            IAuthorizationContext ctx = builder.build();
            assertNotNull(ctx);
            IDomainAuthorizationDefinition def = ctx.getAuthorizationDefinition();
            assertNotNull(def);
            assertNull(def.type());
            assertNull(def.authorities());
            assertNull(def.expiration());
            assertNull(def.creation());
            assertNull(def.revoked());
            assertFalse(def.storable());
            assertFalse(def.signable());
            assertFalse(def.refreshable());
            assertNull(def.signatureField());
            assertNull(def.getDataToSignMethod());
            assertNull(def.refreshExpiration());
            assertNull(def.refreshRevoked());
        }
    }

    @Nested
    @DisplayName("Encode/decode on the plain authorization (no refreshable)")
    class PlainEncodeDecode {

        @Test
        @DisplayName("encode/decode are set on the plain authorization WITHOUT .refreshable() — a stateless token gets a transport form")
        void plainEncodeDecodeWithoutRefreshable() throws ApiException {
            IDomainAuthorizationDefinition def = builder
                    .encode("toWire")
                    .decode("fromWire")
                    .build()
                    .getAuthorizationDefinition();

            assertNotNull(def.encodeMethod(), "encode method must be set on the plain authorization");
            assertEquals("toWire", def.encodeMethod().toString());
            assertNotNull(def.decodeMethod(), "decode method must be set on the plain authorization");
            assertEquals("fromWire", def.decodeMethod().toString());
            assertFalse(def.refreshable(), "encode/decode require no .refreshable() — a non-refreshable token is encodable");
        }
    }

    @Nested
    @DisplayName("Field configuration")
    class FieldConfig {

        @Test
        @DisplayName("type field is set")
        void typeFieldIsSet() throws ApiException {
            builder.type("tokenType");
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertNotNull(def.type());
        }

        @Test
        @DisplayName("authorities field is set")
        void authoritiesFieldIsSet() throws ApiException {
            builder.authorities("authorities");
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertNotNull(def.authorities());
        }

        @Test
        @DisplayName("expirable field is set")
        void expirableFieldIsSet() throws ApiException {
            builder.expirable("expiresAt");
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertNotNull(def.expiration());
        }

        @Test
        @DisplayName("revokable field is set and enables storable")
        void revokableFieldIsSetAndEnablesStorable() throws ApiException {
            builder.revokable("revoked");
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertNotNull(def.revoked());
            assertTrue(def.storable(), "revokable should auto-enable storable");
        }

        @Test
        @DisplayName("storable is false by default")
        void storableIsFalseByDefault() throws ApiException {
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertFalse(def.storable());
        }

        @Test
        @DisplayName("storable can be set explicitly")
        void storableCanBeSet() throws ApiException {
            builder.storable(true);
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertTrue(def.storable());
        }
    }

    @Nested
    @DisplayName("Signable configuration")
    class SignableConfig {

        @Test
        @DisplayName("signable flag is set when signable() is called")
        void signableFlagIsSet() throws ApiException {
            builder.signable().signature("signature").up();
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertTrue(def.signable());
        }

        @Test
        @DisplayName("signature field is captured")
        void signatureFieldIsCaptured() throws ApiException {
            builder.signable().signature("signature").up();
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertNotNull(def.signatureField());
        }

        @Test
        @DisplayName("getDataToSign method is captured")
        void getDataToSignIsCaptured() throws ApiException {
            builder.signable().getDataToSign("getDataToSign").up();
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertNotNull(def.getDataToSignMethod());
        }

        @Test
        @DisplayName("not signable when signable() is not called")
        void notSignableByDefault() throws ApiException {
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertFalse(def.signable());
            assertNull(def.signatureField());
        }
    }

    @Nested
    @DisplayName("Refreshable configuration")
    class RefreshableConfig {

        @Test
        @DisplayName("refreshable flag is set when refreshable() is called")
        void refreshableFlagIsSet() throws ApiException {
            builder.refreshable().expirable("refreshExpiresAt").up();
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertTrue(def.refreshable());
        }

        @Test
        @DisplayName("refresh expiration is captured")
        void refreshExpirationIsCaptured() throws ApiException {
            builder.refreshable().expirable("refreshExpiresAt").up();
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertNotNull(def.refreshExpiration());
        }

        @Test
        @DisplayName("refresh revoked is captured")
        void refreshRevokedIsCaptured() throws ApiException {
            builder.refreshable().revokable("refreshRevoked").up();
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertNotNull(def.refreshRevoked());
        }

        @Test
        @DisplayName("not refreshable when refreshable() is not called")
        void notRefreshableByDefault() throws ApiException {
            IDomainAuthorizationDefinition def = builder.build().getAuthorizationDefinition();
            assertFalse(def.refreshable());
            assertNull(def.refreshExpiration());
            assertNull(def.refreshRevoked());
        }
    }

    @Nested
    @DisplayName("Full configuration")
    class FullConfig {

        @Test
        @DisplayName("all fields configured together")
        void allFieldsConfigured() throws ApiException {
            builder
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
                    .up();

            IAuthorizationContext ctx = builder.build();
            IDomainAuthorizationDefinition def = ctx.getAuthorizationDefinition();

            assertNotNull(def.type());
            assertNotNull(def.authorities());
            assertNotNull(def.expiration());
            assertNotNull(def.revoked());
            assertTrue(def.storable());
            assertTrue(def.signable());
            assertTrue(def.refreshable());
            assertNotNull(def.signatureField());
            assertNotNull(def.getDataToSignMethod());
            assertNotNull(def.refreshExpiration());
            assertNotNull(def.refreshRevoked());
        }
    }
}
