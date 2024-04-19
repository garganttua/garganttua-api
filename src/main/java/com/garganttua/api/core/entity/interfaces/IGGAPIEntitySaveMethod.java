package com.garganttua.api.core.entity.interfaces;

import java.util.Map;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;

@FunctionalInterface
public interface IGGAPIEntitySaveMethod<Entity>  {

	void save(IGGAPICaller caller, Map<String, String> parameters, Entity entity)
			throws GGAPIEntityException;
	
}
