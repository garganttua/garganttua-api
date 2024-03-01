package com.garganttua.api.core.entity.factory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations;
import com.garganttua.api.core.entity.annotations.GGAPIBusinessAnnotations.GGAPIEntityAfterGet;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;
import com.garganttua.api.core.entity.methods.GGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.methods.GGAPIEntitySaveMethod;
import com.garganttua.api.core.filter.GGAPIGeolocFilter;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.sort.GGAPISort;
import com.garganttua.api.core.tools.GGAPIFieldAccessManager;
import com.garganttua.api.engine.GGAPIDynamicDomain;
import com.garganttua.api.engine.GGAPIEngineException;
import com.garganttua.api.engine.registries.IGGAPIRepositoriesRegistry;
import com.garganttua.api.repository.IGGAPIRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class GGAPIEntityFactory implements IGGAPIEntityFactory {

	@Setter
	private IGGAPIRepositoriesRegistry repositoriesRegistry;
	
	private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	@Setter
	private ApplicationContext springContext;
	
	@Setter
	private Environment environment;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getEntityFromJson(GGAPIDynamicDomain domain, Map<String, String> customParameters, byte[] json) throws GGAPIEntityException, GGAPIEngineException {
		if( domain == null ) {
			throw new GGAPIEntityException("Domain is null");
		}
		if( json == null ) {
			throw new GGAPIEntityException("Json is null");
		}
		try {
			T entity = (T) mapper.readValue(json, domain.entityClass);
			this.prepareNewEntity(customParameters, entity);
			entity.setGotFromRepository(false);
			return entity;
		} catch (IOException e) {
			throw new GGAPIEntityException(GGAPIEntityException.BAD_REQUEST, e);
		}
	}
	
	@Override
	public <T> T prepareNewEntity(Map<String, String> customParameters, T entity) throws GGAPIEntityException, GGAPIEngineException {
		if( entity == null ) {
			throw new GGAPIEntityException("Uuid is null");
		}
		entity.setUuid(null);
		GGAPIDynamicDomain dDomain = GGAPIDynamicDomain.fromEntityClass(entity.getClass());
		IGGAPIRepository repository = this.repositoriesRegistry.getRepository(dDomain.domain);
		
		entity.setRepository(repository);
		this.setEntityMethodsAndFields(repository, customParameters, dDomain, entity);
	
		return entity;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T getEntityFromRepository(GGAPIDynamicDomain domain, IGGAPICaller caller, Map<String, String> customParameters, GGAPIEntityIdentifier identifier , String uuid) throws GGAPIEntityException {
		if( domain == null ) {
			throw new GGAPIEntityException("Domain is null");
		}
		if( caller == null ) {
			throw new GGAPIEntityException("Caller is null");
		}
		if( uuid == null ) {
			throw new GGAPIEntityException("Uuid is null");
		}
		
		T entity = null;
		
		IGGAPIRepository repository = this.repositoriesRegistry.getRepository(domain.domain);
	
		switch (identifier) {
			default:
			case UUID:
				entity = (T) repository.getOneByUuid(domain, caller, uuid);
				break;
			case ID:
				entity = (T) repository.getOneById(domain, caller, uuid);
				break;
		}
		
		if( entity == null ) {
			log.warn("[domain ["+domain.domain+"]] "+caller.toString()+" Entity with Uuid " + uuid + " not found");
			throw new GGAPIEntityException(GGAPIEntityException.ENTITY_NOT_FOUND, "Entity does not exist");
		}
		entity.setRepository(repository);
		this.executeAfterGetProcedure(domain, caller, customParameters, entity, repository);
		
		entity.setGotFromRepository(true);
		
		return entity;
	}

	@Override
	public <T> List<T> getEntitiesFromRepository(GGAPIDynamicDomain domain, IGGAPICaller caller, int pageSize, int pageIndex, GGAPILiteral filter, GGAPISort sort, GGAPIGeolocFilter geoloc, Map<String, String> customParameters) throws GGAPIEntityException{
		if( domain == null ) {
			throw new GGAPIEntityException("Domain is null");
		}
		if( caller == null ) {
			throw new GGAPIEntityException("Caller is null");
		}
		
		List<T> entities = new ArrayList<T>();
		
		IGGAPIRepository repository = this.repositoriesRegistry.getRepository(domain.domain);
		entities.addAll(repository.getEntities(domain, caller, pageSize, pageIndex, filter, sort, geoloc));
		for( T entity: entities ) {
			this.executeAfterGetProcedure(domain, caller, customParameters, entity, repository);
			entity.setGotFromRepository(true);
		}
		
		return entities;
	}

	private <T> void executeAfterGetProcedure(GGAPIDynamicDomain domain, IGGAPICaller caller, Map<String, String> customParameters, T entity, IGGAPIRepository repository) throws GGAPIEntityException {
		this.setEntityMethodsAndFields(repository, customParameters, domain, entity);
		try {
			GGAPIBusinessAnnotations.hasAnnotationAndInvoke(entity.getClass(), GGAPIEntityAfterGet.class, entity, caller, customParameters);
		} catch (GGAPIEntityException e) {
			log.warn("[domain ["+domain.domain+"]] "+caller.toString()+" Error during processing AfterGet Method on entity with uuid "+entity.getUuid());
			if(log.isDebugEnabled()) {
				log.warn("[domain ["+domain.domain+"]] "+caller.toString()+" The error :", e);
			}
			throw e;
		}
	}
	
	private <T> void setEntityMethodsAndFields(IGGAPIRepository repository, Map<String, String> customParameters, GGAPIDynamicDomain domain, T entity) throws GGAPIEntityException {
		entity.setRepository(repository);
		entity.setSaveMethod(new GGAPIEntitySaveMethod());
		entity.setDeleteMethod(new GGAPIEntityDeleteMethod());
		this.injectDependenciesAndValues(entity);
	}

	@Override
	public long countEntities(GGAPIDynamicDomain dynamicDomain, IGGAPICaller caller, GGAPILiteral filter, GGAPIGeolocFilter geoloc, Map<String, String> customParameters) {
		IGGAPIRepository repository = this.repositoriesRegistry.getRepository(dynamicDomain.domain);
		return repository.getCount(dynamicDomain, caller, filter, geoloc);
	}
	
	private void injectDependenciesAndValues(IGGAPIEntity target) throws GGAPIEntityException {
        Class<?> clazz = target.getClass();
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
                    throw new GGAPIEntityException(GGAPIEntityException.INTERNAL_ERROR, "Bean not found for field: " + field.getName());
                }

                try (GGAPIFieldAccessManager accessManager = new GGAPIFieldAccessManager(field)) {
					field.set(target, bean);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					if( log.isDebugEnabled() ) {
						log.warn("Field  "+field.getName()+" of entity of type "+target.getClass().getName()+" cannot be set", e);
					}
					throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, "Field  "+field.getName()+" of entity of type "+target.getClass().getName()+" cannot be set", e);
				}
            } else if (field.isAnnotationPresent(Value.class)) {
                String value = field.getAnnotation(Value.class).value();
                if (value.startsWith("${") && value.endsWith("}")) {
                    String propertyName = value.substring(2, value.length() - 1);
                    String propertyValue = this.environment.getProperty(propertyName);
                    if( propertyValue == null ) {
                    	throw new GGAPIEntityException(GGAPIEntityException.INTERNAL_ERROR, "Value not found: " + propertyName);
                    }
                    try (GGAPIFieldAccessManager accessManager = new GGAPIFieldAccessManager(field)) {
    					field.set(target, propertyValue);
    				} catch (IllegalArgumentException | IllegalAccessException e) {
    					if( log.isDebugEnabled() ) {
    						log.warn("Field  "+field.getName()+" of entity of type "+target.getClass().getName()+" cannot be set", e);
    					}
    					throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, "Field  "+field.getName()+" of entity of type "+target.getClass().getName()+" cannot be set", e);
    				}
                } else {
                	 throw new GGAPIEntityException(GGAPIEntityException.INTERNAL_ERROR, "Malformed value annotation: " + field.getName());
                }
            }
        }
    }

}
