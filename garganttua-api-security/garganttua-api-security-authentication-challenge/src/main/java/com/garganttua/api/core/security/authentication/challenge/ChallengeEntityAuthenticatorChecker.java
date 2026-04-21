package com.garganttua.api.core.security.authentication.challenge;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.commons.CoreExceptionCode;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyRealm;
import com.garganttua.api.commons.security.key.KeyAlgorithm;
import com.garganttua.api.commons.security.key.IKeyRealm;

public class ChallengeEntityAuthenticatorChecker {

	private static final IReflection REFLECTION = DefaultMapper.reflection();
	private static Map<Class<?>, ChallengeAuthenticatorInfos> infos = new HashMap<Class<?>, ChallengeAuthenticatorInfos>();

	public static ChallengeAuthenticatorInfos checkEntityAuthenticatorClass(Class<? extends Object> entityAuthenticatorClass) throws SecurityException {
		if( ChallengeEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass) ) {
			return ChallengeEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);
		}

		String keyRealmFieldName = null;
		String challengeFieldName = null;
		String challengeExpirationFieldName = null;

		try {
			keyRealmFieldName= ChallengeEntityAuthenticatorChecker.checkKeyRealmAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorKeyRealm");
		}

		try {
			challengeFieldName = ChallengeEntityAuthenticatorChecker.checkChallengeAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorChallenge");
		}

		try {
			challengeExpirationFieldName = ChallengeEntityAuthenticatorChecker.checkChallengeExpirationAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		} catch (CoreException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " does not have a field annotated with @AuthenticatorChallengeExpiration");
		}

		Field keyRealmField = findFieldByName(entityAuthenticatorClass, keyRealmFieldName);
		AuthenticatorKeyRealm keyRealmAnnotation = keyRealmField.getAnnotation(AuthenticatorKeyRealm.class);

		Class<?> keyType = keyRealmAnnotation.key();
		boolean autoCreateKey = keyRealmAnnotation.autoCreateKey();
		KeyAlgorithm keyAlgorithm = keyRealmAnnotation.keyAlgorithm();
		int keyLifeTime = keyRealmAnnotation.keyLifeTime();
		TimeUnit keyLifeTimeUnit = keyRealmAnnotation.keyLifeTimeUnit();

		try {
			IObjectQuery<?> q = ObjectQueryFactory.objectQuery(
					REFLECTION.extractClass(entityAuthenticatorClass), new RuntimeReflectionProvider());
			List<Object> expiration = q.find(challengeFieldName);
			AuthenticatorChallenge challengeAnnotation = ((Field) expiration.get(expiration.size()-1)).getAnnotation(AuthenticatorChallenge.class);
			ChallengeAuthenticatorInfos authenticatorinfos = new ChallengeAuthenticatorInfos(
					q.address(challengeFieldName),
					q.address(keyRealmFieldName),
					q.address(challengeExpirationFieldName),
					keyType,
					autoCreateKey,
					keyAlgorithm,
					keyLifeTime,
					keyLifeTimeUnit,
					keyRealmAnnotation.encryptionMode(),
					keyRealmAnnotation.encryptionPadding(),
					keyRealmAnnotation.signatureAlgorithm(),
					challengeAnnotation.challengeType(),
					challengeAnnotation.challengeLifeTime(),
					challengeAnnotation.challengeLifeTimeUnit());

			ChallengeEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			return authenticatorinfos;
		} catch (Exception e) {
			throw new SecurityException(e);
		}
	}

	private static String checkChallengeExpirationAnnotationPresentAndFieldHasGoodType(
			Class<? extends Object> entityAuthenticatorClass) throws SecurityException {
		return findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorChallengeExpiration.class, Date.class, "AuthenticatorChallengeExpiration");
	}

	private static String checkChallengeAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws SecurityException {
		return findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorChallenge.class, byte[].class, "AuthenticatorChallenge");
	}

	private static String checkKeyRealmAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass) throws SecurityException {
		return findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorKeyRealm.class, IKeyRealm.class, "AuthenticatorKeyRealm");
	}

	private static String findFieldAnnotatedWith(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotation,
			Class<?> expectedType, String annotationName) throws SecurityException {
		for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(annotation) != null) {
				if (expectedType != null && !expectedType.isAssignableFrom(field.getType())
						&& !field.getType().isPrimitive()) {
					throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
							"Entity Authenticator " + clazz.getSimpleName()
									+ " field annotated with @" + annotationName
									+ " must be of type " + expectedType.getSimpleName());
				}
				return field.getName();
			}
		}
		// Check superclasses
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
			return findFieldAnnotatedWith(clazz.getSuperclass(), annotation, expectedType, annotationName);
		}
		throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
				"Entity Authenticator " + clazz.getSimpleName()
						+ " does not have any field annotated with @" + annotationName);
	}

	private static Field findFieldByName(Class<?> clazz, String fieldName) throws SecurityException {
		Class<?> current = clazz;
		while (current != null && current != Object.class) {
			for (Field field : current.getDeclaredFields()) {
				if (field.getName().equals(fieldName)) {
					return field;
				}
			}
			current = current.getSuperclass();
		}
		throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
				"Field " + fieldName + " not found in " + clazz.getSimpleName());
	}
}
