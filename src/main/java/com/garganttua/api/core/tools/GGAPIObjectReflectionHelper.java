package com.garganttua.api.core.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GGAPIObjectReflectionHelper {
	
	public static Constructor<?> getConstructorWithNoParams(Class<?> classs){
		try {
			return classs.getDeclaredConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			return null;
		}		
	}
	
	public static Field getField(Class<?> objectClass, String fieldName) {
		for( Field f: objectClass.getDeclaredFields() ) {
			if( f.getName().equals(fieldName) ) {
				return f;
			}
		}
		if( objectClass.getSuperclass() != null ) {
			return getField(objectClass.getSuperclass(), fieldName);
		}
		return null;
	}

	public static Method getMethod(Class<?> objectClass, String methodName) {
		for( Method f: objectClass.getDeclaredMethods() ) {
			if( f.getName().equals(methodName) ) {
				return f;
			}
		}
		if( objectClass.getSuperclass() != null ) {
			return getMethod(objectClass.getSuperclass(), methodName);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <destination> destination instanciateNewObject(Class<?> clazz) throws GGAPIObjectReflectionHelperExcpetion {
		Constructor<?> ctor = GGAPIObjectReflectionHelper.getConstructorWithNoParams(clazz);
		if( ctor != null ) {
			try( GGAPIConstructorAccessManager accessor = new GGAPIConstructorAccessManager(ctor) ){
				return (destination) ctor.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new GGAPIObjectReflectionHelperExcpetion(e);
			}
		}
		throw new GGAPIObjectReflectionHelperExcpetion("Class "+clazz+" does not have constructor with no params");
	}
	
	static public void setObjectFieldValue(Object entity, String fieldName, Object value) throws GGAPIObjectReflectionHelperExcpetion {
		Field field = GGAPIObjectReflectionHelper.getField(entity.getClass(), fieldName);
		
		if( field == null ) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot set field "+fieldName+" of object "+entity.getClass().getName()+" with value "+value);
		}
		
		try( GGAPIFieldAccessManager manager = new GGAPIFieldAccessManager(field) ){
			field.set(entity, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot set field "+fieldName+" of object "+entity.getClass().getName()+" with value "+value, e);
		}
	}
	
	public static Object getObjectFieldValue(Object entity, String fieldName) throws GGAPIObjectReflectionHelperExcpetion {
		Field field = GGAPIObjectReflectionHelper.getField(entity.getClass(), fieldName);
		if( field == null ) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot get field "+fieldName+" of object "+entity.getClass().getName());
		}
		
		try( GGAPIFieldAccessManager manager = new GGAPIFieldAccessManager(field) ){
			return field.get(entity);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot get field "+fieldName+" of object "+entity.getClass().getName(), e);
		}
	}

	public static Object invokeMethod(Object entity, String methodName, Object ...args) throws GGAPIObjectReflectionHelperExcpetion {
		Method method = GGAPIObjectReflectionHelper.getMethod(entity.getClass(), methodName);
		
		if( method == null ) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot get method "+methodName+" of object "+entity.getClass().getName());
		}
		
		try( GGAPIMethodAccessManager manager = new GGAPIMethodAccessManager(method) ){
			return method.invoke(entity, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot invoke method "+methodName+" of object "+entity.getClass().getName(), e);
		} 
	}

}
