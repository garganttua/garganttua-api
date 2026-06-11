package com.garganttua.api.core.integ.securityscan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;
import com.garganttua.api.commons.dto.annotations.Dto;
import com.garganttua.api.commons.dto.annotations.DtoId;
import com.garganttua.api.commons.dto.annotations.DtoTenantId;
import com.garganttua.api.commons.dto.annotations.DtoUuid;
import com.garganttua.api.commons.entity.annotations.Entity;
import com.garganttua.api.commons.entity.annotations.EntityId;
import com.garganttua.api.commons.entity.annotations.EntityOwned;
import com.garganttua.api.commons.entity.annotations.EntityOwner;
import com.garganttua.api.commons.entity.annotations.EntitySuperOwner;
import com.garganttua.api.commons.entity.annotations.EntitySuperTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenant;
import com.garganttua.api.commons.entity.annotations.EntityTenantId;
import com.garganttua.api.commons.entity.annotations.EntityUuid;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticationAuthenticate;
import com.garganttua.api.commons.security.annotations.Authenticator;
import com.garganttua.api.commons.security.annotations.AuthenticatorEnabled;
import com.garganttua.api.commons.security.annotations.AuthenticatorLogin;
import com.garganttua.api.commons.security.annotations.Authorization;
import com.garganttua.api.commons.security.annotations.AuthorizationAuthorities;
import com.garganttua.api.commons.security.annotations.AuthorizationExpiration;
import com.garganttua.api.commons.security.annotations.AuthorizationRevoked;
import com.garganttua.api.commons.security.annotations.AuthorizationType;
import com.garganttua.api.commons.security.authenticator.AuthenticatorScope;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.core.integ.crud.AbstractCrudScriptTest;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("Annotation-driven security: scanner builds the full chain without DSL")
class AnnotationDrivenSecurityIntegrationTest extends AbstractCrudScriptTest {

    // ───── @Authentication ─────

    @Authentication
    public static class AnnoLoginPasswordAuth {
        @AuthenticationAuthenticate
        public IAuthentication authenticate(Object principal, byte[] credentials, IAuthenticatorDefinition definition) {
            String password = new String(credentials, StandardCharsets.UTF_8);
            boolean ok = "valid-password".equals(password);
            return new com.garganttua.api.commons.security.authentication.Authentication(
                    ok,
                    ok ? principal : null,
                    credentials,
                    ok ? "anno-token" : null,
                    ok ? List.of("ROLE_USER") : null,
                    null, null, false, false, // tenantId, ownerId, isSuperTenant, isSuperOwner
                    true, true, true, true);
        }
    }

    // ───── Token self-verification authentication (token IS an authenticator) ─────

    @Authentication
    public static class AnnoTokenAuth {
        @AuthenticationAuthenticate
        public IAuthentication authenticate(Object principal, Object credentials, IAuthenticatorDefinition definition) {
            // Token self-validation hook (signature/custom). Expiration + revocation
            // are enforced by the framework before this runs; here we accept.
            return new com.garganttua.api.commons.security.authentication.Authentication(
                    true, principal, credentials, "anno-token", List.of(),
                    null, null, false, false, true, true, true, true);
        }
    }

    // ───── @Authorization linked to AnnoUser; AnnoToken is also its own @Authenticator ─────

    @Entity
    @EntityTenant
    @EntityOwned(ownerId = "ownerId")
    @Authorization
    @Authenticator(authentications = AnnoTokenAuth.class, scope = AuthenticatorScope.tenant)
    public static class AnnoToken {
        @EntityId private String id;
        @EntityUuid @AuthenticatorLogin private String uuid;
        @EntityTenantId private String tenantId;
        @EntitySuperTenant private Boolean superTenant;
        private String ownerId;
        @AuthorizationType private String tokenType;
        @AuthorizationAuthorities private List<String> authorities;
        @AuthorizationExpiration private Instant expiresAt;
        @AuthorizationRevoked private Boolean revoked;
        private Instant createdAt;

        public AnnoToken() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String v) { this.tenantId = v; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean v) { this.superTenant = v; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String v) { this.ownerId = v; }
        public String getTokenType() { return tokenType; }
        public void setTokenType(String v) { this.tokenType = v; }
        public List<String> getAuthorities() { return authorities; }
        public void setAuthorities(List<String> v) { this.authorities = v; }
        public Instant getExpiresAt() { return expiresAt; }
        public void setExpiresAt(Instant v) { this.expiresAt = v; }
        public Boolean getRevoked() { return revoked; }
        public void setRevoked(Boolean v) { this.revoked = v; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant v) { this.createdAt = v; }
    }

    @Dto(entityClass = AnnoToken.class)
    public static class AnnoTokenDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        @DtoTenantId private String tenantId;
        private Boolean superTenant;
        public AnnoTokenDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String v) { this.tenantId = v; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean v) { this.superTenant = v; }
    }

    // ───── @Authenticator linked to AnnoToken authorization + AnnoLoginPasswordAuth ─────

    @Entity
    @EntityTenant
    @EntityOwner(ownerId = "uuid")
    @Authenticator(
            authorization = AnnoToken.class,
            authentications = AnnoLoginPasswordAuth.class,
            scope = AuthenticatorScope.tenant,
            authorizationLifeTime = 30
    )
    public static class AnnoUser {
        @EntityId @AuthenticatorLogin private String id;
        @EntityUuid private String uuid;
        @EntityTenantId private String tenantId;
        @EntitySuperTenant private Boolean superTenant;
        @EntitySuperOwner private Boolean superOwner;
        @AuthenticatorEnabled private Boolean enabled = true;
        private String name;

        public AnnoUser() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String v) { this.tenantId = v; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean v) { this.superTenant = v; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean v) { this.superOwner = v; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean v) { this.enabled = v; }
        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
    }

    @Dto(entityClass = AnnoUser.class)
    public static class AnnoUserDto {
        @DtoId private String id;
        @DtoUuid private String uuid;
        @DtoTenantId private String tenantId;
        private Boolean superTenant;
        private Boolean superOwner;
        private String name;
        public AnnoUserDto() {}
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String v) { this.tenantId = v; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean v) { this.superTenant = v; }
        public Boolean getSuperOwner() { return superOwner; }
        public void setSuperOwner(Boolean v) { this.superOwner = v; }
        public String getName() { return name; }
        public void setName(String v) { this.name = v; }
    }

    private IApi api;
    private IDomain<?> userDomain;
    private CapturingDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();
        CapturingDao tokenDao = new CapturingDao();

        IApiBuilder builder = newBuilder();
        ((com.garganttua.api.core.api.ApiBuilder) builder)
                .withPackage("com.garganttua.api.core.integ.securityscan");
        ((com.garganttua.core.dsl.IAutomaticBuilder<?, ?>) builder).autoDetect(true);

        // Pre-wire DAOs (DAO injection via annotation is out of scope for Phase 4).
        // The scanner re-enters these builders, so the DAO sticks.
        builder.domain(IClass.getClass(AnnoUser.class))
                .dto(IClass.getClass(AnnoUserDto.class))
                    .db(userDao)
                .up().up();
        builder.domain(IClass.getClass(AnnoToken.class))
                .dto(IClass.getClass(AnnoTokenDto.class))
                    .db(tokenDao)
                .up().up();

        api = buildAndStart(builder);
        userDomain = api.getDomain("annousers").orElseThrow();

        AnnoUserDto existingUser = new AnnoUserDto();
        existingUser.setId("alice@example.com");
        existingUser.setUuid("alice-uuid");
        existingUser.setTenantId("SUPER_TENANT");
        existingUser.setName("Alice");
        userDao.save(existingUser);
    }

    @Nested
    @DisplayName("Scanner wires the security chain into the domain definitions")
    class StaticConfig {

        @Test
        @DisplayName("AnnoUser domain has an authenticator definition with scope=tenant and login field set")
        void authenticatorDefRegistered() {
            IDomainSecurityDefinition secDef = userDomain.getDomainDefinition() instanceof
                    com.garganttua.api.core.domain.DomainDefinition<?> d
                    ? d.domainSecurityDefinition() : null;
            assertNotNull(secDef);
            IAuthenticatorDefinition authDef = secDef.authenticatorDefinition();
            assertNotNull(authDef, "@Authenticator should produce an authenticator definition");
            assertEquals(AuthenticatorScope.tenant, authDef.scope());
            assertNotNull(authDef.login(), "@AuthenticatorLogin field should be configured");
        }

        @Test
        @DisplayName("AnnoToken domain has an authorization definition with type/authorities/expiration/revoked fields set")
        void authorizationDefRegistered() {
            IDomain<?> tokenDomain = api.getDomain("annotokens").orElseThrow();
            IDomainSecurityDefinition secDef = tokenDomain.getDomainDefinition() instanceof
                    com.garganttua.api.core.domain.DomainDefinition<?> d
                    ? d.domainSecurityDefinition() : null;
            assertNotNull(secDef);
            IDomainAuthorizationDefinition authzDef = secDef.authorizationDefinition();
            assertNotNull(authzDef, "@Authorization should produce an authorization definition");
            assertNotNull(authzDef.type(), "@AuthorizationType field should be configured");
            assertNotNull(authzDef.authorities(), "@AuthorizationAuthorities field should be configured");
            assertNotNull(authzDef.expiration(), "@AuthorizationExpiration field should be configured");
            assertNotNull(authzDef.revoked(), "@AuthorizationRevoked field should be configured");
        }

        @Test
        @DisplayName("Authenticator is linked to its Authentication strategy and to the Authorization domain")
        void linkagesEstablished() {
            IDomainSecurityDefinition secDef = ((com.garganttua.api.core.domain.DomainDefinition<?>)
                    userDomain.getDomainDefinition()).domainSecurityDefinition();
            IAuthenticatorDefinition authDef = secDef.authenticatorDefinition();
            assertNotNull(authDef.authenticationDefinitions());
            assertTrue(!authDef.authenticationDefinitions().isEmpty(),
                    "@Authenticator.authentications must register at least one IAuthentication strategy");
            assertNotNull(authDef.authorizationDefinition(),
                    "@Authenticator.authorization must produce a linked authorization configuration");
            assertEquals(30, authDef.authorizationDefinition().duration(),
                    "authorization lifeTime carried over from @Authenticator.authorizationLifeTime");
        }
    }

    @Nested
    @DisplayName("End-to-end: login produces a token via the scanned chain")
    class EndToEnd {

        @Test
        @DisplayName("authenticate workflow returns a fully populated AnnoToken (no DSL security wiring used)")
        void authenticateWorks() throws ApiException {
            AuthenticationRequest req = new AuthenticationRequest(
                    "alice@example.com", "valid-password".getBytes(StandardCharsets.UTF_8));
            OperationRequest request = superTenantScriptRequest(
                    OperationDefinition.authenticate("annousers", IClass.getClass(AnnoUser.class)));
            request.arg("entity", req);

            WorkflowResult result = executeScript(userDomain, request);
            assertEquals(0, result.code(), "annotation-only login should reach code 0");

            assertInstanceOf(AnnoToken.class, result.output());
            AnnoToken token = (AnnoToken) result.output();
            assertNotNull(token.getUuid(), "token uuid was populated by createAuthorizationEntity2");
            assertEquals("annousers:alice-uuid", token.getOwnerId(), "ownerId set from principal uuid (qualified ${domainName}:${uuid})");
            assertEquals("SUPER_TENANT", token.getTenantId(), "tenantId propagated");
            assertEquals("anno-token", token.getTokenType(), "tokenType from authentication.authorization");
            assertEquals(List.of("ROLE_USER"), token.getAuthorities());
            assertEquals(false, token.getRevoked());
            assertNotNull(token.getExpiresAt(), "expiration set based on .authorizationLifeTime=30");
        }

        @Test
        @DisplayName("wrong password is rejected with 401")
        void wrongPasswordRefused() throws ApiException {
            AuthenticationRequest req = new AuthenticationRequest(
                    "alice@example.com", "bad-password".getBytes(StandardCharsets.UTF_8));
            OperationRequest request = superTenantScriptRequest(
                    OperationDefinition.authenticate("annousers", IClass.getClass(AnnoUser.class)));
            request.arg("entity", req);

            WorkflowResult result = executeScript(userDomain, request);
            assertEquals(401, result.code());
        }
    }
}
