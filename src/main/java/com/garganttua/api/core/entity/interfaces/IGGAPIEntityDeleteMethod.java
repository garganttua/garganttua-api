package com.garganttua.api.core.entity.interfaces;

import java.util.Map;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;

@FunctionalInterface
public interface IGGAPIEntityDeleteMethod<Entity> {

	void delete(IGGAPICaller caller, Map<String, String> parameters, Entity entity)
			throws GGAPIEntityException, GGAPIEngineException;
	
}
