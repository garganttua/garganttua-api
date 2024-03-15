package com.garganttua.api.core.entity.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterCreate;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterUpdate;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeCreate;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityBeforeUpdate;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelperExcpetion;
import com.garganttua.api.engine.GGAPIDomain;
import com.garganttua.api.repository.GGAPIRepositoryException;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.GGAPISecurityException;
import com.garganttua.api.security.IGGAPISecurity;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GGAPIEntitySaveMethod implements IGGAPIEntitySaveMethod<Object> {
	
	private GGAPIDomain domain;
	private IGGAPIRepository<Object> repository;
	private Optional<IGGAPISecurity> security;
	
	@Override
	public void save(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIEntityException {
		if( domain == null ) {
			throw new GGAPIEntityException("Domain is null");
		}
		if( caller == null ) {
			throw new GGAPIEntityException("Caller is null");
		}
		if( repository == null ) {
			throw new GGAPIEntityException("Repository is null");
		}
		if( entity == null ) {
			throw new GGAPIEntityException("Entity is null");
		}

		try {
			if( repository.doesExist(caller, entity) ) {
				GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityBeforeUpdate.class, entity, caller, parameters);
				this.updateEntity(caller, parameters, entity);
				GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityAfterUpdate.class, entity, caller, parameters);
			} else {
				GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityBeforeCreate.class, entity, caller, parameters);
				this.createEntity(caller, parameters, entity);
				GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityAfterCreate.class, entity, caller, parameters);
			}
		} catch (GGAPIRepositoryException e) {
			throw new GGAPIEntityException(e);
		}

	}
	
	private void updateEntity(IGGAPICaller caller, Map<String, String> customParameters, Object entity) throws GGAPIEntityException {
		log.info("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" Updating entity with Uuid " + GGAPIEntityHelper.getUuid(entity));
		this.applySecurityRuleOnAuthenticatorEntity(domain, security, entity);
		this.applyUpdateUnicityRule(domain, repository, caller, entity);

		try {
			repository.update(caller, entity);
		} catch (GGAPIRepositoryException e) {
			throw new GGAPIEntityException(e);
		}
	}

	private <Entity> void applyUpdateUnicityRule(GGAPIDomain domain, IGGAPIRepository<Entity> repository,
			IGGAPICaller caller, Entity entity) throws GGAPIEntityException {
		if( domain.entity.getValue1().unicityFields() != null && domain.entity.getValue1().unicityFields().size() > 0) {
			List<Entity> entities = this.checkUnicityFields(domain, repository, caller, entity, domain.entity.getValue1().unicityFields() );
			if( entities.size() != 1 && !GGAPIEntityHelper.getUuid(entities.get(0)).equals(GGAPIEntityHelper.getUuid(entity))) {
				log.warn("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" Entity with same unical fields already exists");
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists");
			}
		}
	}

	private void createEntity(IGGAPICaller caller, Map<String, String> customParameters, Object entity) throws GGAPIEntityException {

		this.applyTenantEntityRule(domain, caller, entity);

		log.info("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" Creating entity with uuid {}", GGAPIEntityHelper.getUuid(entity));

		this.applySecurityRuleOnAuthenticatorEntity(domain, security, entity);
		this.applyOwnedEntityRule(domain, caller, entity);

		if( domain.entity.getValue1().mandatoryFields().size() > 0 ) {
			this.checkMandatoryFields(domain.entity.getValue1().mandatoryFields(), entity);
		}

		this.applyCreationUnicityRule(domain, repository, caller, entity);
		try {
			repository.save(caller, entity);
		} catch (GGAPIRepositoryException e) {
			throw new GGAPIEntityException(e);
		}
	}

	private <Entity> void applyCreationUnicityRule(GGAPIDomain domain, IGGAPIRepository<Entity> repository,
			IGGAPICaller caller, Entity entity) throws GGAPIEntityException {
		if( domain.entity.getValue1().unicityFields() != null && domain.entity.getValue1().unicityFields().size() > 0) {
			if( this.checkUnicityFields(domain, repository, caller, entity, domain.entity.getValue1().unicityFields()).size() > 0 ) {
				log.warn("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" Entity with same unical fields already exists");
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists");
			}
		}
	}

	private void applyOwnedEntityRule(GGAPIDomain domain, IGGAPICaller caller,
			Object entity) throws GGAPIEntityException {
		if( domain.entity.getValue1().ownedEntity() ) {
			if( caller.getOwnerId() != null && !caller.getOwnerId().isEmpty()) {
				new GGAPIEntityChecker();
				try {
					GGAPIObjectReflectionHelper.setObjectFieldValue(entity, GGAPIEntityChecker.checkEntity(entity).ownerIdFieldName(), caller.getOwnerId());
				} catch (GGAPIObjectReflectionHelperExcpetion | GGAPIEntityException e) {
					throw new GGAPIEntityException(e);
				}
			} else {
				throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, "No ownerId provided");
			}
		}
	}

	private void applySecurityRuleOnAuthenticatorEntity(GGAPIDomain domain, Optional<IGGAPISecurity> security, Object entity) throws GGAPIEntityException {
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

	private void applyTenantEntityRule(GGAPIDomain domain, IGGAPICaller caller, Object entity) throws GGAPIEntityException {
		if( domain.entity.getValue1().tenantEntity() ) {
			if( (caller.getRequestedTenantId() == null || caller.getRequestedTenantId().isEmpty())) {
				log.info("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" No uuid provided, generating one");
				if (GGAPIEntityHelper.getUuid(entity) == null || ((String) GGAPIEntityHelper.getUuid(entity)).isEmpty()) {
					GGAPIEntityHelper.setUuid(entity, UUID.randomUUID().toString());
				} 
				((GGAPICaller) caller).setRequestedTenantId(GGAPIEntityHelper.getUuid(entity));
			} else {
				GGAPIEntityHelper.setUuid(entity, caller.getRequestedTenantId());
			}
		} else {
			if (GGAPIEntityHelper.getUuid(entity) == null || GGAPIEntityHelper.getUuid(entity).isEmpty()) {
				log.info("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" No uuid provided, generating one");
				GGAPIEntityHelper.setUuid(entity, UUID.randomUUID().toString());
			} 
		}
	}
	
	protected void checkMandatoryFields(List<String> mandatory, Object entity) throws GGAPIEntityException {
		
		for( String field: mandatory ) {
			try {
				Object value = GGAPIObjectReflectionHelper.getObjectFieldValue(entity, field);
				
				if( value == null ) {
					throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, "Field "+field+" is mandatory");
				} else if( value.toString().isEmpty() ){
					throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, "Field "+field+" is mandatory");
				}
			} catch (SecurityException | IllegalArgumentException | GGAPIObjectReflectionHelperExcpetion e) {
				throw new GGAPIEntityException(e);
			}
		}
	}
	
	private <Entity> List<Entity> checkUnicityFields(GGAPIDomain domain, IGGAPIRepository<Entity> repository, IGGAPICaller caller, Entity entity, List<String> unicity) throws GGAPIEntityException {
		try {
			List<String> values = new ArrayList<String>();
			for (String fieldName : unicity) {
				values.add(GGAPIObjectReflectionHelper.getObjectFieldValue(entity, fieldName).toString());
			}
			String[] fieldValues = new String[values.size()];
			values.toArray(fieldValues);

			GGAPICaller superCaller = new GGAPICaller();
			superCaller.setSuperTenant(true);
			
			GGAPILiteral literal = null;
			for( int i = 0; i < unicity.size(); i++ ) {
				GGAPILiteral eqLiteral = GGAPILiteral.eq(unicity.get(i), fieldValues[i]);
				if( literal == null ) {
					literal = eqLiteral;
				} else {
					literal.orOperator(eqLiteral);
				}
			}

			List<Entity> entities = repository.getEntities(superCaller, 0, 0, literal, null);

			return entities;

		} catch (SecurityException | IllegalArgumentException | GGAPIRepositoryException | GGAPIObjectReflectionHelperExcpetion e) {
			log.error("[domain ["+domain.entity.getValue1().domain()+"]] "+caller.toString()+" Error during checking unicity fields for entity with Uuid " + GGAPIEntityHelper.getUuid(entity), e);
			throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, e.getMessage(), e);
		}
	}

}
