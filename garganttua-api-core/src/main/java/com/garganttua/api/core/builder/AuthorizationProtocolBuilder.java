package com.garganttua.api.core.builder;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.core.builder.binder.AuthorizationProtocolMethodBinderBuilder;
import com.garganttua.api.core.context.application.AuthorizationProtocolContext;
import com.garganttua.api.spec.context.IAuthorizationProtocolContext;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.context.dsl.security.IAuthorizationProtocolMethodBinderBuilder;
import com.garganttua.api.spec.context.dsl.security.IApiContextSecurityBuilder;
import com.garganttua.api.spec.security.authorization.IAuthorizationProtocol;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public class AuthorizationProtocolBuilder
        extends AbstractAutomaticLinkedBuilder<IAuthorizationProtocolBuilder, IApiContextSecurityBuilder, IAuthorizationProtocolContext>
        implements IAuthorizationProtocolBuilder {

    private @Nonnull ISupplierBuilder<?, ? extends ISupplier<?>> supplier;
    private IAuthorizationProtocolMethodBinderBuilder getAuthorization;
    private IAuthorizationProtocolMethodBinderBuilder setAuthorization;

    public AuthorizationProtocolBuilder(IApiContextSecurityBuilder contextSecurityBuilder,
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) {
        super(contextSecurityBuilder);
        this.supplier = Objects.requireNonNull(supplier, "Supplier class cannot be null");
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(String methodName) throws DslException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        // TODO: Implement when method binder API supports string method names
        throw new UnsupportedOperationException("Unimplemented method 'getAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(Method method) throws DslException {
        Objects.requireNonNull(method, "Method cannot be null");
        // TODO: Implement when method binder API is complete
        throw new UnsupportedOperationException("Unimplemented method 'getAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(ObjectAddress methodAddress) throws DslException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        // TODO: Implement when method binder API supports ObjectAddress
        throw new UnsupportedOperationException("Unimplemented method 'getAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(String methodName) throws DslException {
        Objects.requireNonNull(methodName, "Method name cannot be null");
        // TODO: Implement when method binder API supports string method names
        throw new UnsupportedOperationException("Unimplemented method 'setAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(Method method) throws DslException {
        Objects.requireNonNull(method, "Method cannot be null");
        // TODO: Implement when method binder API is complete
        throw new UnsupportedOperationException("Unimplemented method 'setAuthorization'");
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(ObjectAddress methodAddress) throws DslException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        // TODO: Implement when method binder API supports ObjectAddress
        throw new UnsupportedOperationException("Unimplemented method 'setAuthorization'");
    }

    @Override
    protected synchronized IAuthorizationProtocolContext doBuild() throws DslException {
        return new AuthorizationProtocolContext(this.setAuthorization, this.getAuthorization);
    }

    @Override
    protected void doAutoDetection() throws DslException {

    }

}
