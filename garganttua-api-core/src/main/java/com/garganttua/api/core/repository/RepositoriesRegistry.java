package com.garganttua.api.core.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.repository.IRepositoriesRegistry;
import com.garganttua.api.spec.repository.IRepository;

public class RepositoriesRegistry implements IRepositoriesRegistry {

	private Map<String, IRepository> repositories;

	public RepositoriesRegistry(Map<String, IRepository> repositories) {
		this.repositories = repositories;
	}

	@Override
	public IRepository getRepository(String domain) {
		return this.repositories.get(domain);
	}

	@Override
	public List<IRepository> getRepositories() {
		List<IRepository> repos = new ArrayList<IRepository>();
		this.repositories.forEach((k,v) -> {
			repos.add(v);
		});
		return repos;
	}

	public void setDomain(IDomain domain) {
	}

	@Override
	public void setEngine(IEngine engine) {
		this.repositories.values().parallelStream().forEach(repo -> {
			repo.setEngine(engine);
		});
	}

}
