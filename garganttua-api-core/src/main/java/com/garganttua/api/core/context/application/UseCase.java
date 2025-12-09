package com.garganttua.api.core.context.application;

import java.util.Optional;
import java.util.Set;

import javax.swing.Action;

import com.garganttua.api.spec.context.IUseCase;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.binders.dsl.IMethodBinderBuilder;

public class UseCase<I,O> implements IUseCase<I,O> {

    public UseCase(String useCaseName, IMethodBinderBuilder<?, ?, ?, ?> methodBinder, String suffix, String path, Action action,
            TechnicalOperation operation, Class<Object> useCaseInput, Class<Object> useCaseOutput) {
        //TODO Auto-generated constructor stub
    }

    @Override
    public String getExecutableReference() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getExecutableReference'");
    }

    @Override
    public Optional<O> execute() throws ReflectionException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public Set<Class<?>> getDependencies() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDependencies'");
    }

}
