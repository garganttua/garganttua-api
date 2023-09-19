/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.controller;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.IGGAPIDomainable;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPIGeolocFilter;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

public interface IGGAPIController<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>{

	public Entity createEntity(String tenantId, String userId, Entity object) throws GGAPIEntityException;

	public Entity updateEntity(String tenantId, String userId, Entity entity) throws GGAPIEntityException;
	
	public void deleteEntity(String tenantId, String userId, String id) throws GGAPIEntityException;

	public void deleteEntities(String tenantId, String userId) throws GGAPIEntityException;

	public Entity getEntity(String tenantId, String userId, String uuid) throws GGAPIEntityException;

	public long getEntityTotalCount(String tenantId, String userId, GGAPILiteral filter) throws GGAPIEntityException;

	public List<?> getEntityList(String tenantId, String userId, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort,
			GGAPIGeolocFilter geoloc, GGAPIReadOutputMode mode) throws GGAPIEntityException;

	public void setRepository(Optional<IGGAPIRepository<Entity, Dto>> repository);

	public void setConnector(Optional<IGGAPIConnector<Entity, List<Entity>, Dto>> connector);

	public void setBusiness(Optional<IGGAPIBusiness<Entity>> businessObj);

	public void setTenant(boolean tenantEntity);
	
	public void setUnicity(String[] unicity);

}
