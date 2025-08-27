package com.garganttua.api.core.security.authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.security.authentication.AuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IAuthenticationInfosRegistry;
import com.garganttua.reflection.injection.IGGInjector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationFactoryFactory {

	private IAuthenticationInfosRegistry authenticationInfosRegistry;
	private Optional<IGGInjector> injector;
	private Map<Class<?>, IAuthenticationFactory> factories = new HashMap<Class<?>, IAuthenticationFactory>();

	public AuthenticationFactoryFactory(IAuthenticationInfosRegistry authenticationInfosRegistry,
			Optional<IGGInjector> injector) {
		this.authenticationInfosRegistry = authenticationInfosRegistry;
		this.injector = injector;
		this.createFactories();
	}

	private void createFactories() {
		log.info("*** Creating Authentication factories ...");
		this.authenticationInfosRegistry.getAuthentications().forEach(authenticationType -> {
			AuthenticationInfos infos = this.authenticationInfosRegistry.getAuthenticationInfos(authenticationType);
			
			AuthenticationFactory AuthenticationFactory = new AuthenticationFactory(infos, this.injector);
			this.factories.put(authenticationType, AuthenticationFactory);
			log.info("	Authentication factory added [authentication {}, factory {}]", authenticationType.getSimpleName(), AuthenticationFactory);
		});
	}

	public IAuthenticationFactoriesRegistry getRegistry() {
		return new AuthenticationFactoriesRegistry(this.factories);
	}

}
