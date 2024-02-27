package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.repository.IGGAPIRepository;

public interface IGGAPIRepositoriesRegistry {
	
	IGGAPIRepository getRepository(String name);

	List<IGGAPIRepository> getRepositories();

}
