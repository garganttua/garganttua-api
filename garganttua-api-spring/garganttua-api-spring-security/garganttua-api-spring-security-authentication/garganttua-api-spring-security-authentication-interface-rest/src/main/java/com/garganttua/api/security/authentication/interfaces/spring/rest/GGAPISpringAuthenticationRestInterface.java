package com.garganttua.api.security.authentication.interfaces.spring.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationRequest;
import com.garganttua.api.core.service.GGAPIMethodConciliator;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPIResponseObject;
import com.garganttua.api.interfaces.spring.rest.GGAPIServiceMethodToHttpMethodBinder;
import com.garganttua.api.interfaces.spring.rest.GGAPIServiceResponseUtils;
import com.garganttua.api.security.spring.core.authentication.GGAPISpringAuthentication;
import com.garganttua.api.security.spring.core.authentication.GGAPISpringAuthenticationRequest;
import com.garganttua.api.security.spring.core.authentication.IGGAPISpringAuthenticationInterface;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GGBean(name = "SpringRestAuthenticationInterface", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPISpringAuthenticationRestInterface
		implements IGGAPIAuthenticationInterface, IGGAPISpringAuthenticationInterface {

	protected IGGAPIAuthenticationService authenticationService;
	protected List<GGAPIAuthenticationInfos> authenticationInfos = new ArrayList<GGAPIAuthenticationInfos>();
	protected IGGAPIDomain domain;

	@Inject
	protected RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Setter
	private AuthenticationManager authenticationManager;
	private List<IGGAPIServiceInfos> customServicesInfos = new ArrayList<IGGAPIServiceInfos>();
	private Map<IGGAPIServiceInfos, PathPattern> patterns = new HashMap<>();
	private PathPatternParser parser = new PathPatternParser();


	@Override
	public void setAuthenticationService(IGGAPIAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public void addAuthenticationInfos(GGAPIAuthenticationInfos authenticationInfos) {
		this.authenticationInfos.add(authenticationInfos);
	}

	@Override
	public void start() throws GGAPIEngineException {
		try {
			this.createRequestMappings();
		} catch (NoSuchMethodException e) {
			throw new GGAPIEngineException(e);
		}
	}

	private void createRequestMappings() throws NoSuchMethodException {
		RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
		options.setPatternParser(new PathPatternParser());

		Object handler = this;
		
		String path = "/api/" + this.domain.getDomain()+"/authenticate";
		RequestMethod requestMethod = RequestMethod.POST;
		Method method = handler.getClass().getMethod("authenticate", IGGAPICaller.class, GGAPISpringRestAuthenticationRequest.class);
		Method customMethod = handler.getClass().getMethod("customService", IGGAPICaller.class, Map.class, HttpServletRequest.class);
		
		this.createMapping(path, method, handler, options, requestMethod);
		
		for( IGGAPIServiceInfos custom: this.customServicesInfos ) {
			this.createMapping(custom.getPath(), customMethod, handler, options, RequestMethod.resolve(GGAPIServiceMethodToHttpMethodBinder.fromServiceMethod(custom.getOperation())));
		}
		
	}
	
	private void createMapping(String path, Method method, Object handler, RequestMappingInfo.BuilderConfiguration options, RequestMethod requestMethod) {
		final RequestMappingInfo requestMappingInfoCreate = RequestMappingInfo.paths(path).methods(requestMethod)
				.options(options).build();
		this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, handler, method);
	}

	public ResponseEntity<?> customService(
			@RequestAttribute(name = GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestParam Map<String, String> customParameters, HttpServletRequest request) {
		
		IGGAPIServiceInfos infos = this.getServiceInfos(request, HttpMethod.valueOf(request.getMethod()));
		String servletPath = request.getServletPath();
		if( infos == null ){
			return new ResponseEntity<>(new GGAPIResponseObject(request.getRequestURI() + " does not match any service", GGAPIResponseObject.BAD_REQUEST),
					HttpStatus.BAD_REQUEST);
		}

		GGAPIMethodConciliator conciliator = new GGAPIMethodConciliator(infos.getMethod());
		Object[] parameters = conciliator.setCaller(caller).setCustomParameters(customParameters).setReferencePath(infos.getPath()).setValuedPath(servletPath).setBody("body").getParameters();
		
		try {
			Object returnedObject = infos.invoke(parameters);
			if( !IGGAPIServiceResponse.class.isAssignableFrom(returnedObject.getClass()) ) {
				return new ResponseEntity<>(new GGAPIResponseObject(returnedObject.getClass().getSimpleName()+" must be of type "+IGGAPIServiceResponse.class.getSimpleName(), GGAPIResponseObject.UNEXPECTED_ERROR),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return GGAPIServiceResponseUtils.toResponseEntity((IGGAPIServiceResponse) returnedObject);
		} catch (GGAPIException e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	private IGGAPIServiceInfos getServiceInfos(ServletRequest request, HttpMethod method) {
		String uri = ((HttpServletRequest) request).getRequestURI();
		PathContainer pathContainer = PathContainer.parsePath(uri);

		for( Entry<IGGAPIServiceInfos, PathPattern> pattern: this.patterns.entrySet()) {
			if (pattern.getValue().matches(pathContainer)) {
				if( pattern.getKey().getOperation().getMethod() == GGAPIServiceMethodToHttpMethodBinder.fromHttpMethodAndEndpoint(method) )
					return pattern.getKey();
            }
		}
		
		for( Entry<IGGAPIServiceInfos, PathPattern> pattern: this.patterns.entrySet()) {
			if( pattern.getKey().getPath().equals(uri) ) {
				return pattern.getKey();
			}
		}
		
		return null;
	}

	public ResponseEntity<?> authenticate(
			@RequestAttribute(name = GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestBody(required = true) GGAPISpringRestAuthenticationRequest request)
			throws JsonProcessingException, GGAPIException {

		for (GGAPIAuthenticationInfos infos : this.authenticationInfos) {
			log.atInfo()
					.log("Triing to authenticate principal " + request.getPrincipal() + " of tenant "
							+ caller.getTenantId() + " with authentication of type "
							+ infos.authenticationType().getSimpleName());
			IGGAPIAuthenticationRequest authenticationRequest = new GGAPIAuthenticationRequest(caller.getDomain(),
					caller.getTenantId(), request.getPrincipal(), request.getCredentials(), infos.authenticationType());

			GGAPISpringAuthentication authentication = (GGAPISpringAuthentication) this.authenticationManager
					.authenticate(new GGAPISpringAuthenticationRequest(authenticationRequest));

			if (authentication.isAuthenticated())
				return new ResponseEntity<>(
						new GGAPISpringRestAuthenticationResponse(authentication.getAuthentication()), HttpStatus.OK);
		}
		return new ResponseEntity<>(new GGAPIResponseObject("Authentication Failed", GGAPIResponseObject.BAD_REQUEST),
				HttpStatus.UNAUTHORIZED);
	}

	@Override
	public String getName() {
		return "SpringRestAuthenticationInterface-" + this.domain.getDomain();
	}

	@Override
	public void setDomain(IGGAPIDomain domain) {
		this.domain = domain;
	}

	@Override
	public Method getAuthenticateMethod() {
		try {
			return this.getClass().getMethod("authenticate", IGGAPICaller.class,
					GGAPISpringRestAuthenticationRequest.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addCustomService(IGGAPIServiceInfos serviceInfos) {
		this.customServicesInfos.add(serviceInfos);
		PathPattern pathPattern = this.parser.parse(serviceInfos.getPath());
		this.patterns.put(serviceInfos, pathPattern);
	}
}
