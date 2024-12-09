package com.garganttua.api.core.security.authentication.challenge;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.security.key.GGAPIKeyHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.GGAPIMethod;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationApplySecurity;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorKeyUsage;
import com.garganttua.api.spec.security.authentication.GGAPIChallenge;
import com.garganttua.api.spec.security.authentication.GGAPIChallengeAuthenticatorInfos;
import com.garganttua.api.spec.security.authentication.GGAPIChallengeType;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@GGAPIAuthentication (
	findPrincipal = true
)
@NoArgsConstructor
@Slf4j
public class GGAPIChallengeAuthentication extends AbstractGGAPIAuthentication {

	private static final Object PARAMETER_RENEW_KEY = "renew_key";
	
	@Inject 
	private IGGAPIEngine engine;

	@Override
	protected void doAuthentication() throws GGAPIException {
		if( !GGAPIEntityAuthenticatorHelper.isAuthenticator(this.principal) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, "Authenticator as principal is mandatory for Challenge authentication, verify that findPrincipal is set to true");
		}
		log.atDebug().log("Challenge signature received from entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
		log.atDebug().log((String) this.credential);
		
		GGAPIChallenge challenge = GGAPIChallengeEntityAuthenticatorHelper.getChallenge(this.principal);
		
		if( challenge.getChallenge() == null ) {
			log.atInfo().log("No challenge for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "No challenge for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
		}
		
		log.atDebug().log("Challenge in DB for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
		log.atDebug().log(new String(challenge.getChallenge()));
		
		if( !GGAPIEntityAuthenticatorHelper.isCredentialsNonExpired(this.principal) ) {
			log.atInfo().log("Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_EXPIRED, "Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
		}
		
		if( challenge.getExpiration() != null &&Instant.now().isAfter(challenge.getExpiration().toInstant()) ) {
			log.atInfo().log("Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
			GGAPIEntityAuthenticatorHelper.setCredentialsNonExpired(this.principal, false);
			GGAPIEntityHelper.save(this.principal, GGAPICaller.createTenantCaller(this.tenantId), new HashMap<String, String>());
			throw new GGAPISecurityException(GGAPIExceptionCode.TOKEN_EXPIRED, "Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
		}
		
		IGGAPIKeyRealm realm = GGAPIChallengeEntityAuthenticatorHelper.getKeyRealm(this.principal);

		if( realm.getKeyForSignatureVerification().verifySignature(Base64.getDecoder().decode((String) this.credential), challenge.getChallenge()) ) {
			if( challenge.getType() == GGAPIChallengeType.ONE_TIME ) {
				challenge.setChallenge(null);
				challenge.setExpiration(null);
				GGAPIEntityAuthenticatorHelper.setCredentialsNonExpired(this.principal, false);
				GGAPIChallengeEntityAuthenticatorHelper.setChallenge(this.principal, challenge);
			}
			this.authenticated = true;
		}
		GGAPIEntityHelper.save(this.principal, GGAPICaller.createTenantCaller(this.tenantId), new HashMap<String, String>());
	}
	
	@GGAPIAuthenticationApplySecurity
	public void applySecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) throws GGAPIException {	
		if( !GGAPIEntityAuthenticatorHelper.isAuthenticator(entity.getClass()) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Entity of type "+entity.getClass().getSimpleName()+" is not an authenticator");
		}
		
		String uuid = GGAPIEntityHelper.getUuid(entity);

		if( uuid == null || uuid.isEmpty() ) {
			uuid = UUID.randomUUID().toString();
			GGAPIEntityHelper.setUuid(entity, uuid);
		}
		String realmName = uuid+"-challenge-key";
		
		GGAPIChallengeAuthenticatorInfos challengeInfos = GGAPIChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());

		GGAPIMethod method = caller.getAccessRule().getOperation().getMethod();
		
		IGGAPIKeyRealm key = GGAPIChallengeEntityAuthenticatorHelper.getKeyRealm(entity);
		
		if( method  == GGAPIMethod.create && key == null) {
			this.getKey(caller, entity, uuid, challengeInfos, realmName);
			
		} 
		if( method == GGAPIMethod.update && params.get(PARAMETER_RENEW_KEY) != null && Boolean.valueOf(params.get(PARAMETER_RENEW_KEY)) ) {
			GGAPIKeyHelper.revokeAllForOwner(realmName, caller.getRequestedTenantId(), GGAPIEntityHelper.getOwnerId(entity), challengeInfos.key(), this.engine.getServicesRegistry());	
			this.getKey(caller, entity, uuid, challengeInfos, realmName);
		}
	}

	private void getKey(IGGAPICaller caller, Object entity, String uuid,
			GGAPIChallengeAuthenticatorInfos challengeInfos, String realmName) throws GGAPIEngineException, GGAPIException {
		IGGAPIKeyRealm key;
		key = GGAPIKeyHelper.getKey(
				realmName,
				challengeInfos.key(), 
				GGAPIAuthenticatorKeyUsage.oneForEach,
				challengeInfos.autoCreateKey(),
				challengeInfos.keyAlgorithm(),
				challengeInfos.keyLifeTime(),
				challengeInfos.keyLifeTimeUnit(),
				GGAPIEntityHelper.getOwnerId(entity), 
				caller.getRequestedTenantId(), 
				this.engine.getTenantsDomain(), 
				this.engine.getServicesRegistry(), 
				challengeInfos.encryptionMode(), 
				challengeInfos.encryptionPadding(), 
				challengeInfos.signatureAlgorithm());
		
		GGAPIChallengeEntityAuthenticatorHelper.setkeyRealm(entity, key);
		
		log.atDebug().log("B64 Public key generated for entity "+entity.getClass().getSimpleName()+" identified by "+uuid);
		log.atDebug().log(new String(key.getKeyForDecryption().getRawKey()));
	}

	@Override
	protected Object doFindPrincipal() {
		try {
			IGGAPIServiceResponse getPrincipalResponse = this.authenticatorService.getEntity(GGAPICaller.createTenantCaller(this.tenantId), (String) this.principal, new HashMap<String, String>());
			if( getPrincipalResponse.getResponseCode() == GGAPIServiceResponseCode.OK ) {
				 return getPrincipalResponse.getResponse();
			} else {
				log.atDebug().log("Failed to find principal identified by id "+this.principal);
				return null;
			}	
		} catch (GGAPIException e) {
			log.atDebug().log("Failed to find principal identified by id "+this.principal, e);
			return null;
		}
	}

}
