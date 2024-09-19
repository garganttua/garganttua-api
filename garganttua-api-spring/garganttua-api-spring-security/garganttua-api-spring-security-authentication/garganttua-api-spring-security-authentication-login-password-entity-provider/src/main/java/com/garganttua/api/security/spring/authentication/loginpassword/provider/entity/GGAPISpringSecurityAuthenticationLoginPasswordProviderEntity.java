package com.garganttua.api.security.spring.authentication.loginpassword.provider.entity;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.security.spring.core.authentication.IGGAPISpringSecurityAuthenticationProvider;
import com.garganttua.api.security.spring.password.encoders.IGGAPISpringPasswordEncoder;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPIAuthentication;

import jakarta.annotation.PostConstruct;

@Service
public class GGAPISpringSecurityAuthenticationLoginPasswordProviderEntity extends DaoAuthenticationProvider implements IGGAPISpringSecurityAuthenticationProvider {

	@Autowired
	private IGGAPIEngine engine;

	@Autowired
	private IGGAPISpringPasswordEncoder passwordEncoder;

	private Optional<IGGAPIDomain> authenticatorDomain;

	private GGAPISpringSecurityEntityDetailsService detailsService;

	@PostConstruct
	private void init() throws GGAPISecurityException {
		this.authenticatorDomain = this.engine.getDomainsRegistry().getDomains().stream().filter(domain -> 
			domain.getSecurity().authenticatorInfos()!=null?true:false
		).findFirst();
		
		if( this.authenticatorDomain.isEmpty() ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "No Authenticator Entity found !");
		}
		this.detailsService = new GGAPISpringSecurityEntityDetailsService(this.authenticatorDomain.get().getSecurity().authenticatorInfos(), this.engine.getServicesRegistry().getService(this.authenticatorDomain.get().getDomain()));
		this.setPasswordEncoder(this.passwordEncoder);
		this.setUserDetailsService(this.detailsService);
	}
}
