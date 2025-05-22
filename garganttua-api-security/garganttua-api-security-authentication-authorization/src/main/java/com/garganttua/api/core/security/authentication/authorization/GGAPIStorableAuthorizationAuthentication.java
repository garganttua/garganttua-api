package com.garganttua.api.core.security.authentication.authorization;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationService;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.security.key.GGAPIKeyHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@GGAPIAuthentication(findPrincipal = true)
public class GGAPIStorableAuthorizationAuthentication extends AbstractGGAPIAuthentication {

	public GGAPIStorableAuthorizationAuthentication(IGGAPIDomain domain) {
		super(domain);
	}

	public GGAPIStorableAuthorizationAuthentication() {
		super(null);
	}

	@Inject 
	private IGGAPIEngine engine;

	@Override
	protected void doAuthentication() throws GGAPIException {
		if (GGAPIEntityAuthorizationHelper.isAuthorization(this.principal)) {
			this.ownerId = GGAPIEntityAuthorizationHelper.getOwnerId(this.principal);

			if (GGAPIEntityAuthorizationHelper.isSignable(this.credential.getClass())) {
				IGGAPIKeyRealm key = GGAPIKeyHelper.getKey(
						GGAPIAuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME,
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
				GGAPIEntityAuthorizationHelper.validateAgainst(this.credential, this.principal, key);
			} else {
				GGAPIEntityAuthorizationHelper.validateAgainst(this.credential, this.principal);
			}

			this.authorities = GGAPIEntityAuthorizationHelper.getAuthorities(this.principal);
		} else {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR,
					"Principal is not an authorization");
		}
		this.authenticated = true;
	}

	@Override
	protected Object doFindPrincipal(IGGAPICaller caller) {
		String ownerId;
		try {
			ownerId = GGAPIEntityAuthorizationHelper.getOwnerId(this.credential);
			String uuid = GGAPIEntityAuthorizationHelper.getUuid(this.credential);
			caller.setOwnerId(ownerId);
			IGGAPIServiceResponse response = this.authenticatorService.getEntity(caller, uuid,
					new HashMap<String, String>());

			if (response.getResponseCode() == GGAPIServiceResponseCode.OK) {
				log.atDebug().log("Found principal identified with uuid " + uuid);
				return response.getResponse();
			} else {
				log.atDebug().log("Failed to find principal identified with uuid " + uuid);
				return null;
			}
		} catch (GGAPIException e) {
			log.atDebug().log("Failed to find principal", e);
			return null;
		}
	}

	@GGAPIAuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
		// Nothgin to do
	}

	@GGAPIAuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
		// Nothing to do
	}
}
