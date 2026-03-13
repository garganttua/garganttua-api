package com.garganttua.api.core.security.authorization;

import java.util.Date;
import java.util.List;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.security.annotations.AuthenticatorRefreshToken;
import com.garganttua.api.spec.security.annotations.AuthorizationCreateRefreshToken;
import com.garganttua.api.spec.security.annotations.AuthorizationRefreshToken;
import com.garganttua.api.spec.security.annotations.AuthorizationRefreshTokenExpiration;
import com.garganttua.api.spec.security.annotations.AuthorizationValidateRefreshToken;
import com.garganttua.api.spec.security.authorization.IRefreshableAuthorization;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.core.CoreException;

public abstract class RefreshableAuthorization extends SignableAuthorization
		implements IRefreshableAuthorization {

	@AuthenticatorRefreshToken
	protected byte[] refreshToken = null;

	@AuthorizationRefreshTokenExpiration
	private Date refreshTokenExpirationDate;

	public RefreshableAuthorization() {
		super();
	}

	public RefreshableAuthorization(byte[] raw) throws SecurityException {
		super(raw);
		this.signature = this.getSignatureFromRaw(raw);
	}

	public RefreshableAuthorization(String uuid, String id, String tenantId, String ownerUuid,
			List<String> authorities, Date creationDate, Date expirationDate) throws SecurityException {
		super(uuid, id, tenantId, ownerUuid, authorities, creationDate, expirationDate);
	}

	@Override
	public boolean isRefreshTokenExpired() throws SecurityException {
		if (new Date().after(this.refreshTokenExpirationDate)) {
			throw new SecurityException(CoreExceptionCode.TOKEN_EXPIRED, "Refresh token expired");
		}
		return false;
	}

	@Override
	@AuthorizationRefreshToken
	public byte[] getRefreshToken() throws SecurityException {
		if (this.refreshToken == null)
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Refresh token not created");
		return this.refreshToken;
	}

	@Override
	@AuthorizationCreateRefreshToken
	public void createRefreshToken(IKeyRealm key, Date refreshTokenExpirationDate) throws CoreException {
		this.refreshTokenExpirationDate = refreshTokenExpirationDate;
		String tokenRaw = this.getTokenRaw();
		this.refreshToken = key.getKeyForSigning().sign(tokenRaw.getBytes());
	}

	@Override
	@AuthorizationValidateRefreshToken
	public void validateRefreshToken(IKeyRealm key, byte[] refreshToken) throws CoreException {
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
