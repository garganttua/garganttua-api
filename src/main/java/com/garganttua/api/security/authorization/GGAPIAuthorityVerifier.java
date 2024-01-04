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

import com.garganttua.api.security.authentication.IGGAPIAuthenticator;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
public class GGAPIAuthorityVerifier extends OncePerRequestFilter {

	private List<IGGAPIAuthorization> rules = new ArrayList<IGGAPIAuthorization>();

	@SuppressWarnings("unchecked")
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		IGGAPIAuthorization rule = this.getRule(request);
		
		if (rule != null && rule.getAuthorization() != null && !rule.getAuthorization().isEmpty() ) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			IGGAPIAuthenticator principal = (IGGAPIAuthenticator) auth.getPrincipal();
			Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) principal.getAuthorities();
			
			if( !this.hasAuthority(authorities, rule.getAuthorization())	) {
				throw new IOException("The authentified user does not have authority "+rule.getAuthorization());
			}
		}
		filterChain.doFilter(request, response);
	}

	private IGGAPIAuthorization getRule(HttpServletRequest request) {
		String methodStr = request.getMethod();

		HttpMethod method = null;
		String uri = request.getRequestURI();

		switch (methodStr) {
		case "GET":
			method = HttpMethod.GET;
			break;
		case "POST":
			method = HttpMethod.POST;
			break;
		case "PATCH":
			method = HttpMethod.PATCH;
			break;
		case "DELETE":
			method = HttpMethod.DELETE;
			break;
		}
		String uriTotest = uri;
		String[] uriParts = uri.split("/");
		if (uriParts.length > 2) {
			uriTotest = "/" + uriParts[1] + "/*";
		}

		for (IGGAPIAuthorization auth : this.rules) {
			if (auth.getEndpoint().equals(uriTotest) && auth.getHttpMethod() == method) {
				return auth;
			}
		}

		return null;
	}
	
	public boolean hasAuthority(Collection<GrantedAuthority> authorities, String targetAuthority) {
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(targetAuthority)) {
                return true;
            }
        }
        return false;
    }

	public void addOwnerRuule(IGGAPIAuthorization a) {
		this.rules.add(a);
	}

}
