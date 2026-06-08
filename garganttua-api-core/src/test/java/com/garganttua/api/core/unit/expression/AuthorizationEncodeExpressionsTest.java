package com.garganttua.api.core.unit.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainSecurityDefinition;
import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.core.reflection.ObjectAddress;

@DisplayName("SecurityExpressions — encode authorization (Phase 3)")
class AuthorizationEncodeExpressionsTest {

    public static class TokenEntity {
        public String uuid = "uuid-1";
        public String tokenType = "auth-token";

        public String encodeForWire() {
            return tokenType + "." + uuid;
        }
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

        when(authzDef.encodeMethod()).thenReturn(new ObjectAddress("encodeForWire"));
    }

    @Nested
    @DisplayName("hasEncodeMethod")
    class HasEncode {

        @Test
        @DisplayName("returns true when encode method is configured")
        void trueWhenConfigured() {
            assertTrue(SecurityExpressions.hasEncodeMethod(domain));
        }

        @Test
        @DisplayName("returns false when encodeMethod() is null")
        void falseWhenAbsent() {
            when(authzDef.encodeMethod()).thenReturn(null);
            assertFalse(SecurityExpressions.hasEncodeMethod(domain));
        }

        @Test
        @DisplayName("returns false when no security def is configured")
        void falseWhenNoSecDef() {
            when(domDef.domainSecurityDefinition()).thenReturn(null);
            assertFalse(SecurityExpressions.hasEncodeMethod(domain));
        }
    }

    @Nested
    @DisplayName("encodeAuthorization")
    class Encode {

        @Test
        @DisplayName("invokes the entity's declared encode method and returns its result")
        void invokesMethod() {
            TokenEntity entity = new TokenEntity();
            entity.uuid = "abc";
            entity.tokenType = "bearer";
            assertEquals("bearer.abc", SecurityExpressions.encodeAuthorization(entity, domain));
        }

        @Test
        @DisplayName("rejects null arguments")
        void rejectsNull() {
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.encodeAuthorization(null, domain));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.encodeAuthorization(new TokenEntity(), null));
        }

        @Test
        @DisplayName("throws ApiException when no encode method is configured")
        void throwsWhenNotConfigured() {
            when(authzDef.encodeMethod()).thenReturn(null);
            ApiException ex = assertThrows(ApiException.class,
                    () -> SecurityExpressions.encodeAuthorization(new TokenEntity(), domain));
            assertTrue(ex.getMessage().contains("no encode method"));
        }
    }

    @Nested
    @DisplayName("encodeIfPossible")
    class EncodeIfPossible {

        @Test
        @DisplayName("returns the encoded form when the method is configured")
        void encodesWhenConfigured() {
            TokenEntity entity = new TokenEntity();
            assertEquals("auth-token.uuid-1", SecurityExpressions.encodeIfPossible(entity, domain));
        }

        @Test
        @DisplayName("returns null when no encode method is configured (no-op)")
        void nullWhenAbsent() {
            when(authzDef.encodeMethod()).thenReturn(null);
            assertNull(SecurityExpressions.encodeIfPossible(new TokenEntity(), domain));
        }

        @Test
        @DisplayName("rejects null arguments")
        void rejectsNull() {
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.encodeIfPossible(null, domain));
            assertThrows(ApiException.class,
                    () -> SecurityExpressions.encodeIfPossible(new TokenEntity(), null));
        }
    }
}
