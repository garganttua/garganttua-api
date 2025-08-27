package com.garganttua.api.security.authentication.interfaces.spring.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.core.security.authentication.AuthenticationRequest;
import com.garganttua.api.interfaces.spring.rest.CallerFilter;
import com.garganttua.api.interfaces.spring.rest.InterfaceSpringCustomizable;
import com.garganttua.api.interfaces.spring.rest.ServiceResponseUtils;
import com.garganttua.api.security.spring.core.authentication.ISpringAuthenticationInterface;
import com.garganttua.api.security.spring.core.authentication.SpringAuthentication;
import com.garganttua.api.security.spring.core.authentication.SpringAuthenticationRequest;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authentication.AuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IAuthenticationInterface;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.authentication.IAuthenticationService;
import com.garganttua.api.spec.service.IServiceCommand;
import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.reflection.beans.annotation.GGBean;
import com.garganttua.reflection.beans.annotation.GGBeanLoadingStrategy;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@GGBean(name = "SpringRestAuthenticationInterface", strategy = GGBeanLoadingStrategy.newInstance)
public class SpringAuthenticationRestInterface extends InterfaceSpringCustomizable
		implements IAuthenticationInterface, ISpringAuthenticationInterface {

	protected IAuthenticationService authenticationService;
	protected List<AuthenticationInfos> authenticationInfos = new ArrayList<AuthenticationInfos>();
	protected IDomain domain;

	@Setter
	private AuthenticationManager authenticationManager;

	@Override
	public void setAuthenticationService(IAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	public void addAuthenticationInfos(AuthenticationInfos authenticationInfos) {
		this.authenticationInfos.add(authenticationInfos);
	}

	@Override
	public void start() throws EngineException {
		try {
			this.createRequestMappings();
		} catch (NoSuchMethodException e) {
			throw new EngineException(e);
		}
	}

	private void createRequestMappings() throws NoSuchMethodException {
		RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
		options.setPatternParser(new PathPatternParser());

		Object handler = this;

		String path = "/api/" + this.domain.getDomain() + "/authenticate";
		RequestMethod requestMethod = RequestMethod.POST;
		Method method = handler.getClass().getDeclaredMethod("authenticate", ICaller.class,
				SpringRestAuthenticationRequest.class);

		this.createMapping(path, method, handler, options, requestMethod);
		this.createCustomMappings();
	}

	public ResponseEntity<?> authenticate(
			@RequestAttribute(name = CallerFilter.CALLER_ATTRIBUTE_NAME) ICaller caller,
			@RequestBody(required = true) SpringRestAuthenticationRequest request) throws CoreException {

				//En fait c'est InterfaceSpringCustomizable qui doit appeler le service
		IServiceCommand command = (event) -> {
			event.setIn("Authenticating principal " + request.getPrincipal() + " of tenant "
					+ caller.getTenantId());
			log.atInfo()
					.log("Authenticating principal " + request.getPrincipal() + " of tenant "
							+ caller.getTenantId());
			for (AuthenticationInfos infos : this.authenticationInfos) {
				log.atInfo()
						.log("Triing to authenticate principal " + request.getPrincipal() + " of tenant "
								+ caller.getTenantId() + " with authentication of type "
								+ infos.authenticationType().getSimpleName());
				IAuthenticationRequest authenticationRequest = new AuthenticationRequest(caller.getDomain(),
						caller.getTenantId(), request.getPrincipal(), request.getCredentials(),
						infos.authenticationType());

				try {
					SpringAuthentication authentication = (SpringAuthentication) this.authenticationManager
							.authenticate(new SpringAuthenticationRequest(authenticationRequest));
					if (authentication.isAuthenticated()) {
						event.setOut("Authentication successfull");
						event.setCode(ServiceResponseCode.OK);
						return event;
					}
				} catch (Exception e) {
					log.atWarn()
							.log(infos.authenticationType().getSimpleName() + " authentication failed for principal "
									+ request.getPrincipal() + " of tenant "
									+ caller.getTenantId());
				}

			}
			log.atWarn().log("Authentication failed for principal " + request.getPrincipal() + " of tenant "
					+ caller.getTenantId());
			event.setOut("Authentication failed");
			event.setCode(ServiceResponseCode.UNAUTHORIZED);

			return event;
		};

		IServiceResponse response = this.service.executeServiceCommand(caller, () -> {
			return true;
		}, command, new HashMap<>(), EntityOperation.authenticate(getName(), getClass()));

		return ServiceResponseUtils.toResponseEntity(response);
	}

	@Override
	public String getName() {
		return "SpringRestAuthenticationInterface-" + this.domain.getDomain();
	}

	@Override
	public void setDomain(IDomain domain) {
		this.domain = domain;
	}

	@Override
	public Method getAuthenticateMethod() {
		try {
			return this.getClass().getMethod("authenticate", ICaller.class,
					SpringRestAuthenticationRequest.class);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void createCustomMappings(RequestMappingHandlerMapping requestMappingHandlerMapping)
			throws NoSuchMethodException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'createCustomMappings'");
	}

}
