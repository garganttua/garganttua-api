package com.garganttua.api.interfaces.spring.rest;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.service.GGAPIServiceResponse;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.caller.IGGAPICallerFactory;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("callerFilter")
public class GGAPICallerFilter implements Filter {

	public static final String CALLER_ATTRIBUTE_NAME = "caller";

	private static final int DOMAIN_INDEX_IN_URI = 2;

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:tenantId}")
	private String tenantIdHeaderName = "tenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.requestedTenantIdHeaderName:requestedTenantId}")
	private String requestedTenantIdHeaderName = "requestedTenantId";

	@Value(value = "${com.garganttua.api.interface.spring.rest.ownerIdHeaderName:ownerId}")
	private String ownerIdHeaderName = "ownerId";

	@Autowired
	private IGGAPIEngine engine;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if( log.isDebugEnabled() ) {
			log.debug("Serving url "+((HttpServletRequest) request).getServletPath());
		}
		if (!((HttpServletRequest) request).getServletPath().startsWith("/api")) {
			chain.doFilter(request, response);
		} else {
				
			HttpMethod method = this.getHttpMethod(request);
			String uri = this.getUri(request);
			
			IGGAPICallerFactory callerFactory = this.engine.getCallerFactory(this.getDomainNameFromRequestUri((HttpServletRequest) request));
	
			String tenantId = ((HttpServletRequest) request).getHeader(this.tenantIdHeaderName);
			String requestedtenantId = ((HttpServletRequest) request).getHeader(this.requestedTenantIdHeaderName);
			String ownerId = ((HttpServletRequest) request).getHeader(this.ownerIdHeaderName);
			
			try {
				IGGAPICaller caller = callerFactory.getCaller(GGAPIServiceMethodToHttpMethodBinder.fromHttpMethodAndEndpoint(method, uri), uri, tenantId, ownerId, requestedtenantId, null);
				
				if( log.isDebugEnabled() ) {
					log.debug("Generated caller "+caller);
				}
				
				request.setAttribute(CALLER_ATTRIBUTE_NAME, caller);
				chain.doFilter(request, response);
			} catch (IOException | ServletException e) {
				if (log.isDebugEnabled()) {
					log.warn("Error : ", e);
				}
				throw e;
	
			} catch (GGAPIException e) {
				if (log.isDebugEnabled()) {
					log.warn("Error : ", e);
				}
				GGAPIServiceResponse responseObject = new GGAPIServiceResponse(e.getMessage(),
						GGAPIServiceResponseCode.fromExceptionCode(e));
				ResponseEntity<?> responseEntity = GGAPIServiceResponseUtils.toResponseEntity(responseObject);
	
				String json = new ObjectMapper().writeValueAsString(responseEntity.getBody());
	
				((HttpServletResponse) response).setStatus(responseEntity.getStatusCode().value());
				response.setContentType("application/json");
				((HttpServletResponse) response).getWriter().write(json);
				((HttpServletResponse) response).getWriter().flush();
				return;
			}
		}
	}
	
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
}
