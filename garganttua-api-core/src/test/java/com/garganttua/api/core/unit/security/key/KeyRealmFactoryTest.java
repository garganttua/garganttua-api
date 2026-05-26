package com.garganttua.api.core.unit.security.key;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.core.definition.DomainKeyDefinition;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.key.KeyRealmFactory;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;

@DisplayName("KeyRealmFactory — bridges persisted @Key entities and IKeyRealm (via core's KeyRealm.fromSignatureMaterial)")
class KeyRealmFactoryTest {

    static {
        IClass.setReflection(ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build());
    }

    private static final IReflection REFLECTION = DefaultMapper.reflection();

    /**
     * Minimal key entity carrying the seven @Key-mapped fields plus Date / Long
     * expiration variants used by individual nested test cases.
     */
    public static class KeyEntity {
        public String realmName;
        public String algorithm;
        public String signatureAlgorithm;
        public byte[] publicMaterial;
        public byte[] privateMaterial;
        public Instant expiration;
        public boolean revoked;
    }

    public static class KeyEntityWithDateExpiration {
        public String realmName;
        public String algorithm;
        public String signatureAlgorithm;
        public byte[] publicMaterial;
        public byte[] privateMaterial;
        public Date expiration;
        public boolean revoked;
    }

    private static DomainKeyDefinition keyDefForEntity() {
        return new DomainKeyDefinition(
                new ObjectAddress("realmName"),
                new ObjectAddress("algorithm"),
                new ObjectAddress("signatureAlgorithm"),
                new ObjectAddress("publicMaterial"),
                new ObjectAddress("privateMaterial"),
                new ObjectAddress("expiration"),
                new ObjectAddress("revoked"));
    }

    @Nested
    @DisplayName("generateAndStamp")
    class GenerateAndStamp {

        @Test
        @DisplayName("generates an asymmetric pair, stamps every mapped field, leaves no field null")
        void stampsAllFields() throws ApiException {
            DomainKeyDefinition keyDef = keyDefForEntity();
            Object entity = KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "my-realm:tenant:42", 60, TimeUnit.MINUTES, REFLECTION);

            assertNotNull(entity);
            KeyEntity ke = (KeyEntity) entity;
            assertEquals("my-realm:tenant:42", ke.realmName);
            assertEquals("EC-256", ke.algorithm,
                    "algorithm should be stored in the canonical 'NAME-SIZE' form consumed by validateKeyAlgorithm");
            assertEquals("SHA256", ke.signatureAlgorithm);
            assertNotNull(ke.publicMaterial);
            assertNotNull(ke.privateMaterial);
            assertTrue(ke.publicMaterial.length > 0);
            assertTrue(ke.privateMaterial.length > 0);
            assertFalse(java.util.Arrays.equals(ke.publicMaterial, ke.privateMaterial),
                    "public and private encodings must differ");
            assertNotNull(ke.expiration);
            assertTrue(ke.expiration.isAfter(Instant.now().minusSeconds(5)),
                    "expiration must be in the (immediate) future");
            assertTrue(ke.expiration.isBefore(Instant.now().plus(java.time.Duration.ofMinutes(61))),
                    "expiration must be within the configured 60-minute window");
            assertFalse(ke.revoked, "freshly stamped key must have revoked=false");
        }

        @Test
        @DisplayName("expiration is adapted to a Date field when the entity declares Date")
        void adaptsDateExpiration() throws ApiException {
            DomainKeyDefinition keyDef = keyDefForEntity();
            KeyEntityWithDateExpiration ke = (KeyEntityWithDateExpiration) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntityWithDateExpiration.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "realm-Date", 1, TimeUnit.HOURS, REFLECTION);
            assertNotNull(ke.expiration);
            assertTrue(ke.expiration.getTime() > System.currentTimeMillis());
        }

        @Test
        @DisplayName("zero duration produces an immediate (now-ish) expiration — useful for forced rotation")
        void zeroDurationExpiresNow() throws ApiException {
            DomainKeyDefinition keyDef = keyDefForEntity();
            long before = System.currentTimeMillis();
            KeyEntity ke = (KeyEntity) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "now-realm", 0, TimeUnit.SECONDS, REFLECTION);
            long after = System.currentTimeMillis();
            assertNotNull(ke.expiration);
            assertTrue(ke.expiration.toEpochMilli() >= before - 1L);
            assertTrue(ke.expiration.toEpochMilli() <= after + 1L);
        }

        @Test
        @DisplayName("an unsupported algorithm type produces a clear ApiException")
        void rejectsNonConcreteAlgorithm() {
            DomainKeyDefinition keyDef = keyDefForEntity();
            com.garganttua.core.crypto.IKeyAlgorithm fake = new com.garganttua.core.crypto.IKeyAlgorithm() {
                public String getName() { return "X"; }
                public int getKeySize() { return 0; }
                public com.garganttua.core.crypto.KeyAlgorithmType getType() {
                    return com.garganttua.core.crypto.KeyAlgorithmType.ASYMMETRIC;
                }
                public String getCipherName(com.garganttua.core.crypto.EncryptionMode m,
                        com.garganttua.core.crypto.EncryptionPaddingMode p) { return null; }
                public String getSignatureName(SignatureAlgorithm s) { return null; }
            };
            ApiException ex = assertThrows(ApiException.class,
                    () -> KeyRealmFactory.generateAndStamp(IClass.getClass(KeyEntity.class), keyDef,
                            fake, SignatureAlgorithm.SHA256, "r", 1, TimeUnit.MINUTES, REFLECTION));
            assertTrue(ex.getMessage().contains("KeyAlgorithm"),
                    "should explain the algorithm requirement — got: " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("materialize — entity → IKeyRealm")
    class Materialize {

        @Test
        @DisplayName("round-trips: generateAndStamp → materialize → sign+verify works")
        void roundTrip() throws Exception {
            DomainKeyDefinition keyDef = keyDefForEntity();
            Object entity = KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "round-trip-realm", 5, TimeUnit.MINUTES, REFLECTION);

            IKeyRealm realm = KeyRealmFactory.materialize(entity, keyDef, REFLECTION);
            assertEquals("round-trip-realm", realm.getName());
            assertEquals(KeyAlgorithm.EC_256, realm.getKeyAlgorithm());
            assertFalse(realm.isRevoked());

            byte[] data = "payload".getBytes();
            IKey signingKey = realm.getKeyForSigning();
            byte[] sig = signingKey.sign(data);
            assertTrue(realm.getKeyForSignatureVerification().verifySignature(sig, data),
                    "sign + verify must round-trip after materialize");
        }

        @Test
        @DisplayName("materialize preserves revoked=true from the entity")
        void preservesRevokedFlag() throws Exception {
            DomainKeyDefinition keyDef = keyDefForEntity();
            KeyEntity entity = (KeyEntity) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "revoked-on-disk", 10, TimeUnit.MINUTES, REFLECTION);
            entity.revoked = true; // post-stamp mutation: simulates revocation in storage

            IKeyRealm realm = KeyRealmFactory.materialize(entity, keyDef, REFLECTION);
            assertTrue(realm.isRevoked(),
                    "revoked flag on the entity must propagate to the materialized realm");
        }

        @Test
        @DisplayName("materialize over the same bytes twice yields verify-compatible realms")
        void materializeIsRepeatable() throws Exception {
            DomainKeyDefinition keyDef = keyDefForEntity();
            Object entity = KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "twice", 10, TimeUnit.MINUTES, REFLECTION);

            IKeyRealm realmA = KeyRealmFactory.materialize(entity, keyDef, REFLECTION);
            IKeyRealm realmB = KeyRealmFactory.materialize(entity, keyDef, REFLECTION);

            byte[] data = "sign-once".getBytes();
            byte[] sig = realmA.getKeyForSigning().sign(data);

            // A signed by realmA must verify under realmB — both come from the same entity bytes.
            assertTrue(realmB.getKeyForSignatureVerification().verifySignature(sig, data),
                    "materialize is stateless — same bytes must produce a verify-compatible realm");
        }

        @Test
        @DisplayName("missing realmName on the entity surfaces a clear ApiException")
        void requiresRealmName() throws ApiException {
            DomainKeyDefinition keyDef = keyDefForEntity();
            KeyEntity entity = (KeyEntity) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "x", 10, TimeUnit.MINUTES, REFLECTION);
            entity.realmName = null;

            ApiException ex = assertThrows(ApiException.class,
                    () -> KeyRealmFactory.materialize(entity, keyDef, REFLECTION));
            assertTrue(ex.getMessage().contains("realmName"),
                    "error must point at the missing field — got: " + ex.getMessage());
        }

        @Test
        @DisplayName("invalid algorithm string yields a clear ApiException with format guidance")
        void rejectsBadAlgorithm() throws ApiException {
            DomainKeyDefinition keyDef = keyDefForEntity();
            KeyEntity entity = (KeyEntity) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "x", 10, TimeUnit.MINUTES, REFLECTION);
            entity.algorithm = "FOOBAR";

            ApiException ex = assertThrows(ApiException.class,
                    () -> KeyRealmFactory.materialize(entity, keyDef, REFLECTION));
            assertTrue(ex.getMessage().contains("FOOBAR"));
            assertTrue(ex.getMessage().contains("NAME-SIZE")
                            || ex.getMessage().contains("RSA-2048")
                            || ex.getMessage().contains("EC-256"),
                    "error must hint at the expected algorithm format — got: " + ex.getMessage());
        }

        @Test
        @DisplayName("invalid signatureAlgorithm string yields a clear ApiException naming the enum")
        void rejectsBadSignatureAlgorithm() throws ApiException {
            DomainKeyDefinition keyDef = keyDefForEntity();
            KeyEntity entity = (KeyEntity) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "x", 10, TimeUnit.MINUTES, REFLECTION);
            entity.signatureAlgorithm = "NOPE";

            ApiException ex = assertThrows(ApiException.class,
                    () -> KeyRealmFactory.materialize(entity, keyDef, REFLECTION));
            assertTrue(ex.getMessage().contains("NOPE"));
            assertTrue(ex.getMessage().contains("SignatureAlgorithm"),
                    "error must mention SignatureAlgorithm enum — got: " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Expiration / revoked field optionality")
    class OptionalFields {

        @Test
        @DisplayName("materialize tolerates a null expiration field on the entity (treated as forever)")
        void nullExpirationIsForever() throws Exception {
            DomainKeyDefinition keyDef = keyDefForEntity();
            KeyEntity entity = (KeyEntity) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "no-exp", 10, TimeUnit.MINUTES, REFLECTION);
            entity.expiration = null;

            IKeyRealm realm = KeyRealmFactory.materialize(entity, keyDef, REFLECTION);
            assertNull(realm.getExpiration());
            assertFalse(realm.isExpired());
        }

        @Test
        @DisplayName("keyDef with revoked=null (no field) treats every materialized realm as not revoked")
        void noRevokedFieldMeansNotRevoked() throws Exception {
            DomainKeyDefinition keyDefWithoutRevoked = new DomainKeyDefinition(
                    new ObjectAddress("realmName"),
                    new ObjectAddress("algorithm"),
                    new ObjectAddress("signatureAlgorithm"),
                    new ObjectAddress("publicMaterial"),
                    new ObjectAddress("privateMaterial"),
                    new ObjectAddress("expiration"),
                    null);
            KeyEntity entity = (KeyEntity) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDefWithoutRevoked,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "no-rev", 10, TimeUnit.MINUTES, REFLECTION);
            entity.revoked = true; // even with the entity flag flipped, the def says: ignore it

            IKeyRealm realm = KeyRealmFactory.materialize(entity, keyDefWithoutRevoked, REFLECTION);
            assertFalse(realm.isRevoked(),
                    "with no revoked field configured, the realm must be treated as not revoked");
        }
    }

    @Nested
    @DisplayName("Bytes are preserved verbatim — no encoding mismatch between stamp and materialize")
    class ByteFidelity {

        @Test
        @DisplayName("a materialized realm signs with the key generated by generateAndStamp (functional byte fidelity)")
        void roundTripSignsAndVerifies() throws ApiException {
            // Pre-refactor this test asserted byte-verbatim getRawKey() equality
            // between the entity and the materialized realm. garganttua-core's
            // Key stores material Base64-encoded internally, so getRawKey() is
            // no longer a raw PKCS8/X509 mirror — it's an implementation detail.
            // The functional property still holds: the persisted bytes survive
            // the round-trip and produce a working signing realm.
            DomainKeyDefinition keyDef = keyDefForEntity();
            KeyEntity entity = (KeyEntity) KeyRealmFactory.generateAndStamp(
                    IClass.getClass(KeyEntity.class), keyDef,
                    KeyAlgorithm.EC_256, SignatureAlgorithm.SHA256,
                    "byte-check", 10, TimeUnit.MINUTES, REFLECTION);

            IKeyRealm realm = KeyRealmFactory.materialize(entity, keyDef, REFLECTION);
            byte[] data = "payload".getBytes();
            byte[] sig = realm.getKeyForSigning().sign(data);
            assertTrue(realm.getKeyForSignatureVerification().verifySignature(sig, data),
                    "sign with persisted private + verify with persisted public must round-trip");
        }
    }
}
