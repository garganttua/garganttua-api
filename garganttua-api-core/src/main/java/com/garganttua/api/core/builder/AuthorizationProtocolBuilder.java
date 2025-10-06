package com.garganttua.api.core.builder;

import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.authorization;
import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.technicalRequest;
import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.technicalResponse;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.garganttua.api.core.builder.binder.AuthorizationProtocolMethodBinderBuilder;
import com.garganttua.api.core.context.application.AuthorizationProtocolContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthorizationProtocolBuilder;
import com.garganttua.api.spec.engine.IAuthorizationProtocolContext;
import com.garganttua.api.spec.engine.IAuthorizationProtocolMethodBinderBuilder;
import com.garganttua.api.spec.engine.IContextSecurityBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.reflection.GGObjectAddress;

public class AuthorizationProtocolBuilder
        extends AbstractAutomaticLinkedBuilder<IAuthorizationProtocolContext, IAuthorizationProtocolBuilder, IContextSecurityBuilder>
        implements IAuthorizationProtocolBuilder {

    private @Nonnull IObjectSupplierBuilder<?> supplier;
    private IAuthorizationProtocolMethodBinderBuilder getAuthorization;
    private IAuthorizationProtocolMethodBinderBuilder setAuthorization;

    public AuthorizationProtocolBuilder(IContextSecurityBuilder contextSecurityBuilder,
            IObjectSupplierBuilder<?> supplier) {
        super(contextSecurityBuilder);
        this.supplier = Objects.requireNonNull(supplier, "Supplier class cannot be null");
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.getAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(methodName, Byte[].class, Object.class)
                .withParam(0, technicalRequest());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.getAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(method, Byte[].class, Object.class)
                .withParam(0, technicalRequest());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder getAuthorization(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.getAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(methodAddress, Byte[].class, Object.class)
                .withParam(0, technicalRequest());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.setAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(methodName, null, Byte[].class, Object.class)
                .withParam(0, authorization())
                .withParam(1, technicalResponse());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.setAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(method, null, Byte[].class, Object.class)
                .withParam(0, authorization())
                .withParam(1, technicalResponse());

        return this;
    }

    @Override
    public IAuthorizationProtocolBuilder setAuthorization(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.setAuthorization = new AuthorizationProtocolMethodBinderBuilder(this,
                this.supplier)
                .method(methodAddress, null, Byte[].class, Object.class)
                .withParam(0, authorization())
                .withParam(1, technicalResponse());

        return this;
    }

    @Override
    protected IAuthorizationProtocolContext doBuild() throws CoreException {
        return new AuthorizationProtocolContext(this.setAuthorization, this.getAuthorization);
    }

    @Override
    protected void doAutoDetection() throws CoreException {

    }

}
