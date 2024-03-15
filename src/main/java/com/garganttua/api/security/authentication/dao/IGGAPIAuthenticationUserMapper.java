package com.garganttua.api.security.authentication.dao;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;

public interface IGGAPIAuthenticationUserMapper extends UserDetailsService {

	IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, Object entity) throws GGAPIEntityException;

}
