package com.garganttua.api.security.authorizations.spring.jwt;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIKey;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GGAPISpringJWTAuthorization implements IGGAPIAuthorization {
	
	private String uuid;
	private String tenantId;
	private String ownerId;
	private Collection<String> authorities;
	private Date creationDate;
	private Date expirationDate;
	private String signingKeyUuid;
	private IGGAPIKey key;
	private String jwtAlgo;

	@Override
	public byte[] toByteArray() throws GGAPIException {
		Map<String, Object> claims = new HashMap<>();
		claims.put("tenantId", this.tenantId);
		claims.put("ownerId", this.ownerId);
		claims.put("uuid", this.uuid);
//		claims.put("authorities", authorities);
		
		JwtBuilder token = Jwts.builder().setClaims(claims).setSubject(this.ownerId)
				.setIssuedAt(this.creationDate).signWith(this.key.getSigningKey(), SignatureAlgorithm.forName(this.jwtAlgo));
		
		token.setExpiration(this.expirationDate);
		return token.compact().getBytes();
	}

	@Override
	public byte[] getSignature() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public static String getJavaAlgorithmFromJJWT(String algorithm) throws GGAPISecurityException {
		switch(algorithm) {
		case "HS256":
			return "HmacSHA512-256";
		case "HS384":
			return "HmacSHA512-384";
		case "HS512":
			return "HmacSHA512-512";
		case "RS256":
			return "RSA-2048";
		case "RS384":
			return "RSA-3072";
		case "RS512":
			return "RSA-4096";
		case "PS256":
			return "RSA-2048";
		case "PS384":
			return "RSA-3072";
		case "PS512":
			return "RSA-4096";
		case "ES256":
			return "EC-256";
		case "ES384":
			return "EC-384";
		case "ES512":
			return "EC-512";
		}
		throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Unsuported JWT algorithm "+algorithm);
	}

}
