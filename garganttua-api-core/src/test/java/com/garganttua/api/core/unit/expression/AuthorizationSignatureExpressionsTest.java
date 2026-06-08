package com.garganttua.api.core.unit.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;
import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@DisplayName("SecurityExpressions — authorization signature")
class AuthorizationSignatureExpressionsTest {

    public static class TokenEntity {
        public byte[] signature;
        private final byte[] payload;

        public TokenEntity() {
            this(new byte[] { 1, 2, 3, 4 });
        }

        public TokenEntity(byte[] payload) {
            this.payload = payload;
        }

        public byte[] getDataToSign() {
            return payload;
        }
    }

    private Domain<TokenEntity> domain;
    private DomainDefinition<TokenEntity> domDef;
    private IDomainSecurityDefinition secDef;
    private IAuthenticatorDefinition authDef;
    private IDomainAuthenticatorAuthorizationDefinition authzAuthDef;
    private IDomainAuthenticatorAuthorizationKeyDefinition keyDef;
    private IDomainAuthorizationDefinition authzDef;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        domain = mock(Domain.class);
        domDef = mock(DomainDefinition.class);
        secDef = mock(IDomainSecurityDefinition.class);
        authDef = mock(IAuthenticatorDefinition.class);
        authzAuthDef = mock(IDomainAuthenticatorAuthorizationDefinition.class);
        keyDef = mock(IDomainAuthenticatorAuthorizationKeyDefinition.class);
        authzDef = mock(IDomainAuthorizationDefinition.class);

        when(domain.getDomainName()).thenReturn("tokens");
        when(domain.getDomainDefinition()).thenReturn(domDef);
        when(domDef.domainSecurityDefinition()).thenReturn(secDef);
        when(secDef.authenticatorDefinition()).thenReturn(authDef);
        when(secDef.authorizationDefinition()).thenReturn(authzDef);
        when(authDef.authorizationDefinition()).thenReturn(authzAuthDef);
        when(authzAuthDef.keyDefinition()).thenReturn(keyDef);

        when(authzDef.signable()).thenReturn(true);
        when(authzDef.signatureField()).thenReturn(new ObjectAddress("signature"));
        when(authzDef.getDataToSignMethod()).thenReturn(new ObjectAddress("getDataToSign"));
    }

    private IKeyRealm keyRealmReturning(IKey signing, IKey verifying) {
        IKeyRealm realm = mock(IKeyRealm.class);
        try {
            when(realm.getKeyForSigning()).thenReturn(signing);
            when(realm.getKeyForSignatureVerification()).thenReturn(verifying);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return realm;
    }

    @Nested
    @DisplayName("isAuthorizationSignable")
    class IsSignable {

        @Test
        @DisplayName("returns true when authorization def reports signable=true")
        void trueWhenSignable() {
            assertTrue(SecurityExpressions.isAuthorizationSignable(domain));
        }

        @Test
        @DisplayName("returns false when authorization def reports signable=false")
        void falseWhenNotSignable() {
            when(authzDef.signable()).thenReturn(false);
            assertFalse(SecurityExpressions.isAuthorizationSignable(domain));
        }

        @Test
        @DisplayName("returns false when domain has no security def")
        void falseWhenNoSecurityDef() {
            when(domDef.domainSecurityDefinition()).thenReturn(null);
            assertFalse(SecurityExpressions.isAuthorizationSignable(domain));
        }

        @Test
        @DisplayName("returns false when context is null")
        void falseWhenNullContext() {
            assertFalse(SecurityExpressions.isAuthorizationSignable(null));
        }
    }

    @Nested
    @DisplayName("resolveKeyRealm")
    class ResolveKeyRealm {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("invokes the user-provided supplier and returns the supplied IKeyRealm")
        void resolvesFromSupplier() throws Exception {
            IKeyRealm realm = mock(IKeyRealm.class);
            ISupplier supplier = mock(ISupplier.class);
            when(supplier.supply()).thenReturn(Optional.of(realm));
            ISupplierBuilder builder = mock(ISupplierBuilder.class);
            when(builder.build()).thenReturn(supplier);
            when(authzAuthDef.keyRealm()).thenReturn(builder);

            IKeyRealm out = SecurityExpressions.resolveKeyRealm(domain, null);
            assertSame(realm, out);
        }

        @Test
        @DisplayName("throws when neither .key(supplier) nor .key(domain) is configured")
        void throwsWhenNotConfigured() {
            when(authzAuthDef.keyRealm()).thenReturn(null);
            when(authzAuthDef.keyDefinition()).thenReturn(null);
            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.resolveKeyRealm(domain, null));
            assertTrue(ex.getMessage().contains("neither .key(supplier) nor .key(domain)"));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("throws when supplier returns empty Optional")
        void throwsWhenSupplyEmpty() throws Exception {
            ISupplier supplier = mock(ISupplier.class);
            when(supplier.supply()).thenReturn(Optional.empty());
            ISupplierBuilder builder = mock(ISupplierBuilder.class);
            when(builder.build()).thenReturn(supplier);
            when(authzAuthDef.keyRealm()).thenReturn(builder);

            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.resolveKeyRealm(domain, null));
            assertTrue(ex.getMessage().contains("returned empty"));
        }

        @Test
        @DisplayName("throws when no authenticator authorization is configured")
        void throwsWhenNoAuthzAuthDef() {
            when(authDef.authorizationDefinition()).thenReturn(null);
            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.resolveKeyRealm(domain, null));
            assertTrue(ex.getMessage().contains("no authenticator authorization"));
        }
    }

    @Nested
    @DisplayName("signAuthorization")
    class Sign {

        @Test
        @DisplayName("invokes getDataToSign, signs via keyRealm, writes signature into the entity field")
        void signsAndWritesField() throws Exception {
            TokenEntity entity = new TokenEntity(new byte[] { 9, 8, 7 });
            byte[] expectedSig = new byte[] { 0x42, 0x43 };
            IKey signingKey = mock(IKey.class);
            when(signingKey.sign(any(byte[].class))).thenReturn(expectedSig);
            IKeyRealm realm = keyRealmReturning(signingKey, null);

            boolean ok = SecurityExpressions.signAuthorization(entity, domain, realm);

            assertTrue(ok);
            assertArrayEquals(expectedSig, entity.signature);
        }

        @Test
        @DisplayName("rejects null entity / domain / keyRealm")
        void rejectsNullArguments() {
            IKeyRealm realm = mock(IKeyRealm.class);
            TokenEntity entity = new TokenEntity();
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.signAuthorization(null, domain, realm));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.signAuthorization(entity, null, realm));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.signAuthorization(entity, domain, null));
        }

        @Test
        @DisplayName("throws when authorization is not signable")
        void throwsWhenNotSignable() {
            when(authzDef.signable()).thenReturn(false);
            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.signAuthorization(new TokenEntity(), domain, mock(IKeyRealm.class)));
            assertTrue(ex.getMessage().contains("not signable"));
        }

        @Test
        @DisplayName("throws when no signature field is configured")
        void throwsWhenNoSignatureField() {
            when(authzDef.signatureField()).thenReturn(null);
            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.signAuthorization(new TokenEntity(), domain, mock(IKeyRealm.class)));
            assertTrue(ex.getMessage().contains("signature field"));
        }

        @Test
        @DisplayName("throws when no getDataToSign method is configured")
        void throwsWhenNoDataMethod() {
            when(authzDef.getDataToSignMethod()).thenReturn(null);
            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.signAuthorization(new TokenEntity(), domain, mock(IKeyRealm.class)));
            assertTrue(ex.getMessage().contains("getDataToSign"));
        }

        @Test
        @DisplayName("wraps key.sign exceptions into ApiException")
        void wrapsKeyException_ignored() throws Exception {
            IKey signingKey = mock(IKey.class);
            when(signingKey.sign(any(byte[].class))).thenThrow(new RuntimeException("boom"));
            IKeyRealm realm = keyRealmReturning(signingKey, null);

            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.signAuthorization(new TokenEntity(), domain, realm));
            assertTrue(ex.getMessage().contains("signAuthorization"));
        }
    }

    @Nested
    @DisplayName("verifyAuthorizationSignature returns false (not throws) on crypto-level errors")
    class VerifyCryptoErrors {

        @Test
        @DisplayName("verifySignature throwing => verifyAuthorizationSignature returns false (a tampered token must surface as 401, not 500)")
        void verifyTreatsCryptoExceptionAsInvalid() throws Exception {
            TokenEntity entity = new TokenEntity(new byte[] { 1 });
            entity.signature = new byte[] { 9 };
            IKey verifyingKey = mock(IKey.class);
            when(verifyingKey.verifySignature(any(byte[].class), any(byte[].class)))
                    .thenThrow(new RuntimeException("malformed signature bytes"));
            IKeyRealm realm = keyRealmReturning(null, verifyingKey);

            assertFalse(SecurityExpressions.verifyAuthorizationSignature(entity, domain, realm));
        }
    }

    @Nested
    @DisplayName("verifyAuthorizationSignature")
    class Verify {

        @Test
        @DisplayName("returns true when key.verifySignature returns true")
        void verifiesValid() throws Exception {
            TokenEntity entity = new TokenEntity(new byte[] { 1, 1, 1 });
            entity.signature = new byte[] { 7, 7 };
            IKey verifyingKey = mock(IKey.class);
            when(verifyingKey.verifySignature(any(byte[].class), any(byte[].class))).thenReturn(true);
            IKeyRealm realm = keyRealmReturning(null, verifyingKey);

            assertTrue(SecurityExpressions.verifyAuthorizationSignature(entity, domain, realm));
        }

        @Test
        @DisplayName("returns false when key.verifySignature returns false (signature mismatch)")
        void rejectsInvalid() throws Exception {
            TokenEntity entity = new TokenEntity(new byte[] { 2, 2, 2 });
            entity.signature = new byte[] { 9 };
            IKey verifyingKey = mock(IKey.class);
            when(verifyingKey.verifySignature(any(byte[].class), any(byte[].class))).thenReturn(false);
            IKeyRealm realm = keyRealmReturning(null, verifyingKey);

            assertFalse(SecurityExpressions.verifyAuthorizationSignature(entity, domain, realm));
        }

        @Test
        @DisplayName("throws when authorization is not signable")
        void throwsWhenNotSignable() {
            when(authzDef.signable()).thenReturn(false);
            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.verifyAuthorizationSignature(new TokenEntity(), domain, mock(IKeyRealm.class)));
            assertTrue(ex.getMessage().contains("not signable"));
        }

        @Test
        @DisplayName("throws when signature field on the entity is null (never signed)")
        void throwsWhenNoSignatureBytes() throws Exception {
            TokenEntity entity = new TokenEntity();
            // entity.signature stays null
            IKey verifyingKey = mock(IKey.class);
            IKeyRealm realm = keyRealmReturning(null, verifyingKey);

            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.verifyAuthorizationSignature(entity, domain, realm));
            assertTrue(ex.getMessage().contains("signature field"));
        }

        @Test
        @DisplayName("rejects null entity / domain / keyRealm")
        void rejectsNullArguments() {
            IKeyRealm realm = mock(IKeyRealm.class);
            TokenEntity entity = new TokenEntity();
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.verifyAuthorizationSignature(null, domain, realm));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.verifyAuthorizationSignature(entity, null, realm));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.verifyAuthorizationSignature(entity, domain, null));
        }
    }

    @Nested
    @DisplayName("signIfSignable / verifyIfSignable composites")
    class Composites {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void wireKeyRealm(IKeyRealm realm) throws Exception {
            ISupplier supplier = mock(ISupplier.class);
            when(supplier.supply()).thenReturn(Optional.of(realm));
            ISupplierBuilder builder = mock(ISupplierBuilder.class);
            when(builder.build()).thenReturn(supplier);
            when(authzAuthDef.keyRealm()).thenReturn(builder);
        }

        @Test
        @DisplayName("signIfSignable is a no-op (returns true) when not signable")
        void signNoOpWhenNotSignable() {
            when(authzDef.signable()).thenReturn(false);
            // No keyRealm wired — proves the composite never even tries to resolve it.
            TokenEntity entity = new TokenEntity();
            assertTrue(SecurityExpressions.signIfSignable(entity, domain, null));
            // Field stays untouched
            org.junit.jupiter.api.Assertions.assertNull(entity.signature);
        }

        @Test
        @DisplayName("signIfSignable signs through when signable")
        void signWhenSignable() throws Exception {
            TokenEntity entity = new TokenEntity(new byte[] { 5, 5 });
            IKey signingKey = mock(IKey.class);
            when(signingKey.sign(any(byte[].class))).thenReturn(new byte[] { 0x55 });
            wireKeyRealm(keyRealmReturning(signingKey, null));

            assertTrue(SecurityExpressions.signIfSignable(entity, domain, null));
            assertArrayEquals(new byte[] { 0x55 }, entity.signature);
        }

        @Test
        @DisplayName("signIfSignable throws when signable but neither supplier nor key domain wired")
        void signThrowsWhenSignableButNoRealm() {
            // signable=true, no supplier, no key domain
            when(authzAuthDef.keyRealm()).thenReturn(null);
            when(authzAuthDef.keyDefinition()).thenReturn(null);
            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.signIfSignable(new TokenEntity(), domain, null));
            assertTrue(ex.getMessage().contains("neither .key(supplier) nor .key(domain)"));
        }

        @Test
        @DisplayName("verifyIfSignable returns true (no-op) when not signable")
        void verifyNoOpWhenNotSignable() {
            when(authzDef.signable()).thenReturn(false);
            TokenEntity entity = new TokenEntity();
            assertTrue(SecurityExpressions.verifyIfSignable(entity, domain, null));
        }

        @Test
        @DisplayName("verifyIfSignable returns false when signable and signature mismatches")
        void verifyRejectsTampered() throws Exception {
            TokenEntity entity = new TokenEntity(new byte[] { 1 });
            entity.signature = new byte[] { 0x00 };
            IKey verifyingKey = mock(IKey.class);
            when(verifyingKey.verifySignature(any(byte[].class), any(byte[].class))).thenReturn(false);
            wireKeyRealm(keyRealmReturning(null, verifyingKey));

            assertFalse(SecurityExpressions.verifyIfSignable(entity, domain, null));
        }

        @Test
        @DisplayName("verifyIfSignable returns true when signable and signature is valid")
        void verifyAcceptsValid() throws Exception {
            TokenEntity entity = new TokenEntity(new byte[] { 2 });
            entity.signature = new byte[] { (byte) 0x99 };
            IKey verifyingKey = mock(IKey.class);
            when(verifyingKey.verifySignature(any(byte[].class), any(byte[].class))).thenReturn(true);
            wireKeyRealm(keyRealmReturning(null, verifyingKey));

            assertTrue(SecurityExpressions.verifyIfSignable(entity, domain, null));
        }
    }

    @Nested
    @DisplayName("end-to-end: sign then verify on the same payload")
    class RoundTrip {

        @Test
        @DisplayName("signing then verifying with a stub key that echoes data ↔ signature is consistent")
        void signThenVerify() throws Exception {
            TokenEntity entity = new TokenEntity(new byte[] { 0xC, 0xA, 0xF, 0xE });

            // Stub key: sign() returns the input, verifySignature() compares equality.
            IKey signingKey = mock(IKey.class);
            when(signingKey.sign(any(byte[].class))).thenAnswer(inv -> {
                byte[] in = inv.getArgument(0);
                byte[] copy = new byte[in.length];
                System.arraycopy(in, 0, copy, 0, in.length);
                return copy;
            });
            IKey verifyingKey = mock(IKey.class);
            when(verifyingKey.verifySignature(any(byte[].class), any(byte[].class))).thenAnswer(inv -> {
                byte[] sig = inv.getArgument(0);
                byte[] data = inv.getArgument(1);
                return java.util.Arrays.equals(sig, data);
            });
            IKeyRealm realm = keyRealmReturning(signingKey, verifyingKey);

            assertTrue(SecurityExpressions.signAuthorization(entity, domain, realm));
            assertNotNull(entity.signature);
            assertArrayEquals(new byte[] { 0xC, 0xA, 0xF, 0xE }, entity.signature);
            assertTrue(SecurityExpressions.verifyAuthorizationSignature(entity, domain, realm));

            // Tamper with signature -> verify must reject
            entity.signature = new byte[] { 0x00, 0x00 };
            assertFalse(SecurityExpressions.verifyAuthorizationSignature(entity, domain, realm));
        }
    }
}
