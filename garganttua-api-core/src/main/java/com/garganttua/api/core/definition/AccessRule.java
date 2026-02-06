package com.garganttua.api.core.definition;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.security.IAccessRule;

public record AccessRule(
        Operation operation,
        String authority,
        Access access
) implements IAccessRule {

    public static AccessRule of(Operation operation, Access access) {
        return new AccessRule(operation, operation.getOperationName().toUpperCase().replace("-", "_"), access);
    }

}
