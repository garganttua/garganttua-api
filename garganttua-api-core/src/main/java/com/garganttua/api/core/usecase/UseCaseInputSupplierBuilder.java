package com.garganttua.api.core.usecase;

import java.lang.reflect.Type;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

/**
 * Builds a {@link UseCaseInputSupplier} typed to the annotated parameter's type — the declarative
 * path for the {@code @UseCaseInput} parameter of a use case's bound method. Mirrors
 * {@code LoginSupplierBuilder}, but the supplied type is the parameter type (the use case's input).
 */
@SuppressWarnings("rawtypes")
public class UseCaseInputSupplierBuilder
		implements ISupplierBuilder<Object, IContextualSupplier<Object, IRuntimeContext>> {

	private final IClass<?> inputType;

	public UseCaseInputSupplierBuilder(IClass<?> inputType) {
		this.inputType = inputType != null ? inputType : IClass.getClass(Object.class);
	}

	@Override
	public Type getSuppliedType() {
		return this.inputType.getType();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IClass<Object> getSuppliedClass() {
		return (IClass<Object>) this.inputType;
	}

	@Override
	public boolean isContextual() {
		return true;
	}

	@Override
	public IContextualSupplier<Object, IRuntimeContext> build() throws DslException {
		return new UseCaseInputSupplier(this.inputType);
	}
}
