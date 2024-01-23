/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.controller;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIDomainable;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;

public interface IGGAPIController<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends IGGAPIDomainable<Entity, Dto>, IGGAPIEngineObject{

	public Entity createEntity(IGGAPICaller caller, Entity object) throws GGAPIEntityException;

	public Entity updateEntity(IGGAPICaller caller, Entity entity) throws GGAPIEntityException;
	
	public void deleteEntity(IGGAPICaller caller, String id) throws GGAPIEntityException;

	public void deleteEntities(IGGAPICaller caller) throws GGAPIEntityException;

	public Entity getEntity(IGGAPICaller caller, String uuid) throws GGAPIEntityException;

	public long getEntityTotalCount(IGGAPICaller caller, GGAPILiteral filter) throws GGAPIEntityException;

	public List<?> getEntityList(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort,
			GGAPIGeolocFilter geoloc, GGAPIReadOutputMode mode) throws GGAPIEntityException;

	public void setRepository(Optional<IGGAPIRepository<Entity, Dto>> repository);

	public void setConnector(Optional<IGGAPIConnector<Entity, List<Entity>, Dto>> connector);

	public void setBusiness(Optional<IGGAPIBusiness<Entity>> businessObj);

	Optional<IGGAPIBusiness<Entity>> getBusiness();

	Optional<IGGAPIConnector<Entity, List<Entity>, Dto>> getConnector();
}
