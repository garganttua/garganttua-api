package com.garganttua.api.core.entity.factory;

import java.util.List;
import java.util.Map;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.IGGAPIEngineObject;

public interface IGGAPIEntityFactory<Entity> extends IGGAPIEngineObject {

	Entity getEntityFromRepository(IGGAPICaller caller, Map<String, String> customParameters, GGAPIEntityIdentifier identifier, String uuid) throws GGAPIFactoryException;

	List<Entity> getEntitiesFromRepository(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, Map<String, String> customParameters) throws GGAPIFactoryException;

	Entity prepareNewEntity(Map<String, String> customParameters, Entity entity) throws GGAPIFactoryException;

	Entity getEntityFromJson(Map<String, String> customParameters, byte[] json) throws GGAPIFactoryException;

	long countEntities(IGGAPICaller caller, GGAPILiteral filter, Map<String, String> customParameters) throws GGAPIFactoryException;

}
