package com.garganttua.api.core.security.authentication.loginpassword;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorLogin;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorPassword;
import com.garganttua.api.spec.security.authentication.GGAPILoginPasswordAuthenticatorInfos;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPILoginPasswordEntityAuthenticatorChecker {

	private static Map<Class<?>, GGAPILoginPasswordAuthenticatorInfos> infos = new HashMap<Class<?>, GGAPILoginPasswordAuthenticatorInfos>();
	
	public static GGAPILoginPasswordAuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass) throws GGAPIException {
		
		if( GGAPILoginPasswordEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return GGAPILoginPasswordEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);  
		}
		
		String loginFieldName = null; 
		String passwordFieldName = null;
		
		try {
			loginFieldName= GGAPILoginPasswordEntityAuthenticatorChecker.checkLoginAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @GGAPIAuthenticatorLogin");
		}
		
		try {
			passwordFieldName = GGAPILoginPasswordEntityAuthenticatorChecker.checkPasswordAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @GGAPIAuthenticatorPassword");
		}
		
		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
			GGAPILoginPasswordAuthenticatorInfos authenticatorinfos = new GGAPILoginPasswordAuthenticatorInfos(q.address(loginFieldName), q.address(passwordFieldName));
			
			GGAPILoginPasswordEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			return authenticatorinfos;
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
		}
	}
	
	private static String checkPasswordAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorPassword.class, String.class);
			if(fieldAddress == null ) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorPassword");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorPassword", e);
		}
		return fieldAddress;
	}
	
	private static String checkLoginAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorLogin.class, String.class);
			if(fieldAddress == null ) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorLogin");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorLogin", e);
		}
		return fieldAddress; 
	}
}
