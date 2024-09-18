package com.garganttua.api.interfaces.spring.rest;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.caller.IGGAPICallerFactory;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("callerFilter")
public class GGAPICallerFilter extends GGAPISpringHttpApiFilter {

	@Autowired
	protected IGGAPIEngine engine;
	
	public static final String CALLER_ATTRIBUTE_NAME = "caller";

	private static final int DOMAIN_INDEX_IN_URI = 2;

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:tenantId}")
	private String tenantIdHeaderName = "tenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.requestedTenantIdHeaderName:requestedTenantId}")
	private String requestedTenantIdHeaderName = "requestedTenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:ownerId}")
	private String ownerIdHeaderName = "ownerId";

	private String getDomainNameFromRequestUri(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String[] uriParts = uri.split("/");
		return uriParts[DOMAIN_INDEX_IN_URI];
	}

	private String getUri(ServletRequest request) {
		String uri = ((HttpServletRequest) request).getRequestURI();
		String uriTotest = uri;
		String[] uriParts = uri.split("/");

		if (uriParts.length > 3) {
			uriTotest = "/" + uriParts[1] + "/" + uriParts[2] + "/*";
		}
		return uriTotest;
	}

	private HttpMethod getHttpMethod(ServletRequest request) {
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

	@Override
	protected void doFilter(HttpServletRequest request, HttpServletResponse response) throws GGAPIException {
		HttpMethod method = this.getHttpMethod(request);
		String uri = this.getUri(request);
		String servletPath = ((HttpServletRequest) request).getServletPath();
		
		if( log.isDebugEnabled() ) {
			log.debug("Serving url "+servletPath);
		}
		
		IGGAPICallerFactory callerFactory = this.engine.getCallerFactory(this.getDomainNameFromRequestUri((HttpServletRequest) request));
		
		if( callerFactory == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.ENTITY_NOT_FOUND, "Path "+servletPath+" not found");
		}

		String tenantId = ((HttpServletRequest) request).getHeader(this.tenantIdHeaderName);
		String requestedtenantId = ((HttpServletRequest) request).getHeader(this.requestedTenantIdHeaderName);
		String ownerId = ((HttpServletRequest) request).getHeader(this.ownerIdHeaderName);

		IGGAPICaller caller = callerFactory.getCaller(GGAPIServiceMethodToHttpMethodBinder.fromHttpMethodAndEndpoint(method, uri), uri, tenantId, ownerId, requestedtenantId, null);
		 
		if( log.isDebugEnabled() ) {
			log.debug("Generated caller "+caller);
		}
		
		request.setAttribute(CALLER_ATTRIBUTE_NAME, caller);
		return;

	}
}
