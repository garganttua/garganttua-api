package com.garganttua.api.security.core.entity.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonLocked;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorCredentialsNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorEnabled;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityAuthenticatorChecker {
	
	private static Map<Class<?>, GGAPIAuthenticatorInfos> infos = new HashMap<Class<?>, GGAPIAuthenticatorInfos>();
	
	public static GGAPIAuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass) throws GGAPIException {
		if( GGAPIEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return GGAPIEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);  
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Checking entity authenticator infos from class " + entityAuthenticatorClass.getName());
		}
		
		GGAPIAuthenticator annotation = entityAuthenticatorClass.getDeclaredAnnotation(GGAPIAuthenticator.class); 
		
		if( annotation == null ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " is not annotated with @GGAPIAuthenticator");
		}
		
		String accountNonExpiredFieldName = GGAPIEntityAuthenticatorChecker.checkAccountNonExpiredAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String accountNonLockedFieldName = GGAPIEntityAuthenticatorChecker.checkAccountNonLockedAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String credentialsNonExpiredFieldName = GGAPIEntityAuthenticatorChecker.checkCredentialsNonLockedAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String enabledFieldName = GGAPIEntityAuthenticatorChecker.checkEnabledAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String autoritiesFieldName = GGAPIEntityAuthenticatorChecker.checkAuthoritiesAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		
		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
			
			GGAPIAuthenticatorInfos authenticatorinfos = new GGAPIAuthenticatorInfos(
					q.address(autoritiesFieldName), 
					q.address(accountNonExpiredFieldName), 
					q.address(accountNonLockedFieldName), 
					q.address(credentialsNonExpiredFieldName), 
					q.address(enabledFieldName));
			
			GGAPIEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);

			return authenticatorinfos;
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(e);
		}
	}
	
	private static String checkAuthoritiesAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorAuthorities.class, List.class);
		if( fieldAddress == null ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorAutorities");
		}
		return fieldAddress; 
	}
	
	private static String checkEnabledAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorEnabled.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorEnabled");
		}
		return fieldAddress; 
	}
	
	private static String checkCredentialsNonLockedAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorCredentialsNonExpired.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorCredentialsNonExpired");
		}
		return fieldAddress; 
	}
	
	private static String checkAccountNonLockedAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorAccountNonLocked.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorAccountNonLocked");
		}
		return fieldAddress; 
	}
	
	private static String checkAccountNonExpiredAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorAccountNonExpired.class, boolean.class);
		if( fieldAddress == null ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorAccountNonExpired");
		}
		return fieldAddress; 
	}

	public static String getFieldAddressAnnotatedWithAndCheckType(Class<?> entityAuthenticatorClass, Class<? extends Annotation> annotationClass, Class<?> fieldClass) throws GGAPIException {
		String fieldAddress = null;
		for( Field field: entityAuthenticatorClass.getDeclaredFields() ) {
			if( field.isAnnotationPresent(annotationClass) ) {
				if( fieldAddress != null && !fieldAddress.isEmpty() ) {
					throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" has more than one field annotated with "+annotationClass);
				}
				if( field.getType().equals(fieldClass) ) {
					fieldAddress = field.getName();
					break;
				} else {
					throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" has field "+field.getName()+" with wrong type "+field.getType().getName()+", should be "+fieldClass);
				}
			} else {
				if( isNotPrimitiveOrInternal(field.getType()) && !entityAuthenticatorClass.equals(field.getType())) {
					fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(field.getType(), annotationClass, fieldClass);
					if( fieldAddress != null )
						break;
				}
			}
		}

		if( entityAuthenticatorClass.getSuperclass() != null && fieldAddress == null ) {
			return GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass.getSuperclass(), annotationClass, fieldClass);
		} else {
			return fieldAddress;
		}
	}
	
	public static boolean isNotPrimitiveOrInternal(Class<?> clazz) {
		if (clazz.isPrimitive()) {
			return false;
		}

		Package package1 = clazz.getPackage();
		if( package1 == null ) {
			return false;
		}
		
		String packageName = package1.getName();
		if (packageName.startsWith("java.") || packageName.startsWith("javax.")) {
			return false;
		}

		return true;
	}

	public static GGAPIAuthenticatorInfos checkEntityAuthenticator(Object entity) throws GGAPIException {
		return checkEntityAuthenticatorClass(entity.getClass());
	}
}
