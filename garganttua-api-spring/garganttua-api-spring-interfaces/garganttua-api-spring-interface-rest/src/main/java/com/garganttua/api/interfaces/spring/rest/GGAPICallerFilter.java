package com.garganttua.api.interfaces.spring.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.GGAPIMethod;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.caller.IGGAPICallerFactory;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("callerFilter")
public class GGAPICallerFilter extends GGAPISpringHttpApiFilter {

	@Autowired
	protected IGGAPIEngine engine;

	private Map<IGGAPIServiceInfos, PathPattern> getPatterns() {
		PathPatternParser parser = new PathPatternParser();
		Map<IGGAPIServiceInfos, PathPattern> patterns = new HashMap<>();
		List<IGGAPIServiceInfos> infos = this.engine.getServicesInfos();
		infos.forEach(info -> {
			log.atDebug().log("Added Path Pattern " + info.getPath());
			patterns.put(info, parser.parse(info.getPath()));
		});
		return patterns;
	}

	public static final String CALLER_ATTRIBUTE_NAME = "caller";

	private static final int DOMAIN_INDEX_IN_URI = 2;

	@Value(value = "${com.garganttua.api.interface.spring.rest.tenantIdHeaderName:tenantId}")
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

	private IGGAPIServiceInfos getServiceInfos(ServletRequest request, HttpMethod method) {
		Map<IGGAPIServiceInfos, PathPattern> patterns = this.getPatterns();
		String uri = ((HttpServletRequest) request).getRequestURI();
		PathContainer pathContainer = PathContainer.parsePath(uri);

		for (Entry<IGGAPIServiceInfos, PathPattern> pattern : patterns.entrySet()) {
			if (pattern.getKey().getPath().equals(uri)) {
				if (pattern.getKey().getOperation().getMethod() == GGAPIServiceMethodToHttpMethodBinder
						.fromHttpMethodAndEndpoint(method) || pattern.getKey().getOperation().getMethod() == GGAPIMethod.authenticate)
					return pattern.getKey();
			}
		}

		for (Entry<IGGAPIServiceInfos, PathPattern> pattern : patterns.entrySet()) {
			log.atDebug().log(pathContainer.toString()+ " matching "+pattern.getValue().getPatternString());
			if (pattern.getValue().matches(pathContainer)) {
				if (pattern.getKey().getOperation().getMethod() == GGAPIServiceMethodToHttpMethodBinder
						.fromHttpMethodAndEndpoint(method))
					return pattern.getKey();
			}
		}

		return null;
	}

	private HttpMethod getHttpMethod(ServletRequest request) {
		HttpMethod method = HttpMethod.GET;
		switch (((HttpServletRequest) request).getMethod()) {
		case "OPTIONS":
			method = HttpMethod.OPTIONS;
			break;
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
	protected HttpServletRequest doFilter(HttpServletRequest request, HttpServletResponse response)
			throws GGAPIException {
		HttpMethod method = this.getHttpMethod(request);
		String servletPath = request.getServletPath();
		if (log.isDebugEnabled()) {
			log.debug("Serving url " + servletPath + " " + method);
		}
		IGGAPIServiceInfos infos = this.getServiceInfos(request, method);
		if (infos == null) {
			throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST,
					request.getRequestURI() + " does not match any service");
		}

		if (method == HttpMethod.OPTIONS) {
			log.warn("*********************************************");
			log.warn("* An Options http request has been received *");
			log.warn("* These requests are not yet developped     *");
			log.warn("* There should be developped soon           *");
			log.warn("*********************************************");
//			return request;
		}

		String tenantId = ((HttpServletRequest) request).getHeader(this.tenantIdHeaderName);
		String requestedtenantId = ((HttpServletRequest) request).getHeader(this.requestedTenantIdHeaderName);
		String ownerId = ((HttpServletRequest) request).getHeader(this.ownerIdHeaderName);

		IGGAPICaller caller = this.engine.getCaller(this.getDomainNameFromRequestUri((HttpServletRequest) request), infos.getOperation(), infos.getPath(), tenantId, ownerId,
				requestedtenantId, null);

		if (log.isDebugEnabled()) {
			log.debug("Generated caller " + caller);
		}

		request.setAttribute(CALLER_ATTRIBUTE_NAME, caller);
		return request;
	}

	public static void printRequest(HttpServletRequest request) {
		System.out.println("Request Method: " + request.getMethod());
		System.out.println("Request URI: " + request.getRequestURI());
		System.out.println("Request URL: " + request.getRequestURL());
		System.out.println("Protocol: " + request.getProtocol());
		System.out.println("Remote Address: " + request.getRemoteAddr());
		System.out.println("Query String: " + request.getQueryString());
		System.out.println("Headers:");

		// Print headers
		request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
			System.out.println(headerName + ": " + request.getHeader(headerName));
		});

		System.out.println("Parameters:");
		// Print parameters
		request.getParameterMap().forEach((key, value) -> {
			System.out.println(key + ": " + String.join(", ", value));
		});
	}
}
