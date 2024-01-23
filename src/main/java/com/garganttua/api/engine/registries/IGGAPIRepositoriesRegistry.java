package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIRepositoriesRegistry {
	
	IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getRepository(String name);

	List<IGGAPIRepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> getRepositories();

}
