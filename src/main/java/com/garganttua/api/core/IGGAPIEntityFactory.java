package com.garganttua.api.core;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;

public interface IGGAPIEntityFactory {
	
	void setRepositoriesRegistry(IGGAPIRepositoriesRegistry registry);

	<T extends IGGAPIEntity> T getEntityFromRepository(GGAPIDynamicDomain domain, IGGAPICaller caller, Map<String, String> customParameters, String uuid) throws GGAPIEntityException;

	<T extends IGGAPIEntity> List<T> getEntitiesFromRepository(GGAPIDynamicDomain domain, IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc, Map<String, String> customParameters) throws GGAPIEntityException;

	<T extends IGGAPIEntity> T prepareNewEntity(Map<String, String> customParameters, T entity) throws GGAPIEntityException, GGAPIEngineException;

	<T extends IGGAPIEntity> T getEntityFromJson(GGAPIDynamicDomain domain, Map<String, String> customParameters, byte[] json) throws GGAPIEntityException, GGAPIEngineException;

	long countEntities(GGAPIDynamicDomain dynamicDomain, IGGAPICaller caller, GGAPILiteral filter, GGAPIGeolocFilter geoloc, Map<String, String> customParameters);

	void setSpringContext(ApplicationContext context);

	void setEnvironment(Environment environment);

}
