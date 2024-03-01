package com.garganttua.api.core.entity.annotations;

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

import com.garganttua.api.core.IGGAPICaller;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.tools.GGAPIMethodAccessManager;

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
		
	public static Method hasAnnotation(Class<?> type, Class<?> searchAnnotation) throws GGAPIEntityException {
		for( Method method: type.getDeclaredMethods()) {	
			for( Annotation annotation: method.getAnnotations()) {
				if( annotation.annotationType().equals(searchAnnotation) ) {
					
					Type[] parameters = method.getGenericParameterTypes();
					if( parameters.length != 2 ) {
						throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}
					
					if( !parameters[0].equals(IGGAPICaller.class) ) {
						throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}

					if( !isMapOfString(parameters[1])) {
						throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
					}
					return method;
				}
			}
		}
		if( type.getSuperclass() != null ) {
			return GGAPIBusinessAnnotations.hasAnnotation(type.getSuperclass(), searchAnnotation);
		}
		return null;		
	}
	
	static private boolean isMapOfString(Type type) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type[] typeArguments = parameterizedType.getActualTypeArguments();
			return parameterizedType.getRawType() == Map.class && typeArguments.length == 2
					&& typeArguments[0] == String.class && typeArguments[1] == String.class;
		}
		return false;
	}

	public static void hasAnnotationAndInvoke(Class<?> type, Class<?> searchAnnotation, Object entity, IGGAPICaller caller, Map<String, String> map) throws GGAPIEntityException {
		Method method = hasAnnotation(type, searchAnnotation);
		
		if( method != null ) {
			try (GGAPIMethodAccessManager accessManager = new GGAPIMethodAccessManager(method)) {
				method.invoke(entity, caller, map);
			} catch (IllegalAccessException | InvocationTargetException e) {
				if( log.isDebugEnabled() ) {
					log.warn("Unable to run the method "+method.getName()+" of entity of type "+type.getName(), e);
				}
				throw new GGAPIEntityException(GGAPIEntityException.ENTITY_DEFINITION_ERROR, "Unable to run the method "+method.getName()+" of entity of type "+type.getName(), e);
			} 
		}
	}

}

