package com.garganttua.api.security.tenants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

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
		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		IGGAPIAuthorization rule = this.getRule(request);

		if (authentication != null && rule != null) {

			String method = request.getMethod();
			String uri = request.getRequestURI();

			String[] splits = uri.split("/");

			String domain = splits[1];
			String rule__ = null;

			if (method.equals("GET") && splits.length == 2) {
				// GET_ALL
				rule__ = domain + "-read-all";
			}
			if (method.equals("GET") && splits.length == 3) {
				// GET_ONE
				rule__ = domain + "-read-one";
			}
			if (method.equals("GET") && splits.length == 3 && splits[2].equals("count")) {
				// COUNT
				rule__ = domain + "-get-count";
			}
			if (method.equals("POST") && splits.length == 2) {
				// CREATE ONE
				rule__ = domain + "-create-one";
			}
			if (method.equals("PATCH") && splits.length == 3) {
				// UPDATE ONE
				rule__ = domain + "-update-one";
			}
			if (method.equals("DELETE") && splits.length == 2) {
				// DELETE_ALL
				rule__ = domain + "-delete-all";
			}
			if (method.equals("DELETE") && splits.length == 3) {
				// DELETE_ONE
				rule__ = domain + "-delete-one";
			}

			if( rule.getAccess() == GGAPICrudAccess.owner ) {
				String userTenantId = (String) request.getAttribute(GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName);
	
				String requestedTenantId = request.getHeader(GGAPIEngineTenantIdHeaderManager.tenantIdHeaderName);
	
				if (!userTenantId.equals(requestedTenantId) && !userTenantId.equals(this.magicTenantId)) {
					throw new IOException("Requested tenantId [" + requestedTenantId + "] and authentifed user's tenantId ["
							+ userTenantId + "] does not match");
				}
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
