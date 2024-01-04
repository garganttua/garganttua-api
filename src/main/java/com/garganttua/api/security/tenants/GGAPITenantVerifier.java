package com.garganttua.api.security.tenants;

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
import com.garganttua.api.spec.GGAPICrudAccess;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
@ConditionalOnProperty(name = "com.garganttua.api.security", havingValue = "enabled", matchIfMissing = true)
public class GGAPITenantVerifier extends OncePerRequestFilter {

	@Value(value = "${com.garganttua.api.magicTenantId}")
	private String magicTenantId;
	
	private List<IGGAPIAuthorization> rules = new ArrayList<IGGAPIAuthorization>();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		IGGAPIAuthorization rule = this.getRule(request);

		if (rule != null && rule.getAccess() == GGAPICrudAccess.tenant ) {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			 
			IGGAPIAuthenticator principal = (IGGAPIAuthenticator) auth.getPrincipal();
			String authentifiedTenantId = principal.getTenantId();

			String requestedTenantId = request.getHeader(GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName);

			if (!authentifiedTenantId.equals(requestedTenantId) && !authentifiedTenantId.equals(this.magicTenantId)) {
				throw new IOException("Requested tenantId [" + requestedTenantId + "] and authentifed user's tenantId ["
						+ authentifiedTenantId + "] do not match");
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
