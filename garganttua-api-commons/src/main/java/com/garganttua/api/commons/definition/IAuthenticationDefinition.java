package com.garganttua.api.commons.definition;

import com.garganttua.core.reflection.binders.IMethodBinder;

public interface IAuthenticationDefinition {

    IMethodBinder<?> authenticateMethodBinder();

    /**
     * Custom method binder run on CREATE/UPDATE of the authenticator entity to apply
     * security on it (e.g. hash a password), declared via
     * {@code .authentication(...).applySecurityOnEntity(method)}. {@code null} when not declared.
     */
    default IMethodBinder<?> applySecurityOnEntityMethodBinder() {
        return null;
    }

}
