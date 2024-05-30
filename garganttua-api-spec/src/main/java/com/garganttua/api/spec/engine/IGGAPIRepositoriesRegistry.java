package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.repository.IGGAPIRepository;

public interface IGGAPIRepositoriesRegistry {
	
	IGGAPIRepository<?> getRepository(String name);

	List<IGGAPIRepository<?>> getRepositories();

}
