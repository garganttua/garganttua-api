package com.garganttua.api.spec.entity;

import java.util.Map;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;

@FunctionalInterface
public interface IGGAPIEntitySaveMethod<Entity>  {

	Object save(IGGAPICaller caller, Map<String, String> parameters, Entity entity)
			throws GGAPIException;
	
}
