package com.garganttua.api.security.spring.core.authorizations;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthorization;

public interface IGGAPISpringSecurityAuthorizationProvider {

	IGGAPIAuthentication createAuthorization(IGGAPIAuthentication authentication) throws GGAPIException;

	IGGAPIAuthorization validateAuthorization(byte[] authorization) throws GGAPIException;

}
