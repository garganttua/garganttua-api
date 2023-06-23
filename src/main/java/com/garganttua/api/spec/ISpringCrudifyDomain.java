package com.garganttua.api.spec;

import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;

public interface ISpringCrudifyDomain<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>>{
	
	Class<Entity> getEntityClass();
	
	Class<Dto> getDtoClass();	
	
	String getDomain();

}
