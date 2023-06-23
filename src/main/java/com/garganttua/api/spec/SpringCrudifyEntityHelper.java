package com.garganttua.api.spec;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SpringCrudifyEntityHelper {
	
	public static <T extends ISpringCrudifyEntity> T getOneInstance(Class<T> clazz) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Constructor<T> constructor;
		constructor = (Constructor<T>) clazz.getConstructor();
		T entity = (T) constructor.newInstance();
		return entity;
	}
	
	public static <T extends ISpringCrudifyEntity> String getDomain(Class<T> entity) {
		
		String domain;
		try {
			domain = entity.getAnnotation(SpringCrudifyEntity.class).domain();
		} catch(Exception e) {
			domain = entity.getSimpleName();
		}
		
		return domain;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ISpringCrudifyEntity> ISpringCrudifyEntityFactory<T> getFactory(Class<T> clazz) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		return (ISpringCrudifyEntityFactory<T>) SpringCrudifyEntityHelper.getOneInstance(clazz).getFactory();
	}

}
