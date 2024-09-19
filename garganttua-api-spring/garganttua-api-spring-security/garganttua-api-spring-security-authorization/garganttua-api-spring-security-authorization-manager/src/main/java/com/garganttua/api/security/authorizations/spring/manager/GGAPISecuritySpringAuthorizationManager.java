package com.garganttua.api.security.authorizations.spring.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.spring.core.authentication.IGGAPISpringAuthentication;
import com.garganttua.api.security.spring.core.authorizations.IGGAPISpringSecurityAuthorizationProtocol;
import com.garganttua.api.security.spring.core.authorizations.IGGAPISpringSecurityAuthorizationProvider;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISecuritySpringAuthorizationManager implements IGGAPIAuthorizationManager {
	
	@Autowired
	private IGGAPISpringSecurityAuthorizationProvider authorizationProvider;
	
	@Autowired
	private IGGAPISpringSecurityAuthorizationProtocol protocol;

	@Override
	public IGGAPIAuthentication validateAuthorization(byte[] authorization) throws GGAPIException {
		IGGAPIAuthorization validatedAuthorization = this.authorizationProvider.validateAuthorization(authorization);
		return GGAPISpringSecurityAuthorizationAuthentication.fromAuthorization(validatedAuthorization);
	}

	@Override
	public IGGAPIAuthentication createAuthorization(IGGAPIAuthentication authentication) throws GGAPIException {
		return this.authorizationProvider.createAuthorization(authentication);
	}

	@Override
	public String getAuthorizationType() {
		return this.authorizationProvider.getType();
	}

	@Override
	public String getAuthorizationFormat() {
		return this.authorizationProvider.getFormat();
	}

	@Override
	public String getAuthorizationProtocol() {
		return this.protocol.getProtocol();
	}

}
