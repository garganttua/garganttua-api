package com.garganttua.api.core.entity.tools;

import java.util.Map;

import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.entity.GGAPIEntityInfos;
import com.garganttua.api.spec.entity.IGGAPIEntityDeleteMethod;
import com.garganttua.api.spec.entity.IGGAPIEntitySaveMethod;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.filter.GGAPILiteral;
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
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return GGObjectQueryFactory.objectQuery(entity).invoke(infos.saveMethodAddress(), caller, parameters);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static void delete(Object entity, IGGAPICaller caller, Map<String, String> parameters) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).invoke(infos.deleteMethodAddress(), caller, parameters);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
	}

	public static void setUuid(Object entity, String uuid) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).setValue(infos.uuidFieldAddress(), uuid);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
	}
	
	public static void setId(Object entity, String id) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).setValue(infos.idFieldAddress(), id);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
	}

	public static void setRepository(Object entity, IGGAPIRepository<Object> repository) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).setValue(infos.repositoryFieldAddress(), repository);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
	}
	
	public static void setEngine(Object entity, IGGAPIEngine engine) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).setValue(infos.engineFieldAddress(), engine);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
	}
	
	public static void setGotFromRepository(Object entity, boolean b) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).setValue(infos.gotFromRepositoryFieldAddress(), b);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
	}

	public static void setSaveMethod(Object entity, IGGAPIEntitySaveMethod<?> method) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).setValue(infos.saveProviderFieldAddress(), method);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
	}

	public static void setDeleteMethod(Object entity, IGGAPIEntityDeleteMethod<?> method) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).setValue(infos.deleteProviderFieldAddress(), method);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
	}
	
	public static String getUuid(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (String) GGObjectQueryFactory.objectQuery(entity).getValue(infos.uuidFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
		// Should never be reached
		return null; 
	}
	
	public static String getId(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (String) GGObjectQueryFactory.objectQuery(entity).getValue( infos.idFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
		// Should never be reached
		return null; 
	}

	@SuppressWarnings("unchecked")
	public static IGGAPIRepository<Object> getRepository(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIRepository<Object>) GGObjectQueryFactory.objectQuery(entity).getValue(infos.repositoryFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
		// Should never be reached
		return null; 
	}
	
	public static boolean isGotFromRepository(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (boolean) GGObjectQueryFactory.objectQuery(entity).getValue(infos.gotFromRepositoryFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
		// Should never be reached
		return false; 
	}

	public static IGGAPIEntitySaveMethod<?> getSaveMethodProvider(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIEntitySaveMethod<?>) GGObjectQueryFactory.objectQuery(entity).getValue(infos.saveProviderFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
		// Should never be reached
		return null; 
	}

	public static IGGAPIEntityDeleteMethod<?> getDeleteMethodProvider(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIEntityDeleteMethod<?>) GGObjectQueryFactory.objectQuery(entity).getValue(infos.deleteProviderFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
		// Should never be reached
		return null; 
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
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (String) GGObjectQueryFactory.objectQuery(entity).getValue(infos.tenantIdFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
		// Should never be reached
		return null; 
	}

	public static void setTenantId(Object entity, String tenantId) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGObjectQueryFactory.objectQuery(entity).setValue(infos.tenantIdFieldAddress(), tenantId);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		} 
	}

	public static String getDomainName(Object entity) throws GGAPIException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		return infos.domain(); 
	}
}
