package com.garganttua.api.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class GGAPIEntityHelper {
	
	public static <T extends IGGAPIEntity> T getOneInstance(Class<T> clazz) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<T> constructor;
		constructor = (Constructor<T>) clazz.getConstructor();
		T entity = (T) constructor.newInstance();
		return entity;
	}
	
	public static <T extends IGGAPIEntity> String getDomain(Class<T> entity) {
		
		String domain;
		try {
			domain = entity.getAnnotation(GGAPIEntity.class).domain();
		} catch(Exception e) {
			domain = entity.getSimpleName();
		}
		
		return domain;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends IGGAPIEntity> IGGAPIEntityFactory<T> getFactory(Class<T> clazz) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return (IGGAPIEntityFactory<T>) GGAPIEntityHelper.getOneInstance(clazz).getFactory();
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
