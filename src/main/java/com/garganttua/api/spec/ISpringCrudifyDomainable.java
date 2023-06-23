package com.garganttua.api.spec;

import com.garganttua.api.repository.dto.ISpringCrudifyDTOFactory;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;

public interface ISpringCrudifyDomainable<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> {

	void setDomain(ISpringCrudifyDomain<Entity, Dto> domain);
	
	String getDomain();

	ISpringCrudifyEntityFactory<Entity> getEntityFactory();
	
	ISpringCrudifyDTOFactory<Entity, Dto> getDtoFactory();

	Class<Entity> getEntityClass();

	Class<Dto> getDtoClass();
	
}
