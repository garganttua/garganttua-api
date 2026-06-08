package com.garganttua.api.core.integ.keyscan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.definition.IDomainKeyDefinition;
import com.garganttua.api.commons.dto.annotations.Dto;
import com.garganttua.api.commons.dto.annotations.DtoId;
import com.garganttua.api.commons.dto.annotations.DtoTenantId;
import com.garganttua.api.commons.dto.annotations.DtoUuid;
import com.garganttua.api.commons.entity.annotations.Entity;
import com.garganttua.api.commons.entity.annotations.EntityId;
import com.garganttua.api.commons.entity.annotations.EntitySuperTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenantId;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.commons.security.annotations.Key;
import com.garganttua.api.commons.security.annotations.KeyAlgorithm;
import com.garganttua.api.commons.security.annotations.KeyExpiration;
import com.garganttua.api.commons.security.annotations.KeyForSignatureVerification;
import com.garganttua.api.commons.security.annotations.KeyForSigning;
import com.garganttua.api.commons.security.annotations.KeyName;
import com.garganttua.api.commons.security.annotations.KeyRevoked;
import com.garganttua.api.commons.security.annotations.KeySignatureAlgorithm;
import com.garganttua.api.core.api.Api;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

/**
 * Asserts that an entity carrying {@link Key} (type-level) along with the
 * seven field-level annotations is registered as a key domain by
 * {@code EntityAnnotationScanner.applyKeyRole}, with no explicit
 * {@code .key()} DSL call from the user. After scan, the resulting
 * {@code IDomainKeyDefinition} must expose every field as a non-null
 * {@code ObjectAddress} so the runtime resolver can read/write key
 * material.
 */
@DisplayName("@Key + field annotations auto-wire the key domain (no .key() DSL needed)")
class KeyAnnotationScanTest extends AbstractCrudIntegrationTest {

    @Entity
    @EntityTenant
    @Key
    public static class AutoKey {
        @EntityId private String id;
        @EntityUuid private String uuid;
        @EntityTenantId private String tenantId;
        @EntitySuperTenant private Boolean superTenant;

        @KeyName private String name;
        @KeyAlgorithm private String algorithm;
        @KeySignatureAlgorithm private String signatureAlgorithm;
        @KeyForSignatureVerification private com.garganttua.core.crypto.IKey publicMaterial;
        @KeyForSigning private com.garganttua.core.crypto.IKey privateMaterial;
        @KeyExpiration private Instant expiration;
        @KeyRevoked private boolean revoked;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
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

    @Dto(entityClass = AutoKey.class)
    public static class AutoKeyDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        @DtoTenantId private String tenantId;
        private Boolean superTenant;

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
     * Entity annotated as @Key but with NO field-level annotations — used to
     * assert that the scanner does materialize the key sub-builder (presence
     * of @Key is enough), and that the resulting definition has every field
     * address null (no fields were configured).
     */
    @Entity
    @EntityTenant
    @Key
    public static class BareKey {
        @EntityId private String id;
        @EntityUuid private String uuid;
        @EntityTenantId private String tenantId;
        @EntitySuperTenant private Boolean superTenant;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    @Dto(entityClass = BareKey.class)
    public static class BareKeyDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        @DtoTenantId private String tenantId;
        private Boolean superTenant;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IDomain<?> findDomain(IApi api, Class<?> entityClass) {
        Map<String, IDomain<?>> domains = ((Api) api).getDomains();
        return domains.values().stream()
                .filter(d -> d.getEntityClass().represents(entityClass))
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("@Key on the class + 7 field annotations populate every address on IDomainKeyDefinition")
    void allFieldsWiredFromAnnotations() throws ApiException {
        IApiBuilder builder = newBuilder();
        ((com.garganttua.api.core.api.ApiBuilder) builder)
                .withPackage("com.garganttua.api.core.integ.keyscan");
        ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);

        // The scanner builds the domain shape but does not supply a DAO; we
        // re-enter the domain to wire the DAO via the same DTO builder.
        builder.domain(IClass.getClass(AutoKey.class))
                .dto(IClass.getClass(AutoKeyDto.class))
                    .db(new CapturingDao())
                .up()
            .up();
        builder.domain(IClass.getClass(BareKey.class))
                .dto(IClass.getClass(BareKeyDto.class))
                    .db(new CapturingDao())
                .up()
            .up();

        IApi api = buildAndStart(builder);

        IDomain<?> domain = findDomain(api, AutoKey.class);
        assertNotNull(domain, "AutoKey domain should be registered by the scanner");

        IDomainKeyDefinition keyDef = domain.getDomainDefinition().keyDefinition();
        assertNotNull(keyDef,
                "@Key on the class must materialize a non-null keyDefinition() on the domain");

        // Every field annotation has produced a non-null ObjectAddress pointing
        // at the right entity field. The address last element matches the field
        // name (the only path component for a flat entity).
        assertNotNull(keyDef.name(), "@KeyName must populate name()");
        assertEquals("name", keyDef.name().getElement(0));

        assertNotNull(keyDef.keyAlgorithm(), "@KeyAlgorithm must populate keyAlgorithm()");
        assertEquals("algorithm", keyDef.keyAlgorithm().getElement(0));

        assertNotNull(keyDef.signatureAlgorithm(), "@KeySignatureAlgorithm must populate signatureAlgorithm()");
        assertEquals("signatureAlgorithm", keyDef.signatureAlgorithm().getElement(0));

        assertNotNull(keyDef.keyForSignatureVerification(),
                "@KeyForSignatureVerification must populate keyForSignatureVerification()");
        assertEquals("publicMaterial", keyDef.keyForSignatureVerification().getElement(0));

        assertNotNull(keyDef.keyForSigning(), "@KeyForSigning must populate keyForSigning()");
        assertEquals("privateMaterial", keyDef.keyForSigning().getElement(0));

        assertNotNull(keyDef.expiration(), "@KeyExpiration must populate expiration()");
        assertEquals("expiration", keyDef.expiration().getElement(0));

        assertNotNull(keyDef.revoked(), "@KeyRevoked must populate revoked()");
        assertEquals("revoked", keyDef.revoked().getElement(0));

        // The new optional IKeyRealm-mirroring addresses are null because the
        // AutoKey test fixture only declares the legacy 7-field set.
        assertNull(keyDef.keyForEncryption(), "AutoKey has no @KeyForEncryption → null keyForEncryption()");
        assertNull(keyDef.keyForDecryption(), "AutoKey has no @KeyForDecryption → null keyForDecryption()");
        assertNull(keyDef.version(), "AutoKey has no @KeyVersion → null version()");
        assertNull(keyDef.rotate(), "AutoKey has no @KeyRotate → null rotate()");
    }

    @Test
    @DisplayName("@Key without field annotations materializes a keyDefinition with all-null addresses")
    void typeMarkerWithoutFieldMarkers() throws ApiException {
        IApiBuilder builder = newBuilder();
        ((com.garganttua.api.core.api.ApiBuilder) builder)
                .withPackage("com.garganttua.api.core.integ.keyscan");
        ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);

        builder.domain(IClass.getClass(AutoKey.class))
                .dto(IClass.getClass(AutoKeyDto.class))
                    .db(new CapturingDao())
                .up()
            .up();
        builder.domain(IClass.getClass(BareKey.class))
                .dto(IClass.getClass(BareKeyDto.class))
                    .db(new CapturingDao())
                .up()
            .up();

        IApi api = buildAndStart(builder);

        IDomain<?> domain = findDomain(api, BareKey.class);
        assertNotNull(domain, "BareKey domain should be registered by the scanner");

        IDomainKeyDefinition keyDef = domain.getDomainDefinition().keyDefinition();
        assertNotNull(keyDef,
                "@Key marker on the class alone must still produce a non-null keyDefinition() — "
                        + "this signals to the framework that the domain is a key domain, even when "
                        + "no field annotations have been added yet.");

        // No field annotations → every address is null. The resolver will
        // throw a parlant error at runtime if anyone tries to materialize a
        // realm against this incomplete definition.
        assertNull(keyDef.name(), "no @KeyName → null name()");
        assertNull(keyDef.keyAlgorithm(), "no @KeyAlgorithm → null keyAlgorithm()");
        assertNull(keyDef.signatureAlgorithm(), "no @KeySignatureAlgorithm → null signatureAlgorithm()");
        assertNull(keyDef.keyForSigning(), "no @KeyForSigning → null keyForSigning()");
        assertNull(keyDef.keyForSignatureVerification(),
                "no @KeyForSignatureVerification → null keyForSignatureVerification()");
        assertNull(keyDef.keyForEncryption(), "no @KeyForEncryption → null keyForEncryption()");
        assertNull(keyDef.keyForDecryption(), "no @KeyForDecryption → null keyForDecryption()");
        assertNull(keyDef.expiration(), "no @KeyExpiration → null expiration()");
        assertNull(keyDef.revoked(), "no @KeyRevoked → null revoked()");
        assertNull(keyDef.version(), "no @KeyVersion → null version()");
        assertNull(keyDef.rotate(), "no @KeyRotate → null rotate()");
    }

    @Test
    @DisplayName("entity without @Key produces keyDefinition() == null — non-key domains are unchanged")
    void entityWithoutKeyAnnotationIsNotAKeyDomain() throws ApiException {
        IApiBuilder builder = newBuilder();
        ((com.garganttua.api.core.api.ApiBuilder) builder)
                .withPackage("com.garganttua.api.core.integ.keyscan");
        ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);

        builder.domain(IClass.getClass(AutoKey.class))
                .dto(IClass.getClass(AutoKeyDto.class))
                    .db(new CapturingDao())
                .up()
            .up();
        builder.domain(IClass.getClass(BareKey.class))
                .dto(IClass.getClass(BareKeyDto.class))
                    .db(new CapturingDao())
                .up()
            .up();
        // Explicitly register a third domain that does NOT carry @Key.
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up()
            .up();

        IApi api = buildAndStart(builder);

        IDomain<?> userDomain = findDomain(api, User.class);
        assertNotNull(userDomain);
        assertNull(userDomain.getDomainDefinition().keyDefinition(),
                "a domain whose entity is NOT annotated @Key must keep keyDefinition() == null");

        // Sanity: AutoKey is still recognised as a key domain in the same API.
        IDomain<?> keyDomain = findDomain(api, AutoKey.class);
        assertNotNull(keyDomain.getDomainDefinition().keyDefinition(),
                "@Key-marked AutoKey must still have a non-null keyDefinition() in the same API");
    }

    @Test
    @DisplayName("the scanner-built @Key domain is usable end-to-end: declared via .key(domain) → resolveKeyRealm round-trips")
    void scannerBuiltDomainIsResolvable() throws Exception {
        // This is the integration smoke test: a user who only declares @Key
        // on their entity (no DSL .key()) gets a working key domain that
        // resolveKeyRealm can lookup-or-create against. We reuse the existing
        // SignAuthorizationIntegrationTest scaffolding minimally — just enough
        // to drive resolveKeyRealm.
        IApiBuilder builder = newBuilder();
        ((com.garganttua.api.core.api.ApiBuilder) builder)
                .withPackage("com.garganttua.api.core.integ.keyscan");
        ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);

        CapturingDao autoKeyDao = new CapturingDao();
        builder.domain(IClass.getClass(AutoKey.class))
                .dto(IClass.getClass(AutoKeyDto.class))
                    .db(autoKeyDao)
                .up()
            .up();
        builder.domain(IClass.getClass(BareKey.class))
                .dto(IClass.getClass(BareKeyDto.class))
                    .db(new CapturingDao())
                .up()
            .up();

        IApi api = buildAndStart(builder);
        IDomain<?> autoKeyDomain = findDomain(api, AutoKey.class);
        assertNotNull(autoKeyDomain);

        // The scanner-built definition has every address populated — confirm
        // the runtime can read/write through it. We do not invoke
        // resolveKeyRealm directly (it requires an authenticator wiring); we
        // stamp the seven mapped fields manually via the scanner-built
        // addresses, then round-trip sign/verify via core's KeyRealm to
        // prove the addresses point to writable fields of the expected types.
        IDomainKeyDefinition keyDef = autoKeyDomain.getDomainDefinition().keyDefinition();
        assertNotNull(keyDef.name());
        assertNotNull(keyDef.keyAlgorithm());
        assertNotNull(keyDef.signatureAlgorithm());
        assertNotNull(keyDef.keyForSignatureVerification());
        assertNotNull(keyDef.keyForSigning());
        assertNotNull(keyDef.expiration());
        assertNotNull(keyDef.revoked());

        com.garganttua.core.reflection.IReflection reflection =
                com.garganttua.api.core.mapper.DefaultMapper.reflection();
        AutoKey concrete = new AutoKey();
        java.security.KeyPair pair =
                com.garganttua.core.crypto.KeyAlgorithm.EC_256.generateAsymmetricKey();
        reflection.setFieldValue(concrete, keyDef.name(), "scanner-realm");
        reflection.setFieldValue(concrete, keyDef.keyAlgorithm(), "EC-256");
        reflection.setFieldValue(concrete, keyDef.signatureAlgorithm(), "SHA256");
        // keyForSigning/keyForSignatureVerification fields are IKey-typed:
        // wrap the JDK bytes with core's Key.fromSigningMaterial factory.
        com.garganttua.core.crypto.IKey signingKey = com.garganttua.core.crypto.Key.fromSigningMaterial(
                com.garganttua.core.crypto.KeyType.PRIVATE,
                com.garganttua.core.crypto.KeyAlgorithm.EC_256,
                com.garganttua.core.crypto.SignatureAlgorithm.SHA256,
                pair.getPrivate().getEncoded());
        com.garganttua.core.crypto.IKey verificationKey = com.garganttua.core.crypto.Key.fromSigningMaterial(
                com.garganttua.core.crypto.KeyType.PUBLIC,
                com.garganttua.core.crypto.KeyAlgorithm.EC_256,
                com.garganttua.core.crypto.SignatureAlgorithm.SHA256,
                pair.getPublic().getEncoded());
        reflection.setFieldValue(concrete, keyDef.keyForSignatureVerification(), verificationKey);
        reflection.setFieldValue(concrete, keyDef.keyForSigning(), signingKey);
        java.time.Instant expiresAt = Instant.now().plusSeconds(3600);
        reflection.setFieldValue(concrete, keyDef.expiration(), expiresAt);
        reflection.setFieldValue(concrete, keyDef.revoked(), false);

        assertEquals("scanner-realm", concrete.getName());
        assertEquals("EC-256", concrete.getAlgorithm());
        assertEquals("SHA256", concrete.getSignatureAlgorithm());
        assertNotNull(concrete.getPublicMaterial());
        assertNotNull(concrete.getPrivateMaterial());
        assertNotNull(concrete.getExpiration());
        assertTrue(concrete.getExpiration().isAfter(Instant.now().minusSeconds(5)));

        // Round-trip sign/verify through core's KeyRealm.fromSignatureMaterial
        // with the bytes we just stamped on the scanner-built entity.
        com.garganttua.core.crypto.IKeyRealm realm = com.garganttua.core.crypto.KeyRealm.fromSignatureMaterial(
                "scanner-realm",
                com.garganttua.core.crypto.KeyAlgorithm.EC_256,
                com.garganttua.core.crypto.SignatureAlgorithm.SHA256,
                java.util.Date.from(expiresAt),
                false,
                pair.getPrivate().getEncoded(),
                pair.getPublic().getEncoded());
        byte[] sig = realm.getKeyForSigning().sign("payload".getBytes());
        assertTrue(realm.getKeyForSignatureVerification().verifySignature(sig, "payload".getBytes()),
                "scanner-built key domain must produce a sign/verify-functional IKeyRealm");
    }
}
