package com.garganttua.api.security.authorization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.ws.filters.GGAPICallerManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthorityVerifier extends OncePerRequestFilter {

	@SuppressWarnings("unchecked")
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		IGGAPICaller caller = (IGGAPICaller) request.getAttribute(GGAPICallerManager.CALLER_ATTRIBUTE_NAME);
		IGGAPIAccessRule rule = caller.getAccessRule();
		
		if (rule != null && rule.getAuthority() != null && !rule.getAuthority().isEmpty() ) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			IGGAPIAuthenticator principal = (IGGAPIAuthenticator) auth.getPrincipal();
			Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) principal.getAuthorities();
			
			if( !this.hasAuthority(authorities, rule.getAuthority())	) {
				throw new IOException("The authentified user does not have authority "+rule.getAuthority());
			}
		}
		filterChain.doFilter(request, response);
	}

	
	public boolean hasAuthority(Collection<GrantedAuthority> authorities, String targetAuthority) {
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(targetAuthority)) {
                return true;
            }
        }
        return false;
    }

}
