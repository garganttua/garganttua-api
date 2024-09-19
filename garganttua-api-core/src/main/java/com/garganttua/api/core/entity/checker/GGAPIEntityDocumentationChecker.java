package com.garganttua.api.core.entity.checker;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.spec.entity.GGAPIEntityDocumentationInfos;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityDocumentation;

public class GGAPIEntityDocumentationChecker {

	private static Map<Class<?>, GGAPIEntityDocumentationInfos> infos = new HashMap<Class<?>, GGAPIEntityDocumentationInfos>();
	
	public static GGAPIEntityDocumentationInfos checkEntity(Object entity) {
		return GGAPIEntityDocumentationChecker.checkEntityClass(entity.getClass());
	}

	public static GGAPIEntityDocumentationInfos checkEntityClass(Class<?> entityClass) {
		if (GGAPIEntityDocumentationChecker.infos.containsKey(entityClass)) {
			return GGAPIEntityDocumentationChecker.infos.get(entityClass);
		}
		
		GGAPIEntityDocumentation annotation = entityClass.getDeclaredAnnotation(GGAPIEntityDocumentation.class);
		
		if( annotation == null ) {
			return null;
		}
		
		return new GGAPIEntityDocumentationInfos(
				annotation.general(), 
				annotation.readAll(), 
				annotation.readOne(), 
				annotation.createOne(), 
				annotation.updateOne(), 
				annotation.deleteOne(), 
				annotation.deleteAll());
		
	}
}
