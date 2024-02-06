/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository;

import java.util.List;

import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIDomainable;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 */
public interface IGGAPIRepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>, IGGAPIEngineObject{

	boolean doesExist(IGGAPICaller caller, Entity entity);

	List<Entity> getEntities(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc);

	void save(IGGAPICaller caller, Entity entity);

	Entity update(IGGAPICaller caller, Entity entity);

	Entity getOneById(IGGAPICaller caller, String id);

	void delete(IGGAPICaller caller, Entity entity);

	boolean doesExist(IGGAPICaller caller, String uuid);
	
//	boolean doesExist(IGGAPICaller caller, String uuid, String[] fieldNames, String[] fieldValues) throws GGAPIEntityException;

	Entity getOneByUuid(IGGAPICaller caller, String uuid);

	long getCount(IGGAPICaller caller, GGAPILiteral filter, GGAPIGeolocFilter geoloc);

	void setDao(IGGAPIDAORepository<Entity, Dto> dao);

	String getTenant(Entity entity);

}
