package com.garganttua.api.spec.repository;

import java.util.List;

public interface IRepositoriesRegistry {
	
	IRepository getRepository(String domain);

	List<IRepository> getRepositories();

}
