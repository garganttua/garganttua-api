package com.garganttua.api.core.entity.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.garganttua.api.core.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryFactory;
import com.garganttua.api.core.objects.query.IGGAPIObjectQuery;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelperExcpetion;
import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.core.security.GGAPISecurityException;
import com.garganttua.api.spec.GGAPICoreExceptionCode;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.dao.GGAPILiteral;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPISecurity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntitySaveMethod implements IGGAPIEntitySaveMethod<Object> {
	
	private GGAPIDomain domain;
	private IGGAPIRepository<Object> repository;
	private Optional<IGGAPISecurity> security;
	private GGAPIObjectAddress afterUpdateMethodAddress;
	private GGAPIObjectAddress beforeUpdateMethodAddress;
	private GGAPIObjectAddress afterCreateMethodAddress;
	private GGAPIObjectAddress beforeCreateMethodAddress;
	private IGGAPIObjectQuery objectQuery;
	
	public GGAPIEntitySaveMethod(GGAPIDomain domain, IGGAPIRepository<Object> repository, Optional<IGGAPISecurity> security) throws GGAPIEntityException {
		this.domain = domain;
		this.repository = repository;
		this.security = security;
		
		this.beforeCreateMethodAddress = this.domain.entity.getValue1().beforeCreateMethodAddress();
		this.afterCreateMethodAddress = this.domain.entity.getValue1().afterCreateMethodAddress();
		this.beforeUpdateMethodAddress = this.domain.entity.getValue1().beforeCreateMethodAddress();
		this.afterUpdateMethodAddress = this.domain.entity.getValue1().afterCreateMethodAddress();
		
		try {
			this.objectQuery = GGAPIObjectQueryFactory.objectQuery(domain.entity.getValue0());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		}
	}

	@Override
	public void save(IGGAPICaller caller, Map<String, String> parameters, Object entity) throws GGAPIEntityException {
		if( domain == null ) {
			throw new GGAPIEntityException(GGAPICoreExceptionCode.BAD_REQUEST, "Domain is null");
		}
		if( caller == null ) {
			throw new GGAPIEntityException(GGAPICoreExceptionCode.BAD_REQUEST, "Caller is null");
		}
		if( repository == null ) {
			throw new GGAPIEntityException(GGAPICoreExceptionCode.BAD_REQUEST, "Repository is null");
		}
		if( entity == null ) {
			throw new GGAPIEntityException(GGAPICoreExceptionCode.BAD_REQUEST, "Entity is null");
		}

		try {
			if( repository.doesExist(caller, entity) ) {
				if( this.beforeUpdateMethodAddress != null ) {
					this.objectQuery.invoke(entity, this.beforeUpdateMethodAddress, caller, parameters);
				}
				this.updateEntity(caller, parameters, entity);
				if( this.afterUpdateMethodAddress != null ) {
					this.objectQuery.invoke(entity, this.afterUpdateMethodAddress, caller, parameters);
				}
			} else {
				if( this.beforeCreateMethodAddress != null ) {
					this.objectQuery.invoke(entity, this.beforeCreateMethodAddress, caller, parameters);
				}
				this.createEntity(caller, parameters, entity);
				if( this.afterCreateMethodAddress != null ) {
					this.objectQuery.invoke(entity, this.afterCreateMethodAddress, caller, parameters);
				}
			}
		} catch (GGAPIRepositoryException | GGAPIObjectQueryException e) {
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
				throw new GGAPIEntityException(GGAPICoreExceptionCode.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists");
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
				throw new GGAPIEntityException(GGAPICoreExceptionCode.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists");
			}
		}
	}

	private void applyOwnedEntityRule(GGAPIDomain domain, IGGAPICaller caller,
			Object entity) throws GGAPIEntityException {
		if( domain.entity.getValue1().ownedEntity() ) {
			if( caller.getOwnerId() != null && !caller.getOwnerId().isEmpty()) {
				try {
					GGAPIObjectQueryFactory.objectQuery(entity).setValue(GGAPIEntityChecker.checkEntity(entity).ownerIdFieldAddress(), caller.getOwnerId());
				} catch (GGAPIEntityException | GGAPIObjectQueryException e) {
					throw new GGAPIEntityException(e);
				}
			} else {
				throw new GGAPIEntityException(GGAPICoreExceptionCode.BAD_REQUEST, "No ownerId provided");
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
					throw new GGAPIEntityException(GGAPICoreExceptionCode.UNKNOWN_ERROR, "Error durnig applying security on entity", e);
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
					throw new GGAPIEntityException(GGAPICoreExceptionCode.BAD_REQUEST, "Field "+field+" is mandatory");
				} else if( value.toString().isEmpty() ){
					throw new GGAPIEntityException(GGAPICoreExceptionCode.BAD_REQUEST, "Field "+field+" is mandatory");
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
			throw new GGAPIEntityException(GGAPICoreExceptionCode.UNKNOWN_ERROR, e.getMessage(), e);
		}
	}
}
