/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.repository.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomainable;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.spec.filter.SpringCrudifyLiteral;
import com.garganttua.api.spec.sort.SpringCrudifySort;

public interface ISpringCrudifyDAORepository<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> extends ISpringCrudifyDomainable<Entity, Dto>{

	List<Dto> findByTenantId(String tenantId, Pageable pageable, SpringCrudifyLiteral filter, SpringCrudifySort sort);

	Dto findOneByUuidAndTenantId(String uuid, String tenantId);

	Dto findOneByIdAndTenantId(String id, String tenantId);

	Dto save(Dto object);

	void delete(Dto object);
	
	long countByTenantId(String tenantId, SpringCrudifyLiteral filter);

	void setMagicTenantId(String magicTenantId);
}
