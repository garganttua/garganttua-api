package com.garganttua.api.business;

import java.util.List;
import java.util.Map;

import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.IGGAPIEngineObject;

public interface IGGAPIBusiness<Entity extends IGGAPIEntity> extends IGGAPIEngineObject {

	Entity beforeCreate(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException;
	
	Entity afterCreate(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException;

	Entity beforeUpdate(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException;
	
	Entity afterUpdate(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException;

	Entity beforeDelete(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException;
	
	Entity afterDelete(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException;

	Entity afterGetOne(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException;

	Entity beforeGetOne(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException;

	List<Entity> beforeGetList(IGGAPICaller caller, List<Entity> entities, Map<String, String> customParameters) throws GGAPIEntityException;

	List<Entity> afterGetList(IGGAPICaller caller, List<Entity> entities, Map<String, String> customParameters) throws GGAPIEntityException;


}
