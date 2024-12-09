package com.garganttua.api.security.authentication.interfaces.spring.challenge.rest;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.inject.Inject;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.security.authentication.challenge.GGAPIChallengeEntityAuthenticatorHelper;
import com.garganttua.api.interfaces.spring.rest.GGAPICallerFilter;
import com.garganttua.api.interfaces.spring.rest.GGAPIResponseObject;
import com.garganttua.api.interfaces.spring.rest.GGAPIServiceResponseUtils;
import com.garganttua.api.security.authentication.interfaces.spring.rest.GGAPISpringAuthenticationRestInterface;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIMethod;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.interfasse.GGAPIInterfaceMethod;
import com.garganttua.api.spec.security.annotations.GGAPICustomServiceSecurity;
import com.garganttua.api.spec.security.authentication.GGAPIChallenge;
import com.garganttua.api.spec.service.GGAPICustomService;
import com.garganttua.api.spec.service.GGAPIServiceAccess;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

@GGBean(name = "SpringRestChallengeAuthenticationInterface", strategy = GGBeanLoadingStrategy.newInstance)
public class GGAPISpringAuthenticationChallengeRestInterface extends GGAPISpringAuthenticationRestInterface {
	
	@Inject
	private IGGAPIEngine engine;
	
	@Inject
//	private IGGAPISecurityEngine security;

	public void start() throws GGAPIEngineException {
		super.start();
		try {
			this.createRequestMappings();
		} catch (NoSuchMethodException e) {
			throw new GGAPIEngineException(e);
		}
	}
	
	private void createRequestMappings() throws NoSuchMethodException {
		RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
		options.setPatternParser(new PathPatternParser());

		String baseUrl = "/api/" + this.domain.getDomain() + "/{uuid}/challenge";

		RequestMappingInfo requestMappingInfoCreate = RequestMappingInfo.paths(baseUrl).methods(RequestMethod.GET)
				.options(options).build();

		this.requestMappingHandlerMapping.registerMapping(requestMappingInfoCreate, this,
					this.getClass().getMethod("getChallenge", IGGAPICaller.class, String.class));
	}
	
	
	@GGAPICustomServiceSecurity(access = GGAPIServiceAccess.anonymous)
	@GGAPICustomService(actionOnAllEntities = false, entity = GGAPIChallenge.class, method = GGAPIMethod.read, path = "/api/{domain}/{uuid}/challenge")
	public ResponseEntity<?> getChallenge(
			@RequestAttribute(name = GGAPICallerFilter.CALLER_ATTRIBUTE_NAME) IGGAPICaller caller,
			@PathVariable(name = "uuid") String uuid) {
		
		IGGAPIService authenticatorService = this.engine.getServicesRegistry().getService(this.domain.getDomain());
		IGGAPIServiceResponse getAuthenticatorResponse = authenticatorService.getEntity(caller, uuid, new HashMap<String, String>());

		if( getAuthenticatorResponse.getResponseCode() != GGAPIServiceResponseCode.OK ) {
			return GGAPIServiceResponseUtils.toResponseEntity(getAuthenticatorResponse);
		}
		
		try {
			GGAPIChallenge challenge = GGAPIChallengeEntityAuthenticatorHelper.getOrCreateChallengeAndSave(caller, getAuthenticatorResponse.getResponse());
		
			return new ResponseEntity<>(new GGAPIChallengeRestResponse(challenge), HttpStatus.OK);
		} catch (GGAPIException e) {
			return new ResponseEntity<>(new GGAPIResponseObject(e.getMessage(), GGAPIResponseObject.UNEXPECTED_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
		}	
	}
	
	@Override
	public HttpSecurity configureFilterChain(HttpSecurity http) throws Exception {
		http = super.configureFilterChain(http);
		http.authorizeHttpRequests()
				.requestMatchers(HttpMethod.GET, "/api/" + this.domain.getDomain() + "/{uuid}/challenge").permitAll()
				.and();
		return http;
	}

	@Override
	public Method getMethod(GGAPIInterfaceMethod method) {
		Method superReturned = super.getMethod(method);
		if( superReturned != null )
			return superReturned;
		if (method == GGAPIInterfaceMethod.authenticate) {
			try {
				return this.getClass().getMethod("getChallenge", IGGAPICaller.class, String.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
