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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.controller.ISpringCrudifyController;
import com.garganttua.api.repository.dto.ISpringCrudifyDTOObject;
import com.garganttua.api.security.authorization.BasicSpringCrudifyAuthorization;
import com.garganttua.api.security.authorization.ISpringCrudifyAuthorization;
import com.garganttua.api.spec.ISpringCrudifyDomain;
import com.garganttua.api.spec.ISpringCrudifyEntity;
import com.garganttua.api.spec.SpringCrudifyDomainable;
import com.garganttua.api.spec.SpringCrudifyEntityException;
import com.garganttua.api.spec.SpringCrudifyReadOutputMode;
import com.garganttua.api.spec.filter.SpringCrudifyLiteral;
import com.garganttua.api.spec.sort.SpringCrudifySort;

import jakarta.annotation.PostConstruct;
import lombok.Setter;

/**
 * 
 * @author J.Colombet
 *
 * @param <Entity>
 */
public abstract class AbstractSpringCrudifyService<Entity extends ISpringCrudifyEntity, Dto extends ISpringCrudifyDTOObject<Entity>> extends SpringCrudifyDomainable<Entity, Dto> implements ISpringCrudifyRestService<Entity, Dto> {

	public AbstractSpringCrudifyService(ISpringCrudifyDomain<Entity, Dto> domain) {
		super(domain);
	}

	protected static final String SUCCESSFULLY_DELETED = "Ressource has been successfully deleted";

	protected static final String NOT_IMPLEMENTED = "This function is not implemented";
	protected static final String FILTER_ERROR = "The filter has error";

	protected boolean AUTHORIZE_CREATION = false;
	protected boolean AUTHORIZE_GET_ALL = false;
	protected boolean AUTHORIZE_GET_ONE = false;
	protected boolean AUTHORIZE_UPDATE = false;
	protected boolean AUTHORIZE_DELETE_ONE = false;
	protected boolean AUTHORIZE_DELETE_ALL = false;
	protected boolean AUTHORIZE_COUNT = false;
	
	protected abstract List<ISpringCrudifyAuthorization> createCustomAuthorizations();

	private ArrayList<ISpringCrudifyAuthorization> authorizations;

	@PostConstruct
	protected void init() {
		this.authorize(this.AUTHORIZE_CREATION, this.AUTHORIZE_COUNT, this.AUTHORIZE_COUNT, this.AUTHORIZE_UPDATE, this.AUTHORIZE_DELETE_ONE, this.AUTHORIZE_DELETE_ALL, this.AUTHORIZE_COUNT);
	}

	@Override
	public List<ISpringCrudifyAuthorization> createAuthorizations() {
		if( this.authorizations == null ) {
			this.authorizations = new ArrayList<ISpringCrudifyAuthorization>();
			
			this.authorizations.add(new BasicSpringCrudifyAuthorization("/"+this.domain.toLowerCase(), this.domain.toLowerCase()+"-read", HttpMethod.GET));
			this.authorizations.add(new BasicSpringCrudifyAuthorization("/"+this.domain.toLowerCase(), this.domain.toLowerCase()+"-create", HttpMethod.POST));
			this.authorizations.add(new BasicSpringCrudifyAuthorization("/"+this.domain.toLowerCase(), this.domain.toLowerCase()+"-delete-all", HttpMethod.DELETE));	
			this.authorizations.add(new BasicSpringCrudifyAuthorization("/"+this.domain.toLowerCase()+"/count", this.domain.toLowerCase()+"-get-count", HttpMethod.GET));
			this.authorizations.add(new BasicSpringCrudifyAuthorization("/"+this.domain.toLowerCase()+"/*", this.domain.toLowerCase()+"-read", HttpMethod.GET));
			this.authorizations.add(new BasicSpringCrudifyAuthorization("/"+this.domain.toLowerCase()+"/*", this.domain.toLowerCase()+"-update", HttpMethod.PATCH));
			this.authorizations.add(new BasicSpringCrudifyAuthorization("/"+this.domain.toLowerCase()+"/*", this.domain.toLowerCase()+"-delete-one", HttpMethod.DELETE));
			
			if( this.createCustomAuthorizations() != null ) {
				this.authorizations.addAll(this.createCustomAuthorizations());
			}
		}
		return authorizations;
	}
	
	@Inject
	@Setter
	protected ISpringCrudifyController<Entity, Dto> controller;

	/**
	 * Creates an entity.
	 * 
	 * @param
	 * @return
	 */
	@Override
	public ResponseEntity<?> createEntity(String entity__, String tenantId) {
		ResponseEntity<?> response = null;

		if (this.AUTHORIZE_CREATION) {
			try {
				
				Entity entity = (Entity) new ObjectMapper().readValue(entity__.getBytes(), this.entityClass);
				
				entity = this.controller.createEntity(tenantId, entity);
				response = new ResponseEntity<>(entity, HttpStatus.CREATED);
			} catch (SpringCrudifyEntityException e) {
				response = new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			response = new ResponseEntity<>(new ISpringCrudifyErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
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
	public ResponseEntity<?> getEntities(String tenantId, SpringCrudifyReadOutputMode mode, Integer pageSize, Integer pageIndex, String filterString, String sortString) {

		if (this.AUTHORIZE_GET_ALL) {

			Object entities = null;
			
			ObjectMapper mapper = new ObjectMapper();
			SpringCrudifyLiteral filter = null;
			SpringCrudifySort sort = null;
			try {
				if( filterString != null && !filterString.isEmpty() ) {
					filter = mapper.readValue(filterString, SpringCrudifyLiteral.class);
				}
				if( sortString != null && !sortString.isEmpty() ) {
					sort = mapper.readValue(sortString, SpringCrudifySort.class);
				}
			} catch (JsonProcessingException e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject("Error parsing request param : "+e.getMessage()), HttpStatus.BAD_REQUEST);
			}

			try {
				entities = this.controller.getEntityList(tenantId, pageSize, pageIndex, filter, sort, mode);
			} catch (SpringCrudifyEntityException e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			if (pageSize > 0) {
				long totalCount = 0;
				try {
					totalCount = this.controller.getEntityTotalCount(tenantId, filter);
				} catch (SpringCrudifyEntityException e) {
					return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()),
							this.getHttpErrorCodeFromEntityExceptionCode(e));
				} catch (Exception e) {
					return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
				}

				SpringCrudifyWsPage page = new SpringCrudifyWsPage(totalCount, ((List<Object>) entities));

				return new ResponseEntity<>(page, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(entities, HttpStatus.OK);
			}

		} else {
			return new ResponseEntity<>(new ISpringCrudifyErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}

	}

	/**
	 * Get one entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> getEntity(String tenantId, String uuid) {
		ResponseEntity<?> response = null;

		if (this.AUTHORIZE_GET_ONE) {
			Entity entity;
			try {
				entity = this.controller.getEntity(tenantId, uuid);
				response = new ResponseEntity<>(entity, HttpStatus.OK);
			} catch (SpringCrudifyEntityException e) {
				response = new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<>(new ISpringCrudifyErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}
		return response;

	}

	/**
	 * Update an entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> updateEntity(String uuid, String entity__, String tenantId) {

		ResponseEntity<?> response = null;

		if (this.AUTHORIZE_UPDATE) {
			try {
				
				Entity entity = (Entity) new ObjectMapper().readValue(entity__.getBytes(), this.entityClass);
				
				entity.setUuid(uuid);
				Entity updatedEntity = this.controller.updateEntity(tenantId, entity);
				response = new ResponseEntity<>(updatedEntity, HttpStatus.OK);
			} catch (SpringCrudifyEntityException e) {
				response = new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} else {
			response = new ResponseEntity<>(new ISpringCrudifyErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}

		return response;
	}

	/**
	 * Delete an entity.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> deleteEntity(@PathVariable(name = "uuid") String uuid, @RequestHeader(name = "tenantId") String tenantId) {

		if (this.AUTHORIZE_DELETE_ONE) {
			ResponseEntity<?> response = null;

			try {
				this.controller.deleteEntity(tenantId, uuid);
				response = new ResponseEntity<>(new ISpringCrudifyErrorObject(SUCCESSFULLY_DELETED), HttpStatus.OK);
			} catch (SpringCrudifyEntityException e) {
				response = new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()),
						HttpStatus.NOT_ACCEPTABLE);
			} catch (Exception e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return response;

		} else {
			return new ResponseEntity<>(new ISpringCrudifyErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}
	}

	/**
	 * Delete all the entities.
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> deleteAll(String tenantId) {

		if (this.AUTHORIZE_DELETE_ALL) {
			ResponseEntity<?> response = null;

			try {
				this.controller.deleteEntities(tenantId);
				response = new ResponseEntity<>(new ISpringCrudifyErrorObject(SUCCESSFULLY_DELETED), HttpStatus.OK);
			} catch (SpringCrudifyEntityException e) {
				response = new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return response;

		} else {
			return new ResponseEntity<>(new ISpringCrudifyErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}
	}

	/**
	 * Get count of entities
	 * 
	 * @return
	 */
	@Override
	public ResponseEntity<?> getCount(@RequestHeader(name = "tenantId") String tenantId) {

		if (this.AUTHORIZE_COUNT) {
			ResponseEntity<?> response = null;

			try {
				long count = this.controller.getEntityTotalCount(tenantId, null);
				response = new ResponseEntity<>(count, HttpStatus.OK);
			} catch (SpringCrudifyEntityException e) {
				response = new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()),
						this.getHttpErrorCodeFromEntityExceptionCode(e));
			} catch (Exception e) {
				return new ResponseEntity<>(new ISpringCrudifyErrorObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return response;

		} else {
			return new ResponseEntity<>(new ISpringCrudifyErrorObject(NOT_IMPLEMENTED), HttpStatus.NOT_IMPLEMENTED);
		}
	}

	/**
	 * 
	 * @param e
	 * @return
	 */
	protected HttpStatus getHttpErrorCodeFromEntityExceptionCode(SpringCrudifyEntityException e) {
		switch (e.getCode()) {
		default:
		case SpringCrudifyEntityException.BAD_REQUEST:
			return HttpStatus.BAD_REQUEST;
		case SpringCrudifyEntityException.ENTITY_NOT_FOUND:
			return HttpStatus.NOT_FOUND;
		}
	}

}
