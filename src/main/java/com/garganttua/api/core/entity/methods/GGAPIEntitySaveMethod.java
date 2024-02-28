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
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIOwnedEntity;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.repository.IGGAPIRepository;
import com.garganttua.api.security.GGAPISecurityException;
import com.garganttua.api.security.IGGAPISecurity;
import com.garganttua.api.security.authentication.IGGAPIAuthenticationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntitySaveMethod implements IGGAPIEntitySaveMethod {
	
	@Override
	public <Entity extends IGGAPIEntity> void save(GGAPIDynamicDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Optional<IGGAPISecurity> security, Map<String, String> parameters, Entity entity) throws GGAPIEntityException, GGAPIEngineException {
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

		if( repository.doesExist(caller, entity) ) {
			GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityBeforeUpdate.class, entity, caller, parameters);
			this.updateEntity(domain, repository, caller, parameters, security, entity);
			GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityAfterUpdate.class, entity, caller, parameters);
		} else {
			GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityBeforeCreate.class, entity, caller, parameters);
			this.createEntity(domain, repository, caller, parameters, security, entity);
			GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityAfterCreate.class, entity, caller, parameters);
		}

	}
	
	private <Entity extends IGGAPIEntity> void updateEntity(GGAPIDynamicDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Map<String, String> customParameters, Optional<IGGAPISecurity> security, Entity entity) throws GGAPIEntityException, GGAPIEngineException {
		log.info("[domain ["+domain.domain+"]] "+caller.toString()+" Updating entity with Uuid " + entity.getUuid());
		this.applySecurityRuleOnAuthenticatorEntity(domain, security, entity);
		this.applyUpdateUnicityRule(domain, repository, caller, entity);

		repository.update(caller, entity);
	}

	private <Entity extends IGGAPIEntity> void applyUpdateUnicityRule(GGAPIDynamicDomain domain, IGGAPIRepository repository,
			IGGAPICaller caller, Entity entity) throws GGAPIEntityException {
		if( domain.unicity != null && domain.unicity.length > 0) {
			List<Entity> entities = this.checkUnicityFields(domain, repository, caller, entity, domain.unicity);
			if( entities.size() != 1 && !entities.get(0).getUuid().equals(entity.getUuid())) {
				log.warn("[domain ["+domain.domain+"]] "+caller.toString()+" Entity with same unical fields already exists");
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists");
			}
		}
	}

	private <Entity extends IGGAPIEntity> void createEntity(GGAPIDynamicDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Map<String, String> customParameters, Optional<IGGAPISecurity> security, Entity entity) throws GGAPIEntityException, GGAPIEngineException {

		this.applyTenantEntityRule(domain, caller, entity);

		log.info("[domain ["+domain.domain+"]] "+caller.toString()+" Creating entity with uuid {}",entity.getUuid());

		this.applySecurityRuleOnAuthenticatorEntity(domain, security, entity);
		this.applyOwnedEntityRule(domain, caller, entity);

		if( domain.mandatory.length > 0 ) {
			this.checkMandatoryFields(domain.mandatory, entity);
		}

		this.applyCreationUnicityRule(domain, repository, caller, entity);
		repository.save(caller, entity);
	}
	
	private <Entity extends IGGAPIEntity> void applyCreationUnicityRule(GGAPIDynamicDomain domain, IGGAPIRepository repository,
			IGGAPICaller caller, Entity entity) throws GGAPIEntityException {
		if( domain.unicity != null && domain.unicity.length > 0) {
			if( this.checkUnicityFields(domain, repository, caller, entity, domain.unicity).size() > 0 ) {
				log.warn("[domain ["+domain.domain+"]] "+caller.toString()+" Entity with same unical fields already exists");
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists");
			}
		}
	}

	private <Entity extends IGGAPIEntity> void applyOwnedEntityRule(GGAPIDynamicDomain domain, IGGAPICaller caller,
			Entity entity) throws GGAPIEntityException {
		if( domain.ownedEntity ) {
			if( caller.getOwnerId() != null && !caller.getOwnerId().isEmpty()) {
				((IGGAPIOwnedEntity) entity).setOwnerId(caller.getOwnerId());
			} else {
				throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, "No ownerId provided");
			}
		}
	}

	private <Entity extends IGGAPIEntity> void applySecurityRuleOnAuthenticatorEntity(GGAPIDynamicDomain domain, Optional<IGGAPISecurity> security, Entity entity) throws GGAPIEntityException {
		if( domain.authenticatorEntity ) {
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
	}

	private <Entity extends IGGAPIEntity> void applyTenantEntityRule(GGAPIDynamicDomain domain, IGGAPICaller caller,
			Entity entity) {
		if( domain.tenantEntity ) {
			if( (caller.getRequestedTenantId() == null || caller.getRequestedTenantId().isEmpty())) {
				log.info("[domain ["+domain.domain+"]] "+caller.toString()+" No uuid provided, generating one");
				if (entity.getUuid() == null || entity.getUuid().isEmpty()) {
					entity.setUuid(UUID.randomUUID().toString());
				} 
				((GGAPICaller) caller).setRequestedTenantId(entity.getUuid());
			} else {
				entity.setUuid(caller.getRequestedTenantId());
			}
		} else {
			if (entity.getUuid() == null || entity.getUuid().isEmpty()) {
				log.info("[domain ["+domain.domain+"]] "+caller.toString()+" No uuid provided, generating one");
				entity.setUuid(UUID.randomUUID().toString());
			} 
		}
	}
	
	protected <Entity extends IGGAPIEntity> void checkMandatoryFields(String[] mandatory, Entity entity) throws GGAPIEntityException {
		
		for( String field: mandatory ) {
			try {
				Object value = GGAPIEntityHelper.getFieldValue(entity.getClass(), field, entity);
				
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
	
	private <Entity extends IGGAPIEntity> List<Entity> checkUnicityFields(GGAPIDynamicDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Entity entity, String[] unicity) throws GGAPIEntityException {
		try {
			List<String> values = new ArrayList<String>();
			for (String fieldName : unicity) {
				values.add(GGAPIEntityHelper.getFieldValue(domain.entityClass, fieldName, entity).toString());
			}
			String[] fieldValues = new String[values.size()];
			values.toArray(fieldValues);

			GGAPICaller superCaller = new GGAPICaller();
			superCaller.setSuperTenant(true);
			
			GGAPILiteral literal = null;
			for( int i = 0; i < unicity.length; i++ ) {
				GGAPILiteral eqLiteral = GGAPILiteral.eq(unicity[i], fieldValues[i]);
				if( literal == null ) {
					literal = eqLiteral;
				} else {
					literal.orOperator(eqLiteral);
				}
			}

			List<Entity> entities = repository.getEntities(domain, superCaller, 0, 0, literal, null, null);

			return entities;

		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			log.error("[domain ["+domain.domain+"]] "+caller.toString()+" Error during checking unicity fields for entity with Uuid " + entity.getUuid(), e);
			throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, e.getMessage(), e);
		}
	}

}
