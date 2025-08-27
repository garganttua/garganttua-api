package com.garganttua.api.core.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.engine.EngineException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.repository.IRepositoriesRegistry;
import com.garganttua.api.spec.repository.IRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RepositoriesFactory {

	private Collection<IDomain> domains;
	private	Map<String, IRepository> repositories = new HashMap<String, IRepository>();

	public RepositoriesFactory(Collection<IDomain> domains) throws EngineException {
		this.domains = domains;
		this.collectRepository();
	}

	private void collectRepository() {
		log.info("*** Creating Repositories ...");
		for( IDomain ddomain: this.domains ){
		
			IRepository repo;
			if(ddomain.getDtos().size() == 1) {
				repo = new SimpleRepository();
			} else {
				repo = new MultipleRepository();
			}
			repo.setDomain(ddomain);
			this.repositories.put(ddomain.getDomain(), repo);
			log.info("	Repository added [domain {}, repo {}]", ddomain.getDomain(), repo);
		}
	}

	public IRepositoriesRegistry getRegistry() {
		return new RepositoriesRegistry(this.repositories);
	}
}
