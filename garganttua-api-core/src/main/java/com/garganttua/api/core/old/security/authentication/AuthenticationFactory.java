package com.garganttua.api.core.security.authentication;

import java.util.Optional;

import com.garganttua.core.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authentication.AuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.service.IService;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.injection.IGGInjector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationFactory implements IAuthenticationFactory {

	private Optional<IGGInjector> injector;

	private AuthenticationInfos infos;

	private Class<?> authenticationType;

	public AuthenticationFactory(AuthenticationInfos infos, Optional<IGGInjector> injector) {
		this.infos = infos;
		this.authenticationType = this.infos.authenticationType();
		this.injector = injector;
	}

	@Override
	public Object createNewAuthentication(IAuthenticationRequest authenticationRequest, IService authenticatorService, AuthenticatorInfos authenticatorInfos) throws CoreException {
		Object authentication = this.createDummy(authenticationRequest.getDomain());

		AuthenticationHelper.setCredentials(authentication, authenticationRequest.getCredentials());
		AuthenticationHelper.setPrincipal(authentication, authenticationRequest.getPrincipal());
		AuthenticationHelper.setTenantId(authentication, authenticationRequest.getTenantId());
		AuthenticationHelper.setAuthenticatorService(authentication, authenticatorService);
		AuthenticationHelper.setAuthenticatorInfos(authentication, authenticatorInfos);

		return authentication;
	}
	
	@Override
	public Object createDummy(IDomain domain) throws CoreException {

		Object authentication = AuthenticationHelper.instanciateNewOject(this.authenticationType, domain);

		this.injector.ifPresent(injector -> {
			try {
				injector.injectBeans(authentication);
				injector.injectProperties(authentication);
			} catch (ReflectionException e) {
				log.atWarn().log( "Injection failed for authentication of type "+authentication.getClass().getSimpleName(), e);
			}
		});
		return authentication;
	}

}
