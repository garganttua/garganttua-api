/*******************************************************************************
 * Copyright (c) 2022 Jérémy COLOMBET
 *******************************************************************************/
package com.garganttua.api.ws;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.controller.IGGAPIController;
import com.garganttua.api.repository.dto.IGGAPIDTOObject;
import com.garganttua.api.security.authorization.BasicGGAPIAuthorization;
import com.garganttua.api.security.authorization.IGGAPIAuthorization;
import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.GGAPIDomainable;
import com.garganttua.api.spec.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIReadOutputMode;
import com.garganttua.api.spec.IGGAPIDomain;
import com.garganttua.api.spec.IGGAPIEntity;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.sort.GGAPISort;

import jakarta.annotation.PostConstruct;
import lombok.Setter;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
public abstract class AbstractGGAPIService<Entity extends IGGAPIEntity, Dto extends IGGAPIDTOObject<Entity>> extends GGAPIDomainable<Entity, Dto> implements IGGAPIRestService<Entity, Dto> {

	public AbstractGGAPIService(IGGAPIDomain<Entity, Dto> domain) {
		super(domain);
	}

	protected static final String SUCCESSFULLY_DELETED = "Ressource has been successfully deleted";

	protected static final String NOT_IMPLEMENTED = "This function is not implemented";
	protected static final String FILTER_ERROR = "The filter has error";

	protected boolean ALLOW_CREATION = false;
	protected boolean ALLOW_GET_ALL = false;
	protected boolean ALLOW_GET_ONE = false;
	protected boolean ALLOW_UPDATE = false;
	protected boolean ALLOW_DELETE_ONE = false;
	protected boolean ALLOW_DELETE_ALL = false;
	protected boolean ALLOW_COUNT = false;
	
	protected GGAPICrudAccess CREATION_ACCESS = GGAPICrudAccess.owner;
	protected GGAPICrudAccess GET_ALL_ACCESS = GGAPICrudAccess.owner;
	protected GGAPICrudAccess GET_ONE_ACCESS = GGAPICrudAccess.owner;
	protected GGAPICrudAccess UPDATE_ACCESS = GGAPICrudAccess.owner;
	protected GGAPICrudAccess DELETE_ONE_ACCESS = GGAPICrudAccess.owner;
	protected GGAPICrudAccess DELETE_ALL_ACCESS = GGAPICrudAccess.owner;
	protected GGAPICrudAccess COUNT_ACCESS = GGAPICrudAccess.owner;
	
	protected abstract List<IGGAPIAuthorization> createCustomAuthorizations();

	private ArrayList<IGGAPIAuthorization> authorizations;

	@PostConstruct
	protected void init() {
		this.allow(this.ALLOW_CREATION, this.ALLOW_COUNT, this.ALLOW_COUNT, this.ALLOW_UPDATE, this.ALLOW_DELETE_ONE, this.ALLOW_DELETE_ALL, this.ALLOW_COUNT);
	}

	@Override
	public List<IGGAPIAuthorization> createAuthorizations() {
		if( this.authorizations == null ) {
			this.authorizations = new ArrayList<IGGAPIAuthorization>();
			
			this.authorizations.add(new BasicGGAPIAuthorization("/"+this.domain.toLowerCase(), this.domain.toLowerCase()+"-read", HttpMethod.GET, this.GET_ALL_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/"+this.domain.toLowerCase(), this.domain.toLowerCase()+"-create", HttpMethod.POST, this.CREATION_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/"+this.domain.toLowerCase(), this.domain.toLowerCase()+"-delete-all", HttpMethod.DELETE, this.DELETE_ALL_ACCESS));	
			this.authorizations.add(new BasicGGAPIAuthorization("/"+this.domain.toLowerCase()+"/count", this.domain.toLowerCase()+"-get-count", HttpMethod.GET, this.COUNT_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/"+this.domain.toLowerCase()+"/*", this.domain.toLowerCase()+"-read", HttpMethod.GET, this.GET_ONE_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/"+this.domain.toLowerCase()+"/*", this.domain.toLowerCase()+"-update", HttpMethod.PATCH, this.UPDATE_ACCESS));
			this.authorizations.add(new BasicGGAPIAuthorization("/"+this.domain.toLowerCase()+"/*", this.domain.toLowerCase()+"-delete-one", HttpMethod.DELETE, this.DELETE_ONE_ACCESS));
			
			if( this.createCustomAuthorizations() != null ) {
				this.authorizations.addAll(this.createCustomAuthorizations());
			}
		}
		return authorizations;
	}
	
	@Inject
	@Setter
	protected IGGAPIController<Entity, Dto> controller;

	/**
	 * Creates an entity.
	 * 
	 * @param
	 * @return
	 */
	@Override
	public ResponseEntity<?> createEntity(String entity__, String tenantId, String userId) {
		ResponseEntity<?> response = null;

		if (this.ALLOW_CREATION) {
			try {
				
				Entity entity = (Entity) new ObjectMapper().readValue(entity__.getBytes(), this.entityClass);
				
				entity = this.controller.createEntity(tenantId, userId, entity);
				response = new ResponseEntity<>(entity, HttpStatus.CREATED);
			} catch (GGAPIEntityException e) {
				response = new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			response = new ResponseEntity<>(new IGGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}

		return response;
	}

	/**
	 * Get a list of entities.
	 * 
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> getEntities(String tenantId, GGAPIReadOutputMode mode, Integer pageSize, Integer pageIndex, String filterString, String sortString, String userId) {

		if (this.ALLOW_GET_ALL) {

			Object entities = null;
			
			ObjectMapper mapper = new ObjectMapper();
			GGAPILiteral filter = null;
			GGAPISort sort = null;
			try {
				if( filterString != null && !filterString.isEmpty() ) {
					filter = mapper.readValue(filterString, GGAPILiteral.class);
				}
				if( sortString != null && !sortString.isEmpty() ) {
					sort = mapper.readValue(sortString, GGAPISort.class);
				}
			} catch (JsonProcessingException e) {
				return new ResponseEntity<>(new IGGAPIErrorObject("Error parsing request param : "+e.getMessage()), HttpStatus.BAD_REQUEST);
			}

			try {
				entities = this.controller.getEntityList(tenantId, userId, pageSize, pageIndex, filter, sort, mode);
			} catch (GGAPIEntityException e) {
				return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			if (pageSize > 0) {
				long totalCount = 0;
				try {
					totalCount = this.controller.getEntityTotalCount(tenantId, userId, filter);
				} catch (GGAPIEntityException e) {
					return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()),
							this.getHttpErrorCodeFromEntityExceptionCode(e));
				} catch (Exception e) {
					return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
				}

				GGAPIWsPage page = new GGAPIWsPage(totalCount, ((List<Object>) entities));

				return new ResponseEntity<>(page, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(entities, HttpStatus.OK);
			}

		} else {
			return new ResponseEntity<>(new IGGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}

	}

	/**
	 * Get one entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> getEntity(String tenantId, String uuid, String userId) {
		ResponseEntity<?> response = null;

		if (this.ALLOW_GET_ONE) {
			Entity entity;
			try {
				entity = this.controller.getEntity(tenantId, userId, uuid);
				response = new ResponseEntity<>(entity, HttpStatus.OK);
			} catch (GGAPIEntityException e) {
				response = new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(new IGGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}
		return response;

	}

	/**
	 * Update an entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> updateEntity(String uuid, String entity__, String tenantId, String userId) {

		ResponseEntity<?> response = null;

		if (this.ALLOW_UPDATE) {
			try {
				
				Entity entity = (Entity) new ObjectMapper().readValue(entity__.getBytes(), this.entityClass);
				
				entity.setUuid(uuid);
				Entity updatedEntity = this.controller.updateEntity(tenantId, userId, entity);
				response = new ResponseEntity<>(updatedEntity, HttpStatus.OK);
			} catch (GGAPIEntityException e) {
				response = new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else {
			response = new ResponseEntity<>(new IGGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}

		return response;
	}

	/**
	 * Delete an entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> deleteEntity(String uuid, String tenantId, String userId) {

		if (this.ALLOW_DELETE_ONE) {
			ResponseEntity<?> response = null;

			try {
				this.controller.deleteEntity(tenantId, userId, uuid);
				response = new ResponseEntity<>(new IGGAPIErrorObject(SUCCESSFULLY_DELETED), HttpStatus.OK);
			} catch (GGAPIEntityException e) {
				response = new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()),
						HttpStatus.NOT_ACCEPTABLE);
			} catch (Exception e) {
				return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return response;

		} else {
			return new ResponseEntity<>(new IGGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}
	}

	/**
	 * Delete all the entities.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> deleteAll(String tenantId, String userId) {

		if (this.ALLOW_DELETE_ALL) {
			ResponseEntity<?> response = null;

			try {
				this.controller.deleteEntities(tenantId, userId);
				response = new ResponseEntity<>(new IGGAPIErrorObject(SUCCESSFULLY_DELETED), HttpStatus.OK);
			} catch (GGAPIEntityException e) {
				response = new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return response;

		} else {
			return new ResponseEntity<>(new IGGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}
	}

	/**
	 * Get count of entities
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> getCount(String tenantId, String userId) {

		if (this.ALLOW_COUNT) {
			ResponseEntity<?> response = null;

			try {
				long count = this.controller.getEntityTotalCount(tenantId, userId, null);
				response = new ResponseEntity<>(count, HttpStatus.OK);
			} catch (GGAPIEntityException e) {
				response = new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new IGGAPIErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return response;

		} else {
			return new ResponseEntity<>(new IGGAPIErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	protected HttpStatus getHttpErrorCodeFromEntityExceptionCode(GGAPIEntityException e) {
		switch (e.getCode()) {
		default:
		case GGAPIEntityException.BAD_REQUEST:
			return HttpStatus.BAD_REQUEST;
		case GGAPIEntityException.ENTITY_NOT_FOUND:
			return HttpStatus.NOT_FOUND;
		}
	}

}
