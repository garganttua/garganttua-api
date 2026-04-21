package com.garganttua.api.commons.repository;

import java.util.List;

public interface IRepositoriesRegistry {
	
	IRepository getRepository(String domain);

	List<IRepository> getRepositories();

}
