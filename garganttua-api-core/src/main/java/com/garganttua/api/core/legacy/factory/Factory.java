package com.garganttua.api.core.legacy.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.api.core.legacy.entity.exceptions.EntityException;
import com.garganttua.api.core.legacy.entity.methods.EntityDeleteMethod;
import com.garganttua.api.core.legacy.entity.methods.EntitySaveMethod;
import com.garganttua.api.core.legacy.entity.tools.EntityHelper;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.factory.EntityIdentifier;
import com.garganttua.api.spec.factory.IFactory;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.api.spec.entity.IEntityUpdater;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.injection.IGGInjector;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.query.IObjectQuery;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Factory implements IFactory {

	@Setter
	private IDomain domain;

	private IObjectQuery objectQuery;

	private ObjectAddress afterGetMethodAddress;
	
	@Setter
	private IRepository repository;
	
	@Setter
	private Optional<IGGInjector> injector;

	private IEngine engine;

	@Setter
	private IEntityUpdater entityUpdater;

	public Factory(IDomain domain) throws CoreException {
		this.domain = domain;

		this.afterGetMethodAddress = this.domain.getAfterGetMethodAddress();
		try {
			this.objectQuery = ObjectQueryFactory.objectQuery(domain.getEntityClass());
		} catch (ReflectionException e) {
			CoreException.processException(e);
		}
	}

	@Override
	public Object prepareNewEntity(Map<String, String> customParameters, Object entity, String uuid, String tenantId) throws CoreException {
		if( entity == null ) {
			throw new EngineException(CoreExceptionCode.GENERIC_FACTORY_EXCEPTION, "Entity is null");
		}
		try {
			EntityHelper.setGotFromRepository( entity, false );
			EntityHelper.setUuid(entity, uuid);
			EntityHelper.setTenantId(entity, tenantId);
			
			this.setEntityMethodsAndFields(customParameters, this.domain, entity);
			
			return entity;
		} catch (EntityException e) {
			CoreException.processException(e);
			//should be never reached
			return null;
		}
	}
	
	@Override
	public Object getEntityFromRepository(ICaller caller, Map<String, String> customParameters, EntityIdentifier identifier , String uuid) throws CoreException  {
		if( this.domain == null ) {
			throw new EngineException(CoreExceptionCode.GENERIC_FACTORY_EXCEPTION, "Domain is null");
		}
		if( caller == null ) {
			throw new EngineException(CoreExceptionCode.GENERIC_FACTORY_EXCEPTION, "Caller is null");
		}
		if( uuid == null ) {
			throw new EngineException(CoreExceptionCode.GENERIC_FACTORY_EXCEPTION, "Uuid is null");
		}
		
		Optional<Object> entity = null;

		switch (identifier) {
			default:
			case UUID:
				entity = repository.getOneByUuid(caller, uuid);
				break;
			case ID:
				entity = repository.getOneById(caller, uuid);
				break;
		}

		if( !entity.isPresent() ) {
			log.warn("[Domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Entity with Uuid " + uuid + " not found");
			throw new EngineException(CoreExceptionCode.ENTITY_NOT_FOUND, "Entity [Uuid: "+uuid+", Type: "+this.domain.getEntityClass().getSimpleName()+"] does not exist");
		}

		EntityHelper.setRepository( entity.get(), repository );
		this.executeAfterGetProcedure(caller, customParameters, entity.get(), repository);
		this.setEntityMethodsAndFields(customParameters, this.domain, entity.get());
		EntityHelper.setGotFromRepository(entity.get(), true);
		return entity.get();
	}

	@Override
	public List<Object> getEntitiesFromRepository(ICaller caller, IPageable pageable, IFilter filter, ISort sort, Map<String, String> customParameters) throws CoreException {
		if( this.domain == null ) {
			throw new EngineException(CoreExceptionCode.GENERIC_FACTORY_EXCEPTION, "this.domain is null");
		}
		if( caller == null ) {
			throw new EngineException(CoreExceptionCode.GENERIC_FACTORY_EXCEPTION, "Caller is null");
		}
		
		List<Object> entities = new ArrayList<Object>();

		entities.addAll(this.repository.getEntities(caller, pageable, filter, sort));

		for( Object entity: entities) {
			this.executeAfterGetProcedure(caller, customParameters, entity, this.repository);
			EntityHelper.setGotFromRepository(entity, true);
			this.setEntityMethodsAndFields(customParameters, this.domain, entity);
		}
		return entities;
	}

	private void executeAfterGetProcedure(ICaller caller, Map<String, String> customParameters, Object entity, IRepository repository) throws CoreException {
		this.setEntityMethodsAndFields(customParameters, this.domain, entity);
		try {
			if( this.afterGetMethodAddress != null ) {
				this.objectQuery.invoke(entity, this.afterGetMethodAddress, caller, customParameters);
			}
		} catch (ReflectionException e) {
			try {
				log.warn("[Domain ["+this.domain.getDomain()+"]] "+caller.toString()+" Error during processing AfterGet Method on entity with uuid "+EntityHelper.getUuid(entity));
			} catch (EntityException e1) {
				throw new EngineException(e);
			}
			if(log.isDebugEnabled()) {
				log.warn("[Domain ["+this.domain.getDomain()+"]] "+caller.toString()+" The error :", e);
			}
			CoreException.processException(e);
			//should be never reached
			return;
		}
	}
	
	private <T> void setEntityMethodsAndFields(Map<String, String> customParameters, IDomain domain, T entity) throws CoreException{
		EntityHelper.setRepository(entity, this.repository );
		EntityHelper.setEngine(entity, this.engine );
		EntityHelper.setSaveMethod(entity, new EntitySaveMethod(this.domain, this.repository, this, this.entityUpdater));
		EntityHelper.setDeleteMethod(entity, new EntityDeleteMethod(this.domain, this.repository));
		this.injectDependenciesAndValues(entity);
	}

	@Override
	public long countEntities(ICaller caller, IFilter filter, Map<String, String> customParameters) throws CoreException {
		return this.repository.getCount(caller, filter);
	}
	
	private void injectDependenciesAndValues(Object entity) throws CoreException {
		if( this.injector.isPresent() ) {
			try {
				this.injector.get().injectBeans(entity);
				this.injector.get().injectProperties(entity);
			} catch (ReflectionException e) {
				CoreException.processException(e);
				//should be never reached
				return ;
			}
		}
    }

	@Override
	public void setEngine(IEngine engine) {
		this.engine = engine;	
	}

}
