package com.garganttua.api.security.authorizations.spring.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.spring.core.authorizations.IGGAPISpringAuthorizationProvider;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISecuritySpringAuthorizationManager implements IGGAPIAuthorizationManager {
	
	@Autowired
	private IGGAPISpringAuthorizationProvider authorizationProvider;

	@Override
	public IGGAPIAuthorization validateAuthorization(byte[] authorization) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IGGAPIAuthentication createAuthorization(IGGAPIAuthentication authentication) throws GGAPIException {
		return authorizationProvider.createAuthorization(authentication);
	}

}
