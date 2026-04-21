package com.garganttua.api.commons.context.dsl;

import com.garganttua.api.commons.context.BuildingStage;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IApiBuilder extends IDependentBuilder<IApiBuilder, IApi> {

	IApiBuilder superTenantId(String string);

	IApiStartupBinderBuilder startup(BuildingStage stage, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException;

	IApiStartupBinderBuilder startup(BuildingStage stage, Object object) throws ApiException;

	<E> IDomainBuilder<E> domain(IClass<E> entityClass) throws ApiException;

	IApiBuilder superTenantAutoCreate(boolean b) throws ApiException;

	IApiBuilder multiTenant(boolean enabled) throws ApiException;

	IApiSecurityBuilder security();

	IApiBuilder serializer(ISerializer serializer) throws ApiException;

	IApiBuilder serializer(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException;

	IApiBuilder protocol(IProtocol<?, ?> protocol) throws ApiException;

	IApiBuilder protocol(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException;

	IApiBuilder authorizationProtocol(IAuthorizationProtocol protocol) throws ApiException;

	IApiBuilder authorizationProtocol(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException;

}
