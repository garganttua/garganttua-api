package com.garganttua.api.core.definition;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.security.IAccessRule;

public record AccessRule(
        String authority,
        Access access) implements IAccessRule {

    public static AccessRule of(Operation operation, boolean authority, Access access) {
        return new AccessRule(authority ? operation.getOperationName().toUpperCase().replace("-", "_") : null, access);
    }

}
