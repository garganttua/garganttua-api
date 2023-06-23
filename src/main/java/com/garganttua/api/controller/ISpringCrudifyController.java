/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.controller;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.business.ISpringCrudifyBusiness;
import com.garganttua.api.connector.ISpringCrudifyConnector;
import com.garganttua.api.events.ISpringCrudifyEventPublisher;
import com.garganttua.api.repository.ISpringCrudifyRepository;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.spec.ISpringCrudifyDomainable;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.spec.SpringCrudifyEntityException;
import com.garganttua.api.spec.SpringCrudifyReadOutputMode;
import com.garganttua.api.spec.filter.SpringCrudifyLiteral;
import com.garganttua.api.spec.sort.SpringCrudifySort;

public interface ISpringCrudifyController<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> extends ISpringCrudifyDomainable<Entity, Dto>{

	public Entity createEntity(String tenantId, Entity object) throws SpringCrudifyEntityException;

	public Entity updateEntity(String tenantId, Entity entity) throws SpringCrudifyEntityException;
	
	public void deleteEntity(String tenantId, String id) throws SpringCrudifyEntityException;

	public void deleteEntities(String tenantId) throws SpringCrudifyEntityException;

	public Entity getEntity(String tenantId, String uuid) throws SpringCrudifyEntityException;

	public long getEntityTotalCount(String tenantId, SpringCrudifyLiteral filter) throws SpringCrudifyEntityException;

	public List<?> getEntityList(String tenantId, int pageSize, int pageIndex, SpringCrudifyLiteral filter, SpringCrudifySort sort,
			SpringCrudifyReadOutputMode mode) throws SpringCrudifyEntityException;

	public void setRepository(Optional<ISpringCrudifyRepository<Entity, Dto>> repository);

	public void setConnector(Optional<ISpringCrudifyConnector<Entity, List<Entity>, Dto>> connector);

	public void setBusiness(Optional<ISpringCrudifyBusiness<Entity>> businessObj);

	public void setEventPublisher(Optional<ISpringCrudifyEventPublisher> eventObj);

}
