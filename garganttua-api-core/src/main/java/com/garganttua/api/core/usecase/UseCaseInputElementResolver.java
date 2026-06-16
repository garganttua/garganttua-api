package com.garganttua.api.core.usecase;

import static com.garganttua.core.injection.IInjectableElementResolver.isNullable;

import com.garganttua.api.commons.usecase.injection.UseCaseInput;
import com.garganttua.core.injection.DiException;
import com.garganttua.core.injection.IElementResolver;
import com.garganttua.core.injection.Resolved;
import com.garganttua.core.injection.annotations.Resolver;
import com.garganttua.core.reflection.IAnnotatedElement;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Resolves a {@link UseCaseInput}-annotated parameter via {@link UseCaseInputSupplierBuilder},
 * typed to the parameter's own type — the declarative dual of {@code .withParam(...)}. Mirrors
 * {@code LoginElementResolver}.
 */
@Reflected
@Resolver(annotations = { UseCaseInput.class })
public class UseCaseInputElementResolver implements IElementResolver {

	@Override
	public Resolved resolve(IClass<?> elementType, IAnnotatedElement element) throws DiException {
		return new Resolved(true, elementType, new UseCaseInputSupplierBuilder(elementType), isNullable(element));
	}
}
