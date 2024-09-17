package com.garganttua.api.security.keys.domain;

import java.security.KeyPair;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.security.IGGAPIKey;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GGAPIKeyRealmEntity extends GenericGGAPIEntity implements IGGAPIKeyRealm {
	
	public static final String domain = "keys";
	
	@GGAPIEntityMandatory
	@Getter
	public String algorithm;
	
	public GGAPIKeyRealmType type;

	private GGAPIKey cipheringKey;

	private GGAPIKey uncipheringKey;
	
	public Date expiration;
	
	@GGAPIEntityAuthorizeUpdate()
	public boolean revoked;
	
	@GGAPIEntityBeforeCreate
	private void createKeys(IGGAPICaller caller, Map<String, String> params) throws GGAPISecurityException {
		String[] infos = GGAPIKeyValidator.validateAlgorithm(this.algorithm);
		this.type = GGAPIKeyValidator.determineAlgorithmType(this.algorithm);
		
		if( this.type == GGAPIKeyRealmType.SYMETRIC) {
			SecretKey key = GGAPIKeyValidator.generateSymetricKey(infos[0], Integer.valueOf(infos[1]));
			this.cipheringKey = new GGAPIKey(GGAPIKeyType.SECRET, key.getAlgorithm(), key.getEncoded());
			this.uncipheringKey = new GGAPIKey(GGAPIKeyType.SECRET, key.getAlgorithm(), key.getEncoded());
		} else {
			KeyPair keyPair = GGAPIKeyValidator.generateAsymetricKey(infos[0], Integer.valueOf(infos[1]));
			this.cipheringKey = new GGAPIKey(GGAPIKeyType.PRIVATE, keyPair.getPrivate().getAlgorithm(), keyPair.getPrivate().getEncoded());
			this.uncipheringKey = new GGAPIKey(GGAPIKeyType.PUBLIC, keyPair.getPublic().getAlgorithm(), keyPair.getPublic().getEncoded());
		}
	}

	@Override
	public String getName() {
		return this.id;
	}
	
	@Override
	public String getUuid() {
		return this.uuid;
	}

	@Override
	public boolean equals(IGGAPIKeyRealm object) {
		// TODO Auto-generated method stub
		return false;
	}

    @Override
    public IGGAPIKey getKeyForCiphering() throws GGAPISecurityException {
    	this.throwExceptionIfExpired();
    	this.throwExceptionIfRevoked();
		return this.cipheringKey;
	}
    
    private void throwExceptionIfRevoked() throws GGAPISecurityException {
    	if( this.revoked ) {
    		throw new GGAPISecurityException(GGAPIExceptionCode.KEY_REVOKED, "The key for realm "+this.id+" has expired");
    	}
	}

	private void throwExceptionIfExpired() throws GGAPISecurityException {
    	if( this.expiration != null && new Date().after(this.expiration) ) {
    		throw new GGAPISecurityException(GGAPIExceptionCode.KEY_EXPIRED, "The key for realm "+this.id+" has expired");
    	}
	}

    @Override
	public IGGAPIKey getKeyForUnciphering() throws GGAPISecurityException {
		this.throwExceptionIfExpired();
		this.throwExceptionIfRevoked();
		return this.uncipheringKey;
    }

	@Override
	public byte[] sign(byte[] toBeSigned) throws GGAPISecurityException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void verifySignature(byte[] signature, byte[] original) throws GGAPISecurityException {
		// TODO Auto-generated method stub
		
	}

	public static GGObjectAddress getExpirationFieldAddress() {
		try {
			return new GGObjectAddress("expiration");
		} catch (GGReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static GGObjectAddress getRevokedFieldAddress() {
		try {
			return new GGObjectAddress("revoked");
		} catch (GGReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static GGObjectAddress getOwnerIdFieldAddress() {
		try {
			return new GGObjectAddress("ownerId");
		} catch (GGReflectionException e) {
			//Should never happen
			return null;
		}
	}

	public static GGObjectAddress getAlgorithmFieldAddress() {
		try {
			return new GGObjectAddress("algorithm");
		} catch (GGReflectionException e) {
			//Should never happen
			return null;
		}
	}
}
