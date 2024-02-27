package com.garganttua.api.core;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GGAPIBusinessAnnotations {
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GGAPIEntityAfterGet{}; 
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GGAPIEntityBeforeCreate{};
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GGAPIEntityAfterCreate{};
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GGAPIEntityBeforeUpdate{};
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GGAPIEntityAfterUpdate{};
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GGAPIEntityBeforeDelete{};
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface GGAPIEntityAfterDelete{};
		
	public static Method hasAnnotation(Class<? extends IGGAPIEntity> type, Class<?> searchAnnotation) throws GGAPIEntityException {
		for( Method method: type.getDeclaredMethods()) {	
			for( Annotation annotation: method.getAnnotations()) {
				if( annotation.annotationType().equals(searchAnnotation) ) {
					
					Type[] parameters = method.getGenericParameterTypes();
					if( parameters.length != 2 ) {
						throw new GGAPIEntityException(GGAPIEntityException.INTERNAL_ERROR, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}
					
					if( !parameters[0].equals(IGGAPICaller.class) ) {
						throw new GGAPIEntityException(GGAPIEntityException.INTERNAL_ERROR, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}

					if( !isMapOfString(parameters[1])) {
						throw new GGAPIEntityException(GGAPIEntityException.INTERNAL_ERROR, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}
					return method;
				}
			}
		}
		return null;		
	}
	
	static private boolean isMapOfString(Type type) {
	        if (type instanceof ParameterizedType) {
	            ParameterizedType parameterizedType = (ParameterizedType) type;
	            Type[] typeArguments = parameterizedType.getActualTypeArguments();
	            return parameterizedType.getRawType() == Map.class &&
	                    typeArguments.length == 2 &&
	                    typeArguments[0] == String.class &&
	                    typeArguments[1] == String.class;
	        }
	        return false;
	    }

	public static void hasAnnotationAndInvoke(Class<? extends IGGAPIEntity> type, Class<?> searchAnnotation, IGGAPIEntity entity, IGGAPICaller caller, Map<String, String> map) throws GGAPIEntityException {
		Method method = hasAnnotation(type, searchAnnotation);
		
		if( method != null ) {
			try (GGAPIMethodAccessManager accessManager = new GGAPIMethodAccessManager(method)) {
				method.invoke(entity, caller, map);
			} catch (IllegalAccessException | InvocationTargetException e) {
				if( log.isDebugEnabled() ) {
					log.warn("Unable to run the method "+method.getName()+" of entity of type "+type.getName(), e);
				}
				throw new GGAPIEntityException(GGAPIEntityException.UNKNOWN_ERROR, "Unable to run the method "+method.getName()+" of entity of type "+type.getName(), e);
			} 
		}
	}

}

