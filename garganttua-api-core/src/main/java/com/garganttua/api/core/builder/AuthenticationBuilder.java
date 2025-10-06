package com.garganttua.api.core.builder;

import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.credentials;
import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.entity;
import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.principal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.javatuples.Pair;

import com.garganttua.api.core.builder.binder.AuthenticationMethodBinderBuilder;
import com.garganttua.api.core.context.application.AuthenticationContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IAuthenticationBuilder;
import com.garganttua.api.spec.engine.IAuthenticationContext;
import com.garganttua.api.spec.engine.IAuthenticationMethodBinderBuilder;
import com.garganttua.api.spec.engine.IContextSecurityBuilder;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;
import com.garganttua.reflection.GGObjectAddress;

public class AuthenticationBuilder extends AbstractAutomaticLinkedBuilder<IAuthenticationContext, IAuthenticationBuilder, IContextSecurityBuilder> implements IAuthenticationBuilder {

    private Boolean findPrincipal;
    private @Nonnull IObjectSupplierBuilder<?> supplier;
    private IAuthenticationMethodBinderBuilder authenticate;
    private List<Pair<Class<? extends Annotation>, Class<?>>> fieldAnnotations = new ArrayList<>();
    private IAuthenticationMethodBinderBuilder applySecurityOnEntity;
    private Map<String, IUseCaseBuilder<IAuthenticationBuilder>> useCases = new HashMap<>();

    public AuthenticationBuilder(IContextSecurityBuilder contextSecurityBuilder, IObjectSupplierBuilder<?> supplier) {
        super(contextSecurityBuilder);
        this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null");
    }

    @Override
    protected IAuthenticationContext doBuild() {
        return new AuthenticationContext(
                this.findPrincipal,
                this.supplier,
                this.authenticate,
                this.fieldAnnotations,
                this.applySecurityOnEntity,
                this.useCases.values());
    }

    @Override
    protected void doAutoDetection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doAutoDetection'");
    }

    @Override
    public IAuthenticationBuilder findPrincipal(boolean b) {
        this.findPrincipal = Objects.requireNonNull(b, "Find principal cannot be null");
        return this;
    }

    @Override
    public IAuthenticationBuilder authenticate(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        if (this.findPrincipal)
            this.authenticate = new AuthenticationMethodBinderBuilder(this,
                    this.supplier)
                    .method(methodName, Boolean.class, Byte[].class, Object.class)
                    .withParam(0, credentials())
                    .withParam(1, principal());
        else
            this.authenticate = new AuthenticationMethodBinderBuilder(this,
                    this.supplier)
                    .method(methodName, Boolean.class, Byte[].class)
                    .withParam(0, credentials());

        return this;
    }

    @Override
    public IAuthenticationBuilder authenticate(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        if (this.findPrincipal)
            this.authenticate = new AuthenticationMethodBinderBuilder(this,
                    this.supplier)
                    .method(method, Boolean.class, Byte[].class, Object.class)
                    .withParam(0, credentials())
                    .withParam(1, principal());
        else
            this.authenticate = new AuthenticationMethodBinderBuilder(this,
                    this.supplier)
                    .method(method, Boolean.class, Byte[].class)
                    .withParam(0, credentials());

        return this;
    }

    @Override
    public IAuthenticationBuilder authenticate(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        if (this.findPrincipal)
            this.authenticate = new AuthenticationMethodBinderBuilder(this,
                    this.supplier)
                    .method(methodAddress, Boolean.class, Byte[].class, Object.class)
                    .withParam(0, credentials())
                    .withParam(1, principal());
        else
            this.authenticate = new AuthenticationMethodBinderBuilder(this,
                    this.supplier)
                    .method(methodAddress, Boolean.class, Byte[].class)
                    .withParam(0, credentials());

        return this;
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        this.applySecurityOnEntity = new AuthenticationMethodBinderBuilder(this,
                this.supplier)
                .method(methodName, null, Object.class)
                .withParam(0, entity());

        return this;
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");

        this.applySecurityOnEntity = new AuthenticationMethodBinderBuilder(this,
                this.supplier)
                .method(method, null, Object.class)
                .withParam(0, entity());

        return this;
    }

    @Override
    public IAuthenticationBuilder applySecurityOnEntity(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");

        this.applySecurityOnEntity = new AuthenticationMethodBinderBuilder(this,
                this.supplier)
                .method(methodAddress, null, Object.class)
                .withParam(0, entity());

        return this;
    }

    @Override
    public IUseCaseBuilder<IAuthenticationBuilder> useCase(String methodName) throws CoreException {
        Objects.requireNonNull(methodName, "Method name cannot be null");

        IUseCaseBuilder<IAuthenticationBuilder> useCaseBuilder = this.useCases.get(methodName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<IAuthenticationBuilder>(methodName, this);
            this.useCases.put(methodName, useCaseBuilder);
        }

        useCaseBuilder.bind(this.supplier).method(methodName);

        return useCaseBuilder;
    }

    @Override
    public IUseCaseBuilder<IAuthenticationBuilder> useCase(Method method) throws CoreException {
        Objects.requireNonNull(method, "Method cannot be null");
        String methodName = method.getName();

        IUseCaseBuilder<IAuthenticationBuilder> useCaseBuilder = this.useCases.get(methodName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<IAuthenticationBuilder>(methodName, this);
            this.useCases.put(methodName, useCaseBuilder);
        }

        useCaseBuilder.bind(this.supplier).method(method);

        return useCaseBuilder;
    }

    @Override
    public IUseCaseBuilder<IAuthenticationBuilder> useCase(GGObjectAddress methodAddress) throws CoreException {
        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        String methodName = methodAddress.getElement(methodAddress.length() - 1);

        IUseCaseBuilder<IAuthenticationBuilder> useCaseBuilder = this.useCases.get(methodName);

        if (useCaseBuilder == null) {
            useCaseBuilder = new UseCaseBuilder<IAuthenticationBuilder>(methodName, this);
            this.useCases.put(methodName, useCaseBuilder);
        }

        useCaseBuilder.bind(this.supplier).method(methodAddress);

        return useCaseBuilder;
    }

    @Override
    public IAuthenticationBuilder entityMustHaveFieldOfTypeAnnotatedWith(Class<? extends Annotation> annotation,
            Class<?> fieldType) throws CoreException {
        Objects.requireNonNull(annotation, "Annotation cannot be null");
        Objects.requireNonNull(fieldType, "Field type cannot be null");
        this.fieldAnnotations.add(new Pair<Class<? extends Annotation>, Class<?>>(annotation, fieldType));
        return this;
    }

}
