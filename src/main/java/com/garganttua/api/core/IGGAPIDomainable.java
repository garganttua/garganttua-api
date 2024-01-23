package com.garganttua.api.core;

import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.repository.dto.IGGAPIDTOFactory;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIDomainable<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> {

	void setDomain(GGAPIDynamicDomain domain);
	
	GGAPIDynamicDomain getDynamicDomain();
	
	String getDomain();

	IGGAPIEntityFactory<Entity> getEntityFactory();
	
	IGGAPIDTOFactory<Entity, Dto> getDtoFactory();

	Class<Entity> getEntityClass();

	Class<Dto> getDtoClass();
	
}
