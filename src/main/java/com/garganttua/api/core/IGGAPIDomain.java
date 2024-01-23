package com.garganttua.api.core;

import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIDomain<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>>{
	
	Class<Entity> getEntityClass();
	
	Class<Dto> getDtoClass();	
	
	String getDomain();

}
