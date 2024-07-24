package com.garganttua.api.spec.entity;

import java.util.Map;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;

@FunctionalInterface
public interface IGGAPIEntityDeleteMethod<Entity> {

	void delete(IGGAPICaller caller, Map<String, String> parameters, Entity entity)
			throws GGAPIException;
	
}
