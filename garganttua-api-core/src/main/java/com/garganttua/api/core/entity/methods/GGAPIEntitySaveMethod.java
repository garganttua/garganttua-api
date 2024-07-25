package com.garganttua.api.core.entity.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.pageable.GGAPIPageable;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.entity.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.filter.GGAPILiteral;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.api.spec.updater.IGGAPIEntityUpdater;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntitySaveMethod implements IGGAPIEntitySaveMethod<Object> {
	
	private IGGAPIDomain domain;
	private IGGAPIRepository<Object> repository;
	private Optional<IGGAPISecurity> security;
	private GGObjectAddress afterUpdateMethodAddress;
	private GGObjectAddress beforeUpdateMethodAddress;
	private GGObjectAddress afterCreateMethodAddress;
	private GGObjectAddress beforeCreateMethodAddress;
	private IGGObjectQuery objectQuery;
	private IGGAPIEntityUpdater<Object> entityUpdater;
	
	
	public GGAPIEntitySaveMethod(IGGAPIDomain domain, IGGAPIRepository<Object> repository, IGGAPIEntityUpdater<Object> updater, Optional<IGGAPISecurity> security) throws GGAPIException {
		this.domain = domain;
		this.repository = repository;
		this.entityUpdater = updater;
		this.security = security;
		
		this.beforeCreateMethodAddress = this.domain.getEntity().getValue1().beforeCreateMethodAddress();
		this.afterCreateMethodAddress = this.domain.getEntity().getValue1().afterCreateMethodAddress();
		this.beforeUpdateMethodAddress = this.domain.getEntity().getValue1().beforeUpdateMethodAddress();
		this.afterUpdateMethodAddress = this.domain.getEntity().getValue1().afterUpdateMethodAddress();
		
		try {
			this.objectQuery = GGObjectQueryFactory.objectQuery(domain.getEntity().getValue0());
		} catch (GGReflectionException e) {
			this.processException(e);
			
			//Should never be reached
			return;
		}
	}

	@Override
	public Object save(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIException {
		if( domain == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Domain is null");
		}
		if( caller == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Caller is null");
		}
		if( this.repository == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Repository is null");
		}
		if( entity == null ) {
			throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Entity is null");
		}

		try {
			if( this.repository.doesExist(caller, entity) ) {
				
				Object storedObject = this.repository.getOneByUuid(caller, GGAPIEntityHelper.getUuid(entity));
				Object updatedObject = this.entityUpdater.update(caller, storedObject, entity, this.domain.getEntity().getValue1().updateAuthorizations());
				
				this.updateEntity(caller, parameters, updatedObject);
				
				return updatedObject;
			} else {

				this.createEntity(caller, parameters, entity);
				
				return entity;
			}
		} catch (GGReflectionException e) {
			this.processException(e);
			
			//Should never be reached
			return null;
		}
	}
	
	private void updateEntity(IGGAPICaller caller, Map<String, String> customParameters, Object entity) throws GGAPIException, GGReflectionException {
		log.info("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Updating entity with Uuid " + GGAPIEntityHelper.getUuid(entity));
		this.applySecurityRuleOnAuthenticatorEntity(domain, security, entity);
		this.applyUpdateUnicityRule(domain, repository, caller, entity);
		if( this.beforeUpdateMethodAddress != null ) {
			this.objectQuery.invoke(entity, this.beforeUpdateMethodAddress, caller, customParameters);
		}
		this.repository.save(caller, entity);
		if( this.afterUpdateMethodAddress != null ) {
			this.objectQuery.invoke(entity, this.afterUpdateMethodAddress, caller, customParameters);
		}
	}

	private <Entity> void applyUpdateUnicityRule(IGGAPIDomain domain, IGGAPIRepository<Entity> repository,
			IGGAPICaller caller, Entity entity) throws GGAPIException {
		if( domain.getEntity().getValue1().unicityFields() != null && domain.getEntity().getValue1().unicityFields().size() > 0) {
			List<Entity> entities = this.checkUnicityFields(domain, repository, caller, entity, domain.getEntity().getValue1().unicityFields() );
			if( entities.size() != 1 && !GGAPIEntityHelper.getUuid(entities.get(0)).equals(GGAPIEntityHelper.getUuid(entity))) {
				log.warn("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Entity with same unical fields already exists, fields "+domain.getEntity().getValue1().unicityFields());
				throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists, fields "+domain.getEntity().getValue1().unicityFields());
			}
		}
	}

	private void createEntity(IGGAPICaller caller, Map<String, String> customParameters, Object entity) throws GGAPIException, GGReflectionException {

		this.applyTenantEntityRule(domain, caller, entity);

		log.info("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Creating entity with uuid {}", GGAPIEntityHelper.getUuid(entity));

		this.applySecurityRuleOnAuthenticatorEntity(domain, security, entity);
		this.applyOwnedEntityRule(domain, caller, entity);

		if( domain.getEntity().getValue1().mandatoryFields().size() > 0 ) {
			this.checkMandatoryFields(domain.getEntity().getValue1().mandatoryFields(), entity);
		}

		this.applyCreationUnicityRule(domain, repository, caller, entity);
		if( this.beforeCreateMethodAddress != null ) {
			this.objectQuery.invoke(entity, this.beforeCreateMethodAddress, caller, customParameters);
		}
		this.repository.save(caller, entity);
		if( this.afterCreateMethodAddress != null ) {
			this.objectQuery.invoke(entity, this.afterCreateMethodAddress, caller, customParameters);
		}

	}

	private <Entity> void applyCreationUnicityRule(IGGAPIDomain domain, IGGAPIRepository<Entity> repository,
			IGGAPICaller caller, Entity entity) throws GGAPIException {
		if( domain.getEntity().getValue1().unicityFields() != null && domain.getEntity().getValue1().unicityFields().size() > 0) {
			if( this.checkUnicityFields(domain, repository, caller, entity, domain.getEntity().getValue1().unicityFields()).size() > 0 ) {
				log.warn("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Entity with same unical fields already exists, fields "+domain.getEntity().getValue1().unicityFields());
				throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists, fields "+domain.getEntity().getValue1().unicityFields());
			}
		}
	}

	private void applyOwnedEntityRule(IGGAPIDomain domain, IGGAPICaller caller,
			Object entity) throws GGAPIException {
		if( domain.getEntity().getValue1().ownedEntity() ) {
			if( caller.getOwnerId() != null && !caller.getOwnerId().isEmpty()) {
				try {
					GGObjectQueryFactory.objectQuery(entity).setValue(GGAPIEntityChecker.checkEntity(entity).ownerIdFieldAddress(), caller.getOwnerId());
				} catch (GGReflectionException e) {
					this.processException(e);
					
					//Should never be reached
					return;
				}
			} else {
				throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "No ownerId provided");
			}
		}
	}

	private void applySecurityRuleOnAuthenticatorEntity(IGGAPIDomain domain, Optional<IGGAPISecurity> security, Object entity) throws GGAPIEntityException {
//		if( security.isPresent() ) {
//			Optional<IGGAPIAuthenticationManager> authenticationManager = security.get().getAuthenticationManager();
//			if( authenticationManager.isPresent() ) {
//				try {
//					entity = authenticationManager.get().applySecurityOnAuthenticatorEntity(entity);
//				} catch (GGAPISecurityException e) {
//					throw new GGAPIEntityException(GGAPIExceptionCode.UNKNOWN_ERROR, "Error durnig applying security on entity", e);
//				}
//			}
//		}
	}

	private void applyTenantEntityRule(IGGAPIDomain domain, IGGAPICaller caller, Object entity) throws GGAPIException {
		if( domain.getEntity().getValue1().tenantEntity() ) {
			if( (caller.getRequestedTenantId() == null || caller.getRequestedTenantId().isEmpty())) {
				log.info("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" No uuid provided, generating one");
				if (GGAPIEntityHelper.getUuid(entity) == null || ((String) GGAPIEntityHelper.getUuid(entity)).isEmpty()) {
					GGAPIEntityHelper.setUuid(entity, UUID.randomUUID().toString());
				} 
//				((GGAPICaller) caller).setRequestedTenantId(GGAPIEntityHelper.getUuid(entity));
			} else {
				GGAPIEntityHelper.setUuid(entity, caller.getRequestedTenantId());
			}
		} else {
			if (GGAPIEntityHelper.getUuid(entity) == null || GGAPIEntityHelper.getUuid(entity).isEmpty()) {
				log.info("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" No uuid provided, generating one");
				GGAPIEntityHelper.setUuid(entity, UUID.randomUUID().toString());
			} 
		}
	}
	
	protected void checkMandatoryFields(List<String> mandatory, Object entity) throws GGAPIException {
		
		for( String field: mandatory ) {
			try {
				Object value = GGObjectReflectionHelper.getObjectFieldValue(entity, field);
				
				if( value == null ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Field "+field+" is mandatory");
				} else if( value.toString().isEmpty() ){
					throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Field "+field+" is mandatory");
				}
			} catch (IllegalArgumentException | GGReflectionException e) {
				this.processException(e);
				
				//Should never be reached
				return;
			}
		}
	}
	
	private <Entity> List<Entity> checkUnicityFields(IGGAPIDomain domain, IGGAPIRepository<Entity> repository, IGGAPICaller caller, Entity entity, List<String> unicity) throws GGAPIException {
		try {
			List<String> values = new ArrayList<String>();
			for (String fieldName : unicity) {
				values.add(GGObjectReflectionHelper.getObjectFieldValue(entity, fieldName).toString());
			}
			String[] fieldValues = new String[values.size()];
			values.toArray(fieldValues);
			
			GGAPILiteral literal = null;
			for( int i = 0; i < unicity.size(); i++ ) {
				GGAPILiteral eqLiteral = GGAPILiteral.eq(unicity.get(i), fieldValues[i]);
				if( literal == null ) {
					literal = eqLiteral;
				} else {
					literal.orOperator(eqLiteral);
				}
			}

			return repository.getEntities(GGAPICaller.createSuperCaller(), GGAPIPageable.getPage(0,0), literal, null);

		} catch (GGReflectionException e) {
			log.error("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Error during checking unicity fields for entity with Uuid " + GGAPIEntityHelper.getUuid(entity), e);
			this.processException(e);
			
			//Should never be reached
			return null;
		}
	}
	
	private void processException(Exception e) throws GGAPIException {
		GGAPIException apiException = GGAPIException.findFirstInException(e);
		if( apiException != null ) {
			throw apiException;
		} else {
			throw new GGAPIEntityException(GGAPIExceptionCode.UNKNOWN_ERROR, e.getMessage(), e);
		}
	}
}
