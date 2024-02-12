package com.garganttua.api.security.authentication.dao;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationProvider;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;

public class GGAPIDaoAuthenticationProvider extends DaoAuthenticationProvider implements IGGAPIAuthenticationProvider {

	@Override
	public IGGAPIAuthenticator getAuthenticatorFromEntity(String tenantId, IGGAPIEntity entity) throws GGAPIEntityException {
		IGGAPIAuthenticationUserMapper mapper = (IGGAPIAuthenticationUserMapper) this.getUserDetailsService();
		IGGAPIAuthenticator authenticator = mapper.getAuthenticatorFromEntity(tenantId, entity);
		
		return authenticator;
	}

}
