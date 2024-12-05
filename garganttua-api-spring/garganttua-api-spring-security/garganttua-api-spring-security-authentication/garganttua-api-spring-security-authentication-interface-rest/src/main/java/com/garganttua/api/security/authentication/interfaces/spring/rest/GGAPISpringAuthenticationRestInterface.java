package com.garganttua.api.security.authentication.interfaces.spring.rest;

import java.lang.reflect.Method;

import javax.inject.Inject;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationRequest;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPIResponseObject;
import com.garganttua.api.security.spring.core.IGGAPISpringSecurityRestConfigurer;
import com.garganttua.api.security.spring.core.authentication.GGAPISpringAuthentication;
import com.garganttua.api.security.spring.core.authentication.GGAPISpringAuthenticationRequest;
import com.garganttua.api.security.spring.core.authentication.IGGAPISpringAuthenticationInterface;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.interfasse.GGAPIInterfaceMethod;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationService;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

import lombok.Setter;

@GGBean(name = "SpringRestAuthenticationInterface", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPISpringAuthenticationRestInterface
		implements IGGAPIAuthenticationInterface, IGGAPISpringAuthenticationInterface, IGGAPISpringSecurityRestConfigurer {

	private IGGAPIAuthenticationService authenticationService;
	private GGAPIAuthenticationInfos authenticationInfos;
	private IGGAPIDomain domain;
	@Inject
	protected RequestMappingHandlerMapping requestMappingHandlerMapping;
	
	@Setter
	private AuthenticationManager authenticationManager;

	@Override
	public void setAuthenticationService(IGGAPIAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public void setAuthenticationInfos(GGAPIAuthenticationInfos authenticationInfos) {
		this.authenticationInfos = authenticationInfos;
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

		String baseUrl = "/api/" + this.domain.getDomain() + "/authenticate";

		RequestMappingInfo requestMappingInfoCreate = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.POST)
				.options(options).build();

		if (this.domain.isAllowCreation()) {
			this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, this,
					this.getClass().getMethod("authenticate", IGGAPICaller.class, GGAPISpringRestAuthenticationRequest.class));
		}
	}

	public ResponseEntity<?> authenticate (
			@RequestAttribute(name = GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestBody(required = true) GGAPISpringRestAuthenticationRequest request) throws JsonProcessingException, GGAPIException {
		
		IGGAPIAuthenticationRequest authenticationRequest = new GGAPIAuthenticationRequest(caller.getDomain(), caller.getTenantId(), request.getPrincipal(), request.getCredentials(), this.authenticationInfos.authenticationType());						
		
		GGAPISpringAuthentication authentication = (GGAPISpringAuthentication) this.authenticationManager.authenticate(new GGAPISpringAuthenticationRequest(authenticationRequest));

		if( authentication.isAuthenticated() )
			return new ResponseEntity<>(new GGAPISpringRestAuthenticationResponse(authentication.getAuthentication()), HttpStatus.OK);
		return new ResponseEntity<>(new GGAPIResponseObject("Authentication Failed", GGAPIResponseObject.BAD_REQUEST), HttpStatus.UNAUTHORIZED);
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
	public HttpSecurity configureFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests()
				.requestMatchers(HttpMethod.POST, "/api/" + this.domain.getDomain() + "/authenticate").permitAll()
				.and();
		return http;
	}

	@Override
	public Method getMethod(GGAPIInterfaceMethod method) {
		if (method == GGAPIInterfaceMethod.authenticate) {
			try {
				return this.getClass().getMethod("authenticate", IGGAPICaller.class, GGAPISpringRestAuthenticationRequest.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

}
