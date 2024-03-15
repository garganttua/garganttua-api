package com.garganttua.api.engine.registries.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.garganttua.api.core.entity.factory.GGAPIEntityFactory;
import com.garganttua.api.core.entity.factory.IGGAPIEntityFactory;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.registries.IGGAPIDomainsRegistry;
import com.garganttua.api.engine.registries.IGGAPIFactoriesRegistry;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.repository.IGGAPIRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("factoriesRegistry")
public class GGAPIFactoriesRegistry implements IGGAPIFactoriesRegistry {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private Environment environment;

	@Autowired
	private IGGAPIRepositoriesRegistry repositoriesRegistry;

	@Autowired
	private IGGAPIDomainsRegistry domainsRegistry;
	
	private Map<String, GGAPIEntityFactory> factories = new HashMap<String, GGAPIEntityFactory>();
	
	@SuppressWarnings("unchecked")
	@PostConstruct
	private void init() throws GGAPIEngineException {
		log.info("Creating Factories ...");

		for( GGAPIDomain domain: this.domainsRegistry.getDomains() ) {
			IGGAPIRepository<?> repo = this.repositoriesRegistry.getRepository(domain.entity.getValue1().domain());
			
			GGAPIEntityFactory factory = new GGAPIEntityFactory(domain, (IGGAPIRepository<Object>) repo, this.context, this.environment);
			
			this.factories.put(domain.entity.getValue1().domain(), factory);
			
			log.info("	Factory added [domain {}, factory {}]", domain.entity.getValue1().domain(), factory);
		}
	}

	@Override
	public List<IGGAPIEntityFactory<?>> getFactories() {
		return new ArrayList<IGGAPIEntityFactory<?>>(this.factories.values());
	}

	@Override
	public IGGAPIEntityFactory<?> getFactory(String domain) {
		return this.factories.get(domain);
	}
}