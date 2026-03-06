package com.garganttua.api.core.builder;

import java.util.Objects;

import com.garganttua.api.core.builder.binder.UseCaseBinderBuilder;
import com.garganttua.api.core.context.application.UseCase;
import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.IUseCase;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBinderBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IUseCaseSecurityBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public class UseCaseBuilder<I, O, E> extends AbstractAutomaticLinkedBuilder<IUseCaseBuilder<I, O, E>, IDomainBuilder<E>, IUseCase<I, O>>
        implements IUseCaseBuilder<I, O, E> {

    private String useCaseName;
    private UseCaseBinderBuilder<I, O, E> binder;
    private UseCaseSecurityBuilder<I, O, E> securityBuilder;
    private String suffix;
    private String path;
    private Scope scope;
    private TechnicalOperation operation;
    private IClass<I> useCaseInput;
    private IClass<O> useCaseOutput;

    public UseCaseBuilder(String useCaseName, IDomainBuilder<E> up) {
        super(up);
        this.useCaseName = Objects.requireNonNull(useCaseName, "Use case name cannot be null");
    }
/* 
    public UseCaseBuilder(IDomainBuilder up) {
        super(up);
        this.binder = Objects.requireNonNull(binder, "Binder cannot be null");
        this.useCaseName = binder.getMethodName();
    }
 */
    @Override
    public IUseCaseBuilder<I, O, E> pathSuffix(String suffix) {
        this.suffix = Objects.requireNonNull(suffix, "Suffix cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<I, O, E> completePath(String path) {
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<I, O, E> scope(Scope scope) {
        this.scope = Objects.requireNonNull(scope, "Scope cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<I, O, E> operation(TechnicalOperation operation) {
        this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
        return this;
    }


/*     @Override
    public IUseCaseBinderBuilder<Object, IMethodBinder<Object>, IUseCaseBuilder<?, ?>, IUseCaseBuilder<Up, IUseCaseBuilder<?, ?>>> bind(
            ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException {
        if (this.binder == null) {
            this.binder = new UseCaseBinderBuilder<Object, IMethodBinder<Object>, IUseCaseBuilder<?, ?>, IUseCaseBuilder<Up, IUseCaseBuilder<?, ?>>>(this, supplier);
        }
        return this.binder;
    }  */
/*
    @Override
    public IUseCaseBinderBuilder<Up, IUseCaseBuilder<?,?>> bind(Object supplier) throws ApiException {
        if (this.binder == null) {
            this.binder = new UseCaseBinderBuilder<Up, IUseCaseBuilder<?,?>>(this,
                    new FixedObjectSupplier<>(supplier));
        }
        return this.binder;
    }

    @Override
    public IUseCaseBinderBuilder<Up, IUseCaseBuilder<?,?>> bind() {
        Objects.requireNonNull(this.binder, "Binder must be set ");
        return this.binder;
    } */

 /*    @Override
    protected IUseCaseContext doBuild() {
        return new UseCaseContext(
                this.useCaseName,
                this.binder,
                this.suffix,
                this.path,
                this.action,
                this.operation,
                this.useCaseInput,
                this.useCaseOutput);
    }
 */
    @Override
    protected void doAutoDetection() {

    }

    @Override
    public IUseCaseBinderBuilder<I, O, E> bind(ISupplierBuilder<?, ? extends ISupplier<?>> supplier)
            throws ApiException {
        if (this.binder == null) {
            this.binder = new UseCaseBinderBuilder<>(this, supplier);
        }
        return this.binder;
    }

    @Override
    public IUseCaseBinderBuilder<I, O, E> bind(Object object) throws ApiException {
        if (this.binder == null) {
            this.binder = new UseCaseBinderBuilder<>(this,
                    FixedSupplierBuilder.of(Objects.requireNonNull(object, "Object cannot be null")));
        }
        return this.binder;
    }

    @Override
    public IUseCaseBinderBuilder<I, O, E> bind() {
        Objects.requireNonNull(this.binder, "Binder must be set first using bind(supplier) or bind(object)");
        return this.binder;
    }

    @Override
    public IUseCaseSecurityBuilder<I, O, E> security() {
        if (this.securityBuilder == null) {
            this.securityBuilder = new UseCaseSecurityBuilder<>(this);
        }
        return this.securityBuilder;
    }

    Scope getScope() {
        return this.scope;
    }

    TechnicalOperation getOperation() {
        return this.operation;
    }

    Access getAccess() {
        return this.securityBuilder != null ? this.securityBuilder.getAccess() : Access.authenticated;
    }

    boolean hasAuthority() {
        return this.securityBuilder != null && this.securityBuilder.hasAuthority();
    }

    @Override
    protected synchronized IUseCase<I, O> doBuild() throws ApiException {
        return new UseCase<>(
                this.useCaseName,
                this.binder,
                this.suffix,
                this.path,
                this.scope,
                this.operation,
                (IClass<Object>) this.useCaseInput,
                (IClass<Object>) this.useCaseOutput);
    }

}
