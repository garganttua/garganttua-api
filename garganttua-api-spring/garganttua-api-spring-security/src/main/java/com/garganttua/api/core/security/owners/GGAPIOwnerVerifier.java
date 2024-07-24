package com.garganttua.api.core.security.owners;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.interfaces.spring.rest.GGAPICallerManager;
import com.garganttua.api.spec.GGAPIServiceAccess;
import com.garganttua.api.spec.caller.IGGAPICallerICallerICaller;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.security.IGGAPIAuthenticator;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
public class GGAPIOwnerVerifier extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerManager.CALLER_ATTRIBUTE_NAME);
		IGGAPIAccessRule rule = caller.getAccessRule();

		if (rule != null && rule.getAccess() == GGAPIServiceAccess.owner ) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			 
			IGGAPIAuthenticator principal = (IGGAPIAuthenticator) auth.getPrincipal();
			String authentifiedOwnerId = principal.getUuid();
			String ownerId = caller.getOwnerId();

			if (!authentifiedOwnerId.equals(ownerId) && !caller.isSuperOwner()) {
				throw new IOException("Requested OwnerId [" + ownerId + "] and authentifed user's OwnerId ["
						+ authentifiedOwnerId + "] do not match");
			}
			
		}
		
		filterChain.doFilter(request, response);
	}
}
