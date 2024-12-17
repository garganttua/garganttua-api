package com.garganttua.api.core.security.authorization.jwt;

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.security.authorization.GGAPISignableAuthorization;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationToByteArray;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationType;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKey;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

//@GGAPIAuthorization(signable = true)
@GGAPIEntityOwned
public class GGAPIJWTAuthorization extends GGAPISignableAuthorization {

	private byte[] signature = null;

	private byte[] raw = null;
	
	@GGAPIAuthorizationType
	private String type = "JWT";
	
	public GGAPIJWTAuthorization() {
		super(null, null);
	}
	
	public GGAPIJWTAuthorization(byte[] raw, IGGAPIKeyRealm realm) throws GGAPIException {
		super(raw, realm);
		
		try {
			this.raw = raw;
			
			String[] chunks = new String (this.raw).split("\\.");
			Base64.Decoder decoder = Base64.getDecoder();
			String payload = new String(decoder.decode(chunks[1]));
			
			this.signature = this.getSignatureFromRaw(this.raw);
			
			DocumentContext jsonPayload = JsonPath.parse(payload);
			this.uuid = jsonPayload.read("$['uuid']");
			this.tenantId = jsonPayload.read("$['tenantId']");
			this.ownerId = jsonPayload.read("$['ownerId']"); 
			this.authorities = jsonPayload.read("$['authorities'][*]");
			this.id = jsonPayload.read("$['sub']");
			int creationDateInt = jsonPayload.read("$['iat']");
			int expirationDateInt = jsonPayload.read("$['exp']");
			this.creationDate = Date.from(Instant.ofEpochSecond(creationDateInt));
			this.expirationDate = Date.from(Instant.ofEpochSecond(expirationDateInt));
		} catch(Exception e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.BAD_REQUEST, "Unable to decrypt JWT token from raw");
		}
	}
	
	public GGAPIJWTAuthorization(String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate, IGGAPIKeyRealm keyRealm) throws GGAPIException {
		super(uuid, id, tenantId, ownerUuid, authorities,
				creationDate, expirationDate, keyRealm);

		JwtBuilder token = createTokenFromThis();
		this.raw = token.compact().getBytes();
		
		this.signature = this.getSignatureFromRaw(this.raw);
	}
	
	@GGAPIAuthorizationToByteArray
	public byte[] toByteArray() throws GGAPIException {
		return this.raw;
	} 

	private byte[] getSignatureFromRaw(byte[] raw) { 
		String[] chunks = new String(raw).split("\\.");
		return chunks[2].getBytes(); 
	}

	public byte[] getSignature() {
		if( this.signature != null ) {
			return this.signature;
		}
		
		return null;
	}

	private static String getJJWTAlgorithmFromJava(GGAPIKeyAlgorithm keyAlgorithm) throws GGAPISecurityException {
	    switch (keyAlgorithm) {
	        case HMAC_SHA512_256:
	            return "HS256";
	        case HMAC_SHA512_384:
	            return "HS384";
	        case HMAC_SHA512_512:
	            return "HS512";
	        case RSA_2048:
	            return "RS256"; 
	        case RSA_3072:
	            return "RS384"; 
	        case RSA_4096:
	            return "RS512";
	        case EC_256:
	            return "ES256";
	        case EC_384:
	            return "ES384";
	        case EC_512:
	            return "ES512";
	    }
	    throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR,
	            "Unsupported Java algorithm " + keyAlgorithm);
	}

	private JwtBuilder createTokenFromThis() throws GGAPIException, GGAPISecurityException {
		Map<String, Object> claims = this.getClaims();
		IGGAPIKey keyForCiphering = this.key.getKeyForEncryption();
		SignatureAlgorithm forName = SignatureAlgorithm.forName(GGAPIJWTAuthorization.getJJWTAlgorithmFromJava(this.key.getKeyAlgorithm()));
		Key key__ = keyForCiphering.getKey();
		JwtBuilder token = Jwts.builder().setClaims(claims).setSubject(this.id).setIssuedAt(this.creationDate).setExpiration(this.expirationDate)
				.signWith(key__, forName);
		return token;
	}

	private Map<String, Object> getClaims() {
		Map<String, Object> claims = new HashMap<>();
		claims.put("tenantId", this.tenantId);
		claims.put("ownerId", this.ownerId);
		claims.put("uuid", this.uuid);
		claims.put("authorities", this.authorities);
		return claims;
	}

	@Override
	protected void doValidation() throws GGAPIException {

		JwtBuilder token = this.createTokenFromThis();
	
		byte[] rawTemp = token.compact().getBytes();
		byte[] signatureTemp = this.getSignatureFromRaw(rawTemp);
		
		if( !Arrays.equals(this.signature, signatureTemp) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH, "Invalid signature");
		}
	}

	@Override
	protected void doValidationAgainst(com.garganttua.api.core.security.authorization.GGAPIAuthorization authorization)
			throws GGAPISecurityException {
		if( !Arrays.equals(this.signature, ((GGAPIJWTAuthorization) authorization).signature) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH, "Invalid signature");
		}
		
	}
}
