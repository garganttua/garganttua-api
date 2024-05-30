package com.garganttua.api.core.entity.factory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.engine.GGAPIDomain;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.methods.GGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.methods.GGAPIEntitySaveMethod;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryFactory;
import com.garganttua.api.core.objects.query.IGGAPIObjectQuery;
import com.garganttua.api.core.objects.utils.GGAPIFieldAccessManager;
import com.garganttua.api.core.repository.GGAPIRepositoryException;
import com.garganttua.api.spec.GGAPICoreExceptionCode;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.dao.GGAPILiteral;
import com.garganttua.api.spec.dao.GGAPISort;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.factory.GGAPIEntityIdentifier;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPISecurity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class GGAPIEntityFactory implements IGGAPIEntityFactory<Object> {
	
	private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	@Setter
	private GGAPIDomain domain;

	private IGGAPIRepository<Object> repository;
	
	private ApplicationContext springContext;
	
	private Environment environment;
	
	@Setter
	private Optional<IGGAPISecurity> security = Optional.empty();

	private IGGAPIObjectQuery objectQuery;

	private GGAPIObjectAddress afterGetMethodAddress;
	
	public GGAPIEntityFactory(GGAPIDomain domain, IGGAPIRepository<Object> repository, ApplicationContext springContext, Environment environment) throws GGAPIFactoryException {
		this.domain = domain;
		this.repository = repository;
		this.springContext = springContext;
		this.environment = environment;
		this.afterGetMethodAddress = this.domain.entity.getValue1().afterGetMethodAddress();
		try {
			this.objectQuery = GGAPIObjectQueryFactory.objectQuery(domain.entity.getValue0());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIFactoryException(e);
		}
	}

	@Override
	public Object getEntityFromJson(Map<String, String> customParameters, byte[] json) throws GGAPIFactoryException {
		if( this.domain == null ) {
			throw new GGAPIFactoryException("domain is null");
		}
		if( json == null ) {
			throw new GGAPIFactoryException("Json is null");
		}
		try {
			Object entity = (Object) mapper.readValue(json, this.domain.entity.getValue0());
			this.prepareNewEntity(customParameters, entity);
			GGAPIEntityHelper.setGotFromRepository( entity, false );
			return entity;
		} catch (IOException | GGAPIEntityException e) {
			throw new GGAPIFactoryException(e);
		}
	}
	
	@Override
	public Object prepareNewEntity(Map<String, String> customParameters, Object entity) throws GGAPIFactoryException {
		if( entity == null ) {
			throw new GGAPIFactoryException("Entity is null");
		}
		try {
			GGAPIEntityHelper.setUuid(entity, null);
			GGAPIEntityHelper.setRepository( entity, this.repository );
			this.setEntityMethodsAndFields(repository, customParameters, this.domain, entity);
			
			return entity;
		} catch (GGAPIEntityException e) {
			throw new GGAPIFactoryException(e);
		}
	}
	
	@Override
	public Object getEntityFromRepository(IGGAPICaller caller, Map<String, String> customParameters, GGAPIEntityIdentifier identifier , String uuid) throws GGAPIFactoryException  {
		if( this.domain == null ) {
			throw new GGAPIFactoryException("this.domain is null");
		}
		if( caller == null ) {
			throw new GGAPIFactoryException("Caller is null");
		}
		if( uuid == null ) {
			throw new GGAPIFactoryException("Uuid is null");
		}
		
		Object entity = null;
		try {

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
				log.warn("[Domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Entity with Uuid " + uuid + " not found");
				throw new GGAPIFactoryException(GGAPICoreExceptionCode.GENERIC_FACTORY_EXCEPTION, "Entity does not exist");
			}

			GGAPIEntityHelper.setRepository( entity, repository );
			this.executeAfterGetProcedure(caller, customParameters, entity, repository);
			GGAPIEntityHelper.setGotFromRepository(entity, true);
			
		} catch (GGAPIRepositoryException | GGAPIEntityException e) {
			throw new GGAPIFactoryException(e);
		}
		
		return entity;
	}

	@Override
	public List<Object> getEntitiesFromRepository(IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, Map<String, String> customParameters) throws GGAPIFactoryException {
		if( this.domain == null ) {
			throw new GGAPIFactoryException("this.domain is null");
		}
		if( caller == null ) {
			throw new GGAPIFactoryException("Caller is null");
		}
		
		List<Object> entities = new ArrayList<Object>();

		try {
			entities.addAll(this.repository.getEntities(caller, pageSize, pageIndex, filter, sort));
		} catch (GGAPIRepositoryException e) {
			throw new GGAPIFactoryException(e);
		}
		for( Object entity: entities) {
			try {
				this.executeAfterGetProcedure(caller, customParameters, entity, this.repository);
				GGAPIEntityHelper.setGotFromRepository(entity, true);
			} catch (GGAPIEntityException e) {
				throw new GGAPIFactoryException(e);
			}
		}
		
		return entities;
	}

	private void executeAfterGetProcedure(IGGAPICaller caller, Map<String, String> customParameters, Object entity, IGGAPIRepository<Object> repository) throws GGAPIFactoryException {
		this.setEntityMethodsAndFields(this.repository, customParameters, this.domain, entity);
		try {
			if( this.afterGetMethodAddress != null ) {
				this.objectQuery.invoke(entity, this.afterGetMethodAddress, caller, customParameters);
			}
		} catch (GGAPIObjectQueryException e) {
			try {
				log.warn("[Domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" Error during processing AfterGet Method on entity with uuid "+GGAPIEntityHelper.getUuid(entity));
			} catch (GGAPIEntityException e1) {
				throw new GGAPIFactoryException(e);
			}
			if(log.isDebugEnabled()) {
				log.warn("[Domain ["+this.domain.entity.getValue1().domain()+"]] "+caller.toString()+" The error :", e);
			}
			throw new GGAPIFactoryException(e);
		}
	}
	
	private <T> void setEntityMethodsAndFields(IGGAPIRepository<Object> repository, Map<String, String> customParameters, GGAPIDomain domain, T entity) throws GGAPIFactoryException{
		try {
			GGAPIEntityHelper.setRepository( entity, this.repository );
			GGAPIEntityHelper.setSaveMethod(entity, new GGAPIEntitySaveMethod(this.domain, repository, security));
			GGAPIEntityHelper.setDeleteMethod(entity, new GGAPIEntityDeleteMethod(this.domain, repository));
			this.injectDependenciesAndValues(entity);
		} catch (GGAPIEntityException e) {
			throw new GGAPIFactoryException(e);
		}
	}

	@Override
	public long countEntities(IGGAPICaller caller, GGAPILiteral filter, Map<String, String> customParameters) throws GGAPIFactoryException {
		try {
			return this.repository.getCount(caller, filter);
		} catch (GGAPIRepositoryException e) {
			throw new GGAPIFactoryException(e);
		}
	}
	
	private void injectDependenciesAndValues(Object entity) throws GGAPIFactoryException {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(Autowired.class)) {
                Object bean;
                if (field.isAnnotationPresent(Qualifier.class)) {
                    String qualifierName = field.getAnnotation(Qualifier.class).toString();
                    bean = this.springContext.getBean(qualifierName);
                } else {
                    bean = this.springContext.getBean(field.getType());
                }
                if (bean == null) {
                    throw new GGAPIFactoryException(GGAPICoreExceptionCode.INJECTION_ERROR, "Bean not found for field: " + field.getName());
                }

                try (GGAPIFieldAccessManager accessManager = new GGAPIFieldAccessManager(field)) {
					field.set(entity, bean);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					if( log.isDebugEnabled() ) {
						log.warn("Field  "+field.getName()+" of entity of type "+entity.getClass().getName()+" cannot be set", e);
					}
					throw new GGAPIFactoryException(GGAPICoreExceptionCode.INJECTION_ERROR, "Field  "+field.getName()+" of entity of type "+entity.getClass().getName()+" cannot be set", e);
				}
            } else if (field.isAnnotationPresent(Value.class)) {
                String value = field.getAnnotation(Value.class).value();
                if (value.startsWith("${") && value.endsWith("}")) {
                    String propertyName = value.substring(2, value.length() - 1);
                    String propertyValue = this.environment.getProperty(propertyName);
                    if( propertyValue == null ) {
                    	throw new GGAPIFactoryException(GGAPICoreExceptionCode.INJECTION_ERROR, "Value not found: " + propertyName);
                    }
                    try (GGAPIFieldAccessManager accessManager = new GGAPIFieldAccessManager(field)) {
    					field.set(entity, propertyValue);
    				} catch (IllegalArgumentException | IllegalAccessException e) {
    					if( log.isDebugEnabled() ) {
    						log.warn("Field  "+field.getName()+" of entity of type "+entity.getClass().getName()+" cannot be set", e);
    					}
    					throw new GGAPIFactoryException(GGAPICoreExceptionCode.INJECTION_ERROR, "Field  "+field.getName()+" of entity of type "+entity.getClass().getName()+" cannot be set", e);
    				}
                } else {
                	 throw new GGAPIFactoryException(GGAPICoreExceptionCode.INJECTION_ERROR, "Malformed value annotation: " + field.getName());
                }
            }
        }
    }

	@Override
	public void setEngine(IGGAPIEngine engine) {	
	}

}
