package com.garganttua.api.core.security.authorization;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.security.annotations.AuthorizationSign;
import com.garganttua.api.spec.security.authorization.IAuthorization;
import com.garganttua.api.spec.security.authorization.ISignableAuthorization;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKeyRealm;

public abstract class SignableAuthorization extends Authorization implements ISignableAuthorization {

	protected byte[] signature = null;
	protected KeyAlgorithm keyAlgorithm;
	protected SignatureAlgorithm signatureAlgorithm;

	public SignableAuthorization() {
		super();
	}

	public SignableAuthorization(byte[] raw) throws CoreException {
		super(raw);
		this.signature = this.getSignatureFromRaw(raw);
	}
	
	public SignableAuthorization(String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate) throws SecurityException {
		super(uuid, id, tenantId, ownerUuid, authorities, creationDate, expirationDate);
	}
	
	@Override
	@AuthorizationSign
	public void sign(IKeyRealm key) throws SecurityException {
		this.keyAlgorithm = key.getKeyAlgorithm();
		byte[] dataToSign = this.getDataToSign();
		try {
			this.signatureAlgorithm = key.getKeyForSigning().getSignatureAlgorithm();
			this.signature = key.getKeyForSigning().sign(dataToSign);
		} catch (CoreException e) {
			throw new SecurityException(e);
		}
	}

	@Override
	public byte[] getSignature() throws SecurityException {
		if (this.signature == null) {
			throw new SecurityException(CoreExceptionCode.AUTHORIZATION_NOT_SIGNED, "Authorization not signed");
		}
		return this.signature;
	}

	@Override
	protected void doValidation(Object ...args) throws SecurityException {
		IKeyRealm key = (IKeyRealm) args[0];

		try {
			key.getKeyForSignatureVerification().verifySignature(signature, this.getDataToSign());
		} catch (CoreException e) {
			throw new SecurityException(e);
		}
	}

	@Override
	protected void doValidationAgainst(IAuthorization authorization, Object ...args)
			throws SecurityException {
		if( !Arrays.equals(this.getSignature(), ((SignableAuthorization) authorization).getSignature()) ) {
			throw new SecurityException(CoreExceptionCode.TOKEN_SIGNATURE_MISMATCH, "Invalid signature");
		}
	}

	protected abstract byte[] getSignatureFromRaw(byte[] raw) throws CoreException;

	protected abstract byte[] getDataToSign();

}
