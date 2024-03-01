package com.garganttua.api.core.entity.tools;

import java.lang.reflect.Field;

import com.garganttua.api.core.entity.AbstractGGAPIEntity;
import com.garganttua.api.core.entity.annotations.GGAPIEntity;
import com.garganttua.api.core.entity.interfaces.IGGAPIEntity;

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

	public static Object getFieldValue(Class<?> clazz, String fieldName, Object entity) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = null; 
		
		if( (fieldName.equals("id") || fieldName.equals("uuid") ) && (entity instanceof AbstractGGAPIEntity) ) {
			field = AbstractGGAPIEntity.class.getDeclaredField(fieldName);
		} 
		
		if( field == null ) {
			field = clazz.getDeclaredField(fieldName);
		}
		
		field.setAccessible(true);
		Object value = field.get(entity);
		
		field.setAccessible(false);

		return value;
	}
	
	public static void setFieldValue(Class<?> clazz, String fieldName, Object entity, Object fieldValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = null; 
		
		if( (fieldName.equals("id") || fieldName.equals("uuid") ) && (entity instanceof AbstractGGAPIEntity) ) {
			field = AbstractGGAPIEntity.class.getDeclaredField(fieldName);
		} 
		
		if( field == null ) {
			field = clazz.getDeclaredField(fieldName);
		}
		
		field.setAccessible(true);
		field.set(entity, fieldValue);
		
		field.setAccessible(false);
	}

}
