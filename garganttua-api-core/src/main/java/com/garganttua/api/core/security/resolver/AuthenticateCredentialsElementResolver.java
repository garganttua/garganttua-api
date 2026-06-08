package com.garganttua.api.core.security.resolver;

import static com.garganttua.core.injection.IInjectableElementResolver.isNullable;

import com.garganttua.api.commons.security.injection.AuthenticateCredentials;
import com.garganttua.api.core.security.authentication.AuthenticateCredentialsSupplierBuilder;
import com.garganttua.core.injection.DiException;
import com.garganttua.core.injection.IElementResolver;
import com.garganttua.core.injection.Resolved;
import com.garganttua.core.injection.annotations.Resolver;
import com.garganttua.core.reflection.IAnnotatedElement;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Resolves a {@link AuthenticateCredentials}-annotated parameter via {@link AuthenticateCredentialsSupplierBuilder} — the
 * declarative path to the supplier that {@code .withParam(...)} wires manually.
 */
@Reflected
@Resolver(annotations = { AuthenticateCredentials.class })
public class AuthenticateCredentialsElementResolver implements IElementResolver {

    @Override
    public Resolved resolve(IClass<?> elementType, IAnnotatedElement element) throws DiException {
        return new Resolved(true, elementType, new AuthenticateCredentialsSupplierBuilder(), isNullable(element));
    }

}
