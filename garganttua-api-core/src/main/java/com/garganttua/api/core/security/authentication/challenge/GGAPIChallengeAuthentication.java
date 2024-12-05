package com.garganttua.api.core.security.authentication.challenge;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.garganttua.api.spec.security.authentication.GGAPIChallengeAuthenticatorInfos;
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
//		IGGAPIKeyRealm realm = null;
//		byte[] challengeB64FromDB = null;
//		
//		//Reference Challenge
//
//		challengeB64FromDB = GGAPIChallengeEntityAuthenticatorHelper.getChallenge(this.principal);
//		
//		log.atDebug().log("B64 Challenge in DB for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
//		log.atDebug().log(new String(challengeB64FromDB));
//
//		byte[] challengeFromDB = Base64.getDecoder().decode(challengeB64FromDB);
//		
//		realm = GGAPIChallengeEntityAuthenticatorHelper.getKeyRealm(this.principal);
//		
//		//Received Challenge decoding
//		
//		log.atDebug().log("B64 Challenge received from entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
//		log.atDebug().log((String) this.credential);
//
//		byte[] receivedChallenge = Base64.getDecoder().decode(((String) this.credential));
//		
//		log.atDebug().log("B64 Public key for entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
//		log.atDebug().log(new String(realm.getKeyForDecryption().getRawKey()));
//		
//		byte[] decodedReceivedChallenge = realm.getKeyForDecryption().uncipher(receivedChallenge);
//	
//		System.out.println(new String(decodedReceivedChallenge));
//		
//		//MUST BE REMOVED BELOW :
//		
//		byte[] decodedChallengeFromDB = realm.getKeyForEncryption().uncipher(challengeFromDB);
//		String uuid = new String(decodedChallengeFromDB);
//		System.out.println(new String(decodedChallengeFromDB));
//		
//		byte[] encryptedChallenge = realm.getKeyForDecryption().cipher(uuid.getBytes());
//		byte[] challengeB64 = Base64.getEncoder().encode(encryptedChallenge);
//		
//		System.out.println(new String(challengeB64));
//		
//		encryptedChallenge = realm.getKeyForDecryption().cipher(uuid.getBytes());
//		challengeB64 = Base64.getEncoder().encode(encryptedChallenge);
//		
//		System.out.println(new String(challengeB64));
//		
//		encryptedChallenge = realm.getKeyForDecryption().cipher(uuid.getBytes());
//		challengeB64 = Base64.getEncoder().encode(encryptedChallenge);
//		
//		System.out.println(new String(challengeB64));
//		
//		//Challenge re-encoding
//		
//		byte[] reencodedReceivedChallenge = realm.getKeyForDecryption().cipher(decodedReceivedChallenge);
//		byte[] reencodedReceivedChallengeB64 = Base64.getEncoder().encode(reencodedReceivedChallenge);
//		
//		log.atDebug().log("B64 Re-encoded challenge received from entity "+this.principal.getClass().getSimpleName()+" identified by "+GGAPIEntityHelper.getUuid(this.principal));
//		log.atDebug().log(new String(reencodedReceivedChallengeB64));
//		
//		if (Arrays.equals(challengeB64FromDB, reencodedReceivedChallengeB64)) {
//			this.authenticated = true;
//		}
	}
	
	@GGAPIAuthenticationApplySecurity
	public void applySecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) throws GGAPIException {	
		if( !GGAPIEntityAuthenticatorHelper.isAuthenticator(entity.getClass()) ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Entity of type "+entity.getClass().getSimpleName()+" is not an authenticator");
		}
		
		String uuid = GGAPIEntityHelper.getUuid(entity);
		String challenge = UUID.randomUUID().toString();
		
		if( uuid == null || uuid.isEmpty() ) {
			uuid = UUID.randomUUID().toString();
			GGAPIEntityHelper.setUuid(entity, uuid);
		}
		String realmName = uuid+"-challenge-key";
		
		GGAPIChallengeAuthenticatorInfos challengeInfos = GGAPIChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());

		GGAPIMethod method = caller.getAccessRule().getOperation().getMethod();
		
		IGGAPIKeyRealm key = GGAPIChallengeEntityAuthenticatorHelper.getKeyRealm(entity);
		
		if( method  == GGAPIMethod.create && key == null) {
			this.getKeyAndCreateChallenge(caller, entity, uuid, challenge, challengeInfos, realmName);
			
		} 
		if( method == GGAPIMethod.update && params.get(PARAMETER_RENEW_KEY) != null && Boolean.valueOf(params.get(PARAMETER_RENEW_KEY)) ) {
			GGAPIKeyHelper.revokeAllForOwner(realmName, caller.getRequestedTenantId(), GGAPIEntityHelper.getOwnerId(entity), challengeInfos.key(), this.engine.getServicesRegistry());	
			this.getKeyAndCreateChallenge(caller, entity, uuid, challenge, challengeInfos, realmName);
			
		}
	}

	private void getKeyAndCreateChallenge(IGGAPICaller caller, Object entity, String uuid, String challenge,
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
		
		ObjectMapper mapper = new ObjectMapper();
		
		GGAPIChallengeEntityAuthenticatorHelper.setkeyRealm(entity, key);
		
		log.atDebug().log("B64 Public key generated for entity "+entity.getClass().getSimpleName()+" identified by "+uuid);
		log.atDebug().log(new String(key.getKeyForDecryption().getRawKey()));
		
		byte[] encryptedChallenge = key.getKeyForDecryption().encrypt(challenge.getBytes());
		byte[] challengeB64 = Base64.getEncoder().encode(encryptedChallenge);
		
		log.atDebug().log("B64 Challenge generated for entity "+entity.getClass().getSimpleName()+" identified by "+uuid);
		log.atDebug().log(new String(challengeB64));
		
		GGAPIChallengeEntityAuthenticatorHelper.setChallenge(entity, challengeB64);
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
