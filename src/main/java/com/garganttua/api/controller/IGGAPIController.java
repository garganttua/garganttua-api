/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.controller;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomainable;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

public interface IGGAPIController<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>{

	public Entity createEntity(String tenantId, Entity object) throws GGAPIEntityException;

	public Entity updateEntity(String tenantId, Entity entity) throws GGAPIEntityException;
	
	public void deleteEntity(String tenantId, String id) throws GGAPIEntityException;

	public void deleteEntities(String tenantId) throws GGAPIEntityException;

	public Entity getEntity(String tenantId, String uuid) throws GGAPIEntityException;

	public long getEntityTotalCount(String tenantId, GGAPILiteral filter) throws GGAPIEntityException;

	public List<?> getEntityList(String tenantId, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort,
			GGAPIReadOutputMode mode) throws GGAPIEntityException;

	public void setRepository(Optional<IGGAPIRepository<Entity, Dto>> repository);

	public void setConnector(Optional<IGGAPIConnector<Entity, List<Entity>, Dto>> connector);

	public void setBusiness(Optional<IGGAPIBusiness<Entity>> businessObj);

	public void setEventPublisher(Optional<IGGAPIEventPublisher> eventObj);

}