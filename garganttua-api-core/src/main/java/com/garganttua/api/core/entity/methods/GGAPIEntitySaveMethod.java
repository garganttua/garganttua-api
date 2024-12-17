package com.garganttua.api.core.entity.methods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.javatuples.Pair;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.pageable.GGAPIPageable;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.entity.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIUnicityScope;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.updater.IGGAPIEntityUpdater;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntitySaveMethod implements IGGAPIEntitySaveMethod {
	
	private IGGAPIDomain domain;
	private IGGAPIRepository repository;
	private GGObjectAddress afterUpdateMethodAddress;
	private GGObjectAddress beforeUpdateMethodAddress;
	private GGObjectAddress afterCreateMethodAddress;
	private GGObjectAddress beforeCreateMethodAddress;
	private IGGAPIEntityUpdater<Object> entityUpdater;
	private IGGAPIEntityFactory<Object> factory;
	
	
	public GGAPIEntitySaveMethod(IGGAPIDomain domain, IGGAPIRepository repository, IGGAPIEntityFactory<Object> factory, IGGAPIEntityUpdater<Object> updater) throws GGAPIException {
		this.domain = domain;
		this.repository = repository;
		this.factory = factory;
		this.entityUpdater = updater;
		
		this.beforeCreateMethodAddress = this.domain.getEntity().getValue1().beforeCreateMethodAddress();
		this.afterCreateMethodAddress = this.domain.getEntity().getValue1().afterCreateMethodAddress();
		this.beforeUpdateMethodAddress = this.domain.getEntity().getValue1().beforeUpdateMethodAddress();
		this.afterUpdateMethodAddress = this.domain.getEntity().getValue1().afterUpdateMethodAddress();
		
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
				
				Object storedObject = this.factory.getEntityFromRepository(caller, new HashMap<String, String>(), GGAPIEntityIdentifier.UUID , GGAPIEntityHelper.getUuid(entity));
				Object updatedObject = this.entityUpdater.update(caller, storedObject, entity, this.domain.getEntity().getValue1().updateAuthorizations());
				
				this.updateEntity(caller, parameters, updatedObject);
				
				return updatedObject;
			} else {

				this.createEntity(caller, parameters, entity);
				
				return entity;
			}
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
			
			//Should never be reached
			return null;
		}
	}
	
	private void updateEntity(IGGAPICaller caller, Map<String, String> customParameters, Object entity) throws GGAPIException, GGReflectionException {
		log.info("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Updating entity with Uuid " + GGAPIEntityHelper.getUuid(entity));
		IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);
		this.applyUpdateUnicityRule(domain, repository, caller, entity);
		if( this.beforeUpdateMethodAddress != null ) {
			objectQuery.invoke(entity, this.beforeUpdateMethodAddress, caller, customParameters);
		}
		this.repository.save(caller, entity);
		if( this.afterUpdateMethodAddress != null ) {
			objectQuery.invoke(entity, this.afterUpdateMethodAddress, caller, customParameters);
		}
	}

	private void applyUpdateUnicityRule(IGGAPIDomain domain, IGGAPIRepository repository,
			IGGAPICaller caller, Object entity) throws GGAPIException {
		if( domain.getEntity().getValue1().unicityFields() != null && domain.getEntity().getValue1().unicityFields().size() > 0) {
			List<Object> entities = this.checkUnicityFields(domain, repository, caller, entity, domain.getEntity().getValue1().unicityFields() );
			if( entities.size() != 1 && !GGAPIEntityHelper.getUuid(entities.get(0)).equals(GGAPIEntityHelper.getUuid(entity))) {
				log.warn("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Entity with same unical fields already exists, fields "+domain.getEntity().getValue1().unicityFields());
				throw new GGAPIEntityException(GGAPIExceptionCode.ENTITY_ALREADY_EXISTS, "Entity with same unical fields already exists, fields "+domain.getEntity().getValue1().unicityFields());
			}
		}
	}

	private void createEntity(IGGAPICaller caller, Map<String, String> customParameters, Object entity) throws GGAPIException, GGReflectionException {
		IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);
		this.applyTenantEntityRule(domain, caller, entity);

		log.info("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Creating entity with uuid {}", GGAPIEntityHelper.getUuid(entity));

		this.applyOwnedEntityRule(domain, caller, entity);

		if( domain.getEntity().getValue1().mandatoryFields().size() > 0 ) {
			this.checkMandatoryFields(domain.getEntity().getValue1().mandatoryFields(), entity);
		}

		this.applyCreationUnicityRule(domain, repository, caller, entity);
		if( this.beforeCreateMethodAddress != null ) {
			objectQuery.invoke(entity, this.beforeCreateMethodAddress, caller, customParameters);
		}
		this.repository.save(caller, entity);
		if( this.afterCreateMethodAddress != null ) {
			objectQuery.invoke(entity, this.afterCreateMethodAddress, caller, customParameters);
		}

	}

	private void applyCreationUnicityRule(IGGAPIDomain domain, IGGAPIRepository repository,
			IGGAPICaller caller, Object entity) throws GGAPIException {
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
					String ownerId = caller.getOwnerId();
				
					if( ownerId.split(":").length!=2 ) {
						throw new GGAPIEngineException(GGAPIExceptionCode.BAD_REQUEST, "Invalid ownerId ["+ownerId+"] should be of format DOMAIN:UUID");
					}
					
					GGObjectQueryFactory.objectQuery(entity).setValue(domain.getEntity().getValue1().ownerIdFieldAddress(), ownerId);
				} catch (GGReflectionException e) {
					GGAPIException.processException(e);
					
					//Should never be reached
					return;
				}
			} else {
				throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "No ownerId provided");
			}
		}
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
	
	protected void checkMandatoryFields(List<GGObjectAddress> mandatory, Object entity) throws GGAPIException {
		
		for( GGObjectAddress field: mandatory ) {
			try {
				
				IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);
				Object value = objectQuery.getValue(field);
				
				if( value == null ) {
					throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Field "+field+" is mandatory");
				} else if( value.toString().isEmpty() ){
					throw new GGAPIEntityException(GGAPIExceptionCode.BAD_REQUEST, "Field "+field+" is mandatory");
				}
			} catch (IllegalArgumentException | GGReflectionException e) {
				GGAPIException.processException(e);
				
				//Should never be reached
				return;
			}
		}
	}
	
	private List<Object> checkUnicityFields(IGGAPIDomain domain, IGGAPIRepository repository, IGGAPICaller caller, Object entity, List<Pair<GGObjectAddress, GGAPIUnicityScope>> unicity) throws GGAPIException {
		try {
			IGGObjectQuery objectQuery = GGObjectQueryFactory.objectQuery(entity);
			List<String> values = new ArrayList<String>();
			for (Pair<GGObjectAddress, GGAPIUnicityScope> fieldName : unicity) {
				values.add(objectQuery.getValue(fieldName.getValue0()).toString());
			}
			String[] fieldValues = new String[values.size()];
			values.toArray(fieldValues);
			
			GGAPILiteral literal = null;
			for( int i = 0; i < unicity.size(); i++ ) {
				GGAPILiteral eqLiteral = GGAPILiteral.eq(unicity.get(i).getValue0().toString(), fieldValues[i]);
				if( literal == null ) {
					literal = eqLiteral;
				} else {
					literal.orOperator(eqLiteral);
				}
			}

			return repository.getEntities(GGAPICaller.createTenantCaller(caller.getRequestedTenantId()), GGAPIPageable.getPage(0,0), literal, null);

		} catch (GGReflectionException e) {
			log.error("[domain ["+domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Error during checking unicity fields for entity with Uuid " + GGAPIEntityHelper.getUuid(entity), e);
			GGAPIException.processException(e);
			
			//Should never be reached
			return null;
		}
	}
}
