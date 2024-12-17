package com.garganttua.api.security.authentication.interfaces.spring.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.security.authentication.GGAPIAuthenticationRequest;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPIInterfaceSpringCustomizable;
import com.garganttua.api.interfaces.spring.rest.GGAPIResponseObject;
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
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GGBean(name = "SpringRestAuthenticationInterface", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPISpringAuthenticationRestInterface extends GGAPIInterfaceSpringCustomizable
		implements IGGAPIAuthenticationInterface, IGGAPISpringAuthenticationInterface {

	protected IGGAPIAuthenticationService authenticationService;
	protected List<GGAPIAuthenticationInfos> authenticationInfos = new ArrayList<GGAPIAuthenticationInfos>();
	protected IGGAPIDomain domain;

	@Setter
	private AuthenticationManager authenticationManager;

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
		Method method = handler.getClass().getDeclaredMethod("authenticate", IGGAPICaller.class, GGAPISpringRestAuthenticationRequest.class);

		this.createMapping(path, method, handler, options, requestMethod);
		this.createCustomMappings();
	}

	public ResponseEntity<?> authenticate(
			@RequestAttribute(name = GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@RequestBody(required = true) GGAPISpringRestAuthenticationRequest request) throws GGAPIException {

		log.atInfo()
		.log("Authenticating principal " + request.getPrincipal() + " of tenant "
				+ caller.getTenantId() );
		for (GGAPIAuthenticationInfos infos : this.authenticationInfos) {
			log.atInfo()
					.log("Triing to authenticate principal " + request.getPrincipal() + " of tenant "
							+ caller.getTenantId() + " with authentication of type "
							+ infos.authenticationType().getSimpleName());
			IGGAPIAuthenticationRequest authenticationRequest = new GGAPIAuthenticationRequest(caller.getDomain(),
					caller.getTenantId(), request.getPrincipal(), request.getCredentials(), infos.authenticationType());

			try {
				GGAPISpringAuthentication authentication = (GGAPISpringAuthentication) this.authenticationManager
						.authenticate(new GGAPISpringAuthenticationRequest(authenticationRequest));
				if (authentication.isAuthenticated())
					return new ResponseEntity<>(
							new GGAPISpringRestAuthenticationResponse(authentication.getAuthentication()), HttpStatus.OK);
			} catch(Exception e) {
				log.atWarn().log(infos.authenticationType().getSimpleName()+" authentication failed for principal " + request.getPrincipal() + " of tenant "
						+ caller.getTenantId());
			}

		}
		log.atWarn().log("Authentication failed for principal " + request.getPrincipal() + " of tenant "
				+ caller.getTenantId());
		
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

}
