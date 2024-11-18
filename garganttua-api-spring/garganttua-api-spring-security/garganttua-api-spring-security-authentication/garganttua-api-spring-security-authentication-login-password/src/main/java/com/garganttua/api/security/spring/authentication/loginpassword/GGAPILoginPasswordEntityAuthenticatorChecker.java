package com.garganttua.api.security.spring.authentication.loginpassword;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.security.core.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPILoginPasswordEntityAuthenticatorChecker {

	private static Map<Class<?>, GGAPILoginPasswordAuthenticatorInfos> infos = new HashMap<Class<?>, GGAPILoginPasswordAuthenticatorInfos>();
	
	public static GGAPILoginPasswordAuthenticatorInfos checkEntityAuthenticator(Class<?> entityAuthenticatorClass) throws GGAPIException {
		
		if( GGAPILoginPasswordEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return GGAPILoginPasswordEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);  
		}
		
		String loginFieldName = null; 
		String passwordFieldName = null;
		
		try {
			loginFieldName= GGAPILoginPasswordEntityAuthenticatorChecker.checkLoginAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @GGAPILogin");
		}
		
		try {
			passwordFieldName = GGAPILoginPasswordEntityAuthenticatorChecker.checkPasswordAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (GGAPIException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @GGAPIPassword");
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
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorPassword.class, String.class);
		if( fieldAddress == null ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorPassword");
		}
		return fieldAddress; 
	}
	
	private static String checkLoginAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress = GGAPIEntityAuthenticatorChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorLogin.class, String.class);
		if( fieldAddress == null ) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @GGAPIAuthenticatorLogin");
		}
		return fieldAddress; 
	}
}
