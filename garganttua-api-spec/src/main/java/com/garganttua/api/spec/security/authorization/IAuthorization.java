package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.ApiException;

public interface IAuthorization {

    void revoke();

    void isRevoked() throws ApiException;

    void isExpired() throws ApiException;

    void validateAgainst(IAuthorization authorizationReference, Object ...args) throws ApiException;
    
    void validate(Object ...args) throws ApiException;

}
