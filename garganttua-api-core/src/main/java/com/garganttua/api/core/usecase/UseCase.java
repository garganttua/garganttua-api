package com.garganttua.api.core.usecase;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.commons.context.IUseCase;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IMethodReturn;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;
import com.garganttua.core.supply.SupplyException;

public class UseCase<I,O> implements IUseCase<I,O> {

    private final String useCaseName;
    private final IMethodBinderBuilder<?, ?, ?, ?> methodBinder;
    private final String suffix;
    private final String path;
    private final Scope scope;
    private final TechnicalOperation operation;
    private final IClass<Object> useCaseInput;
    private final IClass<Object> useCaseOutput;

    public UseCase(String useCaseName, IMethodBinderBuilder<?, ?, ?, ?> methodBinder, String suffix, String path, Scope scope,
            TechnicalOperation operation, IClass<Object> useCaseInput, IClass<Object> useCaseOutput) {
        this.useCaseName = useCaseName;
        this.methodBinder = methodBinder;
        this.suffix = suffix;
        this.path = path;
        this.scope = scope;
        this.operation = operation;
        this.useCaseInput = useCaseInput;
        this.useCaseOutput = useCaseOutput;
    }

    @Override
    public String getExecutableReference() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getExecutableReference'");
    }

    @Override
    public Optional<IMethodReturn<O>> execute() throws ReflectionException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public Set<IClass<?>> dependencies() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'dependencies'");
    }

    @Override
    public Optional<IMethodReturn<O>> supply() throws SupplyException {
        // Delegate to execute()
        try {
            return execute();
        } catch (ReflectionException e) {
            throw new SupplyException(e);
        }
    }

    @Override
    public Type getSuppliedType() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSuppliedType'");
    }

    @Override
    public IClass<IMethodReturn<O>> getSuppliedClass() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSuppliedClass'");
    }

}
