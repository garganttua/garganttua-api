package com.garganttua.api.spec.context.dsl;

import com.garganttua.api.spec.context.ContextBuildingStage;
import com.garganttua.api.spec.context.dsl.security.IApiContextSecurityBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.injection.context.dsl.IDiContextBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public interface IApiContextBuilder extends IDiContextBuilder {

	IApiContextBuilder superTenantId(String string);

	IApiContextStartupBinderBuilder startup(ContextBuildingStage stage, IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) throws DslException;

	IApiContextStartupBinderBuilder startup(ContextBuildingStage stage, Object object) throws DslException;

	<E> IDomainBuilder<E> domain(Class<E> entityClass) throws DslException;

	IApiContextBuilder superTenantAutoCreate(boolean b) throws DslException;

    IApiContextSecurityBuilder security();

}
