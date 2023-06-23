package com.garganttua.api.business;

import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.spec.SpringCrudifyEntityException;

public interface ISpringCrudifyBusiness<Entity extends ISpringCrudifyEntity> {

	void beforeCreate(String tenantId, Entity entity) throws SpringCrudifyEntityException;

	void beforeUpdate(String tenantId, Entity entity) throws SpringCrudifyEntityException;

	void beforeDelete(String tenantId, Entity entity) throws SpringCrudifyEntityException;

}
