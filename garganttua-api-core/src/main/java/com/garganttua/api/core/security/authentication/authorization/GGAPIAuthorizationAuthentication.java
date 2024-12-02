package com.garganttua.api.core.security.authentication.authorization;

import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;

import lombok.NoArgsConstructor;

@GGAPIAuthentication(findPrincipal = false)
@NoArgsConstructor
public class GGAPIAuthorizationAuthentication extends AbstractGGAPIAuthentication {

	@Override
	protected void doAuthentication() throws GGAPIException {
		GGAPIEntityAuthorizationHelper.validate(this.credential);
		this.authorities = GGAPIEntityAuthorizationHelper.getAuthorities(this.credential);
		this.authenticated = true;
	}

	@Override
	protected Object doFindPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

}
