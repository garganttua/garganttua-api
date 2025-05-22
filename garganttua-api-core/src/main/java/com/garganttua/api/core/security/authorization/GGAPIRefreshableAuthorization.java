package com.garganttua.api.core.security.authorization;

import java.util.Date;
import java.util.List;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationCreateRefreshToken;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationRefreshToken;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationRefreshTokenExpiration;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationRevokeRefreshToken;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationValidateRefreshToken;
import com.garganttua.api.spec.security.authorization.IGGAPIRefreshableAuthorization;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public abstract class GGAPIRefreshableAuthorization extends GGAPISignableAuthorization
        implements IGGAPIRefreshableAuthorization {

    protected byte[] refreshToken = null;
    
    @GGAPIAuthorizationRefreshTokenExpiration
    private Date refreshTokenExpirationDate;

    public GGAPIRefreshableAuthorization() {
        super();
    }

    public GGAPIRefreshableAuthorization(byte[] raw) throws GGAPIException {
        super(raw);
        this.signature = this.getSignatureFromRaw(raw);
    }

    public GGAPIRefreshableAuthorization(String uuid, String id, String tenantId, String ownerUuid,
            List<String> authorities, Date creationDate, Date expirationDate) throws GGAPISecurityException {
        super(uuid, id, tenantId, ownerUuid, authorities, creationDate, expirationDate);
    }

    @Override
    @GGAPIAuthorizationRevokeRefreshToken
    public void revokeRefreshToken() {
        this.revoked = true;
    }

    @Override
    public boolean isRefreshTokenExpired() throws GGAPISecurityException {
        if (new Date().after(this.refreshTokenExpirationDate)) {
            throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_EXPIRED, "Refresh token expired");
        }
        return false;
    }

    @Override
    public boolean isRefreshTokenRevoked() throws GGAPISecurityException {
        if (this.revoked)
            throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_REVOKED, "Refresh token revoked");
        return false;
    }

    @Override
    @GGAPIAuthorizationRefreshToken
    public byte[] getRefreshToken() throws GGAPISecurityException {
        if (this.refreshToken == null)
            throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Refresh token not created");
        return this.refreshToken;
    }

    @Override
    @GGAPIAuthorizationCreateRefreshToken
    public void createRefreshToken(IGGAPIKeyRealm key, Date refreshTokenExpirationDate) throws GGAPIException {
        this.refreshTokenExpirationDate = refreshTokenExpirationDate;
        String tokenRaw = this.getTokenRaw();
        this.refreshToken = key.getKeyForSigning().sign(tokenRaw.getBytes());
    }

    @Override
    @GGAPIAuthorizationValidateRefreshToken
    public void validateRefreshToken(IGGAPIKeyRealm key, byte[] refreshToken) throws GGAPIException {
        this.isExpired();
        this.isRevoked();
        key.getKeyForSignatureVerification().verifySignature(refreshToken, this.getTokenRaw().getBytes());
    }

    private String getTokenRaw() {
        String tokenRaw = this.tenantId + "_" + this.uuid + "_" + this.ownerId + "_" + this.creationDate.getTime()
                + "_" + this.expirationDate.getTime();
        return tokenRaw;
    }
}
