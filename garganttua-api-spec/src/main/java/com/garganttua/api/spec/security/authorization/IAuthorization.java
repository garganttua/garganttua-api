package com.garganttua.api.spec.security.authorization;

import com.garganttua.core.CoreException;

public interface IAuthorization {

    void revoke();

    void isRevoked() throws CoreException;

    void isExpired() throws CoreException;

    void validateAgainst(IAuthorization authorizationReference, Object ...args) throws CoreException;
    
    void validate(Object ...args) throws CoreException;

}
