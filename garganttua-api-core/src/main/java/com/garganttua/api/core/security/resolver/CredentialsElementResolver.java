package com.garganttua.api.core.security.resolver;

import static com.garganttua.core.injection.IInjectableElementResolver.isNullable;

import com.garganttua.api.commons.security.injection.Credentials;
import com.garganttua.api.core.security.authentication.CredentialsSupplierBuilder;
import com.garganttua.core.injection.DiException;
import com.garganttua.core.injection.IElementResolver;
import com.garganttua.core.injection.Resolved;
import com.garganttua.core.injection.annotations.Resolver;
import com.garganttua.core.reflection.IAnnotatedElement;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Resolves a {@link Credentials}-annotated parameter via {@link CredentialsSupplierBuilder} — the
 * declarative path to the supplier that {@code .withParam(...)} wires manually.
 */
@Reflected
@Resolver(annotations = { Credentials.class })
public class CredentialsElementResolver implements IElementResolver {

    @Override
    public Resolved resolve(IClass<?> elementType, IAnnotatedElement element) throws DiException {
        return new Resolved(true, elementType, new CredentialsSupplierBuilder(), isNullable(element));
    }

}
