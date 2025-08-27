package com.garganttua.api.core.security.authentication.authorization;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.authentication.AuthenticationService;
import com.garganttua.api.core.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.core.security.entity.checker.EntityAuthenticatorChecker;
import com.garganttua.api.core.security.key.KeyHelper;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.key.IKeyRealm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Authentication(findPrincipal = false)
public class AuthorizationAuthentication extends AbstractAuthentication {

	public AuthorizationAuthentication(IDomain domain) {
		super(domain);
	}

	public AuthorizationAuthentication() {
		super(null);
	}

	@Inject
	private IEngine engine;

	@Override
	protected void doAuthentication() {
		try {
			this.ownerId = EntityAuthorizationHelper.getOwnerId(this.credential);

			String ownerDomainName = EntityHelper.getDomainNameFromOwnerId(this.ownerId);
			Optional<IDomain> ownerDomain = this.engine.getDomain(ownerDomainName);

			ownerDomain.ifPresent((domain) -> {
				try {
					if (EntityAuthorizationHelper.isSignable(this.credential.getClass())) {
						AuthenticatorInfos ownerAuthenticatorInfos = EntityAuthenticatorChecker
								.checkEntityAuthenticatorClass(domain.getEntityClass());
						IKeyRealm key = KeyHelper.getKey(
								AuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME,
								ownerAuthenticatorInfos.authorizationKeyType(),
								ownerAuthenticatorInfos.authorizationKeyUsage(),
								ownerAuthenticatorInfos.autoCreateAuthorizationKey(),
								ownerAuthenticatorInfos.authorizationKeyAlgorithm(),
								ownerAuthenticatorInfos.authorizationKeyLifeTime(),
								ownerAuthenticatorInfos.authorizationKeyLifeTimeUnit(),
								this.ownerId,
								tenantId,
								this.engine,
								null,
								null,
								ownerAuthenticatorInfos.authorizationSignatureAlgorithm());
						EntityAuthorizationHelper.validate(this.credential, key);
					} else {
						EntityAuthorizationHelper.validate(this.credential);
					}
					this.authorities = EntityAuthorizationHelper.getAuthorities(this.credential);
					this.authenticated = true;
				} catch (CoreException e) {
					log.atDebug().log("Authentication failed", e);
				}
			});
		} catch (CoreException e) {
			log.atDebug().log("Authentication failed", e);
		}
	}

	@Override
	protected Object doFindPrincipal(ICaller caller) {
		// Nothing to do
		return null;
	}

	@AuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
		// Nothing to do
	}

	@AuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
		// Nothing to do
	}

}
