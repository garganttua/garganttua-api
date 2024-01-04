package com.garganttua.api.business;

import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.IGGAPIEntity;

public interface IGGAPIBusiness<Entity extends IGGAPIEntity> extends IGGAPIEngineObject {

	void beforeCreate(String tenantId, Entity entity) throws GGAPIEntityException;
	
	void afterCreate(String tenantId, Entity entity) throws GGAPIEntityException;

	void beforeUpdate(String tenantId, Entity entity) throws GGAPIEntityException;
	
	void afterUpdate(String tenantId, Entity entity) throws GGAPIEntityException;

	void beforeDelete(String tenantId, Entity entity) throws GGAPIEntityException;
	
	void afterDelete(String tenantId, Entity entity) throws GGAPIEntityException;

}
