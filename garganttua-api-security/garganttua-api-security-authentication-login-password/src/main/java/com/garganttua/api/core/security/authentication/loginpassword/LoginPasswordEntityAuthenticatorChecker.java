package com.garganttua.api.core.security.authentication.loginpassword;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.security.annotations.AuthenticatorLogin;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginPasswordEntityAuthenticatorChecker {

	private static Map<Class<?>, LoginPasswordAuthenticatorInfos> infos = new HashMap<Class<?>, LoginPasswordAuthenticatorInfos>();
	
	public static LoginPasswordAuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass) throws CoreException {
		
		if( LoginPasswordEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return LoginPasswordEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);  
		}
		
		String loginFieldName = null; 
		String passwordFieldName = null;
		
		try {
			loginFieldName= LoginPasswordEntityAuthenticatorChecker.checkLoginAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorLogin");
		}
		
		try {
			passwordFieldName = LoginPasswordEntityAuthenticatorChecker.checkPasswordAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorPassword");
		}
		
		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
			LoginPasswordAuthenticatorInfos authenticatorinfos = new LoginPasswordAuthenticatorInfos(q.address(loginFieldName), q.address(passwordFieldName));
			
			LoginPasswordEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			return authenticatorinfos;
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}
	
	private static String checkPasswordAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, AuthenticatorPassword.class, String.class);
			if(fieldAddress == null ) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorPassword");
			}
		} catch (GGReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorPassword", e);
		}
		return fieldAddress;
	}
	
	private static String checkLoginAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, AuthenticatorLogin.class, String.class);
			if(fieldAddress == null ) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorLogin");
			}
		} catch (GGReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "+entityAuthenticatorClass.getSimpleName()+" does not have any field annotated with @AuthenticatorLogin", e);
		}
		return fieldAddress; 
	}
}
