package com.garganttua.api.core.entity.tools;

import java.util.Map;
import java.util.Optional;

import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker.GGAPIEntityInfos;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryException;
import com.garganttua.api.core.objects.query.GGAPIObjectQueryFactory;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.objects.utils.GGAPIObjectReflectionHelperExcpetion;
import com.garganttua.api.spec.IGGAPICaller;
import com.garganttua.api.spec.dao.GGAPILiteral;
import com.garganttua.api.spec.entity.annotations.GGAPIEntity;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPISecurity;

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
	
	public static void save(Object entity, IGGAPICaller caller, Map<String, String> parameters, Optional<IGGAPISecurity> security) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectQueryFactory.objectQuery(entity).invoke(infos.saveMethodAddress(), caller, parameters, security);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		}
	}
	
	
	public static void delete(Object entity, IGGAPICaller caller, Map<String, String> parameters) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectQueryFactory.objectQuery(entity).invoke(infos.deleteMethodAddress(), caller, parameters);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		}
	}

	public static void setUuid(Object entity, String uuid) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectQueryFactory.objectQuery(entity).setValue(infos.uuidFieldAddress(), uuid);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static void setId(Object entity, String id) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectQueryFactory.objectQuery(entity).setValue(infos.idFieldAddress(), id);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static void setRepository(Object entity, IGGAPIRepository<Object> repository) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectQueryFactory.objectQuery(entity).setValue(infos.repositoryFieldAddress(), repository);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static void setGotFromRepository(Object entity, boolean b) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectQueryFactory.objectQuery(entity).setValue(infos.gotFromRepositoryFieldAddress(), b);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static void setSaveMethod(Object entity, IGGAPIEntitySaveMethod<?> method) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectQueryFactory.objectQuery(entity).setValue(infos.saveProviderFieldAddress(), method);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static void setDeleteMethod(Object entity, IGGAPIEntityDeleteMethod<?> method) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectQueryFactory.objectQuery(entity).setValue(infos.deleteProviderFieldAddress(), method);
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static String getUuid(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (String) GGAPIObjectQueryFactory.objectQuery(entity).getValue(infos.uuidFieldAddress());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static String getId(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (String) GGAPIObjectQueryFactory.objectQuery(entity).getValue( infos.idFieldAddress());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}

	@SuppressWarnings("unchecked")
	public static IGGAPIRepository<Object> getRepository(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIRepository<Object>) GGAPIObjectQueryFactory.objectQuery(entity).getValue(infos.repositoryFieldAddress());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static boolean isGotFromRepository(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (boolean) GGAPIObjectQueryFactory.objectQuery(entity).getValue(infos.gotFromRepositoryFieldAddress());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static IGGAPIEntitySaveMethod<?> getSaveMethodProvider(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIEntitySaveMethod<?>) GGAPIObjectQueryFactory.objectQuery(entity).getValue(infos.saveProviderFieldAddress());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static IGGAPIEntityDeleteMethod<?> getDeleteMethodProvider(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIEntityDeleteMethod<?>) GGAPIObjectQueryFactory.objectQuery(entity).getValue(infos.deleteProviderFieldAddress());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static Object newInstance(Class<?> entityClass) throws GGAPIEntityException {
		try {
			return GGAPIObjectReflectionHelper.instanciateNewObject(entityClass);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		}
	}

	public static Object newExampleInstance(Class<?> clazz, GGAPILiteral filter) throws GGAPIEntityException {
		Object object = newInstance(clazz);	
		GGAPIEntityHelper.setObjectValuesFromFilter(object, filter);
		return object;
	}

	private static void setObjectValuesFromFilter(Object object, GGAPILiteral filter) throws GGAPIEntityException {
		if( filter.getName().equals(GGAPILiteral.OPERATOR_FIELD) ) {
			String fieldAddress = (String) filter.getValue();
			Object fieldValue = filter.getLiterals().get(0).getValue();
			
			try {
				GGAPIObjectQueryFactory.objectQuery(object).setValue(fieldAddress, fieldValue);
			} catch (GGAPIObjectQueryException e) {
				if( log.isDebugEnabled() ) {
					log.warn("Unable to set value "+fieldValue+" to object "+object+" with address "+fieldAddress, e);
				}
			}
		} else {
			for( GGAPILiteral sub: filter.getLiterals() ) {
				GGAPIEntityHelper.setObjectValuesFromFilter(object, sub);
			}
		}
	}

	public static String getTenantId(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (String) GGAPIObjectQueryFactory.objectQuery(entity).getValue(infos.tenantIdFieldAddress());
		} catch (GGAPIObjectQueryException e) {
			throw new GGAPIEntityException(e);
		}
	}
}
