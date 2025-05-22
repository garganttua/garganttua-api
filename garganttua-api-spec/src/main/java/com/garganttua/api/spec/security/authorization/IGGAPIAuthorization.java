package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorization {

    void revoke();

    boolean isRevoked();

    void validateAgainst(IGGAPIAuthorization authorizationReference, Object ...args) throws GGAPIException;
    
    void validate(Object ...args) throws GGAPIException;

}
