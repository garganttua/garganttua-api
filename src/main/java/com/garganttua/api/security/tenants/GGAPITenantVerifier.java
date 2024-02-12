package com.garganttua.api.security.tenants;

import java.io.IOException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.ws.filters.GGAPICallerManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
public class GGAPITenantVerifier extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerManager.CALLER_ATTRIBUTE_NAME);
		IGGAPIAccessRule rule = caller.getAccessRule();

		if ( rule != null && rule.getAccess() == GGAPICrudAccess.tenant ) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			 
			IGGAPIAuthenticator principal = (IGGAPIAuthenticator) auth.getPrincipal();
			String authentifiedTenantId = principal.getTenantId();
			String tenantId = caller.getTenantId();
			String requestedTenantId = caller.getRequestedTenantId();

			if ( !authentifiedTenantId.equals(tenantId) ) {
				throw new IOException("TenantId [" + tenantId + "] and authentifed user's tenantId ["
						+ authentifiedTenantId + "] do not match");
			}
			
			if( !caller.isSuperTenant() && !requestedTenantId.equals(authentifiedTenantId) ) {
				throw new IOException("Authentifed user's tenant ["
						+ authentifiedTenantId + "] is not super tenant and cannot access to other tenant");
			}
		}
		filterChain.doFilter(request, response);
	}

}
