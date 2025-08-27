package com.garganttua.api.core.security.authentication.authorization;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.authentication.AuthenticationService;
import com.garganttua.api.core.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.core.security.key.KeyHelper;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.security.key.IKeyRealm;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IServiceResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Authentication(findPrincipal = true)
public class StorableAuthorizationAuthentication extends AbstractAuthentication {

	public StorableAuthorizationAuthentication(IDomain domain) {
		super(domain);
	}

	public StorableAuthorizationAuthentication() {
		super(null);
	}

	@Inject 
	private IEngine engine;

	@Override
	protected void doAuthentication() throws CoreException {
		if (EntityAuthorizationHelper.isAuthorization(this.principal)) {
			this.ownerId = EntityAuthorizationHelper.getOwnerId(this.principal);

			if (EntityAuthorizationHelper.isSignable(this.credential.getClass())) {
				IKeyRealm key = KeyHelper.getKey(
						AuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME,
						authenticatorInfos.authorizationKeyType(),
						authenticatorInfos.authorizationKeyUsage(),
						authenticatorInfos.autoCreateAuthorizationKey(),
						authenticatorInfos.authorizationKeyAlgorithm(),
						authenticatorInfos.authorizationKeyLifeTime(),
						authenticatorInfos.authorizationKeyLifeTimeUnit(),
						this.ownerId,
						tenantId,
						this.engine,
						null,
						null,
						authenticatorInfos.authorizationSignatureAlgorithm());
				EntityAuthorizationHelper.validateAgainst(this.credential, this.principal, key);
			} else {
				EntityAuthorizationHelper.validateAgainst(this.credential, this.principal);
			}

			this.authorities = EntityAuthorizationHelper.getAuthorities(this.principal);
		} else {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
					"Principal is not an authorization");
		}
		this.authenticated = true;
	}

	@Override
	protected Object doFindPrincipal(ICaller caller) {
		String ownerId;
		try {
			ownerId = EntityAuthorizationHelper.getOwnerId(this.credential);
			String uuid = EntityAuthorizationHelper.getUuid(this.credential);
			caller.setOwnerId(ownerId);
			IServiceResponse response = this.authenticatorService.getEntity(caller, uuid,
					new HashMap<String, String>());

			if (response.getResponseCode() == ServiceResponseCode.OK) {
				log.atDebug().log("Found principal identified with uuid " + uuid);
				return response.getResponse();
			} else {
				log.atDebug().log("Failed to find principal identified with uuid " + uuid);
				return null;
			}
		} catch (CoreException e) {
			log.atDebug().log("Failed to find principal", e);
			return null;
		}
	}

	@AuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
		// Nothgin to do
	}

	@AuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
		// Nothing to do
	}
}
