package com.garganttua.api.service.rest.filters;

import java.io.IOException;

import org.springframework.http.HttpMethod;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.service.rest.GGAPIServiceMethodToHttpMethodBinder;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;

public class GGAPIFilter implements Filter, IGGAPIEngineObject {

	@Setter
	protected IGGAPIEngine engine;
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		GGAPICaller caller = GGAPICallerManager.getCallerAttributeAndCreateItIfNotPresent(httpRequest);
		HttpMethod method = this.getHttpMethod(request);
		String uriTotest = this.getUri(request);
		IGGAPIAccessRule accessRule = this.engine.getAccessRulesRegistry().getAccessRule(GGAPIServiceMethodToHttpMethodBinder.fromHttpMethod(method), uriTotest);
		
		caller.setAccessRule(accessRule);
	}

	protected String getUri(ServletRequest request) {
		String uri = ((HttpServletRequest) request).getRequestURI();
		String uriTotest = uri;
		String[] uriParts = uri.split("/");
		
		if (uriParts.length > 2) {
			uriTotest = "/" + uriParts[1] + "/*";
		}
		return uriTotest;
	}

	protected HttpMethod getHttpMethod(ServletRequest request) {
		HttpMethod method = HttpMethod.GET;
		switch (((HttpServletRequest) request).getMethod()) {
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
		return method;
	}
	
	protected GGAPICaller getCaller(ServletRequest request) {
		return (GGAPICaller) request.getAttribute(GGAPICallerManager.CALLER_ATTRIBUTE_NAME);
	}
	
	protected GGAPIDynamicDomain getDomain(ServletRequest request) {
		return this.engine.getDynamicDomainsRegistry().getDomain((HttpServletRequest) request);
	}

}
