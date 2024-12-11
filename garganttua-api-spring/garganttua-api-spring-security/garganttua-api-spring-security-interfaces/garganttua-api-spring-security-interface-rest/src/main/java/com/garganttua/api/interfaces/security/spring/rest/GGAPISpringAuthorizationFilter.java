package com.garganttua.api.interfaces.security.spring.rest;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.security.authentication.GGAPIAuthenticationRequest;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.core.security.entity.tools.GGAPIEntityAuthenticatorHelper;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPISpringHttpApiFilter;
import com.garganttua.api.security.spring.core.authentication.GGAPISpringAuthenticationRequest;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringAuthorizationFilter extends GGAPISpringHttpApiFilter {
	
	@Inject 
	private IGGAPISecurityEngine security;
	
	@Inject
	private IGGAPIEngine engine;
	
	@Inject 
	private AuthenticationManager manager;
	
	@Override
	protected HttpServletRequest doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerFilter.CALLER_ATTRIBUTE_NAME);
		IGGAPIDomain domain = caller.getDomain();
		if( caller.getAccessRule().getAccess() != GGAPIServiceAccess.anonymous ) {
			byte[] authorizationRaw = this.security.decodeAuthorizationFromRequest(request, caller);
			if( authorizationRaw != null && authorizationRaw.length > 0 ) {
				Object authorization = this.security.decodeRawAuthorization(authorizationRaw, caller);
				if( authorization != null ) {
					if( GGAPIEntityAuthenticatorHelper.isAuthenticator(authorization.getClass()) ) {		
						GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(authorization);
						Optional<IGGAPIDomain> authorizationsDomain = this.engine.getDomainsRegistry().getDomains().stream().filter(d -> {
							return d.getEntity().getValue0().equals(authorization.getClass());
						}).findFirst();
						
						if( authorizationsDomain.isPresent() ) {
							domain = authorizationsDomain.get();
						}

						//Domain cannot be null at this step
						IGGAPIAuthenticationRequest authenticationRequest = new GGAPIAuthenticationRequest(domain , caller.getTenantId(), GGAPIEntityAuthorizationHelper.getUuid(authorization), authorization, infos.authenticationTypes()[0]);						
						Authentication springAuthentication = this.manager.authenticate(new GGAPISpringAuthenticationRequest(authenticationRequest));		
						SecurityContextHolder.getContext().setAuthentication(springAuthentication);
					}
				} else {
					log.atWarn().log("Undecodable authorization "+new String(authorizationRaw));
				}
			} else {
				log.atWarn().log("Undecodable authorization");
			}
		}
		return request;
	}
}
