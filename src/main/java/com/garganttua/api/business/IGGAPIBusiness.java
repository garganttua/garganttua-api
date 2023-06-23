package com.garganttua.api.business;

import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.GGAPIEntityException;

public interface IGGAPIBusiness<Entity extends IGGAPIEntity> {

	void beforeCreate(String tenantId, Entity entity) throws GGAPIEntityException;

	void beforeUpdate(String tenantId, Entity entity) throws GGAPIEntityException;

	void beforeDelete(String tenantId, Entity entity) throws GGAPIEntityException;

}
