/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository;

import java.util.List;

import com.garganttua.api.repository.dao.IGGAPIDAORepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomainable;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 */
public interface IGGAPIRepository<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>{

	boolean doesExists(String tenantId, Entity entity);

	List<Entity> getEntities(String tenantId, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort);

	void save(String tenantId, Entity entity);

	Entity update(String tenantId, Entity entity);

	Entity getOneById(String tenantId, String id);

	void delete(String tenantId, Entity entity);

	boolean doesExists(String tenantId, String uuid);
	
	boolean doesExist(String tenantId, String fieldName, String fieldValue);

	Entity getOneByUuid(String tenantId, String uuid);

	long getCount(String tenantId, GGAPILiteral filter);

	void setDao(IGGAPIDAORepository<Entity, Dto> dao);

	String getTenant(Entity entity);

}
