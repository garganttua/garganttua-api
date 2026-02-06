package com.garganttua.api.spec.context.dsl;

import com.garganttua.api.spec.context.ContextBuildingStage;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.dsl.security.IApiContextSecurityBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.nativve.INativeBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IApiContextBuilder extends IDependentBuilder<IApiContextBuilder, IApiContext>, INativeBuilder<IApiContextBuilder, IApiContext> {

	IApiContextBuilder superTenantId(String string);

	IApiContextStartupBinderBuilder startup(ContextBuildingStage stage, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws DslException;

	IApiContextStartupBinderBuilder startup(ContextBuildingStage stage, Object object) throws DslException;

	<E> IDomainBuilder<E> domain(Class<E> entityClass) throws DslException;

	IApiContextBuilder superTenantAutoCreate(boolean b) throws DslException;

	IApiContextSecurityBuilder security();

}
