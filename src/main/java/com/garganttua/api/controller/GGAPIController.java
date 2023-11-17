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

import org.springframework.beans.factory.annotation.Autowired;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.connector.GGAPIConnectorException;
import com.garganttua.api.connector.IGGAPIConnector;
import com.garganttua.api.connector.IGGAPIConnector.GGAPIConnectorOperation;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.spec.GGAPIDomainable;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIEntityHelper;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPIGeolocFilter;
import com.garganttua.api.spec.filter.GGAPIGeolocFilterException;
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
 *                 This class implements the data treatments that have to be
 *                 done one the entities during their process. If the Uuid of an
 *                 entity is not set before storage, then the controller
 *                 calculates one and affects it to the entity.
 * 
 * 
 */
@Slf4j
public class GGAPIController<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>>
		extends GGAPIDomainable<Entity, Dto> implements IGGAPIController<Entity, Dto> {

	public GGAPIController(IGGAPIDomain<Entity, Dto> domain) {
		super(domain);
	}

	/**
	 * The repository used to store the entity
	 */
	@Autowired
	@Setter
	protected Optional<IGGAPIRepository<Entity, Dto>> repository;

	@Autowired
	@Setter
	protected Optional<IGGAPIConnector<Entity, List<Entity>, Dto>> connector;

	@Autowired
	@Setter
	protected Optional<IGGAPIBusiness<Entity>> business;

	protected boolean tenant = false;

	@Setter
	protected String[] unicity = {};
	
	@Setter
	protected String[] mandatory = {};

	/**
	 * 
	 */
	@Override
	public Entity getEntity(String tenantId, String userId, String uuid) throws GGAPIEntityException {
		log.info("[Tenant {}] [UserId {}] [Domain {}] Getting entity with Uuid " + uuid, tenantId, userId, this.domain);
		Entity entity = null;

		if (this.connector.isPresent()) {

			try {

				entity = this.entityFactory.newInstance(uuid);

				Future<Entity> entityResponse = this.connector.get().requestEntity(tenantId, entity,
						GGAPIConnectorOperation.READ);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entity = entityResponse.get();
			} catch (Exception e) {
				log.error("[Tenant {" + tenantId + "}] [UserId {" + userId + "}] [Domain {" + this.domain
						+ "}] Error during getting entity with Uuid " + uuid, e);
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}

		} else if (this.repository.isPresent()) {
			entity = this.repository.get().getOneByUuid(tenantId, uuid);
		}

		if (entity == null) {
			log.warn("[Tenant {}] [UserId {}] [Domain {}] Entity with Uuid " + uuid + " not found", tenantId, userId,
					this.domain);
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND, "Entity does not exist");
		}
		return entity;

	}

	@Override
	public Entity createEntity(String tenantId, String userId, Entity entity) throws GGAPIEntityException {

		log.info("[Tenant {}] [UserId {}] [Domain {}] Creating entity with uuid {}", tenantId, userId, this.domain,
				entity.getUuid());

		if (entity.getUuid() == null || entity.getUuid().isEmpty()) {
			entity.setUuid(UUID.randomUUID().toString());
		}

		if (this.tenant) {
			tenantId = entity.getUuid();
		}
		
		if( this.mandatory.length > 0 ) {
			this.checkMandatoryFields(this.mandatory, entity);
		}

		if (this.business.isPresent()) {
			this.business.get().beforeCreate(tenantId, entity);
		}

		if (this.connector.isPresent()) {
			try {

				Future<Entity> entityResponse = this.connector.get().requestEntity(tenantId, entity,
						GGAPIConnectorOperation.CREATE);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entity = entityResponse.get();
			} catch (Exception e) {
				log.error("[Tenant {" + tenantId + "}] [UserId {" + userId + "}] [Domain {" + this.domain
						+ "}] Error during creating entity with Uuid " + entity.getUuid(), e);
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {

			if (this.repository.get().doesExist(tenantId, entity)) {
				log.warn("[Tenant {}] [UserId {}] [Domain {}] Entity with Uuid " + entity.getUuid() + " already exists",
						tenantId, userId, this.domain);
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS, "Entity already exists");
			}

			if( this.unicity != null && this.unicity.length > 0) {
				this.checkUnicityFields(tenantId, userId, entity, false);
			}

			this.repository.get().save(tenantId, entity);
		}

		if (this.business.isPresent()) {
			this.business.get().afterCreate(tenantId, entity);
		}

		return entity;
	}

	protected void checkMandatoryFields(String[] mandatory, Entity entity) throws GGAPIEntityException {
		
		for( String field: mandatory ) {
			try {
				Object value = GGAPIEntityHelper.getFieldValue(this.entityClass, field, entity);
				
				if( value == null ) {
					throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, "Field "+field+" is mandatory");
				} else if( value.toString().isEmpty() ){
					throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, "Field "+field+" is mandatory");
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new GGAPIEntityException(e);
			}
		}
	}

	@Override
	public List<?> getEntityList(String tenantId, String userId, int pageSize, int pageIndex, GGAPILiteral filter,
			GGAPISort sort, GGAPIGeolocFilter geoloc, GGAPIReadOutputMode mode) throws GGAPIEntityException {
		log.info(
				"[Tenant {}] [UserId {}] [Domain {}] Getting entities, mode {}, page size {}, page index {}, filter {}, sort {}, geoloc {}",
				tenantId, userId, this.domain, mode, pageSize, pageIndex, filter, sort, geoloc);

		try {
			GGAPILiteral.validate(filter);
		} catch (GGAPILiteralException e) {
			log.warn("[Tenant {}] [UserId {}] [Domain {}] Cannot validate filter " + filter, tenantId, userId,
					this.domain);
			throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, e);
		}

		try {
			GGAPIGeolocFilter.validate(geoloc);
		} catch (GGAPIGeolocFilterException e) {
			log.warn("[Tenant {}] [UserId {}] [Domain {}] Cannot validate geo filter " + filter, tenantId, userId,
					this.domain);
			throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, e);
		}

		ArrayList<String> entityUuids = new ArrayList<String>();

		List<Entity> entities = null;

		if (this.connector.isPresent()) {
			try {

				Future<List<Entity>> entityResponse = this.connector.get().requestList(tenantId, null,
						GGAPIConnectorOperation.READ);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entities = entityResponse.get();
			} catch (Exception e) {
				log.error("[Tenant {" + tenantId + "}] [UserId {" + userId + "}] [Domain {" + this.domain
						+ "}] Error during getting entity list ", e);
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {
			entities = this.repository.get().getEntities(tenantId, pageSize, pageIndex, filter, sort, geoloc);
		}

		switch (mode) {
		case full -> {
			return entities;
		}
		case id -> {
			entities.forEach(e -> {
				entityUuids.add(e.getId());
			});
		}
		case uuid -> {
			entities.forEach(e -> {
				entityUuids.add(e.getUuid());
			});
		}
		}

		return entityUuids;
	}

	@Override
	public Entity updateEntity(String tenantId, String userId, Entity entity) throws GGAPIEntityException {
		log.info("[Tenant {}] [UserId {}] [Domain {}] Updating entity with Uuid " + entity.getUuid(), tenantId, userId,
				this.domain);
		Entity updated = null;

		if (this.business.isPresent()) {
			this.business.get().beforeUpdate(tenantId, entity);
		}

		if (this.connector.isPresent()) {
			try {

				Future<Entity> entityResponse = this.connector.get().requestEntity(tenantId, entity,
						GGAPIConnectorOperation.UPDATE);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entity = entityResponse.get();
			} catch (Exception e) {
				log.error("[Tenant {" + tenantId + "}] [UserId {" + userId + "}] [Domain {" + this.domain
						+ "}] Error during updating entity ", e);
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {
			if (!this.repository.get().doesExist(tenantId, entity)) {
				log.warn(
						"[Tenant {}] [UserId {}] [Domain {}] Entity with uuid " + entity.getUuid() + " does not exists",
						tenantId, userId, this.domain);
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND, "Entity does not exist");
			}

			if( this.unicity != null && this.unicity.length > 0) {
				this.checkUnicityFields(tenantId, userId, entity, true);
			}

			updated = this.repository.get().update(tenantId, entity);
		}

		if (this.business.isPresent()) {
			this.business.get().afterUpdate(tenantId, entity);
		}

		return updated;
	}

	private void checkUnicityFields(String tenantId, String userId, Entity entity, boolean forUpdate) throws GGAPIEntityException {
		try {
			List<String> values = new ArrayList<String>();
			for (String fieldName : this.unicity) {
				values.add(GGAPIEntityHelper.getFieldValue(this.entityClass, fieldName, entity).toString());
			}
			String[] fieldValues = new String[values.size()];
			values.toArray(fieldValues);

			if (this.repository.get().doesExist(tenantId, forUpdate?entity.getUuid():null, this.unicity, fieldValues)) {
				log.warn("[Tenant {}] [UserId {}] [Domain {}] Entity with value for field " + this.unicity.toString()
						+ " already exists", tenantId, userId, this.domain);
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS,
						"Entity with same " + this.unicity + " value already exists");
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException
				| GGAPIEntityException e) {
			log.error("[Tenant {" + tenantId + "}] [UserId {" + userId + "}] [Domain {" + this.domain
					+ "}] Error during checking unicity fields for entity with Uuid " + entity.getUuid(), e);
			throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, e.getMessage(), e);
		}
	}

	@Override
	public void deleteEntity(String tenantId, String userId, String uuid) throws GGAPIEntityException {
		log.info("[Tenant {}] [UserId {}] [Domain {}] Deleting entity with Uuid " + uuid, tenantId, userId,
				this.domain);

		Entity entity = this.getEntity(tenantId, userId, uuid);

		if (entity == null) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND, "Entity does not exist");
		}

		if (this.business.isPresent()) {
			this.business.get().beforeDelete(tenantId, entity);
		}

		if (this.connector.isPresent()) {
			try {

				Future<Entity> entityResponse = this.connector.get().requestEntity(tenantId, entity,
						GGAPIConnectorOperation.DELETE);

				while (!entityResponse.isDone()) {
					Thread.sleep(250);
				}

				entity = entityResponse.get();
			} catch (Exception e) {
				log.error("[Tenant {" + tenantId + "}] [UserId {" + userId + "}] [Domain {" + this.domain
						+ "}] Error during updating entity ", e);
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {
			this.repository.get().delete(tenantId, entity);
		}

		if (this.business.isPresent()) {
			this.business.get().afterDelete(tenantId, entity);
		}

	}

	@Override
	public void deleteEntities(final String tenantId, String userId) throws GGAPIEntityException {
		log.info("[Tenant {}] [UserId {}] [Domain {}] Deleting all entities", tenantId, userId, this.domain);
		List<Entity> entities = this.repository.get().getEntities(tenantId, 0, 1, null, null, null);

		for (Entity s : entities) {
			try {
				this.deleteEntity(tenantId, userId, s.getUuid());
			} catch (GGAPIEntityException e) {
				log.error("[Tenant {" + tenantId + "}] [UserId {" + userId + "}] [Domain {" + this.domain
						+ "}] Error during entities deletion ", e);
				throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, "Error during entities deletion");
			}
		}
	}

	@Override
	public long getEntityTotalCount(String tenantId, String userId, GGAPILiteral filter) throws GGAPIEntityException {
		if (this.connector.isPresent()) {
			try {
				Future<List<Entity>> list = this.connector.get().requestList(tenantId, null, null);
				while (!list.isDone()) {
					Thread.sleep(250);
				}
				return list.get().size();
			} catch (InterruptedException | ExecutionException | GGAPIConnectorException e) {
				log.error("[Tenant {" + tenantId + "}] [UserId {" + userId + "}] [Domain {" + this.domain
						+ "}] Error during updating entity ", e);
				throw new GGAPIEntityException(GGAPIEntityException.CONNECTOR_ERROR, e);
			}
		} else if (this.repository.isPresent()) {
			return this.repository.get().getCount(tenantId, filter);
		}
		return 0;
	}

	@Override
	public void setTenant(boolean tenant) {
		this.tenant = tenant;
	}

}
