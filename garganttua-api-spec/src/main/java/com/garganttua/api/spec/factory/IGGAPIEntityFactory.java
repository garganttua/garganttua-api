package com.garganttua.api.spec.factory;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.sort.IGGAPISort;
import com.garganttua.api.spec.updater.IGGAPIEntityUpdater;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public interface IGGAPIEntityFactory<Entity> extends IGGAPIEngineObject {

	Entity getEntityFromRepository(IGGAPICaller caller, Map<String, String> customParameters, GGAPIEntityIdentifier identifier, String uuid) throws GGAPIException;

	List<Entity> getEntitiesFromRepository(IGGAPICaller caller, IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort, Map<String, String> customParameters) throws GGAPIException;

	Entity prepareNewEntity(Map<String, String> customParameters, Entity entity, String uuid, String tenantId) throws GGAPIException;

	long countEntities(IGGAPICaller caller, IGGAPIFilter filter, Map<String, String> customParameters) throws GGAPIException;
	
	void setRepository(IGGAPIRepository<Object> repo);

	void setPropertyLoader(IGGPropertyLoader propertyLoader);
	
	void setEntityUpdater(IGGAPIEntityUpdater<Entity> updater);
	
	void setInjector(IGGInjector injector);

}
