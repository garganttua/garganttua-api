package com.garganttua.api.security.spring.core.authorizations;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.IGGAPIAuthentication;

public interface IGGAPISpringAuthorizationProvider {

	IGGAPIAuthentication createAuthorization(IGGAPIAuthentication authentication) throws GGAPIException;

}
