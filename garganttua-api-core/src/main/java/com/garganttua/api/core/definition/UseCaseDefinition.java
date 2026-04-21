package com.garganttua.api.core.definition;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;
import com.garganttua.api.commons.definition.IUseCaseDefinition;

public record UseCaseDefinition(
        Scope scope,
        TechnicalOperation operation,
        Access access,
        boolean authority) implements IUseCaseDefinition {
}
