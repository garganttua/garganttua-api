package com.garganttua.api.security.authorizations.spring.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.garganttua.api.security.authorizations.domain.GGAPIAuthorizationEntity;
import com.garganttua.api.security.authorizations.spring.entity.GGAPISpringSecurityAuthorizationEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityMandatory;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.security.IGGAPIKey;
import com.garganttua.api.spec.security.annotations.GGAPIEntitySecurity;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.NoArgsConstructor;
import lombok.Setter;

@GGAPIEntity(domain = GGAPIAuthorizationEntity.domain, interfaces = { "gg:SpringRestInterface" })
@JsonIgnoreProperties(value = { "gotFromRepository", "saveMethod", "deleteMethod", "repository", "save", "delete",
		"engine" })
@NoArgsConstructor
@GGAPIEntityOwned(ownerId = "ownerId")
@GGAPIEntitySecurity(
		creation_access = GGAPIServiceAccess.owner,
		delete_all_access = GGAPIServiceAccess.owner,
		delete_one_access = GGAPIServiceAccess.owner,
		read_all_access = GGAPIServiceAccess.owner,
		read_one_access = GGAPIServiceAccess.owner,
		update_one_access = GGAPIServiceAccess.owner
		)
public class GGAPISpringJWTAuthorization extends GGAPISpringSecurityAuthorizationEntity {

	@Setter
	private IGGAPIKey key;

	@GGAPIEntityMandatory
	private String jwtAlgo;

	public GGAPISpringJWTAuthorization(String uuid, String id, String tenantId, String ownerId, List<String> authorities,
			Date tokenCreation, Date tokenExpiration, String realmUuid, IGGAPIKey key, String jwtAlgo) {
		super(uuid, id, tenantId, ownerId, authorities, tokenCreation, tokenExpiration, realmUuid);
		this.key = key;
		this.jwtAlgo = jwtAlgo;
	}

	@Override
	public byte[] toByteArray() throws GGAPIException {
		Map<String, Object> claims = new HashMap<>();
		claims.put("tenantId", this.tenantId);
		claims.put("ownerId", this.ownerId);
		claims.put("uuid", this.uuid);

		JwtBuilder token = Jwts.builder().setClaims(claims).setSubject(this.id).setIssuedAt(this.creationDate)
				.signWith(this.key.getSigningKey(), SignatureAlgorithm.forName(this.jwtAlgo));

		token.setExpiration(this.expirationDate);
		return token.compact().getBytes();
	}

	@Override
	public byte[] getSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getJavaAlgorithmFromJJWT(String algorithm) throws GGAPISecurityException {
		switch (algorithm) {
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
		throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR,
				"Unsuported JWT algorithm " + algorithm);
	}

	@Override
	public void validateAgainst(byte[] authorization) throws GGAPIException {
		if( this.revoked ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_REVOKED, "Token revoked");
		}
		if( new Date().after(this.expirationDate) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_EXPIRED, "Token expired");
		}
		try {
			Jwts.parserBuilder()
			.setSigningKey(this.key.getSigningKey())
			.requireExpiration(this.expirationDate)
			.requireIssuedAt(this.creationDate)
			.requireSubject(this.id)
			.require("tenantId", this.tenantId)
			.require("ownerId", this.ownerId)
			.build().parse(new String(authorization));
		} catch (Exception e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, e.getMessage());
		}
	}
}
