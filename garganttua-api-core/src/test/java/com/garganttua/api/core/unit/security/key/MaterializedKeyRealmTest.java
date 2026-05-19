package com.garganttua.api.core.unit.security.key;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.key.MaterializedKey;
import com.garganttua.api.core.security.key.MaterializedKeyRealm;
import com.garganttua.core.crypto.CryptoException;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.KeyType;
import com.garganttua.core.crypto.SignatureAlgorithm;

@DisplayName("MaterializedKey / MaterializedKeyRealm — sign/verify path over persisted bytes")
class MaterializedKeyRealmTest {

    /** Generates an EC_256 pair and wraps each half in a MaterializedKey. */
    private MaterializedKeyRealm freshRealm(String name, Date expiration, boolean revoked) {
        KeyPair pair = KeyAlgorithm.EC_256.generateAsymmetricKey();
        MaterializedKey privateKey = new MaterializedKey(KeyType.PRIVATE, KeyAlgorithm.EC_256,
                SignatureAlgorithm.SHA256, pair.getPrivate().getEncoded());
        MaterializedKey publicKey = new MaterializedKey(KeyType.PUBLIC, KeyAlgorithm.EC_256,
                SignatureAlgorithm.SHA256, pair.getPublic().getEncoded());
        return new MaterializedKeyRealm(name, KeyAlgorithm.EC_256, expiration, revoked, privateKey, publicKey);
    }

    @Nested
    @DisplayName("MaterializedKey.sign / verifySignature")
    class SignVerify {

        @Test
        @DisplayName("sign then verify with the same realm returns true")
        void signThenVerifyRoundTrips() throws Exception {
            MaterializedKeyRealm realm = freshRealm("kr-1", null, false);
            byte[] data = "hello there".getBytes();

            byte[] signature = realm.getKeyForSigning().sign(data);

            assertNotNull(signature);
            assertTrue(signature.length > 0, "signature must not be empty");
            assertTrue(realm.getKeyForSignatureVerification().verifySignature(signature, data),
                    "self-produced signature must verify");
        }

        @Test
        @DisplayName("verifying a tampered payload returns false (signature/data mismatch)")
        void tamperedDataFailsVerify() throws Exception {
            MaterializedKeyRealm realm = freshRealm("kr-2", null, false);
            byte[] data = "original".getBytes();
            byte[] tampered = "0riginal".getBytes(); // flipped O→0

            byte[] signature = realm.getKeyForSigning().sign(data);
            assertFalse(realm.getKeyForSignatureVerification().verifySignature(signature, tampered),
                    "signature must not verify against tampered data");
        }

        @Test
        @DisplayName("a signature from realm A does not verify under realm B")
        void crossRealmSignatureFails() throws Exception {
            MaterializedKeyRealm realmA = freshRealm("kr-a", null, false);
            MaterializedKeyRealm realmB = freshRealm("kr-b", null, false);
            byte[] data = "cross-realm".getBytes();

            byte[] sigFromA = realmA.getKeyForSigning().sign(data);

            // Each realm has its own randomly generated keypair, so verify under B must fail.
            assertFalse(realmB.getKeyForSignatureVerification().verifySignature(sigFromA, data));
        }

        @Test
        @DisplayName("calling sign() on a PUBLIC-typed MaterializedKey throws CryptoException")
        void publicKeyCannotSign() {
            KeyPair pair = KeyAlgorithm.EC_256.generateAsymmetricKey();
            MaterializedKey publicKey = new MaterializedKey(KeyType.PUBLIC, KeyAlgorithm.EC_256,
                    SignatureAlgorithm.SHA256, pair.getPublic().getEncoded());

            CryptoException ex = assertThrows(CryptoException.class, () -> publicKey.sign("x".getBytes()));
            assertTrue(ex.getMessage().contains("PRIVATE"),
                    "error must mention the required PRIVATE type — got: " + ex.getMessage());
        }

        @Test
        @DisplayName("calling verifySignature() on a PRIVATE-typed MaterializedKey throws CryptoException")
        void privateKeyCannotVerify() {
            KeyPair pair = KeyAlgorithm.EC_256.generateAsymmetricKey();
            MaterializedKey privateKey = new MaterializedKey(KeyType.PRIVATE, KeyAlgorithm.EC_256,
                    SignatureAlgorithm.SHA256, pair.getPrivate().getEncoded());

            CryptoException ex = assertThrows(CryptoException.class,
                    () -> privateKey.verifySignature("sig".getBytes(), "data".getBytes()));
            assertTrue(ex.getMessage().contains("PUBLIC"),
                    "error must mention the required PUBLIC type — got: " + ex.getMessage());
        }

        @Test
        @DisplayName("encrypt / decrypt surface explicit CryptoException — sign/verify only realm")
        void encryptionRejected() {
            MaterializedKeyRealm realm = freshRealm("kr-3", null, false);
            assertThrows(CryptoException.class, () -> realm.getKeyForEncryption());
            assertThrows(CryptoException.class, () -> realm.getKeyForDecryption());
        }
    }

    @Nested
    @DisplayName("Realm envelope: name / algorithm / expiration / revoked / version")
    class Envelope {

        @Test
        @DisplayName("getName returns the configured name")
        void exposesName() {
            MaterializedKeyRealm realm = freshRealm("my-realm-X", null, false);
            assertEquals("my-realm-X", realm.getName());
        }

        @Test
        @DisplayName("getKeyAlgorithm returns the configured algorithm (no copy, same reference)")
        void exposesAlgorithm() {
            MaterializedKeyRealm realm = freshRealm("alg", null, false);
            assertSame(KeyAlgorithm.EC_256, realm.getKeyAlgorithm());
        }

        @Test
        @DisplayName("getVersion is fixed to 1 — rotation is delegated to the @Key entity domain")
        void versionIsOne() {
            MaterializedKeyRealm realm = freshRealm("v", null, false);
            assertEquals(1, realm.getVersion());
        }

        @Test
        @DisplayName("expiration in the past is reported as expired")
        void pastExpirationIsExpired() {
            Date past = new Date(System.currentTimeMillis() - 60_000L);
            MaterializedKeyRealm realm = freshRealm("past", past, false);
            assertTrue(realm.isExpired(), "key with past expiration must be expired");
        }

        @Test
        @DisplayName("expiration in the future is reported as not expired")
        void futureExpirationIsNotExpired() {
            Date future = new Date(System.currentTimeMillis() + 60_000L);
            MaterializedKeyRealm realm = freshRealm("future", future, false);
            assertFalse(realm.isExpired(), "key with future expiration must not be expired");
        }

        @Test
        @DisplayName("null expiration is treated as never expiring")
        void nullExpirationIsNotExpired() {
            MaterializedKeyRealm realm = freshRealm("forever", null, false);
            assertFalse(realm.isExpired(), "null expiration must be treated as 'never expires'");
        }

        @Test
        @DisplayName("revoking a realm makes subsequent getKeyForSigning fail with CryptoException")
        void revokedRealmRejectsSigning() {
            MaterializedKeyRealm realm = freshRealm("revoke-me", null, false);
            assertFalse(realm.isRevoked(), "freshly built realm must not be revoked");
            realm.revoke();
            assertTrue(realm.isRevoked());

            CryptoException ex = assertThrows(CryptoException.class, () -> realm.getKeyForSigning());
            assertTrue(ex.getMessage().contains("revoked"),
                    "error must mention revocation — got: " + ex.getMessage());
        }

        @Test
        @DisplayName("a realm constructed with revoked=true rejects signing immediately")
        void preRevokedRealm() {
            MaterializedKeyRealm realm = freshRealm("born-revoked", null, true);
            assertTrue(realm.isRevoked());
            assertThrows(CryptoException.class, realm::getKeyForSigning);
        }

        @Test
        @DisplayName("getExpiration returns a defensive copy — mutating the returned Date does not mutate internal state")
        void expirationDefensiveCopy() {
            Date original = new Date(1_700_000_000_000L);
            MaterializedKeyRealm realm = freshRealm("copy", original, false);
            Date returned = realm.getExpiration();
            returned.setTime(0L);
            assertEquals(1_700_000_000_000L, realm.getExpiration().getTime(),
                    "internal expiration must be insulated from caller mutation");
        }
    }

    @Nested
    @DisplayName("getRawKey / getKey")
    class RawAndJdkKey {

        @Test
        @DisplayName("getRawKey returns the original bytes (defensive copy — mutation does not leak)")
        void rawKeyDefensiveCopy() {
            KeyPair pair = KeyAlgorithm.EC_256.generateAsymmetricKey();
            byte[] originalBytes = pair.getPrivate().getEncoded();
            MaterializedKey key = new MaterializedKey(KeyType.PRIVATE, KeyAlgorithm.EC_256,
                    SignatureAlgorithm.SHA256, originalBytes);

            byte[] returned = key.getRawKey();
            assertArrayEquals(originalBytes, returned);

            // Mutate the returned copy
            if (returned.length > 0) returned[0] = (byte) ~returned[0];
            // Internal state must remain intact — re-read still matches the original
            assertArrayEquals(originalBytes, key.getRawKey(),
                    "getRawKey must return a defensive copy");
        }

        @Test
        @DisplayName("getKey reconstructs a JDK Key matching the declared algorithm")
        void getKeyReconstructsJdkKey() throws Exception {
            KeyPair pair = KeyAlgorithm.EC_256.generateAsymmetricKey();
            MaterializedKey privateKey = new MaterializedKey(KeyType.PRIVATE, KeyAlgorithm.EC_256,
                    SignatureAlgorithm.SHA256, pair.getPrivate().getEncoded());

            java.security.Key jdkKey = privateKey.getKey();
            assertNotNull(jdkKey);
            assertEquals("EC", jdkKey.getAlgorithm());
            assertTrue(jdkKey instanceof java.security.PrivateKey,
                    "PRIVATE-typed MaterializedKey must reconstruct a java.security.PrivateKey");
        }

        @Test
        @DisplayName("malformed key bytes produce a CryptoException at sign time, not a NullPointerException")
        void malformedBytesFailExplicitly() {
            byte[] garbage = new byte[]{0x01, 0x02, 0x03, 0x04};
            MaterializedKey privateKey = new MaterializedKey(KeyType.PRIVATE, KeyAlgorithm.EC_256,
                    SignatureAlgorithm.SHA256, garbage);

            CryptoException ex = assertThrows(CryptoException.class,
                    () -> privateKey.sign("data".getBytes()));
            assertNotNull(ex.getMessage(), "malformed-bytes error must carry a message");
        }
    }

    @Nested
    @DisplayName("getKeyForSigning vs getKeyForSignatureVerification")
    class KeyDispatch {

        @Test
        @DisplayName("getKeyForSigning returns the configured PRIVATE half")
        void signingIsPrivate() throws CryptoException {
            MaterializedKeyRealm realm = freshRealm("dispatch", null, false);
            IKey signingKey = realm.getKeyForSigning();
            assertEquals(KeyType.PRIVATE, signingKey.getType());
        }

        @Test
        @DisplayName("getKeyForSignatureVerification returns the configured PUBLIC half")
        void verificationIsPublic() throws CryptoException {
            MaterializedKeyRealm realm = freshRealm("dispatch", null, false);
            IKey verifyKey = realm.getKeyForSignatureVerification();
            assertEquals(KeyType.PUBLIC, verifyKey.getType());
        }
    }

    @Nested
    @DisplayName("Rotation is not supported on materialized realms")
    class Rotation {

        @Test
        @DisplayName("rotate() throws UnsupportedOperationException with a pointer to the @Key entity flow")
        void rotateRefuses() {
            MaterializedKeyRealm realm = freshRealm("nope", null, false);
            UnsupportedOperationException ex = assertThrows(UnsupportedOperationException.class, realm::rotate);
            assertTrue(ex.getMessage().contains("@Key entity"),
                    "rotation error must point users to the @Key entity flow — got: " + ex.getMessage());
        }
    }
}
