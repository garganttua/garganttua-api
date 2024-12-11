package com.garganttua.api.core.security.authentication.pin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorLogin;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

public class GGAPIPinEntityAuthenticatorChecker {

	private static Map<Class<?>, GGAPIPinAuthenticatorInfos> infos = new HashMap<Class<?>, GGAPIPinAuthenticatorInfos>();
	
	public static GGAPIPinAuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass) throws GGAPIException {
		if( GGAPIPinEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return GGAPIPinEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);  
		}
		
		String loginFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorLogin.class, String.class, true);
		String pinFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorPin.class, String.class, true);
		String pinErrorCounterFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass, GGAPIAuthenticatorPinErrorCounter.class, int.class, true);

		GGAPIAuthenticatorPin pinAnnotation = (GGAPIAuthenticatorPin) getAnnotation(GGAPIAuthenticatorPin.class, entityAuthenticatorClass);
		GGAPIAuthenticatorPinErrorCounter pinErrorCounterAnnotation = (GGAPIAuthenticatorPinErrorCounter) getAnnotation(GGAPIAuthenticatorPinErrorCounter.class, entityAuthenticatorClass);
		
		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
			GGAPIPinAuthenticatorInfos authenticatorinfos = new GGAPIPinAuthenticatorInfos(q.address(loginFieldName), q.address(pinFieldName), q.address(pinErrorCounterFieldName), pinAnnotation.size(), pinErrorCounterAnnotation.maxErrorNumber());
			
			GGAPIPinEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			
			return authenticatorinfos;
		} catch (Exception e) {
			throw new GGAPISecurityException(e);
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
