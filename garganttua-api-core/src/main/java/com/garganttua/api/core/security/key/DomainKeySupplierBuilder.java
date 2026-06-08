package com.garganttua.api.core.security.key;

import java.lang.reflect.Type;

import com.garganttua.core.dsl.DslException;
import com.garganttua.core.observability.Logger;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

/**
 * Builder for {@link DomainKeySupplier}. Pass an instance to
 * {@code .withParam(i, new DomainKeySupplierBuilder())} when wiring a token
 * authenticator's verification method (DSL), so the framework injects the key
 * object (the {@code @Key} entity, resolved by the token's {@code signedBy}
 * domain reference) that signed the authorization being verified.
 */
@SuppressWarnings("rawtypes")
public class DomainKeySupplierBuilder
		implements ISupplierBuilder<Object, IContextualSupplier<Object, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(DomainKeySupplierBuilder.class);

	private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);

	@Override
	public Type getSuppliedType() {
		return SUPPLIED_CLASS.getType();
	}

	@Override
	public IClass<Object> getSuppliedClass() {
		return SUPPLIED_CLASS;
	}

	@Override
	public boolean isContextual() {
		return true;
	}

	@Override
	public IContextualSupplier<Object, IRuntimeContext> build() throws DslException {
		log.debug("Building DomainKeySupplier");
		return new DomainKeySupplier();
	}

}
