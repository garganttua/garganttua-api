package com.garganttua.api.security.authorizations.spring.jwt;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.garganttua.api.security.authorizations.domain.GGAPIAuthorizationEntity;
import com.garganttua.api.security.authorizations.spring.entity.GGAPISpringSecurityAuthorizationEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.security.IGGAPIKey;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.NoArgsConstructor;
import lombok.Setter;

@GGAPIEntity(domain = GGAPIAuthorizationEntity.domain, interfaces = { "gg:SpringRestInterface" })
@JsonIgnoreProperties(value = { "gotFromRepository","saveMethod","deleteMethod", "repository", "save", "delete", "engine" })
@NoArgsConstructor
public class GGAPISpringJWTAuthorization extends GGAPISpringSecurityAuthorizationEntity {
	@Setter
	private IGGAPIKey key;
	
	@GGAPIEntityMandatory
	private String jwtAlgo;

	public GGAPISpringJWTAuthorization(String uuid, String tenantId, String ownerId, Collection<String> authorities,
			Date tokenCreation, Date tokenExpiration, String realmUuid, IGGAPIKey key, String jwtAlgo) {
		super(uuid, tenantId, ownerId, authorities, tokenCreation, tokenExpiration, realmUuid);
		this.key = key;
		this.jwtAlgo = jwtAlgo;
	}
	
	@Override
	public byte[] toByteArray() throws GGAPIException {
		Map<String, Object> claims = new HashMap<>();
		claims.put("tenantId", this.tenantId);
		claims.put("ownerId", this.ownerId);
		claims.put("uuid", this.uuid);

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
