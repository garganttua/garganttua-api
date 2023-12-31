package com.garganttua.api.repository.dto;

import com.garganttua.api.spec.IGGAPIEntity;

public interface IGGAPIDTOFactory<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> {

	Dto newInstance(String tenantId, Entity entity);
	
}
