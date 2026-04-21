package com.garganttua.api.core.security.authentication.authorization;

import java.util.Map;

import javax.inject.Inject;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.authentication.AuthenticationService;
import com.garganttua.api.core.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.core.security.key.KeyHelper;
import com.garganttua.core.CoreException;
import com.garganttua.api.commons.CoreExceptionCode;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.commons.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.commons.security.key.IKeyRealm;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Authentication(findPrincipal = true)
public class StorableAuthorizationAuthentication extends AbstractAuthentication {

	public StorableAuthorizationAuthentication(IDomain<?> domainContext) {
		super(domainContext);
	}

	public StorableAuthorizationAuthentication() {
		super(null);
	}

	@Inject
	private IApi apiContext;

	@Override
	protected void doAuthentication() throws CoreException {
		if (EntityAuthorizationHelper.isAuthorization(this.principal)) {
			this.ownerId = EntityAuthorizationHelper.getOwnerId(this.principal);

			if (EntityAuthorizationHelper.isSignable(this.credential.getClass())) {
				IKeyRealm key = KeyHelper.getKey(
						AuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME,
						(Class<?>) authenticatorInfos.authorizationKeyType().getType(),
						authenticatorInfos.authorizationKeyUsage(),
						authenticatorInfos.autoCreateAuthorizationKey(),
						authenticatorInfos.authorizationKeyAlgorithm(),
						authenticatorInfos.authorizationKeyLifeTime(),
						authenticatorInfos.authorizationKeyLifeTimeUnit(),
						this.ownerId,
						tenantId,
						this.apiContext,
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
			caller = Caller.createTenantCallerWithOwnerId(caller.tenantId(), ownerId);
			IOperationResponse response = this.authenticatorDomain.readOne(uuid, caller);

			if (response.getResponseCode() == OperationResponseCode.OK) {
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
