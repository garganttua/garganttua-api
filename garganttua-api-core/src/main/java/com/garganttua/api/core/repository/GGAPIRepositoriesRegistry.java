package com.garganttua.api.core.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.repository.IGGAPIRepositoriesRegistry;
import com.garganttua.api.spec.repository.IGGAPIRepository;

public class GGAPIRepositoriesRegistry implements IGGAPIRepositoriesRegistry {

	private Map<String, IGGAPIRepository<Object>> repositories;

	public GGAPIRepositoriesRegistry(Map<String, IGGAPIRepository<Object>> repositories) {
		this.repositories = repositories;
	}

	@Override
	public IGGAPIRepository<?> getRepository(String domain) {
		return this.repositories.get(domain);
	}

	@Override
	public List<IGGAPIRepository<?>> getRepositories() {
		List<IGGAPIRepository<?>> repos = new ArrayList<IGGAPIRepository<?>>();
		this.repositories.forEach((k,v) -> {
			repos.add(v);
		});
		return repos;
	}

}
