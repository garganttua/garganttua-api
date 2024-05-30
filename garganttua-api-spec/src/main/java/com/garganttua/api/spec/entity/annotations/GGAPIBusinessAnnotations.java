package com.garganttua.api.spec.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
		
//	public static Method hasAnnotation(Class<?> type, Class<?> searchAnnotation) throws GGAPIException {
//		for( Method method: type.getDeclaredMethods()) {	
//			for( Annotation annotation: method.getAnnotations()) {
//				if( annotation.annotationType().equals(searchAnnotation) ) {
//					
//					Type[] parameters = method.getGenericParameterTypes();
//					if( parameters.length != 2 ) {
//						throw new GGAPIException(GGAPIExceptionCode.ENTITY_DEFINITION, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
//					}
//					if( !parameters[0].equals(IGGAPICaller.class) ) {
//						throw new GGAPIException(GGAPIExceptionCode.ENTITY_DEFINITION, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
//					}
//					if( !isMapOfString(parameters[1])) {
//						throw new GGAPIException(GGAPIExceptionCode.ENTITY_DEFINITION, "The method "+method.getName()+" of entity of type "+type.getName()+" must have two parameters (IGGAPICaller, Map<String,String>");
//					}
//					return method;
//				}
//			}
//		}
//		if( type.getSuperclass() != null ) {
//			return GGAPIBusinessAnnotations.hasAnnotation(type.getSuperclass(), searchAnnotation);
//		}
//		return null;		
//	}
//	
//	static private boolean isMapOfString(Type type) {
//		if (type instanceof ParameterizedType) {
//			ParameterizedType parameterizedType = (ParameterizedType) type;
//			Type[] typeArguments = parameterizedType.getActualTypeArguments();
//			return parameterizedType.getRawType() == Map.class && typeArguments.length == 2
//					&& typeArguments[0] == String.class && typeArguments[1] == String.class;
//		}
//		return false;
//	}
}

