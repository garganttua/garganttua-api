/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository;

import java.util.List;

import com.garganttua.api.repository.dao.ISpringCrudifyDAORepository;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomainable;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.spec.filter.SpringCrudifyLiteral;
import com.garganttua.api.spec.sort.SpringCrudifySort;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 */
public interface ISpringCrudifyRepository<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> extends ISpringCrudifyDomainable<Entity, Dto>{

	boolean doesExists(String tenantId, Entity entity);

	List<Entity> getEntities(String tenantId, int pageSize, int pageIndex, SpringCrudifyLiteral filter,
			SpringCrudifySort sort);

	void save(String tenantId, Entity entity);

	Entity update(String tenantId, Entity entity);

	Entity getOneById(String tenantId, String id);

	void delete(String tenantId, Entity entity);

	boolean doesExists(String tenantId, String uuid);

	Entity getOneByUuid(String tenantId, String uuid);

	long getCount(String tenantId, SpringCrudifyLiteral filter);

	void setDao(ISpringCrudifyDAORepository<Entity, Dto> dao);

}
