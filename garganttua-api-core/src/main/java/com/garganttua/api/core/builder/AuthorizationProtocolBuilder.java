package com.garganttua.api.core.builder;

import com.garganttua.core.reflection.IMethod;
import java.util.Objects;

import com.garganttua.api.core.context.security.AuthorizationProtocolContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.security.context.IAuthorizationProtocolContext;
import com.garganttua.api.spec.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolMethodBinderBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class AuthorizationProtocolBuilder
        extends AbstractAutomaticLinkedBuilder<IAuthorizationProtocolBuilder, IApiSecurityBuilder, IAuthorizationProtocolContext>
        implements IAuthorizationProtocolBuilder {

    private ISupplierBuilder<?, ? extends ISupplier<?>> supplier;
    private IAuthorizationProtocolMethodBinderBuilder getAuthorization;
    private IAuthorizationProtocolMethodBinderBuilder setAuthorization;

    public AuthorizationProtocolBuilder(IApiSecurityBuilder contextSecurityBuilder,
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(contextSecurityBuilder);
        this.supplier = Objects.requireNonNull(supplier, "Supplier class cannot be null");
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        // TODO: Implement when method binder API supports string method names
        throw new UnsupportedOperationException("Unimplemented method 'getAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        // TODO: Implement when method binder API is complete
        throw new UnsupportedOperationException("Unimplemented method 'getAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        // TODO: Implement when method binder API supports ObjectAddress
        throw new UnsupportedOperationException("Unimplemented method 'getAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(String methodName) throws ApiException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        // TODO: Implement when method binder API supports string method names
        throw new UnsupportedOperationException("Unimplemented method 'setAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(IMethod method) throws ApiException {
        Objects.requireNonNull(method, "Method cannot be null");
        // TODO: Implement when method binder API is complete
        throw new UnsupportedOperationException("Unimplemented method 'setAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(ObjectAddress methodAddress) throws ApiException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        // TODO: Implement when method binder API supports ObjectAddress
        throw new UnsupportedOperationException("Unimplemented method 'setAuthorization'");
    }

    @Override
    protected synchronized IAuthorizationProtocolContext doBuild() throws ApiException {
        return new AuthorizationProtocolContext(this.setAuthorization, this.getAuthorization);
    }

    @Override
    protected void doAutoDetection() throws ApiException {

    }

}
