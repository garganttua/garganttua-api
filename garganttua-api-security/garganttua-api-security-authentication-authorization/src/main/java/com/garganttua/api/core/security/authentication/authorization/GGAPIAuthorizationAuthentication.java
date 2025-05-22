package com.garganttua.api.core.security.authentication.authorization;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationService;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.core.security.key.GGAPIKeyHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@GGAPIAuthentication(findPrincipal = false)
public class GGAPIAuthorizationAuthentication extends AbstractGGAPIAuthentication {

	public GGAPIAuthorizationAuthentication(IGGAPIDomain domain) {
		super(domain);
	}

	public GGAPIAuthorizationAuthentication() {
		super(null);
	}

	@Inject
	private IGGAPIEngine engine;

	@Override
	protected void doAuthentication() throws GGAPIException {
		this.ownerId = GGAPIEntityAuthorizationHelper.getOwnerId(this.credential);

		String ownerDomainName = GGAPIEntityHelper.getDomainNameFromOwnerId(this.ownerId);
		Optional<IGGAPIDomain> ownerDomain = this.engine.getDomain(ownerDomainName);

		ownerDomain.ifPresent((domain) -> {
			try {
				if (GGAPIEntityAuthorizationHelper.isSignable(this.credential.getClass())) {
					GGAPIAuthenticatorInfos ownerAuthenticatorInfos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(domain.getEntityClass());
					IGGAPIKeyRealm key = GGAPIKeyHelper.getKey(
							GGAPIAuthenticationService.AUTHORIZATION_SIGNING_KEY_REALM_NAME,
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
					GGAPIEntityAuthorizationHelper.validate(this.credential, key);
				} else {
					GGAPIEntityAuthorizationHelper.validate(this.credential);
				}
				this.authorities = GGAPIEntityAuthorizationHelper.getAuthorities(this.credential);
				this.authenticated = true;
			} catch (GGAPIException e) {
				log.atDebug().log("Authentication failed", e);
			}
		});
	}

	@Override
	protected Object doFindPrincipal(IGGAPICaller caller) {
		// Nothing to do
		return null;
	}

	@GGAPIAuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
		// Nothing to do
	}

	@GGAPIAuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
		// Nothing to do
	}

}
