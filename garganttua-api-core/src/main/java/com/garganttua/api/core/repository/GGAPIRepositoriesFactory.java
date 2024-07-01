package com.garganttua.api.core.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.repository.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.repository.IGGAPIRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIRepositoriesFactory {

	private Collection<IGGAPIDomain> domains;
	private	Map<String, IGGAPIRepository<Object>> repositories = new HashMap<String, IGGAPIRepository<Object>>();

	public GGAPIRepositoriesFactory(Collection<IGGAPIDomain> domains) throws GGAPIEngineException {
		this.domains = domains;
		this.collectRepository();
	}

	private void collectRepository() {
		log.info("Creating Repositories ...");
		for( IGGAPIDomain ddomain: this.domains ){
		
			IGGAPIRepository<Object> repo;
			if(ddomain.getDtos().size() == 1) {
				repo = new GGAPISimpleRepository();
			} else {
				repo = new GGAPIMultipleRepository();
			}
			repo.setDomain(ddomain);
			this.repositories.put(ddomain.getEntity().getValue1().domain(), repo);
			log.info("	Repository added [domain {}, repo {}]", ddomain.getEntity().getValue1().domain(), repo);
		}
	}

	public IGGAPIRepositoriesRegistry getRegistry() {
		return new GGAPIRepositoriesRegistry(this.repositories);
	}
}
