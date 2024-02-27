package com.garganttua.api.core;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.IGGAPISecurity;

@FunctionalInterface
public interface IGGAPIEntitySaveMethod {

	<Entity extends IGGAPIEntity> void save(GGAPIDynamicDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Optional<IGGAPISecurity> security, Map<String, String> parameters, Entity entity)
			throws GGAPIEntityException, GGAPIEngineException;
	
}
