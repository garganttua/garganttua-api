/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.business.IGGAPIBusiness;
import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.GGAPIDomainable;
import com.garganttua.api.core.GGAPIDuplication;
import com.garganttua.api.core.GGAPIEntityException;
import com.garganttua.api.core.GGAPIEntityHelper;
import com.garganttua.api.core.GGAPIReadOutputMode;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.core.IGGAPIOwnedEntity;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPIGeolocFilterException;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.filter.GGAPILiteralException;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.engine.IGGAPIEngine;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.GGAPISecurityException;
import com.garganttua.api.security.IGGAPISecurity;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;

import lombok.Getter;
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

	/**
	 * The repository used to store the entity
	 */
	@Setter
	protected Optional<IGGAPIRepository<Entity, Dto>> repository;

	@Setter
	@Getter
	protected Optional<IGGAPIBusiness<Entity>> business;

	@Setter
	protected IGGAPIEngine engine;

	/**
	 * 
	 */
	@Override
	public Entity getEntity(IGGAPICaller caller, String uuid, Map<String, String> customParameters) throws GGAPIEntityException {
		log.info("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Getting entity with Uuid " + uuid, caller.getRequestedTenantId());
		Entity entity = null;
		
		if (this.business.isPresent()) {
			entity = this.business.get().beforeGetOne(caller, entity, customParameters);
		}

		if (this.repository.isPresent()) {
			entity = this.repository.get().getOneByUuid(caller, uuid);
		}

		if (entity == null) {
			log.warn("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with Uuid " + uuid + " not found");
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND, "Entity does not exist");
		}
		
		if (this.business.isPresent()) {
			entity = this.business.get().afterGetOne(caller, entity, customParameters);
		}
		return entity;

	}

	@Override
	public Entity createEntity(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException {

		if( this.dynamicDomain.tenantEntity() ) {
			if( (caller.getRequestedTenantId() == null || caller.getRequestedTenantId().isEmpty())) {
				log.info("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" No uuid provided, generating one");
				if (entity.getUuid() == null || entity.getUuid().isEmpty()) {
					entity.setUuid(UUID.randomUUID().toString());
				} 
				((GGAPICaller) caller).setRequestedTenantId(entity.getUuid());
			} else {
				entity.setUuid(caller.getRequestedTenantId());
			}
		} else {
			if (entity.getUuid() == null || entity.getUuid().isEmpty()) {
				log.info("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" No uuid provided, generating one");
				entity.setUuid(UUID.randomUUID().toString());
			} 
		}

		log.info("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Creating entity with uuid {}",
				entity.getUuid());

		if( this.dynamicDomain.authenticatorEntity()) {
			Optional<IGGAPISecurity> security = this.engine.getSecurity();
			if( security.isPresent() ) {
				Optional<IGGAPIAuthenticationManager> authenticationManager = security.get().getAuthenticationManager();
				if( authenticationManager.isPresent() ) {
					try {
						entity = authenticationManager.get().applySecurityOnAuthenticatorEntity(entity);
					} catch (GGAPISecurityException e) {
						throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, "Error durnig applying security on entity", e);
					}
				}
			}
		}
		
		if( this.dynamicDomain.ownedEntity() ) {
			if( caller.getOwnerId() != null && !caller.getOwnerId().isEmpty()) {
				((IGGAPIOwnedEntity) entity).setOwnerId(caller.getOwnerId());
			} else {
				throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, "No ownerId provided");
			}
		}

		if( this.dynamicDomain.mandatory().length > 0 ) {
			this.checkMandatoryFields(this.dynamicDomain.mandatory(), entity);
		}

		if (this.business.isPresent()) {
			entity = this.business.get().beforeCreate(caller, entity, customParameters);
		}

		if (this.repository.isPresent()) {

			if (this.repository.get().doesExist(caller, entity)) {
				
				if( this.dynamicDomain.duplication() == GGAPIDuplication.returnExisting ) {
					log.warn("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with Uuid " + entity.getUuid() + " already exists, returning the already existing entity");
				} else {
					log.warn("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with Uuid " + entity.getUuid() + " already exists");
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS, "Entity already exists");
				}
			} else {

				if( this.dynamicDomain.unicity() != null && this.dynamicDomain.unicity().length > 0) {
					if( this.checkUnicityFields(caller, entity).size() > 0 ) {
						log.warn("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with same unical fields already exists");
						throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists");
					}
				}
				this.repository.get().save(caller, entity);
			}

		}

		if (this.business.isPresent()) {
			entity = this.business.get().afterCreate(caller, entity, customParameters);
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
	public List<?> getEntityList(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter,
			GGAPISort sort, GGAPIGeolocFilter geoloc, GGAPIReadOutputMode mode, Map<String, String> customParameters) throws GGAPIEntityException {
		log.info(
				"[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Getting entities, mode {}, page size {}, page index {}, filter {}, sort {}, geoloc {}",mode, pageSize, pageIndex, filter, sort, geoloc);

		try {
			GGAPILiteral.validate(filter);
		} catch (GGAPILiteralException e) {
			log.warn("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Cannot validate filter " + filter);
			throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, e);
		}

		try {
			GGAPIGeolocFilter.validate(geoloc);
		} catch (GGAPIGeolocFilterException e) {
			log.warn("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Cannot validate geo filter " + filter);
			throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, e);
		}

		ArrayList<String> entityUuids = new ArrayList<String>();
		List<Entity> entities = null;
		List<?> listToReturn = null;
		
		if (this.business.isPresent()) {
			entities = this.business.get().beforeGetList(caller, entities, customParameters);
		}

		if (this.repository.isPresent()) {
			entities = this.repository.get().getEntities(caller, pageSize, pageIndex, filter, sort, geoloc);
		}

		switch (mode) {
		case full -> {
			listToReturn = entities;
		}
		case id -> {
			entities.forEach(e -> {
				entityUuids.add(e.getId());
			});
			listToReturn = entityUuids;
		}
		case uuid -> {
			entities.forEach(e -> {
				entityUuids.add(e.getUuid());
			});
			listToReturn = entityUuids;
		}
		}
		
		if (this.business.isPresent()) {
			listToReturn = this.business.get().afterGetList(caller, listToReturn, customParameters);
		}

		return listToReturn;
	}

	@Override
	public Entity updateEntity(IGGAPICaller caller, Entity entity, Map<String, String> customParameters) throws GGAPIEntityException {
		log.info("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Updating entity with Uuid " + entity.getUuid());
		Entity updated = null;

		if (this.business.isPresent()) {
			entity = this.business.get().beforeUpdate(caller, entity, customParameters);
		}

		if (this.repository.isPresent()) {
			if (!this.repository.get().doesExist(caller, entity)) {
				log.warn(
						"[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with uuid " + entity.getUuid() + " does not exists");
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND, "Entity does not exist");
			}

			if( this.dynamicDomain.unicity() != null && this.dynamicDomain.unicity().length > 0) {
				List<Entity> entities = this.checkUnicityFields(caller, entity);
				if( entities.size() != 1 && !entities.get(0).getUuid().equals(entity.getUuid())) {
					log.warn("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Entity with same unical fields already exists");
					throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists");
				}
			}

			updated = this.repository.get().update(caller, entity);
		}

		if (this.business.isPresent()) {
			entity = this.business.get().afterUpdate(caller, entity, customParameters);
		}

		return updated;
	}
	
	/**
	 * 
	 * @param caller
	 * @param entity
	 * @param forUpdate
	 * @return
	 * @throws GGAPIEntityException
	 */
	private List<Entity> checkUnicityFields(IGGAPICaller caller, Entity entity) throws GGAPIEntityException {
		try {
			List<String> values = new ArrayList<String>();
			for (String fieldName : this.dynamicDomain.unicity()) {
				values.add(GGAPIEntityHelper.getFieldValue(this.entityClass, fieldName, entity).toString());
			}
			String[] fieldValues = new String[values.size()];
			values.toArray(fieldValues);

			GGAPICaller superCaller = new GGAPICaller();
			superCaller.setSuperTenant(true);
			
			List<GGAPILiteral> orList = new ArrayList<GGAPILiteral>();
			
			for( int i = 0; i < this.dynamicDomain.unicity().length; i++ ) {
				orList.add(GGAPILiteral.getFilterForTestingFieldEquality(this.dynamicDomain.unicity()[i], fieldValues[i]));
			}

			GGAPILiteral orFilter = new GGAPILiteral(GGAPILiteral.OPERATOR_OR, null, orList);
			
			List<Entity> entities = this.repository.get().getEntities(superCaller, 0, 0, orFilter, null, null);

			return entities;

		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			log.error("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Error during checking unicity fields for entity with Uuid " + entity.getUuid(), e);
			throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, e.getMessage(), e);
		}
	}

	@Override
	public void deleteEntity(IGGAPICaller caller, String uuid, Map<String, String> customParameters) throws GGAPIEntityException {
		log.info("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Deleting entity with Uuid " + uuid);

		Entity entity = this.getEntity(caller, uuid, customParameters);

		if (entity == null) {
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND, "Entity does not exist");
		}

		if (this.business.isPresent()) {
			entity = this.business.get().beforeDelete(caller, entity, customParameters);
		}

		if (this.repository.isPresent()) {
			this.repository.get().delete(caller, entity);
		}

		if (this.business.isPresent()) {
			entity = this.business.get().afterDelete(caller, entity, customParameters);
		}

	}

	@Override
	public void deleteEntities(IGGAPICaller caller, Map<String, String> customParameters) throws GGAPIEntityException {
		log.info("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Deleting all entities");
		List<Entity> entities = this.repository.get().getEntities(caller, 0, 1, null, null, null);

		for (Entity s : entities) {
			try {
				this.deleteEntity(caller, s.getUuid(), customParameters);
			} catch (GGAPIEntityException e) {
				log.error("[domain ["+this.dynamicDomain.domain()+"]] "+caller.toString()+" Error during entities deletion ", e);
				throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, "Error during entities deletion");
			}
		}
	}

	@Override
	public long getEntityTotalCount(IGGAPICaller caller, GGAPILiteral filter, GGAPIGeolocFilter geoloc, Map<String, String> customParameters) throws GGAPIEntityException {
		if (this.repository.isPresent()) {
			return this.repository.get().getCount(caller, filter, geoloc);
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getTenant(IGGAPIEntity entity) {
		if( this.repository.isPresent() ) {
			return this.repository.get().getTenant((Entity) entity);
		}
		return null;
	}
}
