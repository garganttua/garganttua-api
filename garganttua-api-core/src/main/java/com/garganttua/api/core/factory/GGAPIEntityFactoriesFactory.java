package com.garganttua.api.core.factory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIFactoriesRegistry;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityFactoriesFactory {

	private Collection<IGGAPIDomain> domains;
	private Map<String, IGGAPIEntityFactory<?>> factories = new HashMap<String, IGGAPIEntityFactory<?>>();

	public GGAPIEntityFactoriesFactory(Collection<IGGAPIDomain> domains) throws GGAPIException {
		this.domains = domains;
		this.collectFactories();
	}

	private void collectFactories() throws GGAPIException {
		log.info("Creating Factories ...");

		for( IGGAPIDomain domain: this.domains ) {
			IGGAPIEntityFactory<Object> factory = new GGAPIEntityFactory(domain);
			this.factories.put(domain.getEntity().getValue1().domain(), factory);
			
			log.info("	Factory added [domain {}, factory {}]", domain.getEntity().getValue1().domain(), factory);
		}
	}

	public IGGAPIFactoriesRegistry getRegistry() {
		return new GGAPIFactoriesRegistry(this.factories);
	}

}
