package com.garganttua.api.core.entity.tools;

import java.lang.reflect.Field;

import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.checker.GGAPIEntityChecker.GGAPIEntityInfos;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntityDeleteMethod;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntitySaveMethod;
import com.garganttua.api.core.tools.GGAPIFieldAccessManager;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelper;
import com.garganttua.api.core.tools.GGAPIObjectReflectionHelperExcpetion;
import com.garganttua.api.repository.IGGAPIRepository;

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

	public static void setUuid(Object entity, String uuid) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectReflectionHelper.setObjectFieldValue(entity, infos.uuidFieldName(), uuid);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static void setId(Object entity, String id) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectReflectionHelper.setObjectFieldValue(entity, infos.idFieldName(), id);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static void setRepository(Object entity, IGGAPIRepository<Object> repository) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectReflectionHelper.setObjectFieldValue(entity, infos.repositoryFieldName(), repository);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static void setGotFromRepository(Object entity, boolean b) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectReflectionHelper.setObjectFieldValue(entity, infos.gotFromRepositoryFieldName(), b);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static void setSaveMethod(Object entity, IGGAPIEntitySaveMethod method) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectReflectionHelper.setObjectFieldValue(entity, infos.saveProviderFieldName(), method);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static void setDeleteMethod(Object entity, IGGAPIEntityDeleteMethod method) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			GGAPIObjectReflectionHelper.setObjectFieldValue(entity, infos.deleteProviderFieldName(), method);
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static String getUuid(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (String) GGAPIObjectReflectionHelper.getObjectFieldValue(entity, infos.uuidFieldName());
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static String getId(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (String) GGAPIObjectReflectionHelper.getObjectFieldValue(entity, infos.idFieldName());
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}

	@SuppressWarnings("unchecked")
	public static IGGAPIRepository<Object> getRepository(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIRepository<Object>) GGAPIObjectReflectionHelper.getObjectFieldValue(entity, infos.repositoryFieldName());
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}
	
	public static boolean isGotFromRepository(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (boolean) GGAPIObjectReflectionHelper.getObjectFieldValue(entity, infos.gotFromRepositoryFieldName());
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static IGGAPIEntitySaveMethod getSaveMethodProvider(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIEntitySaveMethod) GGAPIObjectReflectionHelper.getObjectFieldValue(entity, infos.saveProviderFieldName());
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}

	public static IGGAPIEntityDeleteMethod getDeleteMethodProvider(Object entity) throws GGAPIEntityException {
		GGAPIEntityInfos infos = GGAPIEntityChecker.checkEntity(entity);
		try {
			return (IGGAPIEntityDeleteMethod) GGAPIObjectReflectionHelper.getObjectFieldValue(entity, infos.deleteProviderFieldName());
		} catch (GGAPIObjectReflectionHelperExcpetion e) {
			throw new GGAPIEntityException(e);
		} 
	}
}
