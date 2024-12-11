package com.garganttua.api.core.security.authentication;

import java.util.Optional;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationRequest;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.injection.IGGInjector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticationFactory implements IGGAPIAuthenticationFactory {

	private Optional<IGGInjector> injector;

	private GGAPIAuthenticationInfos infos;

	private Class<?> authenticationType;

	public GGAPIAuthenticationFactory(GGAPIAuthenticationInfos infos, Optional<IGGInjector> injector) {
		this.infos = infos;
		this.authenticationType = this.infos.authenticationType();
		this.injector = injector;
	}

	@Override
	public Object createNewAuthentication(IGGAPIAuthenticationRequest authenticationRequest, IGGAPIService authenticatorService, GGAPIAuthenticatorInfos authenticatorInfos) throws GGAPIException {
		Object authentication = this.createDummy(authenticationRequest.getDomain());

		GGAPIAuthenticationHelper.setCredentials(authentication, authenticationRequest.getCredentials());
		GGAPIAuthenticationHelper.setPrincipal(authentication, authenticationRequest.getPrincipal());
		GGAPIAuthenticationHelper.setTenantId(authentication, authenticationRequest.getTenantId());
		GGAPIAuthenticationHelper.setAuthenticatorService(authentication, authenticatorService);
		GGAPIAuthenticationHelper.setAuthenticatorInfos(authentication, authenticatorInfos);

		return authentication;
	}
	
	@Override
	public Object createDummy(IGGAPIDomain domain) throws GGAPIException {

		Object authentication = GGAPIAuthenticationHelper.instanciateNewOject(this.authenticationType, domain);

		this.injector.ifPresent(injector -> {
			try {
				injector.injectBeans(authentication);
				injector.injectProperties(authentication);
			} catch (GGReflectionException e) {
				log.atWarn().log( "Injection failed for authentication of type "+authentication.getClass().getSimpleName(), e);
			}
		});
		return authentication;
	}

}
