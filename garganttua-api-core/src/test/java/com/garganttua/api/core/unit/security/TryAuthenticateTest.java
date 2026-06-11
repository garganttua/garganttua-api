package com.garganttua.api.core.unit.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.definition.IAuthenticationDefinition;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.security.authentication.Authentication;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IMethodReturn;
import com.garganttua.core.reflection.binders.IContextualMethodBinder;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.runtime.RuntimeExpressionContext;

@DisplayName("tryAuthenticate expression tests")
class TryAuthenticateTest {

    static {
        try {
            var reflectionBuilder = ReflectionBuilder.builder()
                    .withProvider(new RuntimeReflectionProvider())
                    .withScanner(new ReflectionsAnnotationScanner());
            IClass.setReflection(reflectionBuilder.build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize reflection", e);
        }
    }

    // --- Helpers ---

    private IAuthenticatorDefinition authenticatorWith(List<IAuthenticationDefinition> authDefs) {
        IAuthenticatorDefinition def = mock(IAuthenticatorDefinition.class);
        doReturn(authDefs).when(def).authenticationDefinitions();
        return def;
    }

    @SuppressWarnings("unchecked")
    private IAuthenticationDefinition authDefWithBinder(IMethodBinder<?> binder) {
        IAuthenticationDefinition authDef = mock(IAuthenticationDefinition.class);
        doReturn(binder).when(authDef).authenticateMethodBinder();
        return authDef;
    }

    private IAuthenticationDefinition authDefWithNullBinder() {
        IAuthenticationDefinition authDef = mock(IAuthenticationDefinition.class);
        when(authDef.authenticateMethodBinder()).thenReturn(null);
        return authDef;
    }

    @SuppressWarnings("unchecked")
    private IMethodBinder<?> nonContextualBinderReturning(IAuthentication auth) {
        IMethodBinder<Object> binder = mock(IMethodBinder.class);
        IMethodReturn<Object> methodReturn = mock(IMethodReturn.class);
        when(methodReturn.single()).thenReturn(auth);
        when(binder.execute()).thenReturn(Optional.of(methodReturn));
        return binder;
    }

    @SuppressWarnings("unchecked")
    private IMethodBinder<?> nonContextualBinderReturningEmpty() {
        IMethodBinder<Object> binder = mock(IMethodBinder.class);
        when(binder.execute()).thenReturn(Optional.empty());
        return binder;
    }

    @SuppressWarnings("unchecked")
    private IContextualMethodBinder<?, Object> contextualBinderReturning(IAuthentication auth) {
        IContextualMethodBinder<Object, Object> binder = mock(IContextualMethodBinder.class);
        when(binder.getOwnerContextType()).thenReturn((IClass) IClass.getClass(IRuntimeContext.class));
        when(binder.getParametersContextTypes()).thenReturn(new IClass<?>[0]);
        IMethodReturn<Object> methodReturn = mock(IMethodReturn.class);
        when(methodReturn.single()).thenReturn(auth);
        when(binder.execute(any())).thenReturn(Optional.of(methodReturn));
        return binder;
    }

    @SuppressWarnings("unchecked")
    private IContextualMethodBinder<?, Object> contextualBinderReturningEmpty() {
        IContextualMethodBinder<Object, Object> binder = mock(IContextualMethodBinder.class);
        when(binder.getOwnerContextType()).thenReturn((IClass) IClass.getClass(IRuntimeContext.class));
        when(binder.getParametersContextTypes()).thenReturn(new IClass<?>[0]);
        when(binder.execute(any())).thenReturn(Optional.empty());
        return binder;
    }

    private Authentication successAuth() {
        return new Authentication(true, "principal", null, "token-123", List.of("ROLE_USER"),
                null, null, false, false, true, true, true, true);
    }

    private Authentication failedAuth() {
        return new Authentication(false, null, null, null, null,
                null, null, false, false, true, true, true, true);
    }

    // --- Tests ---

    @Nested
    @DisplayName("Null and empty checks")
    class NullAndEmptyChecks {

        @Test
        @DisplayName("throws when authenticatorDefinition is null")
        void throwsWhenNull() {
            assertThrows(ApiException.class, () -> SecurityExpressions.tryAuthenticate(null));
        }

        @Test
        @DisplayName("throws when no authentication methods configured")
        void throwsWhenNoAuthMethods() {
            IAuthenticatorDefinition def = authenticatorWith(List.of());
            assertThrows(ApiException.class, () -> SecurityExpressions.tryAuthenticate(def));
        }

        @Test
        @DisplayName("throws when authenticationDefinitions is null")
        void throwsWhenAuthDefsNull() {
            IAuthenticatorDefinition def = authenticatorWith(null);
            assertThrows(ApiException.class, () -> SecurityExpressions.tryAuthenticate(def));
        }
    }

    @Nested
    @DisplayName("Non-contextual binder")
    class NonContextualBinder {

        @Test
        @DisplayName("returns authentication result on success")
        void returnsAuthOnSuccess() {
            Authentication auth = successAuth();
            IMethodBinder<?> binder = nonContextualBinderReturning(auth);
            IAuthenticationDefinition authDef = authDefWithBinder(binder);
            IAuthenticatorDefinition def = authenticatorWith(List.of(authDef));

            Object result = SecurityExpressions.tryAuthenticate(def);

            assertInstanceOf(IAuthentication.class, result);
            IAuthentication returned = (IAuthentication) result;
            assertTrue(returned.authenticated());
            assertEquals("token-123", returned.authorization());
            assertEquals(List.of("ROLE_USER"), returned.authorities());
        }

        @Test
        @DisplayName("throws when authentication fails (authenticated=false)")
        void throwsWhenAuthFails() {
            Authentication auth = failedAuth();
            IMethodBinder<?> binder = nonContextualBinderReturning(auth);
            IAuthenticationDefinition authDef = authDefWithBinder(binder);
            IAuthenticatorDefinition def = authenticatorWith(List.of(authDef));

            assertThrows(ApiException.class, () -> SecurityExpressions.tryAuthenticate(def));
        }

        @Test
        @DisplayName("throws when binder returns empty result")
        void throwsWhenBinderReturnsEmpty() {
            IMethodBinder<?> binder = nonContextualBinderReturningEmpty();
            IAuthenticationDefinition authDef = authDefWithBinder(binder);
            IAuthenticatorDefinition def = authenticatorWith(List.of(authDef));

            assertThrows(ApiException.class, () -> SecurityExpressions.tryAuthenticate(def));
        }

        @Test
        @DisplayName("skips null binder and tries next")
        void skipsNullBinder() {
            Authentication auth = successAuth();
            IAuthenticationDefinition nullBinderDef = authDefWithNullBinder();
            IAuthenticationDefinition validDef = authDefWithBinder(nonContextualBinderReturning(auth));
            IAuthenticatorDefinition def = authenticatorWith(List.of(nullBinderDef, validDef));

            Object result = SecurityExpressions.tryAuthenticate(def);

            assertInstanceOf(IAuthentication.class, result);
            assertTrue(((IAuthentication) result).authenticated());
        }
    }

    @Nested
    @DisplayName("Contextual binder")
    class ContextualBinderTests {

        @Test
        @DisplayName("succeeds with IRuntimeContext-compatible owner type")
        void succeedsWithRuntimeContext() {
            // Migrated from the dropped set/clear pair to the ScopedValue-based
            // runIn binding (garganttua-core 2.0.0-ALPHA02 turned the runtime
            // context holder into a structured scope). Outer binding is
            // unbound automatically when the lambda returns.
            RuntimeExpressionContext.runIn(mock(IRuntimeContext.class), () -> {
                Authentication auth = successAuth();
                IContextualMethodBinder<?, Object> binder = contextualBinderReturning(auth);
                IAuthenticationDefinition authDef = authDefWithBinder(binder);
                IAuthenticatorDefinition def = authenticatorWith(List.of(authDef));

                Object result = SecurityExpressions.tryAuthenticate(def);

                assertInstanceOf(IAuthentication.class, result);
                assertTrue(((IAuthentication) result).authenticated());
            });
        }

        @Test
        @DisplayName("throws when contextual binder returns empty")
        void throwsWhenContextualBinderEmpty() {
            RuntimeExpressionContext.runIn(mock(IRuntimeContext.class), () -> {
                IContextualMethodBinder<?, Object> binder = contextualBinderReturningEmpty();
                IAuthenticationDefinition authDef = authDefWithBinder(binder);
                IAuthenticatorDefinition def = authenticatorWith(List.of(authDef));

                assertThrows(ApiException.class, () -> SecurityExpressions.tryAuthenticate(def));
            });
        }

        @Test
        @DisplayName("rejects binder with incompatible owner context type")
        @SuppressWarnings("unchecked")
        void rejectsIncompatibleOwnerContextType() {
            RuntimeExpressionContext.runIn(mock(IRuntimeContext.class), () -> {
                IContextualMethodBinder<Object, Object> binder = mock(IContextualMethodBinder.class);
                when(binder.getOwnerContextType()).thenReturn((IClass) IClass.getClass(String.class));
                when(binder.getParametersContextTypes()).thenReturn(new IClass<?>[0]);

                IAuthenticationDefinition authDef = authDefWithBinder(binder);
                IAuthenticatorDefinition def = authenticatorWith(List.of(authDef));

                // Should fail because String is not IRuntimeContext — caught and treated as failed attempt
                assertThrows(ApiException.class, () -> SecurityExpressions.tryAuthenticate(def));
            });
        }
    }

    @Nested
    @DisplayName("Cascade behavior")
    class CascadeBehavior {

        @Test
        @DisplayName("first method fails, second succeeds")
        void cascadeFallthrough() {
            Authentication failAuth = failedAuth();
            Authentication successAuth = successAuth();

            IMethodBinder<?> failBinder = nonContextualBinderReturning(failAuth);
            IMethodBinder<?> successBinder = nonContextualBinderReturning(successAuth);

            IAuthenticationDefinition failDef = authDefWithBinder(failBinder);
            IAuthenticationDefinition successDef = authDefWithBinder(successBinder);

            IAuthenticatorDefinition def = authenticatorWith(List.of(failDef, successDef));

            Object result = SecurityExpressions.tryAuthenticate(def);

            assertInstanceOf(IAuthentication.class, result);
            assertTrue(((IAuthentication) result).authenticated());
            assertEquals("token-123", ((IAuthentication) result).authorization());
        }

        @Test
        @DisplayName("all methods fail throws exception")
        void allMethodsFail() {
            Authentication failAuth = failedAuth();
            IMethodBinder<?> failBinder = nonContextualBinderReturning(failAuth);
            IAuthenticationDefinition failDef1 = authDefWithBinder(failBinder);
            IAuthenticationDefinition failDef2 = authDefWithBinder(failBinder);

            IAuthenticatorDefinition def = authenticatorWith(List.of(failDef1, failDef2));

            ApiException ex = assertThrows(ApiException.class, () -> SecurityExpressions.tryAuthenticate(def));
            assertTrue(ex.getMessage().contains("All authentication methods failed"));
        }

        @Test
        @DisplayName("exception in first method does not block second")
        @SuppressWarnings("unchecked")
        void exceptionInFirstDoesNotBlockSecond() {
            // First binder throws
            IMethodBinder<Object> throwingBinder = mock(IMethodBinder.class);
            when(throwingBinder.execute()).thenThrow(new RuntimeException("connection error"));
            IAuthenticationDefinition throwingDef = authDefWithBinder(throwingBinder);

            // Second binder succeeds
            Authentication auth = successAuth();
            IMethodBinder<?> successBinder = nonContextualBinderReturning(auth);
            IAuthenticationDefinition successDef = authDefWithBinder(successBinder);

            IAuthenticatorDefinition def = authenticatorWith(List.of(throwingDef, successDef));

            Object result = SecurityExpressions.tryAuthenticate(def);

            assertInstanceOf(IAuthentication.class, result);
            assertTrue(((IAuthentication) result).authenticated());
        }
    }
}
