package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthenticationProvider /*extends AuthenticationProvider*/ {

	IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, Object entity) throws GGAPIException;

}
