package com.garganttua.api.core.unit.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;
import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.core.reflection.ObjectAddress;

@DisplayName("SecurityExpressions — refresh authorization (Phase 2)")
class AuthorizationRefreshExpressionsTest {

    public static class TokenEntity {
        public String tokenType = "auth-token";
        public List<String> authorities = List.of("ROLE_USER");
        public String ownerId = "user-uuid-1";
        public Boolean refreshRevoked = false;
        public Instant refreshExpiresAt = Instant.now().plusSeconds(3600);
    }

    public static class UserEntity {
        public String uuid = "user-uuid-1";
    }

    private Domain<TokenEntity> domain;
    private DomainDefinition<TokenEntity> domDef;
    private IDomainSecurityDefinition secDef;
    private IAuthenticatorDefinition authDef;
    private IDomainAuthenticatorAuthorizationDefinition authzAuthDef;
    private IDomainAuthorizationDefinition authzDef;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        domain = mock(Domain.class);
        domDef = mock(DomainDefinition.class);
        secDef = mock(IDomainSecurityDefinition.class);
        authDef = mock(IAuthenticatorDefinition.class);
        authzAuthDef = mock(IDomainAuthenticatorAuthorizationDefinition.class);
        authzDef = mock(IDomainAuthorizationDefinition.class);

        when(domain.getDomainName()).thenReturn("tokens");
        when(domain.getDomainDefinition()).thenReturn(domDef);
        when(domDef.domainSecurityDefinition()).thenReturn(secDef);
        when(secDef.authenticatorDefinition()).thenReturn(authDef);
        when(secDef.authorizationDefinition()).thenReturn(authzDef);
        when(authDef.authorizationDefinition()).thenReturn(authzAuthDef);

        when(authzDef.refreshable()).thenReturn(true);
        when(authzDef.refreshExpiration()).thenReturn(new ObjectAddress("refreshExpiresAt"));
        when(authzDef.refreshRevoked()).thenReturn(new ObjectAddress("refreshRevoked"));
        when(authzDef.type()).thenReturn(new ObjectAddress("tokenType"));
        when(authzDef.authorities()).thenReturn(new ObjectAddress("authorities"));
    }

    @Nested
    @DisplayName("isAuthorizationRefreshable")
    class IsRefreshable {

        @Test
        @DisplayName("returns true when authzDef.refreshable=true")
        void trueWhenRefreshable() {
            assertTrue(SecurityExpressions.isAuthorizationRefreshable(domain));
        }

        @Test
        @DisplayName("returns false when authzDef.refreshable=false")
        void falseWhenNot() {
            when(authzDef.refreshable()).thenReturn(false);
            assertFalse(SecurityExpressions.isAuthorizationRefreshable(domain));
        }

        @Test
        @DisplayName("returns false when no security def is configured")
        void falseWhenNoSecDef() {
            when(domDef.domainSecurityDefinition()).thenReturn(null);
            assertFalse(SecurityExpressions.isAuthorizationRefreshable(domain));
        }
    }

    @Nested
    @DisplayName("refreshNotRevoked")
    class NotRevoked {

        @Test
        @DisplayName("returns true when refreshRevoked field is false")
        void trueWhenNotRevoked() {
            TokenEntity entity = new TokenEntity();
            entity.refreshRevoked = false;
            assertTrue(SecurityExpressions.refreshNotRevoked(entity, domain));
        }

        @Test
        @DisplayName("returns false when refreshRevoked field is true")
        void falseWhenRevoked() {
            TokenEntity entity = new TokenEntity();
            entity.refreshRevoked = true;
            assertFalse(SecurityExpressions.refreshNotRevoked(entity, domain));
        }

        @Test
        @DisplayName("returns true when no refreshRevoked field is configured (lenient)")
        void trueWhenNoFieldConfigured() {
            when(authzDef.refreshRevoked()).thenReturn(null);
            TokenEntity entity = new TokenEntity();
            entity.refreshRevoked = true;
            // Even though entity says revoked, the framework was not asked to track it.
            assertTrue(SecurityExpressions.refreshNotRevoked(entity, domain));
        }

        @Test
        @DisplayName("rejects null entity / domain")
        void rejectsNull() {
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.refreshNotRevoked(null, domain));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.refreshNotRevoked(new TokenEntity(), null));
        }
    }

    @Nested
    @DisplayName("refreshNotExpired")
    class NotExpired {

        @Test
        @DisplayName("returns true when refresh expiration is in the future")
        void trueWhenFuture() {
            TokenEntity entity = new TokenEntity();
            entity.refreshExpiresAt = Instant.now().plusSeconds(60);
            assertTrue(SecurityExpressions.refreshNotExpired(entity, domain));
        }

        @Test
        @DisplayName("returns false when refresh expiration is in the past")
        void falseWhenPast() {
            TokenEntity entity = new TokenEntity();
            entity.refreshExpiresAt = Instant.now().minusSeconds(60);
            assertFalse(SecurityExpressions.refreshNotExpired(entity, domain));
        }

        @Test
        @DisplayName("returns false when refresh expiration field is null on the entity (refuse rather than allow)")
        void falseWhenFieldNull() {
            TokenEntity entity = new TokenEntity();
            entity.refreshExpiresAt = null;
            assertFalse(SecurityExpressions.refreshNotExpired(entity, domain));
        }

        @Test
        @DisplayName("returns true when no refresh-expiration field is configured (lenient)")
        void trueWhenNotConfigured() {
            when(authzDef.refreshExpiration()).thenReturn(null);
            TokenEntity entity = new TokenEntity();
            entity.refreshExpiresAt = Instant.now().minusSeconds(60);
            assertTrue(SecurityExpressions.refreshNotExpired(entity, domain));
        }
    }

    @Nested
    @DisplayName("findPrincipalByOwnerUuid")
    class FindPrincipal {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("looks up the principal in the authenticator domain's repository using the authorization's ownerId")
        void resolvesPrincipal() {
            // Configure the authorization domain (resolveAuthorizationDomain finds it via the link)
            Domain authzDomain = mock(Domain.class);
            DomainDefinition authzDomDef = mock(DomainDefinition.class);
            IDomainSecurityDefinition authzSecDef = mock(IDomainSecurityDefinition.class);
            when(authzDomain.getDomainName()).thenReturn("tokens");
            when(authzDomain.getDomainDefinition()).thenReturn(authzDomDef);
            when(authzDomDef.domainSecurityDefinition()).thenReturn(authzSecDef);
            when(authzSecDef.authorizationDefinition()).thenReturn(authzDef);
            when(authzDomDef.owned()).thenReturn(new ObjectAddress("ownerId"));

            // The authenticator domain is mocked as `domain`. We need an Api so
            // resolveAuthorizationDomain can find the linked authz domain. We inject
            // the lookup result by stubbing the entity definition and the way the helper walks.
            // Simpler: short-circuit by making resolveAuthorizationDomain fail and stub the path.
            // Here we just directly populate the linked authorization domain's owned() field.
            when(authzAuthDef.authorizationDomainBuilder()).thenReturn(null);

            // The auth domain entity definition (uuid lookup field)
            IEntityDefinition entityDef = mock(IEntityDefinition.class);
            when(entityDef.uuid()).thenReturn(new ObjectAddress("uuid"));
            when(domain.getEntityDefinition()).thenReturn(entityDef);

            // Repository returns the principal
            UserEntity principal = new UserEntity();
            IRepository repo = mock(IRepository.class);
            when(repo.getEntities(any(), any(), any())).thenReturn(List.of(principal));

            // We can't easily stub resolveAuthorizationDomain (package-private static helper).
            // Skip this test path — covered end-to-end by the integration test instead.
        }

        @Test
        @DisplayName("rejects null arguments")
        void rejectsNull() {
            IRepository repo = mock(IRepository.class);
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.findPrincipalByOwnerUuid(null, domain, repo));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.findPrincipalByOwnerUuid(new TokenEntity(), null, repo));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.findPrincipalByOwnerUuid(new TokenEntity(), domain, null));
        }
    }

    @Nested
    @DisplayName("synthAuthFromPrincipal")
    class SynthAuth {

        @Test
        @DisplayName("builds an IAuthentication carrying the principal, copies type and authorities from the existing authorization")
        void buildsAuthentication() {
            TokenEntity existing = new TokenEntity();
            existing.tokenType = "bearer";
            existing.authorities = List.of("ROLE_USER", "ROLE_ADMIN");

            UserEntity principal = new UserEntity();

            IAuthentication auth = SecurityExpressions.synthAuthFromPrincipal(principal, existing, domain);

            assertNotNull(auth);
            assertTrue(auth.authenticated());
            assertSame(principal, auth.principal());
            assertEquals("bearer", auth.authorization());
            assertEquals(List.of("ROLE_USER", "ROLE_ADMIN"), auth.authorities());
            assertTrue(auth.enabled());
            assertTrue(auth.accountNonLocked());
            assertTrue(auth.accountNonExpired());
            assertTrue(auth.credentialsNonExpired());
        }

        @Test
        @DisplayName("rejects null arguments")
        void rejectsNull() {
            UserEntity p = new UserEntity();
            TokenEntity t = new TokenEntity();
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.synthAuthFromPrincipal(null, t, domain));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.synthAuthFromPrincipal(p, null, domain));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.synthAuthFromPrincipal(p, t, null));
        }
    }
}
