package com.garganttua.api.core.security.authorization;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.core.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.services.rest.filters.GGAPICallerManager;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIAccessRule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthorityVerifier extends OncePerRequestFilter {
	
	public final static String tokenAttributeName = "token";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerManager.CALLER_ATTRIBUTE_NAME);
		IGGAPIAccessRule rule = caller.getAccessRule();
		GGAPIToken token = (GGAPIToken) request.getAttribute(GGAPIAuthorityVerifier.tokenAttributeName);
		
		if (rule != null && rule.getAuthority() != null && !rule.getAuthority().isEmpty() ) {
			List<String> authorities = token.getAuthorities();
			
			if( !this.hasAuthority(authorities, rule.getAuthority())	) {
				String responseToClient = "The authenticated user does not have the appropriated authority";
				response.setStatus(403);
				response.getWriter().write(responseToClient);
				response.getWriter().flush();
				return ;
			}
		}
		filterChain.doFilter(request, response);
	}

	
	public boolean hasAuthority(List<String> authorities, String targetAuthority) {
        for (String authority : authorities) {
            if (authority.equals(targetAuthority)) {
                return true;
            }
        }
        return false;
    }

}
