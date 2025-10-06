package com.garganttua.api.core.context.application;

import com.garganttua.api.spec.engine.Action;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.engine.IUseCaseContext;
import com.garganttua.api.spec.engine.TechnicalOperation;

public class UseCaseContext implements IUseCaseContext {

    public UseCaseContext(String useCaseName, IMethodBinderBuilder<?, ?> methodBinder, String suffix, String path, Action action,
            TechnicalOperation operation, Class<Object> useCaseInput, Class<Object> useCaseOutput) {
        //TODO Auto-generated constructor stub
    }

}
