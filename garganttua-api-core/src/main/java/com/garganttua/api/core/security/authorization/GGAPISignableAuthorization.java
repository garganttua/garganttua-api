package com.garganttua.api.core.security.authorization;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationSign;
import com.garganttua.api.spec.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.spec.security.authorization.IGGAPISignableAuthorization;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPIKeyRealmType;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public abstract class GGAPISignableAuthorization extends GGAPIAuthorization implements IGGAPISignableAuthorization {

	protected byte[] signature = null;
	protected GGAPIKeyAlgorithm keyAlgorithm;
	protected GGAPISignatureAlgorithm signatureAlgorithm;

	public GGAPISignableAuthorization() {
		super();
	}

	public GGAPISignableAuthorization(byte[] raw) throws GGAPIException {
		super(raw);
		this.signature = this.getSignatureFromRaw(raw);
	}
	
	public GGAPISignableAuthorization(String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate) {
		super(uuid, id, tenantId, ownerUuid, authorities, creationDate, expirationDate);
	}
	
	@Override
	@GGAPIAuthorizationSign
	public void sign(IGGAPIKeyRealm key) throws GGAPISecurityException {
		this.keyAlgorithm = key.getKeyAlgorithm();
		byte[] dataToSign = this.getDataToSign();
		try {
			this.signatureAlgorithm = key.getKeyForSigning().getSignatureAlgorithm();;
			this.signature = key.getKeyForSigning().sign(dataToSign);
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(e);
		}
	}

	@Override
	public byte[] getSignature() throws GGAPISecurityException {
		if (this.signature == null) {
			throw new GGAPISecurityException(GGAPIExceptionCode.AUTHORIZATION_NOT_SIGNED, "Authorization not signed");
		}
		return this.signature;
	}

	@Override
	protected void doValidation(Object ...args) throws GGAPISecurityException {
		IGGAPIKeyRealm key = (IGGAPIKeyRealm) args[0];

		try {
			key.getKeyForSignatureVerification().verifySignature(signature, this.getDataToSign());
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(e);
		}

		/* if( !Arrays.equals(this.signature, signatureTemp) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH, "Invalid signature");
		} */
	}

	@Override
	protected void doValidationAgainst(IGGAPIAuthorization authorization, Object ...args)
			throws GGAPISecurityException {
		if( !Arrays.equals(this.getSignature(), ((GGAPISignableAuthorization) authorization).getSignature()) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH, "Invalid signature");
		}
	}

	protected abstract byte[] getSignatureFromRaw(byte[] raw) throws GGAPIException;

	protected abstract byte[] getDataToSign();

}
