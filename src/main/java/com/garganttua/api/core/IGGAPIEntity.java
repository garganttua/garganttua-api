/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.core;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

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
	
	
	
	void setGotFromRepository(boolean gotFromRepository);
	
	boolean isGotFromRepository();
	
	
	
	void save(IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException, GGAPIEngineException;
	
	void delete(IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException, GGAPIEngineException;
	

	

	void setRepository(IGGAPIRepository repository);
	
	void setSaveMethod(IGGAPIEntitySaveMethod saveMethod);

	void setDeleteMethod(IGGAPIEntityDeleteMethod deleteMethod);

}
