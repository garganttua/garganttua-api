package com.garganttua.api.security.authorization.bearer;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.security.authentication.dao.AbstractGGAPIUserDetails;
import com.garganttua.api.security.authentication.dao.IGGAPIAuthenticationUserMapper;
import com.garganttua.api.security.authorization.IGGAPIAuthorizationProvider;
import com.garganttua.api.security.authorization.token.jwt.GGAPIJwtTokenProvider;
import com.garganttua.api.security.keys.GGAPIKeyExpiredException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class GGAPIBearerAuthorizationExtractor extends OncePerRequestFilter {
	
	private IGGAPIAuthorizationProvider authorizationProvider;
	
	private IGGAPIAuthenticationUserMapper userMapper;
	
	private String extractUserId;
	
	@Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
				username = this.authorizationProvider.getUserNameFromAuthorization(token);
			} catch (GGAPIKeyExpiredException e) {
				throw new IOException(e);
			}
        }
        
        logger.info("Checking Authorization for user "+username);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        	AbstractGGAPIUserDetails userDetails = (AbstractGGAPIUserDetails) this.userMapper.loadUserByUsername(username);
            try {
				if (this.authorizationProvider.validateAuthorization(token, userDetails)) {
				    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				    
				    request.setAttribute("tenantId", userDetails.getTenantId());
				    
				    if( this.extractUserId != null && !this.extractUserId.isEmpty() && this.extractUserId.equals("enabled")) {
				    	request.setAttribute("userId", userDetails.getUuid());
				    }
		    
				    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				    
				    SecurityContextHolder.getContext().setAuthentication(authToken);
				}
			} catch (GGAPIKeyExpiredException e) {
				throw new IOException(e);
			}
        }
        filterChain.doFilter(request, response);
    }

}
