package com.garganttua.api.spec.repository;

import java.util.List;

import com.garganttua.api.spec.engine.IGGAPIEngineObject;

public interface IGGAPIRepositoriesRegistry extends IGGAPIEngineObject {
	
	IGGAPIRepository getRepository(String domain);

	List<IGGAPIRepository> getRepositories();

}
