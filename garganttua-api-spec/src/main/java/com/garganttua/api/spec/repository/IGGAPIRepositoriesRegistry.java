package com.garganttua.api.spec.repository;

import java.util.List;

public interface IGGAPIRepositoriesRegistry {
	
	IGGAPIRepository<?> getRepository(String domain);

	List<IGGAPIRepository<?>> getRepositories();

}
