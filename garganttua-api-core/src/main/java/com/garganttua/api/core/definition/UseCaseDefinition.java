package com.garganttua.api.core.definition;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Scope;
import com.garganttua.api.spec.context.TechnicalOperation;
import com.garganttua.api.spec.definition.IUseCaseDefinition;

public record UseCaseDefinition(
        Scope scope,
        TechnicalOperation operation,
        Access access,
        boolean authority) implements IUseCaseDefinition {
}
