package com.garganttua.api.core.security.authentication.challenge;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.entity.tools.EntityAuthenticatorHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.core.security.key.KeyHelper;
import com.garganttua.api.commons.CoreExceptionCode;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.commons.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.commons.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.commons.security.key.IKeyRealm;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;

import lombok.extern.slf4j.Slf4j;

@Authentication (
	findPrincipal = true
)
@Slf4j
public class ChallengeAuthentication extends AbstractAuthentication {

	public ChallengeAuthentication(IDomain<?> domainContext) {
		super(domainContext);
	}

	public ChallengeAuthentication() {
		super(null);
	}

	public static final String CHALLENGE_KEY_REALM_NAME_PREFIX = "-challenge-key";

	@Inject
	private IApi apiContext;

	@Override
	protected void doAuthentication() throws CoreException {
		if( !EntityAuthenticatorHelper.isAuthenticator(this.principal) ) {
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, "Authenticator as principal is mandatory for Challenge authentication, verify that findPrincipal is set to true");
		}
		String uuid = getUuid(this.principal);
		log.atDebug().log("Challenge signature received from entity "+this.principal.getClass().getSimpleName()+" identified by "+uuid);
		log.atDebug().log((String) this.credential);

		Challenge challenge = ChallengeEntityAuthenticatorHelper.getChallenge(this.principal);

		if( challenge.getChallenge() == null ) {
			log.atInfo().log("No challenge for entity "+this.principal.getClass().getSimpleName()+" identified by "+uuid);
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "No challenge for entity "+this.principal.getClass().getSimpleName()+" identified by "+uuid);
		}

		log.atDebug().log("Challenge in DB for entity "+this.principal.getClass().getSimpleName()+" identified by "+uuid);
		log.atDebug().log(new String(challenge.getChallenge()));

		if( !EntityAuthenticatorHelper.isCredentialsNonExpired(this.principal) ) {
			log.atInfo().log("Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+uuid);
			throw new SecurityException(CoreExceptionCode.TOKEN_EXPIRED, "Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+uuid);
		}

		if( challenge.getExpiration() != null && Instant.now().isAfter(challenge.getExpiration().toInstant()) ) {
			log.atInfo().log("Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+uuid);
			EntityAuthenticatorHelper.setCredentialsNonExpired(this.principal, false);
			this.authenticatorDomain.updateOne(uuid, this.principal, Caller.createTenantCaller(this.tenantId));
			throw new SecurityException(CoreExceptionCode.TOKEN_EXPIRED, "Challenge expired for entity "+this.principal.getClass().getSimpleName()+" identified by "+uuid);
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
		this.authenticatorDomain.updateOne(uuid, this.principal, Caller.createTenantCaller(this.tenantId));
	}

	@AuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) throws CoreException {
		if( !EntityAuthenticatorHelper.isAuthenticator(entity.getClass()) ) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity of type "+entity.getClass().getSimpleName()+" is not an authenticator");
		}

		String uuid = getUuid(entity);

		if( uuid == null || uuid.isEmpty() ) {
			uuid = UUID.randomUUID().toString();
			setUuid(entity, uuid);
		}
		String realmName = uuid+CHALLENGE_KEY_REALM_NAME_PREFIX;

		ChallengeAuthenticatorInfos challengeInfos = ChallengeEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entity.getClass());

		// In v3, the pipeline handles method routing; always create key if needed
		this.getKey(caller, entity, uuid, challengeInfos, realmName);
	}

	@AuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
		//Nothing to do
	}

	private void getKey(ICaller caller, Object entity, String uuid,
			ChallengeAuthenticatorInfos challengeInfos, String realmName) throws CoreException {
		IKeyRealm key;
		key = KeyHelper.getKey(
				realmName,
				challengeInfos.key(),
				AuthenticatorKeyUsage.oneForEach,
				challengeInfos.autoCreateKey(),
				challengeInfos.keyAlgorithm(),
				challengeInfos.keyLifeTime(),
				challengeInfos.keyLifeTimeUnit(),
				getOwnerId(entity),
				caller.requestedTenantId(),
				this.apiContext,
				challengeInfos.encryptionMode(),
				challengeInfos.encryptionPadding(),
				challengeInfos.signatureAlgorithm());

		ChallengeEntityAuthenticatorHelper.setkeyRealm(entity, key);

		log.atDebug().log("B64 Public key generated for entity "+entity.getClass().getSimpleName()+" identified by "+uuid);
		log.atDebug().log(new String(key.getKeyForDecryption().getRawKey()));
	}

	@Override
	protected Object doFindPrincipal(ICaller caller) {
		try {
			IOperationResponse response = this.authenticatorDomain.readOne((String) this.principal, caller);
			if( response.getResponseCode() == OperationResponseCode.OK ) {
				return response.getResponse();
			} else {
				log.atDebug().log("Failed to find principal identified by id "+this.principal);
				return null;
			}
		} catch (Exception e) {
			log.atDebug().log("Failed to find principal identified by id "+this.principal, e);
			return null;
		}
	}

	/*
	 * TODO: Port custom services to v3 use cases
	 *
	 * The getChallenge() and renewKeys() custom service methods from v2 are not yet ported.
	 * They relied on @CustomService, @CustomServiceSecurity, IServiceCommand, IEngine, and
	 * executeServiceCommand() which do not exist in v3.
	 */

	// --- Helper methods ---

	private String getUuid(Object entity) throws CoreException {
		ObjectAddress uuidAddress = this.authenticatorDomain.getUuidFieldAddress();
		return (String) DefaultMapper.reflection().getFieldValue(entity, uuidAddress);
	}

	private void setUuid(Object entity, String uuid) throws CoreException {
		ObjectAddress uuidAddress = this.authenticatorDomain.getUuidFieldAddress();
		DefaultMapper.reflection().setFieldValue(entity, uuidAddress, uuid);
	}

	private String getOwnerId(Object entity) throws CoreException {
		ObjectAddress ownerIdAddress = this.authenticatorDomain.getOwnerIdFieldAddress();
		if (ownerIdAddress == null) return null;
		return (String) DefaultMapper.reflection().getFieldValue(entity, ownerIdAddress);
	}

}
