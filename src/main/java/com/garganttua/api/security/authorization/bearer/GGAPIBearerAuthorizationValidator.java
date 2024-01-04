package com.garganttua.api.security.authorization.bearer;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationProvider;
import com.garganttua.api.security.authorization.token.GGAPIToken;
import com.garganttua.api.security.authorization.token.jwt.GGAPITokenExpired;
import com.garganttua.api.security.authorization.token.jwt.GGAPITokenNotFoundException;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GGAPIBearerAuthorizationValidator extends OncePerRequestFilter {
	
	private IGGAPIAuthorizationProvider authorizationProvider;
	
	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token__ = null;

        if (authHeader != null && authHeader.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            token__ = authHeader.substring(7);
            try {
           
            	GGAPIToken token = this.authorizationProvider.validateAuthorization(token__.getBytes());
            	
				if ( token != null) {
				    Authentication auth = token.getAuthenticator().getAuthentication();
				    auth.setAuthenticated(true);
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (GGAPIKeyExpiredException | GGAPITokenNotFoundException | GGAPIEngineException | GGAPITokenExpired e) {
				String responseToClient= e.getMessage();

				response.setStatus(403);
				response.getWriter().write(responseToClient);
				response.getWriter().flush();
				return;
			}
        }
        filterChain.doFilter(request, response);
    }

}
