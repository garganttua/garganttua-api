package com.garganttua.api.core.entity.interfaces;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

@FunctionalInterface
public interface IGGAPIEntitySaveMethod<Entity>  {

	void save(IGGAPICaller caller, Map<String, String> parameters, Entity entity)
			throws GGAPIEntityException;
	
}
