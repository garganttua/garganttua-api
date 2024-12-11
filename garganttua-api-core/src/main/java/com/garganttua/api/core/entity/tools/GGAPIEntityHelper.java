package com.garganttua.api.core.entity.tools;

import java.util.Map;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.core.engine.GGAPIEngineException;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.api.spec.entity.IGGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.filter.IGGAPIFilter;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityHelper {
	
	public static String getDomain(Class<?> entity) {
		String domain;
		try {
			domain = entity.getAnnotation(GGAPIEntity.class).domain();
		} catch(Exception e) {
			domain = entity.getSimpleName();
		}
		
		return domain;
	}
	
	public static Object save(Object entity, IGGAPICaller caller, Map<String, String> parameters) throws GGAPIException {
		return GGAPIInfosHelper.invoke(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::saveMethodAddress, caller, parameters);
	}

	public static void delete(Object entity, IGGAPICaller caller, Map<String, String> parameters) throws GGAPIException {
		GGAPIInfosHelper.invoke(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::deleteMethodAddress, caller, parameters);
	}

	public static void setUuid(Object entity, String uuid) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::uuidFieldAddress, uuid);
	}
	
	public static void setId(Object entity, String id) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::idFieldAddress, id);
	}

	public static void setRepository(Object entity, IGGAPIRepository repository) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::repositoryFieldAddress, repository);
	}
	
	public static void setEngine(Object entity, IGGAPIEngine engine) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::engineFieldAddress, engine);
	}
	
	public static void setGotFromRepository(Object entity, boolean b) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::gotFromRepositoryFieldAddress, b);
	}

	public static void setSaveMethod(Object entity, IGGAPIEntitySaveMethod method) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::saveProviderFieldAddress, method);
	}

	public static void setDeleteMethod(Object entity, IGGAPIEntityDeleteMethod method) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::deleteProviderFieldAddress, method);
	}
	
	public static String getUuid(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::uuidFieldAddress);
	}
	
	public static String getId(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::idFieldAddress);
	}

	public static IGGAPIRepository getRepository(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::repositoryFieldAddress);
	}
	
	public static boolean isGotFromRepository(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::gotFromRepositoryFieldAddress);
	}

	public static IGGAPIEntitySaveMethod getSaveMethodProvider(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::saveProviderFieldAddress);
	}

	public static IGGAPIEntityDeleteMethod getDeleteMethodProvider(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::deleteProviderFieldAddress);
	}

	public static Object newInstance(Class<?> entityClass) throws GGAPIException {
		try {
			return GGObjectReflectionHelper.instanciateNewObject(entityClass);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
		// Should never be reached
		return null; 
	}

	public static Object newExampleInstance(Class<?> clazz, IGGAPIFilter filter) throws GGAPIException {
		Object object = newInstance(clazz);	
		GGAPIEntityHelper.setObjectValuesFromFilter(object, filter);
		return object;
	}

	private static void setObjectValuesFromFilter(Object object, IGGAPIFilter filter) throws GGAPIException {
		if( filter == null ) {
			return;
		}
		if( filter.getName().equals(GGAPILiteral.OPERATOR_FIELD) ) {
			String fieldAddress = (String) filter.getValue();
			Object fieldValue = filter.getLiterals().get(0).getValue();
			
			try {
				GGObjectQueryFactory.objectQuery(object).setValue(fieldAddress, fieldValue);
			} catch (GGReflectionException e) {
				if( log.isDebugEnabled() ) {
					log.warn("Unable to set value "+fieldValue+" to object "+object+" with address "+fieldAddress, e);
				}
			}
		} else {
			for( IGGAPIFilter sub: filter.getLiterals() ) {
				GGAPIEntityHelper.setObjectValuesFromFilter(object, sub);
			}
		}
	}

	public static String getTenantId(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::tenantIdFieldAddress);
	}

	public static void setTenantId(Object entity, String tenantId) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::tenantIdFieldAddress, tenantId);
	}

	public static String getDomainName(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		return infos.domain(); 
	}
	
	public static void setOwnerId(Object entity, String ownerId) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		
		if( !infos.ownedEntity() ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity of type "+entity.getClass().getSimpleName()+" is not owned");
		}
		
		GGAPIInfosHelper.setValue(entity, GGAPIEntityChecker::checkEntityClass, GGAPIEntityInfos::ownerIdFieldAddress, ownerId);
	}

	public static String getOwnerId(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		
		if( !infos.ownerEntity() ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity of type "+entity.getClass().getSimpleName()+" is not owner");
		}
		
		return infos.domain()+":"+getUuid(entity);
	}
	
	public static String getUuidFromOwnerId(String ownerId) throws GGAPIException {
		String[] parts = ownerId.split(":");
		
		if( parts.length != 2 ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.UNKNOWN_ERROR, "Invalid ownerId, should be domain:uuid");
		}
		
		return parts[1];
	}
}
