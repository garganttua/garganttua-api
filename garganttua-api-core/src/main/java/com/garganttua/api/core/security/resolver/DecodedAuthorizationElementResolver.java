package com.garganttua.api.core.security.resolver;

import static com.garganttua.core.injection.IInjectableElementResolver.isNullable;

import com.garganttua.api.commons.security.injection.DecodedAuthorization;
import com.garganttua.api.core.security.authentication.DecodedAuthorizationSupplierBuilder;
import com.garganttua.core.injection.DiException;
import com.garganttua.core.injection.IElementResolver;
import com.garganttua.core.injection.Resolved;
import com.garganttua.core.injection.annotations.Resolver;
import com.garganttua.core.reflection.IAnnotatedElement;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Resolves a {@link DecodedAuthorization}-annotated parameter via {@link DecodedAuthorizationSupplierBuilder} — the
 * declarative path to the supplier that {@code .withParam(...)} wires manually.
 */
@Reflected
@Resolver(annotations = { DecodedAuthorization.class })
public class DecodedAuthorizationElementResolver implements IElementResolver {

    @Override
    public Resolved resolve(IClass<?> elementType, IAnnotatedElement element) throws DiException {
        return new Resolved(true, elementType, new DecodedAuthorizationSupplierBuilder(), isNullable(element));
    }

}
