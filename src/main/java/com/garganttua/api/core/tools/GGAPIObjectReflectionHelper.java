package com.garganttua.api.core.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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

}
