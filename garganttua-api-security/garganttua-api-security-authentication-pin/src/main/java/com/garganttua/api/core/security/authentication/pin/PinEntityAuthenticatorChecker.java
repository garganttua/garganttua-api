package com.garganttua.api.core.security.authentication.pin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.entity.checker.EntityChecker;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.annotations.AuthenticatorLogin;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

public class PinEntityAuthenticatorChecker {

	private static Map<Class<?>, PinAuthenticatorInfos> infos = new HashMap<Class<?>, PinAuthenticatorInfos>();
	
	public static PinAuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass) throws CoreException {
		if( PinEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return PinEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);  
		}
		
		String loginFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, AuthenticatorLogin.class, String.class, true);
		String pinFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, AuthenticatorPin.class, String.class, true);
		String pinErrorCounterFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, AuthenticatorPinErrorCounter.class, int.class, true);

		AuthenticatorPin pinAnnotation = (AuthenticatorPin) getAnnotation(AuthenticatorPin.class, entityAuthenticatorClass);
		AuthenticatorPinErrorCounter pinErrorCounterAnnotation = (AuthenticatorPinErrorCounter) getAnnotation(AuthenticatorPinErrorCounter.class, entityAuthenticatorClass);
		
		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
			PinAuthenticatorInfos authenticatorinfos = new PinAuthenticatorInfos(q.address(loginFieldName), q.address(pinFieldName), q.address(pinErrorCounterFieldName), pinAnnotation.size(), pinErrorCounterAnnotation.maxErrorNumber());
			
			PinEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			
			return authenticatorinfos;
		} catch (Exception e) {
			throw new SecurityException(e);
		}
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
