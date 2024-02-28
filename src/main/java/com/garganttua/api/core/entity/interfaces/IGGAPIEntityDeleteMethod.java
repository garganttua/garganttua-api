package com.garganttua.api.core.entity.interfaces;

import java.util.Map;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;

public interface IGGAPIEntityDeleteMethod {

	<Entity> void delete(GGAPIDynamicDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Map<String, String> parameters, Entity entity)
			throws GGAPIEntityException, GGAPIEngineException;
	
}
