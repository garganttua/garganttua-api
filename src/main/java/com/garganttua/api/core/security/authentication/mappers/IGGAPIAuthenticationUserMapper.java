package com.garganttua.api.core.security.authentication.mappers;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.security.authentication.IGGAPIAuthenticator;

public interface IGGAPIAuthenticationUserMapper extends UserDetailsService {

	IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, Object entity) throws GGAPIEntityException;

}
