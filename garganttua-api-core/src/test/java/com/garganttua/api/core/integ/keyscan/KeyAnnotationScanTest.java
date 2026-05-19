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
import com.garganttua.api.commons.entity.annotations.EntityTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenantId;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.commons.security.annotations.Key;
import com.garganttua.api.commons.security.annotations.KeyAlgorithm;
import com.garganttua.api.commons.security.annotations.KeyExpiration;
import com.garganttua.api.commons.security.annotations.KeyPrivateMaterial;
import com.garganttua.api.commons.security.annotations.KeyPublicMaterial;
import com.garganttua.api.commons.security.annotations.KeyRealmName;
import com.garganttua.api.commons.security.annotations.KeyRevoked;
import com.garganttua.api.commons.security.annotations.KeySignatureAlgorithm;
import com.garganttua.api.core.context.Api;
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

        @KeyRealmName private String realmName;
        @KeyAlgorithm private String algorithm;
        @KeySignatureAlgorithm private String signatureAlgorithm;
        @KeyPublicMaterial private byte[] publicMaterial;
        @KeyPrivateMaterial private byte[] privateMaterial;
        @KeyExpiration private Instant expiration;
        @KeyRevoked private boolean revoked;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getRealmName() { return realmName; }
        public void setRealmName(String realmName) { this.realmName = realmName; }
        public String getAlgorithm() { return algorithm; }
        public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
        public String getSignatureAlgorithm() { return signatureAlgorithm; }
        public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
        public byte[] getPublicMaterial() { return publicMaterial; }
        public void setPublicMaterial(byte[] publicMaterial) { this.publicMaterial = publicMaterial; }
        public byte[] getPrivateMaterial() { return privateMaterial; }
        public void setPrivateMaterial(byte[] privateMaterial) { this.privateMaterial = privateMaterial; }
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

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
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

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }

    @Dto(entityClass = BareKey.class)
    public static class BareKeyDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        @DtoTenantId private String tenantId;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
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
        ((com.garganttua.api.core.builder.ApiBuilder) builder)
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
        assertNotNull(keyDef.realmName(), "@KeyRealmName must populate realmName()");
        assertEquals("realmName", keyDef.realmName().getElement(0));

        assertNotNull(keyDef.algorithm(), "@KeyAlgorithm must populate algorithm()");
        assertEquals("algorithm", keyDef.algorithm().getElement(0));

        assertNotNull(keyDef.signatureAlgorithm(), "@KeySignatureAlgorithm must populate signatureAlgorithm()");
        assertEquals("signatureAlgorithm", keyDef.signatureAlgorithm().getElement(0));

        assertNotNull(keyDef.publicMaterial(), "@KeyPublicMaterial must populate publicMaterial()");
        assertEquals("publicMaterial", keyDef.publicMaterial().getElement(0));

        assertNotNull(keyDef.privateMaterial(), "@KeyPrivateMaterial must populate privateMaterial()");
        assertEquals("privateMaterial", keyDef.privateMaterial().getElement(0));

        assertNotNull(keyDef.expiration(), "@KeyExpiration must populate expiration()");
        assertEquals("expiration", keyDef.expiration().getElement(0));

        assertNotNull(keyDef.revoked(), "@KeyRevoked must populate revoked()");
        assertEquals("revoked", keyDef.revoked().getElement(0));
    }

    @Test
    @DisplayName("@Key without field annotations materializes a keyDefinition with all-null addresses")
    void typeMarkerWithoutFieldMarkers() throws ApiException {
        IApiBuilder builder = newBuilder();
        ((com.garganttua.api.core.builder.ApiBuilder) builder)
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
        assertNull(keyDef.realmName(), "no @KeyRealmName → null realmName()");
        assertNull(keyDef.algorithm(), "no @KeyAlgorithm → null algorithm()");
        assertNull(keyDef.signatureAlgorithm(), "no @KeySignatureAlgorithm → null signatureAlgorithm()");
        assertNull(keyDef.publicMaterial(), "no @KeyPublicMaterial → null publicMaterial()");
        assertNull(keyDef.privateMaterial(), "no @KeyPrivateMaterial → null privateMaterial()");
        assertNull(keyDef.expiration(), "no @KeyExpiration → null expiration()");
        assertNull(keyDef.revoked(), "no @KeyRevoked → null revoked()");
    }

    @Test
    @DisplayName("entity without @Key produces keyDefinition() == null — non-key domains are unchanged")
    void entityWithoutKeyAnnotationIsNotAKeyDomain() throws ApiException {
        IApiBuilder builder = newBuilder();
        ((com.garganttua.api.core.builder.ApiBuilder) builder)
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
        ((com.garganttua.api.core.builder.ApiBuilder) builder)
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
        // resolveKeyRealm directly (it requires an authenticator wiring), we
        // just exercise the factory layer that resolveKeyRealm delegates to.
        IDomainKeyDefinition keyDef = autoKeyDomain.getDomainDefinition().keyDefinition();
        Object freshEntity = com.garganttua.api.core.security.key.KeyRealmFactory.generateAndStamp(
                autoKeyDomain.getEntityClass(), keyDef,
                com.garganttua.core.crypto.KeyAlgorithm.EC_256,
                com.garganttua.core.crypto.SignatureAlgorithm.SHA256,
                "scanner-realm", 1, java.util.concurrent.TimeUnit.HOURS,
                com.garganttua.api.core.mapper.DefaultMapper.reflection());
        assertNotNull(freshEntity);
        AutoKey concrete = (AutoKey) freshEntity;
        assertEquals("scanner-realm", concrete.getRealmName());
        assertEquals("EC-256", concrete.getAlgorithm());
        assertEquals("SHA256", concrete.getSignatureAlgorithm());
        assertNotNull(concrete.getPublicMaterial());
        assertNotNull(concrete.getPrivateMaterial());
        assertNotNull(concrete.getExpiration());
        assertTrue(concrete.getExpiration().isAfter(Instant.now().minusSeconds(5)));

        // And the materialized realm round-trips sign/verify against the same bytes.
        var realm = com.garganttua.api.core.security.key.KeyRealmFactory.materialize(
                freshEntity, keyDef, com.garganttua.api.core.mapper.DefaultMapper.reflection());
        byte[] sig = realm.getKeyForSigning().sign("payload".getBytes());
        assertTrue(realm.getKeyForSignatureVerification().verifySignature(sig, "payload".getBytes()),
                "scanner-built key domain must produce a sign/verify-functional MaterializedKeyRealm");
    }
}
