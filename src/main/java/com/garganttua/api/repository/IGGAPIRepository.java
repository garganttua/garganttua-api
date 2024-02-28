/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository;

import java.util.List;

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 */
public interface IGGAPIRepository extends IGGAPIEngineObject {

	<Entity extends IGGAPIEntity> boolean doesExist(IGGAPICaller caller, Entity entity) throws GGAPIEngineException;

	<Entity extends IGGAPIEntity> List<Entity> getEntities(GGAPIDynamicDomain domain, IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc);

	<Entity extends IGGAPIEntity> void save(IGGAPICaller caller, Entity entity) throws GGAPIEntityException, GGAPIEngineException;

	<Entity extends IGGAPIEntity> Entity update(IGGAPICaller caller, Entity entity) throws GGAPIEntityException, GGAPIEngineException;

	<Entity extends IGGAPIEntity> Entity getOneById(GGAPIDynamicDomain domain, IGGAPICaller caller, String id);

	<Entity extends IGGAPIEntity> void delete(IGGAPICaller caller, Entity entity) throws GGAPIEntityException, GGAPIEngineException;

	boolean doesExist(GGAPIDynamicDomain domain, IGGAPICaller caller, String uuid);
	
	<Entity extends IGGAPIEntity> Entity getOneByUuid(GGAPIDynamicDomain domain, IGGAPICaller caller, String uuid);

	long getCount(GGAPIDynamicDomain domain, IGGAPICaller caller, GGAPILiteral filter, GGAPIGeolocFilter geoloc);

	void setDao(IGGAPIDAORepository dao);

	<Entity extends IGGAPIEntity> String getTenant(Entity entity) throws GGAPIEngineException;

}
