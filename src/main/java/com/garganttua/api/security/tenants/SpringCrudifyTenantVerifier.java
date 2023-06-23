package com.garganttua.api.security.tenants;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@ConditionalOnProperty(name = "spring.domain.crudify.security.tenant.verify", havingValue = "enabled", matchIfMissing = true)
public class SpringCrudifyTenantVerifier extends OncePerRequestFilter {

	@Value("${spring.domain.crudify.magicTenantId}")
	private String magicTenantId;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		
		if( authentication != null ) {
			String userTenantId = (String) request.getAttribute("tenantId");
			
			String requestedTenantId = request.getHeader("tenantId");
			
			if( !userTenantId.equals(requestedTenantId) && !userTenantId.equals(this.magicTenantId) ) {
				throw new IOException("Requested tenantId ["+requestedTenantId+"] and authentifed user's tenantId ["+userTenantId+"] does not match");
			}
		}
		
        filterChain.doFilter(request, response);
	}
}
