package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;

import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class AuthenticatorDefinitionSupplierBuilder implements ISupplierBuilder<IAuthenticatorDefinition, IContextualSupplier<IAuthenticatorDefinition, IRuntimeContext>> {
	private static final Logger log = Logger.getLogger(AuthenticatorDefinitionSupplierBuilder.class);


    private static final IClass<IAuthenticatorDefinition> SUPPLIED_CLASS = IClass.getClass(IAuthenticatorDefinition.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<IAuthenticatorDefinition> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public boolean isContextual() {
        return true;
    }

    @Override
    public IContextualSupplier<IAuthenticatorDefinition, IRuntimeContext> build() throws DslException {
        log.debug("Building AuthenticatorDefinitionSupplier");
        return new AuthenticatorDefinitionSupplier();
    }

}
