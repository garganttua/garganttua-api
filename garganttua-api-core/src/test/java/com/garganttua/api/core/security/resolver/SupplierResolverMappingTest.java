package com.garganttua.api.core.security.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.core.injection.IElementResolver;
import com.garganttua.core.injection.Resolved;
import com.garganttua.core.reflection.IAnnotatedElement;
import com.garganttua.core.reflection.IClass;
import com.garganttua.api.core.security.authentication.ApiSupplierBuilder;
import com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder;
import com.garganttua.api.core.security.authentication.AuthenticatorDefinitionSupplierBuilder;
import com.garganttua.api.core.security.authentication.AuthoritiesSupplierBuilder;
import com.garganttua.api.core.security.authentication.AuthorizationSupplierBuilder;
import com.garganttua.api.core.security.authentication.CallerSupplierBuilder;
import com.garganttua.api.core.security.authentication.CredentialsSupplierBuilder;
import com.garganttua.api.core.security.authentication.DecodedAuthorizationSupplierBuilder;
import com.garganttua.api.core.security.authentication.DomainSupplierBuilder;
import com.garganttua.api.core.security.authentication.ExecutionUuidSupplierBuilder;
import com.garganttua.api.core.security.authentication.LoginSupplierBuilder;
import com.garganttua.api.core.security.authentication.OperationSupplierBuilder;
import com.garganttua.api.core.security.authentication.OwnerIdSupplierBuilder;
import com.garganttua.api.core.security.authentication.PrincipalSupplierBuilder;
import com.garganttua.api.core.security.authentication.RawAuthorizationSupplierBuilder;
import com.garganttua.api.core.security.authentication.RepositorySupplierBuilder;
import com.garganttua.api.core.security.authentication.TenantSupplierBuilder;
import com.garganttua.api.core.security.authorization.AuthenticationSupplierBuilder;
import com.garganttua.api.core.security.authorization.RequestSupplierBuilder;
import com.garganttua.api.core.security.key.DomainKeySupplierBuilder;
import com.garganttua.api.core.security.resolver.ApiContextElementResolver;
import com.garganttua.api.core.security.resolver.AuthenticateCredentialsElementResolver;
import com.garganttua.api.core.security.resolver.AuthenticationResultElementResolver;
import com.garganttua.api.core.security.resolver.AuthenticatorDefinitionElementResolver;
import com.garganttua.api.core.security.resolver.AuthoritiesElementResolver;
import com.garganttua.api.core.security.resolver.AuthorizationTokenElementResolver;
import com.garganttua.api.core.security.resolver.CallerElementResolver;
import com.garganttua.api.core.security.resolver.CredentialsElementResolver;
import com.garganttua.api.core.security.resolver.DecodedAuthorizationElementResolver;
import com.garganttua.api.core.security.resolver.DomainContextElementResolver;
import com.garganttua.api.core.security.resolver.ExecutionUuidElementResolver;
import com.garganttua.api.core.security.resolver.LoginElementResolver;
import com.garganttua.api.core.security.resolver.OperationElementResolver;
import com.garganttua.api.core.security.resolver.OwnerIdElementResolver;
import com.garganttua.api.core.security.resolver.PrincipalElementResolver;
import com.garganttua.api.core.security.resolver.RawAuthorizationElementResolver;
import com.garganttua.api.core.security.resolver.RepositoryElementResolver;
import com.garganttua.api.core.security.resolver.RequestElementResolver;
import com.garganttua.api.core.security.resolver.SigningKeyElementResolver;
import com.garganttua.api.core.security.resolver.TenantElementResolver;

/**
 * Pins the declarative-injection contract: each security parameter annotation
 * has a {@code @Resolver} that yields its matching {@code *SupplierBuilder} — the
 * dual of the manual {@code .withParam(i, new XBuilder())}. One assertion per
 * supplier.
 */
@DisplayName("Supplier resolvers map each injection annotation to its SupplierBuilder")
class SupplierResolverMappingTest {

    @Test
    void everyResolverYieldsItsSupplierBuilder() throws Exception {
            check(new ApiContextElementResolver(), ApiSupplierBuilder.class);
            check(new AuthenticateCredentialsElementResolver(), AuthenticateCredentialsSupplierBuilder.class);
            check(new AuthenticatorDefinitionElementResolver(), AuthenticatorDefinitionSupplierBuilder.class);
            check(new AuthoritiesElementResolver(), AuthoritiesSupplierBuilder.class);
            check(new AuthorizationTokenElementResolver(), AuthorizationSupplierBuilder.class);
            check(new CallerElementResolver(), CallerSupplierBuilder.class);
            check(new CredentialsElementResolver(), CredentialsSupplierBuilder.class);
            check(new DecodedAuthorizationElementResolver(), DecodedAuthorizationSupplierBuilder.class);
            check(new DomainContextElementResolver(), DomainSupplierBuilder.class);
            check(new ExecutionUuidElementResolver(), ExecutionUuidSupplierBuilder.class);
            check(new LoginElementResolver(), LoginSupplierBuilder.class);
            check(new OperationElementResolver(), OperationSupplierBuilder.class);
            check(new OwnerIdElementResolver(), OwnerIdSupplierBuilder.class);
            check(new PrincipalElementResolver(), PrincipalSupplierBuilder.class);
            check(new RawAuthorizationElementResolver(), RawAuthorizationSupplierBuilder.class);
            check(new RepositoryElementResolver(), RepositorySupplierBuilder.class);
            check(new TenantElementResolver(), TenantSupplierBuilder.class);
            check(new AuthenticationResultElementResolver(), AuthenticationSupplierBuilder.class);
            check(new RequestElementResolver(), RequestSupplierBuilder.class);
            check(new SigningKeyElementResolver(), DomainKeySupplierBuilder.class);
    }

    private void check(IElementResolver resolver, Class<?> expectedBuilder) throws Exception {
        IAnnotatedElement element = mock(IAnnotatedElement.class);
        when(element.getAnnotation(any())).thenReturn(null); // neither @Nullable nor @Nonnull
        IClass<?> elementType = IClass.getClass(Object.class);

        Resolved resolved = resolver.resolve(elementType, element);

        String who = resolver.getClass().getSimpleName();
        assertTrue(resolved.resolved(), who + " must resolve");
        assertSame(elementType, resolved.elementType(), who + ": element type passed through");
        assertEquals(expectedBuilder, resolved.elementSupplier().getClass(),
                who + " must yield " + expectedBuilder.getSimpleName());
        assertFalse(resolved.nullable(), who + ": not nullable without @Nullable");
    }
}
