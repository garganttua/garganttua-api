package com.garganttua.api.business;

import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.IGGAPIEngineObject;

public interface IGGAPIBusiness<Entity extends IGGAPIEntity> extends IGGAPIEngineObject {

	void beforeCreate(IGGAPICaller caller, Entity entity) throws GGAPIEntityException;
	
	void afterCreate(IGGAPICaller caller, Entity entity) throws GGAPIEntityException;

	void beforeUpdate(IGGAPICaller caller, Entity entity) throws GGAPIEntityException;
	
	void afterUpdate(IGGAPICaller caller, Entity entity) throws GGAPIEntityException;

	void beforeDelete(IGGAPICaller caller, Entity entity) throws GGAPIEntityException;
	
	void afterDelete(IGGAPICaller caller, Entity entity) throws GGAPIEntityException;

}
