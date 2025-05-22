package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorization {

    void revoke();

    void isRevoked() throws GGAPIException;

    void isExpired() throws GGAPIException;

    void validateAgainst(IGGAPIAuthorization authorizationReference, Object ...args) throws GGAPIException;
    
    void validate(Object ...args) throws GGAPIException;

}
