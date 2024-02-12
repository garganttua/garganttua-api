package com.garganttua.api.security.authentication;

import org.springframework.security.authentication.AuthenticationProvider;

import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPIEntity;

public interface IGGAPIAuthenticationProvider extends AuthenticationProvider {

	IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, IGGAPIEntity entity) throws GGAPIEntityException;

}
