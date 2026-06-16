package com.garganttua.api.core.usecase;

import com.garganttua.api.commons.definition.IUseCaseDefinition;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationPath;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.binders.IMethodBinder;

public record UseCaseDefinition(
        String name,
        OperationPath path,
        IClass<?> inputType,
        IClass<?> outputType,
        IMethodBinder<?> binder,
        Scope scope,
        TechnicalOperation operation,
        Access access,
        boolean authority,
        String authorityName) implements IUseCaseDefinition {
}
