package com.garganttua.api.core.security;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.security.IAccessRule;

public record AccessRule(
        String authority,
        Access access) implements IAccessRule {

    public static AccessRule of(OperationDefinition operation, boolean authority, Access access) {
        return new AccessRule(authority ? operation.getOperationName().toUpperCase().replace("-", "_") : null, access);
    }

}
