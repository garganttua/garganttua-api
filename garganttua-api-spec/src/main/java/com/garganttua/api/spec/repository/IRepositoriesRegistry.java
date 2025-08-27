package com.garganttua.api.spec.repository;

import java.util.List;

import com.garganttua.api.spec.engine.IEngineObject;

public interface IRepositoriesRegistry extends IEngineObject {
	
	IRepository getRepository(String domain);

	List<IRepository> getRepositories();

}
