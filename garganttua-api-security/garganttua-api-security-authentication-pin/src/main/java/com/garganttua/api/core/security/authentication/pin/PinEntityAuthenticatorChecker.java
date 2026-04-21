package com.garganttua.api.core.security.authentication.pin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.commons.CoreExceptionCode;
import com.garganttua.api.commons.security.annotations.AuthenticatorLogin;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;

public class PinEntityAuthenticatorChecker {

	private static Map<Class<?>, PinAuthenticatorInfos> infos = new HashMap<Class<?>, PinAuthenticatorInfos>();

	public static PinAuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass) throws CoreException {
		if( PinEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return PinEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);
		}

		String loginFieldName = findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorLogin.class);
		if (loginFieldName == null) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorLogin");
		}

		String pinFieldName = findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorPin.class);
		if (pinFieldName == null) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorPin");
		}

		String pinErrorCounterFieldName = findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorPinErrorCounter.class);
		if (pinErrorCounterFieldName == null) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorPinErrorCounter");
		}

		AuthenticatorPin pinAnnotation = (AuthenticatorPin) getAnnotation(AuthenticatorPin.class, entityAuthenticatorClass);
		AuthenticatorPinErrorCounter pinErrorCounterAnnotation = (AuthenticatorPinErrorCounter) getAnnotation(AuthenticatorPinErrorCounter.class, entityAuthenticatorClass);

		IObjectQuery<?> q;
		try {
			q = ObjectQueryFactory.objectQuery(DefaultMapper.reflection().extractClass(entityAuthenticatorClass), new RuntimeReflectionProvider());
			PinAuthenticatorInfos authenticatorinfos = new PinAuthenticatorInfos(q.address(loginFieldName), q.address(pinFieldName), q.address(pinErrorCounterFieldName), pinAnnotation.size(), pinErrorCounterAnnotation.maxErrorNumber());

			PinEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);

			return authenticatorinfos;
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	private static String findFieldAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(annotation) != null) {
				return field.getName();
			}
		}
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
			return findFieldAnnotatedWith(clazz.getSuperclass(), annotation);
		}
		return null;
	}

	private static Annotation getAnnotation(Class<? extends Annotation> annotation,
			Class<?> entityAuthenticatorClass) {
		for( Field field: entityAuthenticatorClass.getDeclaredFields() ) {
			if( field.getAnnotation(annotation) !=null ) {
				return field.getAnnotation(annotation);
			}
		}
		return null;
	}

}
