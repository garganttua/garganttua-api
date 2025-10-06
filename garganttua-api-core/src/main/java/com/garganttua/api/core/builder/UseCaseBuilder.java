package com.garganttua.api.core.builder;

import java.util.Objects;

import com.garganttua.api.core.builder.binder.UseCaseBinderBuilder;
import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.core.context.application.UseCaseContext;
import com.garganttua.api.spec.engine.Action;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.api.spec.engine.IUseCaseBinderBuilder;
import com.garganttua.api.spec.engine.IUseCaseBuilder;
import com.garganttua.api.spec.engine.IUseCaseContext;
import com.garganttua.api.spec.engine.TechnicalOperation;

public class UseCaseBuilder<Up> extends AbstractAutomaticLinkedBuilder<IUseCaseContext, IUseCaseBuilder<Up>, Up>
        implements IUseCaseBuilder<Up> {

    private String useCaseName;
    private IUseCaseBinderBuilder<IUseCaseBuilder<Up>> binder;
    private String suffix;
    private String path;
    private Action action;
    private TechnicalOperation operation;
    private Class<Object> useCaseInput;
    private Class<Object> useCaseOutput;

    public UseCaseBuilder(String useCaseName, Up up) {
        super(up);
        this.useCaseName = Objects.requireNonNull(useCaseName, "Use case name cannot be null");
    }

    public UseCaseBuilder(IUseCaseBinderBuilder<IUseCaseBuilder<Up>> binder, Up up) {
        super(up);
        this.binder = Objects.requireNonNull(binder, "Binder cannot be null");
        this.useCaseName = binder.getMethodName();
    }

    @Override
    public IUseCaseBuilder<Up> pathSuffix(String suffix) {
        this.suffix = Objects.requireNonNull(suffix, "Suffix cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<Up> completePath(String path) {
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<Up> action(Action action) {
        this.action = Objects.requireNonNull(action, "Action cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<Up> operation(TechnicalOperation operation) {
        this.operation = Objects.requireNonNull(operation, "Operation cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<Up> input(Class<Object> useCaseInput) {
        this.useCaseInput = Objects.requireNonNull(useCaseInput, "Use case input cannot be null");
        return this;
    }

    @Override
    public IUseCaseBuilder<Up> output(Class<Object> useCaseOutput) {
        this.useCaseOutput = Objects.requireNonNull(useCaseOutput, "Use case output cannot be null");
        return this;
    }

    @Override
    public IUseCaseBinderBuilder<IUseCaseBuilder<Up>> bind(IObjectSupplierBuilder<?> supplier) throws BuilderException {
        if (this.binder == null) {
            this.binder = new UseCaseBinderBuilder<Up>(this, supplier);
        }
        return this.binder;
    }

    @Override
    public IUseCaseBinderBuilder<IUseCaseBuilder<Up>> bind(Object supplier) throws BuilderException {
        if (this.binder == null) {
            this.binder = new UseCaseBinderBuilder<Up>(this,
                    new FixedObjectSupplierBuilder<>(supplier));
        }
        return this.binder;
    }

    @Override
    public IUseCaseBinderBuilder<IUseCaseBuilder<Up>> bind() {
        Objects.requireNonNull(this.binder, "Binder must be set ");
        return this.binder;
    }

    @Override
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

    @Override
    protected void doAutoDetection() {

    }

}
