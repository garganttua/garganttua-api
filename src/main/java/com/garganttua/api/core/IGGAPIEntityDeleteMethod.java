package com.garganttua.api.core;

import java.util.Map;

import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;

public interface IGGAPIEntityDeleteMethod {

	<Entity extends IGGAPIEntity> void delete(GGAPIDynamicDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Map<String, String> parameters, Entity entity)
			throws GGAPIEntityException, GGAPIEngineException;
	
}
