package com.garganttua.api.core.legacy.entity.checker;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.spec.entity.EntityDocumentationInfos;
import com.garganttua.api.spec.entity.annotations.EntityDocumentation;

public class EntityDocumentationChecker {

	private static Map<Class<?>, EntityDocumentationInfos> infos = new HashMap<Class<?>, EntityDocumentationInfos>();
	
	public static EntityDocumentationInfos checkEntity(Object entity) {
		return EntityDocumentationChecker.checkEntityClass(entity.getClass());
	}

	public static EntityDocumentationInfos checkEntityClass(Class<?> entityClass) {
		if (EntityDocumentationChecker.infos.containsKey(entityClass)) {
			return EntityDocumentationChecker.infos.get(entityClass);
		}
		
		EntityDocumentation annotation = entityClass.getDeclaredAnnotation(EntityDocumentation.class);
		
		if( annotation == null ) {
			return null;
		}
		
		return new EntityDocumentationInfos(
				annotation.general(), 
				annotation.readAll(), 
				annotation.readOne(), 
				annotation.createOne(), 
				annotation.updateOne(), 
				annotation.deleteOne(), 
				annotation.deleteAll());
		
	}
}
