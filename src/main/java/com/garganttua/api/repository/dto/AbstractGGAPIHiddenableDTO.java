package com.garganttua.api.repository.dto;

import org.springframework.data.mongodb.core.mapping.Field;

import com.garganttua.api.spec.IGGAPIHiddenableEntity;

public abstract class AbstractGGAPIHiddenableDTO<Entity extends IGGAPIHiddenableEntity> extends AbstractGGAPIDTOObject<Entity> implements IGGAPIHiddenableDTO {

	@Field
	private boolean visible;
	
	@Override
	public boolean isVisible() {
		return this.visible;
	}
	
	protected Entity convert(Entity entity) {
		super.convert(entity);
		entity.setVisible(this.visible);
		return entity;
	}
	
	protected AbstractGGAPIHiddenableDTO(String tenantId, Entity entity){
		super(tenantId, entity);
		this.visible = entity.isVisible();
	}
	
	protected AbstractGGAPIHiddenableDTO(){
		super();
		this.visible = true;
	}

}
