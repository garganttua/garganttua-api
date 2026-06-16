package com.garganttua.api.core.usecase;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.Objects;

import com.garganttua.api.core.usecase.UseCaseBinderBuilder;
import com.garganttua.api.core.usecase.UseCase;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.context.IUseCase;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.IUseCaseBinderBuilder;
import com.garganttua.api.commons.context.dsl.IUseCaseBuilder;
import com.garganttua.api.commons.context.dsl.security.IUseCaseSecurityBuilder;
import com.garganttua.core.dsl.AbstractAutomaticLinkedBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@Reflected
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
    private IMethodBinder<?> builtBinder;

    public UseCaseBuilder(String useCaseName, IDomainBuilder<E> up) {
        super(up);
        this.useCaseName = Objects.requireNonNull(useCaseName, "Use case name cannot be null");
    }

    public UseCaseBuilder(String useCaseName, IDomainBuilder<E> up, IClass<I> inputType, IClass<O> outputType) {
        super(up);
        this.useCaseName = Objects.requireNonNull(useCaseName, "Use case name cannot be null");
        this.useCaseInput = inputType;
        this.useCaseOutput = outputType;
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

    public Scope getScope() {
        return this.scope;
    }

    public TechnicalOperation getOperation() {
        return this.operation;
    }

    public Access getAccess() {
        return this.securityBuilder != null ? this.securityBuilder.getAccess() : Access.authenticated;
    }

    public boolean hasAuthority() {
        return this.securityBuilder != null && this.securityBuilder.hasAuthority();
    }

    public String getCustomAuthority() {
        return this.securityBuilder != null ? this.securityBuilder.getCustomAuthority() : null;
    }

    public String getName() {
        return this.useCaseName;
    }

    public String getPathSuffix() {
        return this.suffix;
    }

    public String getCompletePath() {
        return this.path;
    }

    public IClass<I> getInputType() {
        return this.useCaseInput;
    }

    public IClass<O> getOutputType() {
        return this.useCaseOutput;
    }

    /** The functional method binder built from {@code bind(...)} — the use case's executable, or null. */
    public IMethodBinder<?> getBuiltBinder() {
        return this.builtBinder;
    }

    /**
     * The binder <em>builder</em> backing {@code bind(...)} — exposed so the domain build can auto-wire
     * the bound method's annotated parameters ({@code @UseCaseInput}, {@code @Caller}, …) via the
     * injection resolvers before {@link #doBuild()} materialises it. Null when no method is bound.
     */
    public UseCaseBinderBuilder<I, O, E> getBinderBuilder() {
        return this.binder;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized IUseCase<I, O> doBuild() throws ApiException {
        // Build the real method binder (the bound method, fed by suppliers) — the definition keeps
        // it; the UseCase wrapper below only satisfies the builder contract.
        this.builtBinder = (this.binder != null) ? this.binder.build() : null;
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
