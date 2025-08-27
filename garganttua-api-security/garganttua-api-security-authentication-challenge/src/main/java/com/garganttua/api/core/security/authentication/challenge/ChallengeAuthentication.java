package com.garganttua.api.core.security.authentication.challenge;

import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.core.security.key.KeyHelper;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.Method;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.security.annotations.CustomServiceSecurity;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.api.spec.service.CustomService;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServiceCommand;
import com.garganttua.api.spec.service.IServiceResponse;

import lombok.extern.slf4j.Slf4j;

@Authentication (
	findPrincipal = true
)
@Slf4j
public class ChallengeAuthentication extends AbstractAuthentication {

	public ChallengeAuthentication(IDomain domain) {
		super(domain);
	}
	
	public ChallengeAuthentication() {
		super(null);
	}

	public static final String CHALLENGE_KEY_REALM_NAME_PREFIX = "-challenge-key";
	
	@Inject 
	private IEngine engine;

	@Override
	protected void doAuthentication() throws CoreException {
		if( !EntityAuthenticatorHelper.isAuthenticator(this.principal) ) {
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, "Authenticator as principal is mandatory for Challenge authentication, verify that findPrincipal is set to true");
		}
		log.atDebug().log("Challenge signature received from entity "+this.principal.getClass().getSimpleName()+" identified by "+EntityHelper.getUuid(this.principal));
		log.atDebug().log((String) this.credential);
		
		Challenge challenge = ChallengeEntityAuthenticatorHelper.getChallenge(this.principal);
		
		if( challenge.getChallenge() == null ) {
			log.atInfo().log("No challenge for entity "+this.principal.getClass().getSimpleName()+" identified by "+EntityHelper.getUuid(this.principal));
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "No challenge for entity "+this.principal.getClass().getSimpleName()+" identified by "+EntityHelper.getUuid(this.principal));
		}
		
		log.atDebug().log("Challenge in DB for entity "+this.principal.getClass().getSimpleName()+" identified by "+EntityHelper.getUuid(this.principal));
		log.atDebug().log(new String(challenge.getChallenge()));
		
		if( !EntityAuthenticatorHelper.isCredentialsNonExpired(this.principal) ) {
			log.atInfo().log("Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+EntityHelper.getUuid(this.principal));
			throw new SecurityException(CoreExceptionCode.TOKEN_EXPIRED, "Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+EntityHelper.getUuid(this.principal));
		}
		
		if( challenge.getExpiration() != null &&Instant.now().isAfter(challenge.getExpiration().toInstant()) ) {
			log.atInfo().log("Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+EntityHelper.getUuid(this.principal));
			EntityAuthenticatorHelper.setCredentialsNonExpired(this.principal, false);
			EntityHelper.save(this.principal, Caller.createTenantCaller(this.tenantId), new HashMap<String, String>());
			throw new SecurityException(CoreExceptionCode.TOKEN_EXPIRED, "Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+EntityHelper.getUuid(this.principal));
		}
		
		IKeyRealm realm = ChallengeEntityAuthenticatorHelper.getKeyRealm(this.principal);

		if( realm.getKeyForSignatureVerification().verifySignature(Base64.getDecoder().decode((String) this.credential), challenge.getChallenge()) ) {
			if( challenge.getType() == ChallengeType.ONE_TIME ) {
				challenge.setChallenge(null);
				challenge.setExpiration(null);
				EntityAuthenticatorHelper.setCredentialsNonExpired(this.principal, false);
				ChallengeEntityAuthenticatorHelper.setChallenge(this.principal, challenge);
			}
			this.authenticated = true;
		}
		EntityHelper.save(this.principal, Caller.createTenantCaller(this.tenantId), new HashMap<String, String>());
	}
	
	@AuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) throws CoreException {	
		if( !EntityAuthenticatorHelper.isAuthenticator(entity.getClass()) ) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity of type "+entity.getClass().getSimpleName()+" is not an authenticator");
		}
		
		String uuid = EntityHelper.getUuid(entity);

		if( uuid == null || uuid.isEmpty() ) {
			uuid = UUID.randomUUID().toString();
			EntityHelper.setUuid(entity, uuid);
		}
		String realmName = uuid+CHALLENGE_KEY_REALM_NAME_PREFIX;
		
		ChallengeAuthenticatorInfos challengeInfos = ChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());

		Method method = caller.getMethod();
	
		if( method  == Method.create) {
			this.getKey(caller, entity, uuid, challengeInfos, realmName);
		} 
	}
	
	@AuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
		//Nothing to do
	}

	private void getKey(ICaller caller, Object entity, String uuid,
			ChallengeAuthenticatorInfos challengeInfos, String realmName) throws EngineException, CoreException {
		IKeyRealm key;
		key = KeyHelper.getKey(
				realmName,
				challengeInfos.key(), 
				AuthenticatorKeyUsage.oneForEach,
				challengeInfos.autoCreateKey(),
				challengeInfos.keyAlgorithm(),
				challengeInfos.keyLifeTime(),
				challengeInfos.keyLifeTimeUnit(),
				EntityHelper.getOwnerId(entity), 
				caller.getRequestedTenantId(), 
				this.engine, 
				challengeInfos.encryptionMode(), 
				challengeInfos.encryptionPadding(), 
				challengeInfos.signatureAlgorithm());
		
		ChallengeEntityAuthenticatorHelper.setkeyRealm(entity, key);
		
		log.atDebug().log("B64 Public key generated for entity "+entity.getClass().getSimpleName()+" identified by "+uuid);
		log.atDebug().log(new String(key.getKeyForDecryption().getRawKey()));
	}

	@Override
	protected Object doFindPrincipal(ICaller caller) {
		IServiceResponse getPrincipalResponse = this.authenticatorService.getEntity(caller, (String) this.principal, new HashMap<String, String>());
    if( getPrincipalResponse.getResponseCode() == ServiceResponseCode.OK ) {
    	 return getPrincipalResponse.getResponse();
    } else {
    	log.atDebug().log("Failed to find principal identified by id "+this.principal);
    	return null;
    }
	}
	
	@CustomServiceSecurity(access = ServiceAccess.anonymous)
	@CustomService(actionOnAllEntities = false, entity = Challenge.class, method = Method.read, path = "/api/{domain}/{uuid}/challenge")
	public IServiceResponse getChallenge(
			ICaller caller,
			Map<String, String> customParameters, String uuid) {

		IServiceCommand command = (event) -> {
			IService authenticatorService = this.engine.getService(this.domain.getDomain());
			IServiceResponse getAuthenticatorResponse = authenticatorService.getEntity(caller, uuid,
					new HashMap<String, String>());
	
			if (getAuthenticatorResponse.getResponseCode() != ServiceResponseCode.OK) {
				event.setCode(getAuthenticatorResponse.getResponseCode());
				event.setOut(getAuthenticatorResponse.getResponse());
				return event;
			}
			Challenge challenge = ChallengeEntityAuthenticatorHelper.getOrCreateChallengeAndSave(caller,
					getAuthenticatorResponse.getResponse());

			event.setOut(challenge);
			event.setCode(ServiceResponseCode.OK);
			return event;
		};
		return this.executeServiceCommand(caller, () -> {return true;}, command, customParameters, null);
	}

	@CustomServiceSecurity(access = ServiceAccess.owner)
	@CustomService(actionOnAllEntities = false, entity = IKeyRealm.class, method = Method.create, path = "/api/{domain}/{uuid}/keys/renew")
	public IServiceResponse renewKeys(
			ICaller caller,
			Map<String, String> customParameters, String uuid) {
		
		IServiceCommand command = (event) -> {
			IService authenticatorService = this.engine.getService(this.domain.getDomain());
			IServiceResponse getAuthenticatorResponse = authenticatorService.getEntity(caller, uuid,
					new HashMap<String, String>());
	
			if (getAuthenticatorResponse.getResponseCode() != ServiceResponseCode.OK) {
				event.setCode(getAuthenticatorResponse.getResponseCode());
				event.setOut(getAuthenticatorResponse.getResponse());
				return event;
			}
			
			Object entity = getAuthenticatorResponse.getResponse();
			
			ChallengeAuthenticatorInfos infos = ChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(this.domain.getEntityClass());
			
			KeyHelper.revokeAllForOwner(
					uuid+ChallengeAuthentication.CHALLENGE_KEY_REALM_NAME_PREFIX, 
					caller.getTenantId(), 
					EntityHelper.getOwnerId(entity), 
					infos.key(), 
					this.engine);
			IKeyRealm key;
			key = KeyHelper.getKey(
					uuid+ChallengeAuthentication.CHALLENGE_KEY_REALM_NAME_PREFIX,
					infos.key(), 
					AuthenticatorKeyUsage.oneForEach,
					infos.autoCreateKey(),
					infos.keyAlgorithm(),
					infos.keyLifeTime(),
					infos.keyLifeTimeUnit(),
					EntityHelper.getOwnerId(entity), 
					caller.getRequestedTenantId(), 
					this.engine, 
					infos.encryptionMode(), 
					infos.encryptionPadding(), 
					infos.signatureAlgorithm());
			ChallengeEntityAuthenticatorHelper.setkeyRealm(entity, key);
			EntityHelper.save(entity, caller, new HashMap<String, String>());

			event.setOut(entity);
			event.setCode(ServiceResponseCode.OK);
			return event;
		};
		return this.executeServiceCommand(caller, () -> {return true;}, command, customParameters, null);
	}

}
