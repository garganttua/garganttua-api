package com.garganttua.api.core.security.authentication.authorization;

import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;

import lombok.NoArgsConstructor;

@GGAPIAuthentication(findPrincipal = false)
@NoArgsConstructor
public class GGAPIAuthorizationAuthentication extends AbstractGGAPIAuthentication {
//
	//ATTENTION NE PAS OUBLIER DE METTRE LES AUTHORITIES 
//	public GGAPIAuthorizationAuthentication(IGGAPIAuthenticationRequest request) {
//		this.tenantId = request.getTenantId();
//		this.credential = request.getCredentials();
//		this.principal = request.getTenantId();
//		this.authorities = GGAPIEntityAuthorizationHelper.getAuthorities(request.getCredentials());
//	}
	
	@Override
	protected void doAuthentication() throws GGAPIException {
		if( GGAPIEntityAuthorizationHelper.isAuthorization(this.principal) ) {
			GGAPIEntityAuthorizationHelper.validateAgainst(this.principal, this.credential);
		} else {
			GGAPIEntityAuthorizationHelper.validate(this.credential);
		}	
		this.authenticated = true;
	}

	@Override
	protected Object findPrincipal() {
		// TODO Auto-generated method stub
		
		return null;
	}

}
