/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.connector.GGAPIConnectorException;
import com.garganttua.api.connector.IGGAPIConnector.GGAPIConnectorOperation;
import com.garganttua.api.events.IGGAPIEventPublisher;
import com.garganttua.api.events.GGAPIEntityEvent;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.GGAPIDomainable;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.filter.GGAPILiteralException;
import com.garganttua.api.spec.sort.GGAPISort;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author JérémyCOLOMBET
 *
 * @param <Entity>
 * 
 *            This class implements the data treatments that have to be done one
 *            the entities during their process. If the Uuid of an entity is not
 *            set before storage, then the controller calculates one and affects
 *            it to the entity.
 * 
 * 
 */
@Slf4j
public class GGAPIController<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends GGAPIDomainable<Entity, Dto> implements IGGAPIController<Entity, Dto> {

	public GGAPIController(IGGAPIDomain<Entity, Dto> domain) {
		super(domain);
	}

	/**
	 * The repository used to store the entity
	 */
	@Inject
	@Setter
	protected Optional<IGGAPIRepository<Entity, Dto>> repository;

	@Inject
	@Setter
	protected Optional<IGGAPIConnector<Entity, List<Entity>, Dto>> connector;
	
	@Inject
	@Setter
	protected Optional<IGGAPIEventPublisher> eventPublisher;
	
	@Inject
	@Setter
	protected Optional<IGGAPIBusiness<Entity>> business;
	
	/**
	 * 
	 */
	@Override
	public Entity getEntity(String tenantId, String uuid) throws GGAPIEntityException {
		log.info("[Tenant {}] [Domain {}] Getting entity with Uuid " + uuid, tenantId, this.domain);
		Entity entity = null;

		if (this.connector.isPresent()) {

			try {

				entity = this.entityFactory.newInstance(uuid);

				Future<Entity> entityResponse = this.connector.get().requestEntity(tenantId, entity, GGAPIConnectorOperation.READ);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entity = entityResponse.get();
			} catch (Exception e) {
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}

		} else if (this.repository.isPresent()) {
			entity = this.repository.get().getOneByUuid(tenantId, uuid);
		}

		if (entity == null) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND,
					"Entity does not exist");
		}
		return entity;
	}

	@Override
	public Entity createEntity(String tenantId, Entity entity) throws GGAPIEntityException {

		log.info("[Tenant {}] [Domain {}] Creating entity with uuid {}", tenantId, this.domain, entity.getUuid());

		if (entity.getUuid() == null || entity.getUuid().isEmpty()) {
			entity.setUuid(UUID.randomUUID().toString());
		}

		if(this.business.isPresent()) {
			this.business.get().beforeCreate(tenantId, entity);
		}

		if (this.connector.isPresent()) {
			try {

				Future<Entity> entityResponse = this.connector.get().requestEntity(tenantId, entity, GGAPIConnectorOperation.CREATE);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entity = entityResponse.get();
			} catch (Exception e) {
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {

			if (this.repository.get().doesExists(tenantId, entity)) {
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS,
						"Entity already exists");
			}

			this.repository.get().save(tenantId, entity);
		}
		
		if( this.eventPublisher.isPresent()) {
			this.eventPublisher.get().publishEntityEvent(GGAPIEntityEvent.CREATE, entity);
		}
		
		return entity;
	}

	@Override
	public List<?> getEntityList(String tenantId, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, GGAPIReadOutputMode mode)
			throws GGAPIEntityException {
		log.info("[Tenant {}] [Domain {}] Getting entities, mode {}, page size {}, page index {}, filter {}, sort {}", tenantId, this.domain, mode, pageSize, pageIndex, filter, sort);
		try {
			GGAPILiteral.validate(filter);
		} catch (GGAPILiteralException e) {
			throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, e);
		}
		ArrayList<String> entityUuids = new ArrayList<String>();

		List<Entity> entities = null;

		if (this.connector.isPresent()) {
			try {

				Future<List<Entity>> entityResponse = this.connector.get().requestList(tenantId, null, GGAPIConnectorOperation.READ);
				
				while( !entityResponse.isDone() ) {
					Thread.sleep(250);
				}
				
				entities = entityResponse.get();	
			} catch (Exception e) {
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {
			entities = this.repository.get().getEntities(tenantId, pageSize, pageIndex, filter, sort);
		}

		switch (mode) {
		case full -> {return entities;}
		case id -> {entities.forEach(e -> { entityUuids.add(e.getId()); });}
		case uuid -> {entities.forEach(e -> { entityUuids.add(e.getUuid()); });}
		}
		
		return entityUuids;
	}

	@Override
	public Entity updateEntity(String tenantId, Entity entity) throws GGAPIEntityException {
		log.info("[Tenant {}] [Domain {}] Updating entity with Uuid " + entity.getUuid(), tenantId, this.domain);
		Entity updated = null;
		
		if(this.business.isPresent()) {
			this.business.get().beforeUpdate(tenantId, entity);
		}

		if (this.connector.isPresent()) {
			try {

				Future<Entity> entityResponse = this.connector.get().requestEntity(tenantId, entity, GGAPIConnectorOperation.UPDATE);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entity = entityResponse.get();
			} catch (Exception e) {
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {
			if (!this.repository.get().doesExists(tenantId, entity)) {
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND,
						"Entity does not exist");
			}

			updated = this.repository.get().update(tenantId, entity);
		}
		
		if( this.eventPublisher.isPresent()) {
			this.eventPublisher.get().publishEntityEvent(GGAPIEntityEvent.UPDATE, entity);
		}

		return updated;
	}

	@Override
	public void deleteEntity(String tenantId, String uuid) throws GGAPIEntityException {
		log.info("[Tenant {}] [Domain {}] Deleting entity with Uuid " + uuid, tenantId, this.domain);

		Entity entity = this.getEntity(tenantId, uuid);

		if (entity == null) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND,
					"Entity does not exist");
		}
		
		if(this.business.isPresent()) {
			this.business.get().beforeDelete(tenantId, entity);
		}
		
		if (this.connector.isPresent()) {
			try {

				Future<Entity> entityResponse = this.connector.get().requestEntity(tenantId, entity, GGAPIConnectorOperation.DELETE);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entity = entityResponse.get();
			} catch (Exception e) {
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {
			this.repository.get().delete(tenantId, entity);
		}
		
		if( this.eventPublisher.isPresent()) {
			this.eventPublisher.get().publishEntityEvent(GGAPIEntityEvent.DELETE, entity);
		}

	}

	@Override
	public void deleteEntities(final String tenantId) throws GGAPIEntityException {
		log.info("[Tenant {}] [Domain {}] Deleting all entities", tenantId, this.domain);
		List<Entity> entities = this.repository.get().getEntities(tenantId, 0, 1, null, null);

		for (Entity s : entities) {
			try {
				this.deleteEntity(tenantId, s.getUuid());
			} catch (GGAPIEntityException e) {
				throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR,
						"Error during entities deletion");
			}
		}
	}

	@Override
	public long getEntityTotalCount(String tenantId, GGAPILiteral filter) throws GGAPIEntityException {
		if (this.connector.isPresent()) {
			try {
				Future<List<Entity>> list = this.connector.get().requestList(tenantId, null, null);
				while (!list.isDone()) {
					Thread.sleep(250);
				}
				return list.get().size();
			} catch (InterruptedException | ExecutionException | GGAPIConnectorException e) {
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {
			return this.repository.get().getCount(tenantId, filter);
		}
		return 0;

	}

}
