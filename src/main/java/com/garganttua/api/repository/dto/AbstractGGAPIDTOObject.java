/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;

import lombok.Data;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
@Data
public abstract class AbstractGGAPIDTOObject<Entity extends IGGAPIEntity> /*implements IGGAPIDTOObject<Entity> */{
	
	@Id
	@Indexed(unique=true)
	@GGAPIDtoUuid
	protected String uuid;
	
	@Field
	@GGAPIDtoId
	protected String id;
	
	@Field
	@GGAPIDtoTenantId
	protected String tenantId;
	
	protected Entity convert(Entity entity) {
		entity.setId(this.getId());
		entity.setUuid(this.uuid);
		return entity;
	}

	protected AbstractGGAPIDTOObject() {
	}

	protected AbstractGGAPIDTOObject(String tenantId, Entity entity){
		this.tenantId = tenantId;
		this.id = entity.getId();
		this.uuid = entity.getUuid();
		this.create(entity);
	}

}
