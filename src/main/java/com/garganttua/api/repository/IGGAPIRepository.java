/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository;

import java.util.List;

import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.IGGAPIDomainable;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPIGeolocFilter;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 */
public interface IGGAPIRepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>, IGGAPIEngineObject{

	boolean doesExist(String tenantId, String ownerId, Entity entity);

	List<Entity> getEntities(String tenantId, String ownerId, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc);

	void save(String tenantId, String ownerId, Entity entity);

	Entity update(String tenantId, String ownerId, Entity entity);

	Entity getOneById(String tenantId, String ownerId, String id);

	void delete(String tenantId, String ownerId, Entity entity);

	boolean doesExist(String tenantId, String ownerId, String uuid);
	
	boolean doesExist(String tenantId, String ownerId, String uuid, String[] fieldNames, String[] fieldValues) throws GGAPIEntityException;

	Entity getOneByUuid(String tenantId, String ownerId, String uuid);

	long getCount(String tenantId, String ownerId, GGAPILiteral filter);

	void setDao(IGGAPIDAORepository<Entity, Dto> dao);

	String getTenant(Entity entity);
	
	void setMagicTenantId(String magicTenantId);

}
