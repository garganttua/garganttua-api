package com.garganttua.api.core.legacy.entity.tools;

import java.util.Map;

import com.garganttua.api.core.context.InfosHelper;
import com.garganttua.api.core.legacy.engine.EngineException;
import com.garganttua.api.core.legacy.entity.checker.EntityChecker;
import com.garganttua.api.core.filter.Literal;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IEngine;
import com.garganttua.api.spec.entity.EntityInfos;
import com.garganttua.api.spec.entity.IEntityDeleteMethod;
import com.garganttua.api.spec.entity.IEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityHelper {
	
	public static String getDomain(Class<?> entity) {
		String domain;
		try {
			domain = entity.getAnnotation(Entity.class).domain();
		} catch(Exception e) {
			domain = entity.getSimpleName();
		}
		
		return domain;
	}
	
	public static Object save(Object entity, ICaller caller, Map<String, String> parameters) throws CoreException {
		return InfosHelper.invoke(entity, EntityChecker::checkEntityClass, EntityInfos::saveMethodAddress, caller, parameters);
	}

	public static void delete(Object entity, ICaller caller, Map<String, String> parameters) throws CoreException {
		InfosHelper.invoke(entity, EntityChecker::checkEntityClass, EntityInfos::deleteMethodAddress, caller, parameters);
	}

	public static void setUuid(Object entity, String uuid) throws CoreException {
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::uuidFieldAddress, uuid);
	}
	
	public static void setId(Object entity, String id) throws CoreException {
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::idFieldAddress, id);
	}

	public static void setRepository(Object entity, IRepository repository) throws CoreException {
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::repositoryFieldAddress, repository);
	}
	
	public static void setEngine(Object entity, IEngine engine) throws CoreException {
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::engineFieldAddress, engine);
	}
	
	public static void setGotFromRepository(Object entity, boolean b) throws CoreException {
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::gotFromRepositoryFieldAddress, b);
	}

	public static void setSaveMethod(Object entity, IEntitySaveMethod method) throws CoreException {
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::saveProviderFieldAddress, method);
	}

	public static void setDeleteMethod(Object entity, IEntityDeleteMethod method) throws CoreException {
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::deleteProviderFieldAddress, method);
	}
	
	public static String getUuid(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityChecker::checkEntityClass, EntityInfos::uuidFieldAddress);
	}
	
	public static String getId(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityChecker::checkEntityClass, EntityInfos::idFieldAddress);
	}

	public static IRepository getRepository(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityChecker::checkEntityClass, EntityInfos::repositoryFieldAddress);
	}
	
	public static boolean isGotFromRepository(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityChecker::checkEntityClass, EntityInfos::gotFromRepositoryFieldAddress);
	}

	public static IEntitySaveMethod getSaveMethodProvider(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityChecker::checkEntityClass, EntityInfos::saveProviderFieldAddress);
	}

	public static IEntityDeleteMethod getDeleteMethodProvider(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityChecker::checkEntityClass, EntityInfos::deleteProviderFieldAddress);
	}

	public static Object newInstance(Class<?> entityClass) throws CoreException {
		try {
			return GGObjectReflectionHelper.instanciateNewObject(entityClass);
		} catch (ReflectionException e) {
			CoreException.processException(e);
		}
		// Should never be reached
		return null; 
	}

	public static Object newExampleInstance(Class<?> clazz, IFilter filter) throws CoreException {
		Object object = newInstance(clazz);	
		EntityHelper.setObjectValuesFromFilter(object, filter);
		return object;
	}

	private static void setObjectValuesFromFilter(Object object, IFilter filter) throws CoreException {
		if( filter == null ) {
			return;
		}
		if( filter.getName().equals(Literal.OPERATOR_FIELD) ) {
			String fieldAddress = (String) filter.getValue();
			Object fieldValue = filter.getLiterals().get(0).getValue();
			
			try {
				ObjectQueryFactory.objectQuery(object).setValue(fieldAddress, fieldValue);
			} catch (ReflectionException e) {
				if( log.isDebugEnabled() ) {
					log.warn("Unable to set value "+fieldValue+" to object "+object+" with address "+fieldAddress, e);
				}
			}
		} else {
			for( IFilter sub: filter.getLiterals() ) {
				EntityHelper.setObjectValuesFromFilter(object, sub);
			}
		}
	}

	public static String getTenantId(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityChecker::checkEntityClass, EntityInfos::tenantIdFieldAddress);
	}

	public static void setTenantId(Object entity, String tenantId) throws CoreException {
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::tenantIdFieldAddress, tenantId);
	}

	public static String getDomainName(Object entity) throws CoreException {
		EntityInfos infos = EntityChecker.checkEntity(entity);
		return infos.domain(); 
	}
	
	public static void setOwnerId(Object entity, String ownerId) throws CoreException {
		EntityInfos infos = EntityChecker.checkEntity(entity);
		
		if( !infos.ownedEntity() ) {
			throw new EngineException(CoreExceptionCode.ENTITY_DEFINITION, "Entity of type "+entity.getClass().getSimpleName()+" is not owned");
		}
		
		InfosHelper.setValue(entity, EntityChecker::checkEntityClass, EntityInfos::ownerIdFieldAddress, ownerId);
	}

	public static String getOwnerId(Object entity) throws CoreException {
		EntityInfos infos = EntityChecker.checkEntity(entity);
		
		if( !infos.ownerEntity() ) {
			throw new EngineException(CoreExceptionCode.ENTITY_DEFINITION, "Entity of type "+entity.getClass().getSimpleName()+" is not owner");
		}
		
		return infos.domain()+":"+getUuid(entity);
	}
	
	public static String getUuidFromOwnerId(String ownerId) throws CoreException {
		String[] parts = ownerId.split(":");
		
		if( parts.length != 2 ) {
			throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, "Invalid ownerId, should be domain:uuid");
		}
		
		return parts[1];
	}

	public static String getDomainNameFromOwnerId(String ownerId) throws CoreException {
		String[] parts = ownerId.split(":");
		
		if( parts.length != 2 ) {
			throw new EngineException(CoreExceptionCode.UNKNOWN_ERROR, "Invalid ownerId, should be domain:uuid");
		}
		
		return parts[0];
	}
}
