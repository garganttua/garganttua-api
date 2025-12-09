package com.garganttua.api.core.builder;

import java.util.Objects;

import javax.swing.Action;

import com.garganttua.api.spec.context.IUseCase;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBinderBuilder;
import com.garganttua.api.spec.context.dsl.IUseCaseBuilder;
import com.garganttua.api.spec.context.dsl.security.IUseCaseSecurityBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public class UseCaseBuilder<I, O, E> extends AbstractAutomaticLinkedBuilder<IUseCaseBuilder<I, O, E>, IDomainBuilder<E>, IUseCase<I, O>>
        implements IUseCaseBuilder<I, O, E> {

    private String useCaseName;
    private IUseCaseBinderBuilder<I, O, E> binder;
    private String suffix;
    private String path;
    private Action action;
    private TechnicalOperation operation;
    private Class<Object> useCaseInput;
    private Class<Object> useCaseOutput;

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
        this.action = Objects.requireNonNull(action, "Action cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<I, O, E> operation(TechnicalOperation operation) {
        this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
        return this;
    }


/*     @Override
    public IUseCaseBinderBuilder<Object, IMethodBinder<Object>, IUseCaseBuilder<?, ?>, IUseCaseBuilder<Up, IUseCaseBuilder<?, ?>>> bind(
            IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) throws CoreException {
        if (this.binder == null) {
            this.binder = new UseCaseBinderBuilder<Object, IMethodBinder<Object>, IUseCaseBuilder<?, ?>, IUseCaseBuilder<Up, IUseCaseBuilder<?, ?>>>(this, supplier);
        }
        return this.binder;
    }  */
/*
    @Override
    public IUseCaseBinderBuilder<Up, IUseCaseBuilder<?,?>> bind(Object supplier) throws DslException {
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
    public IUseCaseBinderBuilder<I, O, E> bind(IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier)
            throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bind'");
    }

    @Override
    public IUseCaseBinderBuilder<I, O, E> bind(Object object) throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bind'");
    }

    @Override
    public IUseCaseBinderBuilder<I, O, E> bind() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bind'");
    }

    @Override
    public IUseCaseSecurityBuilder<I, O, E> security() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'security'");
    }

    @Override
    protected IUseCase<I, O> doBuild() throws DslException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
    }

   /*  @Override
    public IUseCaseBinderBuilder<Object, IMethodBinder<Object>, IUseCaseBuilder<?, ?>, IUseCaseBuilder<Up, IUseCaseBuilder<?, ?>>> bind(
            IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) throws CoreException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bind'");
    } */

   

}
