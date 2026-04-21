package com.garganttua.api.commons.security.authorization;

import java.util.Date;

import com.garganttua.api.commons.security.key.IKeyRealm;
import com.garganttua.api.commons.ApiException;

public interface IRefreshableAuthorization extends ISignableAuthorization {

    boolean isRefreshTokenExpired() throws ApiException;

    void createRefreshToken(IKeyRealm key, Date expirationDate) throws ApiException;

    void validateRefreshToken(IKeyRealm key, byte[] refreshToken) throws ApiException;

    byte[] getRefreshToken() throws ApiException;

    IRefreshableAuthorization refresh(Date newExpirationDate) throws ApiException;

}
