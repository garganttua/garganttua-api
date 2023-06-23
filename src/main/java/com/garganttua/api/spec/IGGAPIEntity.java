/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.spec;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 
 * @author Jérémy COLOMBET
 * 
 * This interface describes the methods that an entity musts have in order to be used by the library and processed. 
 * 
 * An entity should provide methods to set/get 
 *    - Uuid : unique Id used by the library
 *    - Id : an Id that is not unique 
 *    
 * An entity must provide its domain
 *
 */
public interface IGGAPIEntity {

	@JsonIgnore
	String getId(); 
	
	void setId(String id); 
	
	@JsonIgnore
	String getUuid(); 
	
	void setUuid(String uuid); 
	
	@JsonIgnore
	IGGAPIEntityFactory<? extends IGGAPIEntity> getFactory();	
}
