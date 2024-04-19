package com.garganttua.api.core.objects.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

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
			return GGAPIObjectReflectionHelper.getField(objectClass.getSuperclass(), fieldName);
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
		throw new GGAPIObjectReflectionHelperExcpetion("Class "+clazz.getSimpleName()+" does not have constructor with no params");
	}
	
	static public void setObjectFieldValue(Object entity, Field field, Object value) throws GGAPIObjectReflectionHelperExcpetion {
	
		if( field == null ) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot set null field of object "+entity.getClass().getName()+" with value "+value);
		}
		
		try( GGAPIFieldAccessManager manager = new GGAPIFieldAccessManager(field) ){
			field.set(entity, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot set field "+field.getName()+" of object "+entity.getClass().getName()+" with value "+value, e);
		}
	}
	
	public static Object getObjectFieldValue(Object entity, String fieldName) throws GGAPIObjectReflectionHelperExcpetion {
		Field field = GGAPIObjectReflectionHelper.getField(entity.getClass(), fieldName);
		if( field == null ) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot get field "+fieldName+" of object "+entity.getClass().getName());
		}
		
		return getObjectFieldValue(entity, fieldName, field);
	}

	public static Object getObjectFieldValue(Object entity, String fieldName, Field field)
			throws GGAPIObjectReflectionHelperExcpetion {
		try( GGAPIFieldAccessManager manager = new GGAPIFieldAccessManager(field) ){
			return field.get(entity);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot get field "+fieldName+" of object "+entity.getClass().getName(), e);
		}
	}

	public static Object invokeMethod(Object object, String methodName, Method method, Object ...args) throws GGAPIObjectReflectionHelperExcpetion {
		GGAPIObjectReflectionHelper.checkMethodAndParams(method, args);
		
		try( GGAPIMethodAccessManager manager = new GGAPIMethodAccessManager(method) ){
			return method.invoke(object, args);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new GGAPIObjectReflectionHelperExcpetion("Cannot invoke method "+methodName+" of object "+object.getClass().getName(), e);
		} 
	}
	
	public static void checkMethodAndParams(Method method, Object ...args) throws GGAPIObjectReflectionHelperExcpetion {
		if( method.getParameterCount() != args.length ) {
			throw new GGAPIObjectReflectionHelperExcpetion("Method "+method.getName()+" needs "+method.getParameterCount()+" "+method.getParameterTypes()+" but "+args.length+" have been provided : "+args);
		}
		
		Class<?>[] params = method.getParameterTypes();
		for( int i = 0; i < args.length; i++ ) {
			if( !params[i].isAssignableFrom(args[i].getClass()) ) {
				throw new GGAPIObjectReflectionHelperExcpetion("Method "+method.getName()+" needs parameter "+i+" to be of type "+params[i]+", not "+args[i].getClass());
			}
		}
	}

	public static <K, V> Map<K, V> newHashMapOf(Class<K> keyType, Class<V> valueType) {
		return new HashMap<K, V>();
	}

	public static <K> ArrayList<K> newArrayListOf(Class<K> type) {
		return new ArrayList<K>();
	}

	public static <K> HashSet<K> newHashSetOf(Class<K> type) {
		return new HashSet<K>();
	}

	public static <K> LinkedList<K> newLinkedlistOf(Class<K> type) {
		return new LinkedList<K>();
	}

	public static <K> Vector<K>  newVectorOf(Class<?> type) {
		return new Vector<K>();
	}

}
