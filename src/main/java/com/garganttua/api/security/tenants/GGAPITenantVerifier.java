package com.garganttua.api.security.tenants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
public class GGAPITenantVerifier extends OncePerRequestFilter {

	@Value("${com.garganttua.api.magicTenantId}")
	private String magicTenantId;
	
	private List<String> rules = new ArrayList<String>();
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

		if( authentication != null ) {
			String method = request.getMethod();
			String uri = request.getRequestURI();
			
			String[] splits = uri.split("/");
			
			String domain = splits[1];
			String rule = null;
			
			if( method.equals("GET") && splits.length == 2 ) {
				//GET_ALL
				rule = domain+"-read-all";
			}
			if( method.equals("GET") && splits.length == 3 ) {
				//GET_ONE
				rule = domain+"-read-one";
			}
			if( method.equals("GET") && splits.length == 3 && splits[2].equals("count")) {
				//COUNT
				rule = domain+"-get-count";
			}
			if( method.equals("POST") && splits.length == 2 ) {
				//CREATE ONE
				rule = domain+"-create-one";
			}
			if( method.equals("PATCH") && splits.length == 3 ) {
				//UPDATE ONE
				rule = domain+"-update-one";
			}
			if( method.equals("DELETE") && splits.length == 2 ) {
				//DELETE_ALL
				rule = domain+"-delete-all";
			}
			if( method.equals("DELETE") && splits.length == 3 ) {
				//DELETE_ONE
				rule = domain+"-delete-one";
			}
			
			
			if( this.rules.contains(rule) ) {
				String userTenantId = (String) request.getAttribute("tenantId");
				
				String requestedTenantId = request.getHeader("tenantId");
				
				if( !userTenantId.equals(requestedTenantId) && !userTenantId.equals(this.magicTenantId) ) {
					throw new IOException("Requested tenantId ["+requestedTenantId+"] and authentifed user's tenantId ["+userTenantId+"] does not match");
				}
			}
		}
		
        filterChain.doFilter(request, response);
	}

	public void addOwnerRule(String authorization) {
		this.rules.add(authorization);
	}
}
