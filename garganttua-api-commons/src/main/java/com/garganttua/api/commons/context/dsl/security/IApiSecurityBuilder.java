package com.garganttua.api.commons.context.dsl.security;

import java.util.Optional;

import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.security.IApiSecurityContext;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.IAutomaticLinkedBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.dsl.IPackageableBuilder;

public interface IApiSecurityBuilder
        extends IAutomaticLinkedBuilder<IApiSecurityBuilder, IApiBuilder, IApiSecurityContext>, IPackageableBuilder<IApiSecurityBuilder, IApiSecurityContext> {

    IAuthenticationBuilder<IApiSecurityBuilder> authentication(ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException;

    IAuthenticationBuilder<IApiSecurityBuilder> authentication(IClass<?> authenticationClass) throws ApiException;

    Optional<IAuthenticationBuilder<IApiSecurityBuilder>> isAuthenticationAvailable(IClass<?> authenticationClass);

    IApiSecurityBuilder disable(boolean b);

}
