package com.garganttua.api.spec.factory;

import java.util.List;
import java.util.Map;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.IGGAPISort;

public interface IGGAPIEntityFactory<Entity> extends IGGAPIEngineObject {

	Entity getEntityFromRepository(IGGAPICaller caller, Map<String, String> customParameters, GGAPIEntityIdentifier identifier, String uuid) throws GGAPIException;

	List<Entity> getEntitiesFromRepository(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, IGGAPISort sort, Map<String, String> customParameters) throws GGAPIException;

	Entity prepareNewEntity(Map<String, String> customParameters, Entity entity) throws GGAPIException;

	Entity getEntityFromJson(Map<String, String> customParameters, byte[] json) throws GGAPIException;

	long countEntities(IGGAPICaller caller, GGAPILiteral filter, Map<String, String> customParameters) throws GGAPIException;

}
