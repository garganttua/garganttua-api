package com.garganttua.api.core.security.authentication.authorization;

import java.util.Map;

import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPreProcessing;

@GGAPIAuthentication(findPrincipal = false)
public class GGAPIAuthorizationAuthentication extends AbstractGGAPIAuthentication {

	public GGAPIAuthorizationAuthentication(IGGAPIDomain domain) {
		super(domain);
	}
	
	public GGAPIAuthorizationAuthentication() {
		super(null);
	}

	@Override
	protected void doAuthentication() throws GGAPIException {
		GGAPIEntityAuthorizationHelper.validate(this.credential);
		this.authorities = GGAPIEntityAuthorizationHelper.getAuthorities(this.credential);
		this.ownerId = GGAPIEntityAuthorizationHelper.getOwnerId(this.credential);
		this.authenticated = true;
	}

	@Override
	protected Object doFindPrincipal(IGGAPICaller caller) {
		// Nothing to do
		return null;
	}
	
	@GGAPIAuthenticatorSecurityPreProcessing
	public void applySecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
		//Nothing to do
	}
	
	@GGAPIAuthenticatorSecurityPostProcessing
	public void postProcessSecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
		//Nothing to do
	}

}
