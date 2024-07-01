package com.garganttua.api.core.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.methods.GGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.methods.GGAPIEntitySaveMethod;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.pageable.IGGAPIPageable;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.api.spec.sort.IGGAPISort;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class GGAPIEntityFactory implements IGGAPIEntityFactory<Object> {

	@Setter
	private IGGAPIDomain domain;

	@Setter
	private Optional<IGGAPISecurity> security = Optional.empty();

	private IGGObjectQuery objectQuery;

	private GGObjectAddress afterGetMethodAddress;
	
	@Setter
	private IGGAPIRepository<Object> repository;

	@Setter
	private IGGBeanLoader beanLoader;
	
	@Setter
	private IGGInjector injector;

	@Setter
	private IGGPropertyLoader propertyLoader;

	public GGAPIEntityFactory(IGGAPIDomain domain) throws GGAPIEngineException {
		this.domain = domain;

		this.afterGetMethodAddress = this.domain.getEntity().getValue1().afterGetMethodAddress();
		try {
			this.objectQuery = GGObjectQueryFactory.objectQuery(domain.getEntity().getValue0());
		} catch (GGReflectionException e) {
			throw new GGAPIEngineException(e);
		}
	}

	@Override
	public Object prepareNewEntity(Map<String, String> customParameters, Object entity) throws GGAPIException {
		if( entity == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.GENERIC_FACTORY_EXCEPTION, "Entity is null");
		}
		try {
			GGAPIEntityHelper.setGotFromRepository( entity, false );
			GGAPIEntityHelper.setUuid(entity, null);
			GGAPIEntityHelper.setRepository( entity, this.repository );
			this.setEntityMethodsAndFields(repository, customParameters, this.domain, entity);
			
			return entity;
		} catch (GGAPIEntityException e) {
			throw new GGAPIEngineException(e);
		}
	}
	
	@Override
	public Object getEntityFromRepository(IGGAPICaller caller, Map<String, String> customParameters, GGAPIEntityIdentifier identifier , String uuid) throws GGAPIException  {
		if( this.domain == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.GENERIC_FACTORY_EXCEPTION, "this.domain is null");
		}
		if( caller == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.GENERIC_FACTORY_EXCEPTION, "Caller is null");
		}
		if( uuid == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.GENERIC_FACTORY_EXCEPTION, "Uuid is null");
		}
		
		Object entity = null;

		switch (identifier) {
			default:
			case UUID:
				entity = (Object) repository.getOneByUuid(caller, uuid);
				break;
			case ID:
				entity = (Object) repository.getOneById(caller, uuid);
				break;
		}
		
		if( entity == null ) {
			log.warn("[Domain ["+this.domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Entity with Uuid " + uuid + " not found");
			throw new GGAPIEngineException(GGAPIExceptionCode.GENERIC_FACTORY_EXCEPTION, "Entity does not exist");
		}

		GGAPIEntityHelper.setRepository( entity, repository );
		this.executeAfterGetProcedure(caller, customParameters, entity, repository);
		GGAPIEntityHelper.setGotFromRepository(entity, true);
		return entity;
	}

	@Override
	public List<Object> getEntitiesFromRepository(IGGAPICaller caller, IGGAPIPageable pageable, IGGAPIFilter filter, IGGAPISort sort, Map<String, String> customParameters) throws GGAPIException {
		if( this.domain == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.GENERIC_FACTORY_EXCEPTION, "this.domain is null");
		}
		if( caller == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.GENERIC_FACTORY_EXCEPTION, "Caller is null");
		}
		
		List<Object> entities = new ArrayList<Object>();

		entities.addAll(this.repository.getEntities(caller, pageable, filter, sort));

		for( Object entity: entities) {
			this.executeAfterGetProcedure(caller, customParameters, entity, this.repository);
			GGAPIEntityHelper.setGotFromRepository(entity, true);
		}
		return entities;
	}

	private void executeAfterGetProcedure(IGGAPICaller caller, Map<String, String> customParameters, Object entity, IGGAPIRepository<Object> repository) throws GGAPIException {
		this.setEntityMethodsAndFields(this.repository, customParameters, this.domain, entity);
		try {
			if( this.afterGetMethodAddress != null ) {
				this.objectQuery.invoke(entity, this.afterGetMethodAddress, caller, customParameters);
			}
		} catch (GGReflectionException e) {
			try {
				log.warn("[Domain ["+this.domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" Error during processing AfterGet Method on entity with uuid "+GGAPIEntityHelper.getUuid(entity));
			} catch (GGAPIEntityException e1) {
				throw new GGAPIEngineException(e);
			}
			if(log.isDebugEnabled()) {
				log.warn("[Domain ["+this.domain.getEntity().getValue1().domain()+"]] "+caller.toString()+" The error :", e);
			}
			throw new GGAPIEngineException(e);
		}
	}
	
	private <T> void setEntityMethodsAndFields(IGGAPIRepository<Object> repository, Map<String, String> customParameters, IGGAPIDomain domain, T entity) throws GGAPIException{
		GGAPIEntityHelper.setRepository(entity, this.repository );
		GGAPIEntityHelper.setSaveMethod(entity, new GGAPIEntitySaveMethod(this.domain, repository, security));
		GGAPIEntityHelper.setDeleteMethod(entity, new GGAPIEntityDeleteMethod(this.domain, repository));
		this.injectDependenciesAndValues(entity);
	}

	@Override
	public long countEntities(IGGAPICaller caller, IGGAPIFilter filter, Map<String, String> customParameters) throws GGAPIException {
		return this.repository.getCount(caller, filter);
	}
	
	private void injectDependenciesAndValues(Object entity) throws GGAPIException {
		try {
			this.injector.injectBeans(entity);
			this.injector.injectProperties(entity);
		} catch (GGReflectionException e) {
			throw new GGAPIEngineException(e);
		}
    }

	@Override
	public void setEngine(IGGAPIEngine engine) {	
	}

}
