package com.garganttua.api.spec.security.authorization;

import java.util.Date;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public interface IGGAPIRefreshableAuthorization extends IGGAPISignableAuthorization {

    void revokeRefreshToken();

    boolean isRefreshTokenRevoked() throws GGAPIException;

    boolean isRefreshTokenExpired() throws GGAPIException;

    void createRefreshToken(IGGAPIKeyRealm key, Date expirationDate) throws GGAPIException;

    void validateRefreshToken(IGGAPIKeyRealm key, byte[] refreshToken) throws GGAPIException;

    byte[] getRefreshToken() throws GGAPIException;

    IGGAPIRefreshableAuthorization refresh(Date newExpirationDate) throws GGAPIException;

}
