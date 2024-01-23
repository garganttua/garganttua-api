package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIDaosRegistry {

	IGGAPIDAORepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>> getDao(String name);

	List<IGGAPIDAORepository<? extends IGGAPIEntity, ? extends IGGAPIDTOObject<? extends IGGAPIEntity>>> getDaos();
	
}
