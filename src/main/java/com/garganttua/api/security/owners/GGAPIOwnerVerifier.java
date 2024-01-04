package com.garganttua.api.security.owners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import com.garganttua.api.security.authentication.IGGAPIAuthenticator;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.security.tenants.GGAPIEngineTenantIdHeaderManager;
import com.garganttua.api.spec.GGAPICrudAccess;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
public class GGAPIOwnerVerifier extends OncePerRequestFilter {

	@Value(value = "${com.garganttua.api.magicOwnerId}")
	private String magicOwnerId;
	
	private List<IGGAPIAuthorization> rules = new ArrayList<IGGAPIAuthorization>();
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		IGGAPIAuthorization rule = this.getRule(request);

		if (rule != null && rule.getAccess() == GGAPICrudAccess.owner ) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			 
			IGGAPIAuthenticator principal = (IGGAPIAuthenticator) auth.getPrincipal();
			String authentifiedOwnerId = principal.getUuid();

			String requestedOwnerId = request.getHeader(GGAPIEngineTenantIdHeaderManager.ownerIdHeaderName);

			if (!authentifiedOwnerId.equals(requestedOwnerId) && !authentifiedOwnerId.equals(this.magicOwnerId)) {
				throw new IOException("Requested OwnerId [" + requestedOwnerId + "] and authentifed user's OwnerId ["
						+ authentifiedOwnerId + "] do not match");
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

	public void addOwnerRule(IGGAPIAuthorization a) {
		this.rules.add(a);
	}
	
}
