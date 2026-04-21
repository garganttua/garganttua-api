package com.garganttua.api.commons.factory;

import java.util.List;
import java.util.Map;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.api.commons.entity.IEntityUpdater;
import com.garganttua.api.commons.ApiException;

public interface IFactory {

	Object getEntityFromRepository(ICaller caller, Map<String, String> customParameters, EntityIdentifier identifier, String uuid) throws ApiException;

	List<?> getEntitiesFromRepository(ICaller caller, IPageable pageable, IFilter filter, ISort sort, Map<String, String> customParameters) throws ApiException;

	Object prepareNewEntity(Map<String, String> customParameters, Object entity, String uuid, String tenantId) throws ApiException;

	long countEntities(ICaller caller, IFilter filter, Map<String, String> customParameters) throws ApiException;
	
	void setRepository(IRepository repo);

	void setEntityUpdater(IEntityUpdater updater);


}
