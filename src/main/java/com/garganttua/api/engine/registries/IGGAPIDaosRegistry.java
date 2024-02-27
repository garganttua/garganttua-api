package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.repository.dao.IGGAPIDAORepository;

public interface IGGAPIDaosRegistry {

	IGGAPIDAORepository getDao(String name);

	List<IGGAPIDAORepository> getDaos();
	
}
