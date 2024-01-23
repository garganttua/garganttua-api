package com.garganttua.api.ws.filters;

import java.io.IOException;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.security.authorization.IGGAPIAccessRule;

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
		IGGAPIAccessRule accessRule = this.engine.getAccessRulesRegistry().getAccessRule(httpRequest);
		caller.setAccessRule(accessRule);
	}
	
	protected GGAPICaller getCaller(ServletRequest request) {
		return (GGAPICaller) request.getAttribute(GGAPICallerManager.CALLER_ATTRIBUTE_NAME);
	}

}
