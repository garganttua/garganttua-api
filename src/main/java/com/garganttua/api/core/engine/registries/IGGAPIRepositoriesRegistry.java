package com.garganttua.api.core.engine.registries;

import java.util.List;

import com.garganttua.api.core.repository.IGGAPIRepository;

public interface IGGAPIRepositoriesRegistry {
	
	IGGAPIRepository<?> getRepository(String name);

	List<IGGAPIRepository<?>> getRepositories();

}
