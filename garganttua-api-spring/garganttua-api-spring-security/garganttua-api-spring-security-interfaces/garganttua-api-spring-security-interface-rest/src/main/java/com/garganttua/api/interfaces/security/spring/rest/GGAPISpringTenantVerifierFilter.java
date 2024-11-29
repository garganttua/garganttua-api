package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.stereotype.Service;

import com.garganttua.api.interfaces.spring.rest.GGAPISpringHttpApiFilter;
import com.garganttua.api.spec.GGAPIException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GGAPISpringTenantVerifierFilter extends GGAPISpringHttpApiFilter {
	
	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		// TODO Auto-generated method stub
		
	}
	
//	@Override
//	public void ifPresent(IGGAPIAuthorizationManager manager, IGGAPICaller caller) throws GGAPIException {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication(); 
//		if( IGGAPIAuthentication.class.isAssignableFrom(authentication.getClass()) ) {
//			IGGAPIAuthentication auth = (IGGAPIAuthentication) authentication;
//			IGGAPIAuthorization authorization = auth.getAuthorization();
//			log.atDebug().log("Checking caller tenantId ["+caller.getTenantId()+"] against authentication tenantId ["+authorization.getTenantId()+"]");
//			this.security.verifyTenant(caller, authorization);
//		} else if( AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass()) ) {
//			//Nothing to do
//		} else {
//			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR, "Unsupported Authentiction of type "+authentication.getClass().getSimpleName());
//		}
//	}
}
