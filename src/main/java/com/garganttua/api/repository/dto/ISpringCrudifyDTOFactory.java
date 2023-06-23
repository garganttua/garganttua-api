package com.garganttua.api.repository.dto;

import com.garganttua.api.spec.ISpringCrudifyEntity;

public interface ISpringCrudifyDTOFactory<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> {

	Dto newInstance(String tenantId, Entity entity);
	
}
