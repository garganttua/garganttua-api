package com.garganttua.api.core.security.authentication.loginpassword;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.security.annotations.AuthenticatorLogin;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

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
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorLogin");
		}

		try {
			passwordFieldName = LoginPasswordEntityAuthenticatorChecker.checkPasswordAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorPassword");
		}

		IObjectQuery<?> q;
		try {
			q = ObjectQueryFactory.objectQuery(DefaultMapper.reflection().extractClass(entityAuthenticatorClass), new RuntimeReflectionProvider());
			LoginPasswordAuthenticatorInfos authenticatorinfos = new LoginPasswordAuthenticatorInfos(q.address(loginFieldName), q.address(passwordFieldName));

			LoginPasswordEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			return authenticatorinfos;
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	private static String findFieldAnnotatedWith(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotation) {
		for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(annotation) != null) {
				if (!String.class.isAssignableFrom(field.getType())) {
					return null;
				}
				return field.getName();
			}
		}
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
			return findFieldAnnotatedWith(clazz.getSuperclass(), annotation);
		}
		return null;
	}

	private static String checkPasswordAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws CoreException {
		String fieldName = findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorPassword.class);
		if (fieldName == null) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have any field annotated with @AuthenticatorPassword");
		}
		return fieldName;
	}

	private static String checkLoginAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws CoreException {
		String fieldName = findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorLogin.class);
		if (fieldName == null) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have any field annotated with @AuthenticatorLogin");
		}
		return fieldName;
	}
}
