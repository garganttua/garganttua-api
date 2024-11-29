package com.garganttua.api.core.security.key;

import java.security.KeyPair;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import com.garganttua.api.core.entity.GenericGGAPIEntity;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityAuthorizeUpdate;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPIKeyRealmType;
import com.garganttua.api.spec.security.key.GGAPIKeyType;
import com.garganttua.api.spec.security.key.IGGAPIKey;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@GGAPIEntityOwned(ownerId = "ownerId")
public class GGAPIKeyRealm extends GenericGGAPIEntity implements IGGAPIKeyRealm {
	
	public static final String domain = "keys";
	
	public GGAPIKeyRealm(String keyRealmName, GGAPIKeyAlgorithm algorithm, Date expiration) {
		this.id = keyRealmName;
		this.algorithm = algorithm;
		this.expiration = expiration;
	}
	
	@GGAPIEntityMandatory
	@Getter
	protected GGAPIKeyAlgorithm algorithm;
	@Getter
	protected GGAPIKeyRealmType type;
	@Getter
	protected GGAPIKey cipheringKey;
	@Getter
	protected GGAPIKey uncipheringKey;
	@Getter
	protected String ownerId;
	@Getter
	protected Date expiration;
	@Getter
	@GGAPIEntityAuthorizeUpdate()
	protected boolean revoked;
	
	@GGAPIEntityBeforeCreate
	public void createKeys(IGGAPICaller caller, Map<String, String> params) throws GGAPISecurityException {
		this.type = GGAPIKeyAlgorithm.determineAlgorithmType(this.algorithm);
		
		if( this.type == GGAPIKeyRealmType.SYMETRIC) {
			SecretKey key = GGAPIKeyAlgorithm.generateSymetricKey(this.algorithm);
			this.cipheringKey = new GGAPIKey(GGAPIKeyType.SECRET, key.getAlgorithm(), key.getEncoded());
			this.uncipheringKey = new GGAPIKey(GGAPIKeyType.SECRET, key.getAlgorithm(), key.getEncoded());
		} else {
			KeyPair keyPair = GGAPIKeyAlgorithm.generateAsymetricKey(this.algorithm);
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
