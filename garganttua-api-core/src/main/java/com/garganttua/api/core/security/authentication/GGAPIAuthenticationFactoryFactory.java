package com.garganttua.api.core.security.authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactoriesRegistry;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationFactory;
import com.garganttua.api.spec.security.authentication.IGGAPIAuthenticationInfosRegistry;
import com.garganttua.reflection.injection.IGGInjector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIAuthenticationFactoryFactory {

	private IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry;
	private Optional<IGGInjector> injector;
	private Map<Class<?>, IGGAPIAuthenticationFactory> factories = new HashMap<Class<?>, IGGAPIAuthenticationFactory>();

	public GGAPIAuthenticationFactoryFactory(IGGAPIAuthenticationInfosRegistry authenticationInfosRegistry,
			Optional<IGGInjector> injector) {
		this.authenticationInfosRegistry = authenticationInfosRegistry;
		this.injector = injector;
		this.createFactories();
	}

	private void createFactories() {
		log.info("*** Creating Authentication factories ...");
		this.authenticationInfosRegistry.getAuthentications().forEach(authenticationType -> {
			GGAPIAuthenticationInfos infos = this.authenticationInfosRegistry.getAuthenticationInfos(authenticationType);
			
			GGAPIAuthenticationFactory ggapiAuthenticationFactory = new GGAPIAuthenticationFactory(infos, this.injector);
			this.factories.put(authenticationType, ggapiAuthenticationFactory);
			log.info("	Authentication factory added [authentication {}, factory {}]", authenticationType.getSimpleName(), ggapiAuthenticationFactory);
		});
	}

	public IGGAPIAuthenticationFactoriesRegistry getRegistry() {
		return new GGAPIAuthenticationFactoriesRegistry(this.factories);
	}

}
