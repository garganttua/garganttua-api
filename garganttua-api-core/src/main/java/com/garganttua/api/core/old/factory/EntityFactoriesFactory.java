package com.garganttua.api.core.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.updater.EntityUpdater;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.factory.IFactoriesRegistry;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.reflection.injection.IGGInjector;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityFactoriesFactory {

	private Collection<IDomain> domains;
	private Map<String, IFactory> factories = new HashMap<String, IFactory>();
	private Optional<IGGInjector> injector;

	public EntityFactoriesFactory(Collection<IDomain> domains, Optional<IGGInjector> injector) throws CoreException {
		this.domains = domains;
		this.injector = injector;
		this.collectFactories();
	}

	private void collectFactories() throws CoreException {
		log.info("*** Creating Factories ...");

		for( IDomain domain: this.domains ) {
			IFactory factory = new Factory(domain);
			factory.setEntityUpdater(new EntityUpdater());
			factory.setInjector(this.injector);
			
			this.factories.put(domain.getDomain(), factory);
			
			log.info("	Factory added [domain {}, factory {}]", domain.getDomain(), factory);
		}
	}

	public IFactoriesRegistry getRegistry() {
		return new FactoriesRegistry(this.factories);
	}
}
