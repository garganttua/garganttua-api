package com.garganttua.api.interfaces.security.spring.rest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAuthentication;
import com.garganttua.api.spec.security.IGGAPIAuthorization;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;

@Service
public class GGAPISpringOwnerVerifierFilter extends GGAPISpringWithAuthorizationManagerGenericFilter {

	@Override
	public void ifPresent(IGGAPIAuthorizationManager manager, IGGAPICaller caller) throws GGAPIException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (IGGAPIAuthentication.class.isAssignableFrom(authentication.getClass())) {
			IGGAPIAuthentication auth = (IGGAPIAuthentication) SecurityContextHolder.getContext().getAuthentication();
			IGGAPIAuthorization authorization = auth.getAuthorization();
			this.security.verifyOwner(caller, authorization);
		} else if (AnonymousAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
			// Nothing to do
		} else {
			throw new GGAPISecurityException(GGAPIExceptionCode.UNKNOWN_ERROR,
					"Unsupported Authentiction of type " + authentication.getClass().getSimpleName());
		}
	}
}
