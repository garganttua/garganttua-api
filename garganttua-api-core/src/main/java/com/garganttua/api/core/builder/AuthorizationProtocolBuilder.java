package com.garganttua.api.core.builder;

import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.*;

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
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;
import com.garganttua.core.reflection.ObjectAddress;

public class AuthorizationProtocolBuilder
        extends AbstractAutomaticLinkedBuilder<IAuthorizationProtocolBuilder, IApiContextSecurityBuilder, IAuthorizationProtocol>
        implements IAuthorizationProtocolBuilder {

    private @Nonnull IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier;
    private IAuthorizationProtocolMethodBinderBuilder getAuthorization;
    private IAuthorizationProtocolMethodBinderBuilder setAuthorization;

    public AuthorizationProtocolBuilder(IContextSecurityBuilder contextSecurityBuilder,
            IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) {
        super(contextSecurityBuilder);
        this.supplier = Objects.requireNonNull(supplier, "Supplier class cannot be null");
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(String methodName) throws DslException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.getAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(methodName, Byte[].class, Object.class)
                .withParam(0, technicalRequest());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(Method method) throws DslException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.getAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(method, Byte[].class, Object.class)
                .withParam(0, technicalRequest());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(ObjectAddress methodAddress) throws DslException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.getAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(methodAddress, Byte[].class, Object.class)
                .withParam(0, technicalRequest());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(String methodName) throws DslException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.setAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(methodName, null, Byte[].class, Object.class)
                .withParam(0, authorization())
                .withParam(1, technicalResponse());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(Method method) throws DslException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.setAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(method, null, Byte[].class, Object.class)
                .withParam(0, authorization())
                .withParam(1, technicalResponse());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(ObjectAddress methodAddress) throws DslException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.setAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(methodAddress, null, Byte[].class, Object.class)
                .withParam(0, authorization())
                .withParam(1, technicalResponse());

        return this;
    }

    @Override
    protected IAuthorizationProtocol doBuild() throws DslException {
        return new AuthorizationProtocolContext(this.setAuthorization, this.getAuthorization);
    }

    @Override
    protected void doAutoDetection() throws DslException {

    }

}
