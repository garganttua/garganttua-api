package com.garganttua.api.spec.security.authorization;

import java.util.Date;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.key.IKeyRealm;

public interface IRefreshableAuthorization extends ISignableAuthorization {

    boolean isRefreshTokenExpired() throws CoreException;

    void createRefreshToken(IKeyRealm key, Date expirationDate) throws CoreException;

    void validateRefreshToken(IKeyRealm key, byte[] refreshToken) throws CoreException;

    byte[] getRefreshToken() throws CoreException;

    IRefreshableAuthorization refresh(Date newExpirationDate) throws CoreException;

}
