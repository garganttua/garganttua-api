package com.garganttua.api.spec;

import com.garganttua.api.repository.dto.IGGAPIDTOFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIDomainable<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> {

	void setDomain(IGGAPIDomain<Entity, Dto> domain);
	
	String getDomain();

	IGGAPIEntityFactory<Entity> getEntityFactory();
	
	IGGAPIDTOFactory<Entity, Dto> getDtoFactory();

	Class<Entity> getEntityClass();

	Class<Dto> getDtoClass();
	
}
