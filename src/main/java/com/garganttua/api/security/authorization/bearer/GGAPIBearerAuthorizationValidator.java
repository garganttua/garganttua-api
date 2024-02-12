package com.garganttua.api.security.authorization.bearer;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;
import com.garganttua.api.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.security.authorization.GGAPIAuthorityVerifier;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationProvider;
import com.garganttua.api.security.authorization.tokens.GGAPIToken;
import com.garganttua.api.security.authorization.tokens.jwt.GGAPITokenExpired;
import com.garganttua.api.security.authorization.tokens.jwt.GGAPITokenNotFoundException;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
public class GGAPIBearerAuthorizationValidator extends OncePerRequestFilter {
	
	private IGGAPIAuthorizationProvider authorizationProvider;
	
	@Setter
	private IGGAPIAuthenticationManager authenticationManager;
	
	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token__ = null;

        if (authHeader != null && authHeader.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            token__ = authHeader.substring(7);
            try {
           
            	GGAPIToken token = this.authorizationProvider.validateAuthorization(token__.getBytes());
            	
				if ( token != null) {
					IGGAPIAuthenticator authenticator = this.authenticationManager.getAuthenticatorFromOwnerId(token.getTenantId(), token.getOwnerId());
					authenticator.setAuthorities(token.getAuthorities());
				    Authentication auth = authenticator.getAuthentication();
				    auth.setAuthenticated(true);
					SecurityContextHolder.getContext().setAuthentication(auth);
					request.setAttribute(GGAPIAuthorityVerifier.tokenAttributeName, token);
				}
			} catch (GGAPIKeyExpiredException | GGAPITokenNotFoundException | GGAPIEngineException | GGAPITokenExpired | GGAPIEntityException e) {
				String responseToClient= "invalid credentials";
				response.setStatus(403);
				response.getWriter().write(responseToClient);
				response.getWriter().flush();
				return;
			}
        }
        filterChain.doFilter(request, response);
    }
}
