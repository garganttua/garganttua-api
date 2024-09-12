package com.garganttua.api.security.authorizations.spring.provider;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.spring.core.authentication.AbstractGGAPISpringAuthentication;
import com.garganttua.api.security.spring.core.authorizations.IGGAPISpringAuthorizationProvider;
import com.garganttua.api.security.spring.core.keys.IGGAPISpringKeyProvider;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIKey;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;

import io.jsonwebtoken.security.InvalidKeyException;
import io.jsonwebtoken.security.SignatureException;

@Service
public class GGAPIJwtAuthorizationProvider implements IGGAPISpringAuthorizationProvider {
	
//	private static final String keyRealmName = "jwt-signing-key";
	
	@Value("${com.garganttua.api.security.authorization.tokens.lifetime}")
	private int tokenLifetime;
	
	@Value("${com.garganttua.api.security.authorization.tokens.jwt.key.algorithm}")
	private String keyAlgorythm;

	@Value("${com.garganttua.api.security.authorization.tokens.jwt.key.realm}")
	private String keyRealmName;

	@Value("${com.garganttua.api.security.authorization.tokens.jwt.key.renewal}")
	private GGAPIKeyRenewal keyRenewal;

	@Value("${com.garganttua.api.security.authorization.tokens.jwt.key.lifetime}")
	private long keyLifetime;

	@Value("${com.garganttua.api.security.authorization.tokens.jwt.key.lifetime.unit}")
	private TimeUnit keyLifetimeUnit;

	@Autowired
	private IGGAPISpringKeyProvider keyProvider;

	@Override
	public IGGAPIAuthentication createAuthorization(IGGAPIAuthentication authentication) throws GGAPISecurityException {
		IGGAPIAuthorization authorization = this.generateAuthorization(authentication.getAuthenticator());
		((AbstractGGAPISpringAuthentication) authentication).setAuthorization(authorization);		
		return authentication;
	}
	
	private IGGAPIAuthorization generateAuthorization(IGGAPIAuthenticator authenticator) throws GGAPISecurityException {
		IGGAPIKeyRealm keyRealm = null;
		try {
			keyRealm = this.keyProvider.getRealm(authenticator, this.keyRealmName+"-tenant-"+authenticator.getTenantId());
		} catch(GGAPISecurityException e) {
			if( e.getCode() != GGAPIExceptionCode.ENTITY_NOT_FOUND ) {
				throw e;
			}
			keyRealm = this.keyProvider.createRealm(authenticator, this.keyRealmName+"-tenant-"+authenticator.getTenantId(), GGAPISpringJWTAuthorization.getJavaAlgorithmFromJJWT(this.keyAlgorythm), null);
		}

		try {
			IGGAPIKey keyForCiphering = keyRealm.getKeyForCiphering();
		
			Date expiration = null;
	
			if (this.tokenLifetime != 0) {
				long expirationDate = new Date().getTime() + TimeUnit.MINUTES.toMillis(this.tokenLifetime);
				expiration = new Date(expirationDate);
			}

			return new GGAPISpringJWTAuthorization (
					UUID.randomUUID().toString(), 
					authenticator.getTenantId(), 
					authenticator.getUuid(), 
					authenticator.getAuthoritiesList(), 
					new Date(), 
					expiration, 
					keyRealm.getUuid(),
					keyForCiphering );

		} catch (InvalidKeyException | SignatureException | GGAPIException e) {
			throw new GGAPISecurityException(e);
		}
	}

	

//	private <T> T extractClaim(byte[] token, Function<Claims, T> claimsResolver)
//			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
//			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
//		final Claims claims = extractAllClaims(token);
//		return claimsResolver.apply(claims);
//	}
//
//	private Claims extractAllClaims(byte[] token)
//			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
//			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
//		return Jwts.parserBuilder().setSigningKey(this.keyRealm.getCipheringKey().getKey()).build()
//				.parseClaimsJws(new String(token)).getBody();
//	}
//
//	public String extractUsername(byte[] token)
//			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
//			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
//		return extractClaim(token, Claims::getSubject);
//	}
//
//	private Boolean isTokenExpired(byte[] token)
//			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
//			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
//		return extractExpiration(token).before(new Date());
//	}
//
//	public Date extractExpiration(byte[] token)
//			throws GGAPIKeyExpiredException, SignatureException, ExpiredJwtException, UnsupportedJwtException,
//			MalformedJwtException, IllegalArgumentException, InvalidKeySpecException, NoSuchAlgorithmException {
//		return extractClaim(token, Claims::getExpiration);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public GGAPIToken validateAuthorization(byte[] token) throws GGAPIAuthorizationProviderException {
//
//		try {
//			Claims claims = extractAllClaims(token);
//
//			String ownerId = (String) claims.get("ownerId");
//			String uuid = (String) claims.get("uuid");
//			List<String> authorities = (List<String>) claims.get("authorities");
//			String tenantId = (String) claims.get("tenantId");
//			Integer iat = (Integer) claims.get("iat");
//			Integer exp = (Integer) claims.get("exp");
//
//			GGAPIToken tokenExample = new GGAPIToken(tenantId, uuid, ownerId, null, null, null, token, null);
//
//			GGAPIToken storeToken = this.findToken(tokenExample);
//
//			boolean storeToken__ = (storeToken != null && new String(token).equals(new String(storeToken.getToken())));
//
//			if (!storeToken__) {
//				return null;
//			}
//
//			boolean tokenValid = (storeToken.getOwnerId().equals(ownerId)
//					&& !(this.tokenLifetime != 0 && isTokenExpired(token)) && storeToken__);
//			if (!tokenValid) {
//				return null;
//			}
//
//			return new GGAPIToken(tenantId, storeToken.getUuid(), ownerId, new Date(iat * 1000), new Date(exp * 1000),
//					authorities, token, storeToken.getSigningKeyId());
//
//		} catch (GGAPIKeyExpiredException | SignatureException | ExpiredJwtException | UnsupportedJwtException
//				| MalformedJwtException | IllegalArgumentException | InvalidKeySpecException
//				| NoSuchAlgorithmException e) {
//			throw new GGAPIAuthorizationProviderException(e);
//		}
//	}

}
