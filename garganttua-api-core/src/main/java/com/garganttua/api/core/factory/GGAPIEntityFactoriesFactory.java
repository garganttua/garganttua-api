package com.garganttua.api.core.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.updater.GGAPIEntityUpdater;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.factory.IGGAPIFactoriesRegistry;
import com.garganttua.reflection.injection.IGGInjector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityFactoriesFactory {

	private Collection<IGGAPIDomain> domains;
	private Map<String, IGGAPIEntityFactory<?>> factories = new HashMap<String, IGGAPIEntityFactory<?>>();
	private Optional<IGGInjector> injector;

	public GGAPIEntityFactoriesFactory(Collection<IGGAPIDomain> domains, Optional<IGGInjector> injector) throws GGAPIException {
		this.domains = domains;
		this.injector = injector;
		this.collectFactories();
	}

	private void collectFactories() throws GGAPIException {
		log.info("*** Creating Factories ...");

		for( IGGAPIDomain domain: this.domains ) {
			IGGAPIEntityFactory<Object> factory = new GGAPIEntityFactory(domain);
			factory.setEntityUpdater(new GGAPIEntityUpdater());
			factory.setInjector(this.injector);
			
			this.factories.put(domain.getEntity().getValue1().domain(), factory);
			
			log.info("	Factory added [domain {}, factory {}]", domain.getEntity().getValue1().domain(), factory);
		}
	}

	public IGGAPIFactoriesRegistry getRegistry() {
		return new GGAPIFactoriesRegistry(this.factories);
	}
}
