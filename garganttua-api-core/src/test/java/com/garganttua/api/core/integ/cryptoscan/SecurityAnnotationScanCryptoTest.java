package com.garganttua.api.core.integ.cryptoscan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.dto.annotations.Dto;
import com.garganttua.api.commons.dto.annotations.DtoId;
import com.garganttua.api.commons.dto.annotations.DtoUuid;
import com.garganttua.api.commons.entity.annotations.Entity;
import com.garganttua.api.commons.entity.annotations.EntityId;
import com.garganttua.api.commons.entity.annotations.EntityOwned;
import com.garganttua.api.commons.entity.annotations.EntityOwner;
import com.garganttua.api.commons.entity.annotations.EntitySuperOwner;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticationAuthenticate;
import com.garganttua.api.commons.security.annotations.Authenticator;
import com.garganttua.api.commons.security.annotations.AuthenticatorAlwaysEnabled;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.commons.security.annotations.AuthenticatorLogin;
import com.garganttua.api.commons.security.annotations.Authorization;
import com.garganttua.api.commons.security.annotations.AuthorizationExpiration;
import com.garganttua.api.commons.security.annotations.AuthorizationType;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.reflection.IClass;

@DisplayName("@Authenticator crypto params propagate through the scanner onto the key definition")
class SecurityAnnotationScanCryptoTest extends AbstractCrudIntegrationTest {

    @Authentication
    public static class CryptoAuth {
        @AuthenticationAuthenticate
        public IAuthentication authenticate(Object principal, byte[] credentials) {
            return null;
        }
    }

    @Entity
    @EntityOwned(ownerId = "ownerId")
    @Authorization
    @Authenticator(authentications = CryptoAuth.class, scope = AuthenticatorScope.tenant)
    public static class CryptoToken {
        @EntityId private String id;
        @EntityUuid @AuthenticatorLogin private String uuid;
        private String ownerId;
        @AuthorizationType private String tokenType;
        @AuthorizationExpiration private java.time.Instant expiresAt;
        public CryptoToken() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String v) { this.ownerId = v; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String v) { this.tokenType = v; }
        public java.time.Instant getExpiresAt() { return expiresAt; }
        public void setExpiresAt(java.time.Instant v) { this.expiresAt = v; }
    }

    @Dto(entityClass = CryptoToken.class)
    public static class CryptoTokenDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        public CryptoTokenDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
    }

    // Plain key-storage entity referenced by @Authenticator.authorizationKey
    @Entity
    public static class CryptoKey {
        @EntityId private String id;
        @EntityUuid private String uuid;
        public CryptoKey() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
    }

    @Dto(entityClass = CryptoKey.class)
    public static class CryptoKeyDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        public CryptoKeyDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
    }

    @Entity
    @EntityOwner(ownerId = "uuid")
    @Authenticator(
            authorization = CryptoToken.class,
            authentications = CryptoAuth.class,
            authorizationKey = CryptoKey.class,
            authorizationKeyUsage = AuthenticatorKeyUsage.oneForAll,
            authorizationSignatureAlgorithm = SignatureAlgorithm.SHA256,
            authorizationKeyLifeTime = 90,
            authorizationKeyLifeTimeUnit = TimeUnit.SECONDS,
            scope = AuthenticatorScope.system
    )
    @AuthenticatorAlwaysEnabled
    public static class CryptoUser {
        @EntityId @AuthenticatorLogin private String id;
        @EntityUuid private String uuid;
        @EntitySuperOwner private Boolean superOwner;
        public CryptoUser() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean superOwner) { this.superOwner = superOwner; }
    }

    @Dto(entityClass = CryptoUser.class)
    public static class CryptoUserDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        private Boolean superOwner;
        public CryptoUserDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean superOwner) { this.superOwner = superOwner; }
    }

    /**
     * Force-loads every fixture class before the first test runs. The
     * Reflections backend used by the framework's scanner discovers classes
     * via the classloader; on a cold JVM the first scan can miss classes
     * that haven't been loaded yet, which made the first-running test fail
     * intermittently. Touching the classes here guarantees they're loaded.
     */
    /**
     * Runs once before any @Test method. Builds and discards a throw-away API
     * so the framework's Reflections-based scanner warms up its caches /
     * classpath URL set on the fresh JVM. Without this, the very first @Test
     * that runs in this class would hit a cold scanner that misses some of
     * the fixture classes (intermittent: depended on JVM/loader ordering).
     * Subsequent test methods then see a fully populated scanner.
     */
    @BeforeAll
    static void warmUpReflectionsScanner() throws Exception {
        IApiBuilder warmup = newBaseBuilder();
        warmup.multiTenant(false);
        ((com.garganttua.api.core.api.ApiBuilder) warmup)
                .withPackage("com.garganttua.api.core.integ.cryptoscan");
        ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) warmup).autoDetect(true);
        warmup.domain(IClass.getClass(CryptoUser.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(CryptoUserDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        warmup.domain(IClass.getClass(CryptoToken.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(CryptoTokenDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        warmup.domain(IClass.getClass(CryptoKey.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(CryptoKeyDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        warmup.domain(IClass.getClass(PlainUser.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(PlainUserDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        warmup.domain(IClass.getClass(PlainToken.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(PlainTokenDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        try {
            buildAndStart(warmup);
        } catch (Throwable ignored) {
            // Best-effort warmup — any failure is recoverable; per-test buildApi()
            // builds a fresh context.
        }
    }

    private IApi buildApi() throws ApiException {
        IApiBuilder builder = newBaseBuilder();
        builder.multiTenant(false);
        ((com.garganttua.api.core.api.ApiBuilder) builder)
                .withPackage("com.garganttua.api.core.integ.cryptoscan");
        ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);

        // Pre-wire DAOs + id/uuid — the EntityAnnotationScanner re-enters the
        // builders so the DAOs and identity fields stick. Wiring identity here
        // is belt-and-braces: it shields the test from any Reflections-cache
        // edge case where the @DtoUuid lookup is racy.
        builder.domain(IClass.getClass(CryptoUser.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(CryptoUserDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        builder.domain(IClass.getClass(CryptoToken.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(CryptoTokenDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        builder.domain(IClass.getClass(CryptoKey.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(CryptoKeyDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        builder.domain(IClass.getClass(PlainUser.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(PlainUserDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();
        builder.domain(IClass.getClass(PlainToken.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(PlainTokenDto.class)).id("id").uuid("uuid").db(new CapturingDao()).up()
                .up();

        return buildAndStart(builder);
    }

    private IDomainAuthenticatorAuthorizationKeyDefinition keyDef(IApi api) {
        IDomain<?> userDomain = api.getDomain("cryptousers").orElseThrow();
        var secDef = ((com.garganttua.api.core.domain.DomainDefinition<?>) userDomain.getDomainDefinition())
                .domainSecurityDefinition();
        assertNotNull(secDef, "user domain should have a security definition");
        assertNotNull(secDef.authenticatorDefinition(), "scanner should produce an authenticator definition");
        IDomainAuthenticatorAuthorizationDefinition authzAuth = secDef.authenticatorDefinition().authorizationDefinition();
        assertNotNull(authzAuth, "@Authenticator.authorization should produce an authorization config");
        return authzAuth.keyDefinition();
    }

    @Nested
    @DisplayName("Scanner wires the key sub-builder when authorizationKey is declared")
    class KeyDeclared {

        @Test
        @DisplayName("usage from the annotation is carried onto the key definition")
        void usageIsPropagated() throws ApiException {
            IApi api = buildApi();
            IDomainAuthenticatorAuthorizationKeyDefinition def = keyDef(api);
            assertNotNull(def, "authorizationKey class was declared → key definition should be wired");
            assertEquals(AuthenticatorKeyUsage.oneForAll, def.usage(),
                    "usage from @Authenticator.authorizationKeyUsage must reach the definition");
        }

        @Test
        @DisplayName("signatureAlgorithm from the annotation is carried onto the key definition")
        void signatureAlgorithmIsPropagated() throws ApiException {
            IApi api = buildApi();
            IDomainAuthenticatorAuthorizationKeyDefinition def = keyDef(api);
            assertEquals(SignatureAlgorithm.SHA256, def.signatureAlgorithm(),
                    "signatureAlgorithm from @Authenticator must reach the definition");
        }

        @Test
        @DisplayName("lifeTime + unit are carried onto the key definition")
        void lifeTimeIsPropagated() throws ApiException {
            IApi api = buildApi();
            IDomainAuthenticatorAuthorizationKeyDefinition def = keyDef(api);
            assertEquals(90, def.duration(),
                    "authorizationKeyLifeTime must reach the definition");
            assertEquals(TimeUnit.SECONDS, def.unit(),
                    "authorizationKeyLifeTimeUnit must reach the definition");
        }

        @Test
        @DisplayName("algorithm (IKeyAlgorithm instance) stays null when only the name is given")
        void algorithmStaysNullWithoutRegistry() throws ApiException {
            IApi api = buildApi();
            IDomainAuthenticatorAuthorizationKeyDefinition def = keyDef(api);
            assertNull(def.algorithm(),
                    "the scanner does not resolve the algorithm name → the DSL still needs to wire it");
        }

        @Test
        @DisplayName("@AuthenticatorAlwaysEnabled marker is carried onto the authenticator definition")
        void alwaysEnabledMarkerIsPropagated() throws ApiException {
            IApi api = buildApi();
            IDomain<?> userDomain = api.getDomain("cryptousers").orElseThrow();
            var secDef = ((com.garganttua.api.core.domain.DomainDefinition<?>) userDomain.getDomainDefinition())
                    .domainSecurityDefinition();
            assertEquals(true, secDef.authenticatorDefinition().alwaysEnabled(),
                    "@AuthenticatorAlwaysEnabled on the type should flip alwaysEnabled to true");
        }
    }

    @Authentication
    public static class PlainAuth {
        @AuthenticationAuthenticate
        public IAuthentication authenticate(Object principal, byte[] credentials) { return null; }
    }

    @Entity
    @EntityOwned(ownerId = "ownerId")
    @Authorization
    @Authenticator(authentications = PlainAuth.class, scope = AuthenticatorScope.tenant)
    public static class PlainToken {
        @EntityId private String id;
        @EntityUuid @AuthenticatorLogin private String uuid;
        private String ownerId;
        @AuthorizationType private String tokenType;
        public PlainToken() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String v) { this.ownerId = v; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String v) { this.tokenType = v; }
    }

    @Dto(entityClass = PlainToken.class)
    public static class PlainTokenDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        public PlainTokenDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
    }

    @Entity
    @EntityOwner(ownerId = "uuid")
    @Authenticator(
            authorization = PlainToken.class,
            authentications = PlainAuth.class,
            scope = AuthenticatorScope.system
    )
    public static class PlainUser {
        @EntityId @AuthenticatorLogin private String id;
        @EntityUuid private String uuid;
        @EntitySuperOwner private Boolean superOwner;
        public PlainUser() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean superOwner) { this.superOwner = superOwner; }
    }

    @Dto(entityClass = PlainUser.class)
    public static class PlainUserDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        private Boolean superOwner;
        public PlainUserDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean superOwner) { this.superOwner = superOwner; }
    }

    @Nested
    @DisplayName("No key wiring when authorizationKey is not declared")
    class NoKeyDeclared {

        @Test
        @DisplayName("no key class declared → keyDefinition stays null")
        void keyDefinitionIsNull() throws ApiException {
            // Scanner walks the same crypto sub-package, which also exposes the
            // CryptoUser fixture. Both authenticators get registered side by side;
            // we only inspect the PlainUser one here.
            IApi api = buildApi();
            IDomain<?> userDomain = api.getDomain("plainusers").orElseThrow();
            var secDef = ((com.garganttua.api.core.domain.DomainDefinition<?>) userDomain.getDomainDefinition())
                    .domainSecurityDefinition();
            assertNull(secDef.authenticatorDefinition().authorizationDefinition().keyDefinition(),
                    "without authorizationKey, the key sub-builder should not be created");
        }
    }
}
