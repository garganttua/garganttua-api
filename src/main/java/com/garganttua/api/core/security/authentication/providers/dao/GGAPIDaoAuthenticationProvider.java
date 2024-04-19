package com.garganttua.api.core.security.authentication.providers.dao;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.security.authentication.IGGAPIAuthenticationProvider;
import com.garganttua.api.core.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.core.security.authentication.mappers.IGGAPIAuthenticationUserMapper;

public class GGAPIDaoAuthenticationProvider extends DaoAuthenticationProvider implements IGGAPIAuthenticationProvider {

	public IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, Object entity) throws GGAPIEntityException {
		IGGAPIAuthenticationUserMapper mapper = (IGGAPIAuthenticationUserMapper) this.getUserDetailsService();
		IGGAPIAuthenticator authenticator = mapper.getAuthenticatorFromEntity(tenantId, entity);
		
		return authenticator;
	}

}
