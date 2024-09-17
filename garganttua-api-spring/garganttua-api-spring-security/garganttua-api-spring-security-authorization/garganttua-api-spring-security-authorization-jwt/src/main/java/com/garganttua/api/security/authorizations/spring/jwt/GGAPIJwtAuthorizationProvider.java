package com.garganttua.api.security.authorizations.spring.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.security.authorizations.domain.GGAPIAuthorizationEntity;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.spring.core.authentication.AbstractGGAPISpringAuthentication;
import com.garganttua.api.security.spring.core.authorizations.IGGAPISpringAuthorizationProvider;
import com.garganttua.api.security.spring.core.keys.GGAPISpringSecurityKeyEntityRequest;
import com.garganttua.api.security.spring.core.keys.IGGAPISpringKeyProvider;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import jakarta.annotation.PostConstruct;

@Service
public class GGAPIJwtAuthorizationProvider implements IGGAPISpringAuthorizationProvider {
	
	@Value("${com.garganttua.api.security.authorization.tokens.lifetime}")
	private int tokenLifetime;
	
	@Value("${com.garganttua.api.security.keys.provider.tokens.jwt.key.algorithm}")
	private String algorithm;

	@Autowired
	private IGGAPISpringKeyProvider keyProvider;
	
	@Autowired
	private IGGAPIEngine engine;

	private IGGAPIService authorizationService;
	
	@PostConstruct
	private void init() {
		this.authorizationService = this.engine.getServicesRegistry().getService(GGAPIAuthorizationEntity.domain);
	}

	@Override
	public IGGAPIAuthentication createAuthorization(IGGAPIAuthentication authentication) throws GGAPIException {
		IGGAPIAuthorization authorization = this.generateAuthorization(authentication.getAuthenticator());
		((AbstractGGAPISpringAuthentication) authentication).setAuthorization(authorization);		
		return authentication;
	}
	
	private IGGAPIAuthorization generateAuthorization(IGGAPIAuthenticator authenticator) throws GGAPIException {
		GGAPISpringJWTAuthorization token = null;
		if((token = this.lookForToken(authenticator, token)) == null ) {
			token = this.createNewToken(authenticator, token);
		}		
		return token;
	}

	private GGAPISpringJWTAuthorization createNewToken(IGGAPIAuthenticator authenticator, GGAPISpringJWTAuthorization token)
			throws GGAPISecurityException {
		GGAPISpringSecurityKeyEntityRequest request = new GGAPISpringSecurityKeyEntityRequest(authenticator.getTenantId(), authenticator.getUuid(), "jwt-signing-key-tenant-"+authenticator.getTenantId(), GGAPISpringJWTAuthorization.getJavaAlgorithmFromJJWT(this.algorithm));
		IGGAPIKeyRealm keyRealm = this.keyProvider.getRealm(request);
		
		try {
		
			Date tokenExpiration = null;

			if (this.tokenLifetime != 0) {
				long tokenExpirationDate = new Date().getTime() + TimeUnit.MINUTES.toMillis(this.tokenLifetime);
				tokenExpiration = new Date(tokenExpirationDate);
			}

			token = new GGAPISpringJWTAuthorization (
					UUID.randomUUID().toString(), 
					authenticator.getTenantId(), 
					authenticator.getUuid(), 
					authenticator.getAuthoritiesList(), 
					new Date(), 
					tokenExpiration, 
					keyRealm.getUuid(),
					keyRealm.getKeyForCiphering(), 
					this.algorithm);
			
			this.authorizationService.createEntity(GGAPICaller.createTenantCaller(authenticator.getTenantId()), token, new HashMap<String, String> ());
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(e);
		}
		return token;
	}

	@SuppressWarnings("unchecked")
	private GGAPISpringJWTAuthorization lookForToken(IGGAPIAuthenticator authenticator, GGAPISpringJWTAuthorization token) throws GGAPIException, GGAPISecurityException {
		IGGAPIFilter filter = this.buildAuthorizationFilter(authenticator);
		IGGAPIServiceResponse response = this.authorizationService.getEntities(GGAPICaller.createTenantCaller(authenticator.getTenantId()), GGAPIReadOutputMode.full, null, filter, null, new HashMap<String, String>());
		
		if( response.getResponseCode() == GGAPIServiceResponseCode.OK ) {
			List<GGAPISpringJWTAuthorization> authorizations = (List<GGAPISpringJWTAuthorization>) response.getResponse();
			if( authorizations.size() > 0) {
				token = authorizations.get(0);
				
				String realmUuid = token.getSigningKeyUuid();
				
				try {
					IGGAPIKeyRealm realm = this.keyProvider.getRealm(realmUuid);
					token.setKey(realm.getKeyForCiphering());
				} catch(GGAPIException e) {
					if( e.getCode() != GGAPIExceptionCode.ENTITY_NOT_FOUND 
							&& e.getCode() != GGAPIExceptionCode.KEY_EXPIRED 
							&& e.getCode() != GGAPIExceptionCode.KEY_REVOKED ) {
						throw e;
					}
					token.revoke();
					IGGAPIServiceResponse updateResponse = this.authorizationService.updateEntity(GGAPICaller.createTenantCaller(authenticator.getTenantId()), token.getUuid(), token, new HashMap<String, String>());
					token = null;
					if( updateResponse.getResponseCode() != GGAPIServiceResponseCode.UPDATED ) {
						throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, updateResponse.getResponse().toString());
					}
				}
			}
		} else {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Unknown error during authorization for tenant "+authenticator.getTenantId()+" and owner "+authenticator.getId()+" retrival");
		}
		return token;
	}

	private IGGAPIFilter buildAuthorizationFilter(IGGAPIAuthenticator authenticator) {
		GGAPILiteral expirationFilter = GGAPILiteral.gt("expirationDate", new Date());
		GGAPILiteral ownerIdFilter = GGAPILiteral.eq("ownerId", authenticator.getUuid());
		GGAPILiteral revokedFilter = GGAPILiteral.eq("revoked", false);
		IGGAPIFilter filter = GGAPILiteral.and(expirationFilter, ownerIdFilter, revokedFilter);
		return filter;
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
