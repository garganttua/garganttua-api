package com.garganttua.api.core.security.entity.checker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.api.spec.security.annotations.Authenticator;
import com.garganttua.api.spec.security.annotations.AuthenticatorAccountNonExpired;
import com.garganttua.api.spec.security.annotations.AuthenticatorAccountNonLocked;
import com.garganttua.api.spec.security.annotations.AuthenticatorAuthorities;
import com.garganttua.api.spec.security.annotations.AuthenticatorCredentialsNonExpired;
import com.garganttua.api.spec.security.annotations.AuthenticatorEnabled;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKeyRealm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityAuthenticatorChecker {

	private static final IReflection REFLECTION = DefaultMapper.reflection();
	private static Map<Class<?>, AuthenticatorInfos> infos = new HashMap<>();

	public static AuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass)
			throws CoreException {
		if (EntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass)) {
			return EntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity authenticator infos from class " + entityAuthenticatorClass.getName());
		}

		Authenticator annotation = entityAuthenticatorClass.getDeclaredAnnotation(Authenticator.class);

		if (annotation == null) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Entity Authenticator "
					+ entityAuthenticatorClass.getSimpleName() + " is not annotated with @Authenticator");
		}

		IClass<?> authorizationType = REFLECTION.extractClass(annotation.authorization());
		IClass<?>[] authentications = java.util.Arrays.stream(annotation.authentications())
				.map(c -> REFLECTION.extractClass(c)).toArray(IClass[]::new);
		String[] authenticationInterfaces = annotation.interfaces();
		IClass<?> keyType = REFLECTION.extractClass(annotation.authorizationKey());
		boolean autoCreateKey = annotation.autoCreateAuthorizationKey();
		KeyAlgorithm keyAlgorithm = annotation.authorizationKeyAlgorithm();
		SignatureAlgorithm signatureAlgorithm = annotation.authorizationSignatureAlgorithm();
		int keyLifeTime = annotation.authorizationKeyLifeTime();
		TimeUnit keyLifeTimeUnit = annotation.authorizationKeyLifeTimeUnit();
		AuthenticatorKeyUsage keyUsage = annotation.authorizationKeyUsage();

		String accountNonExpiredFieldName = findFieldAnnotatedWith(entityAuthenticatorClass,
				AuthenticatorAccountNonExpired.class, Boolean.class, "AuthenticatorAccountNonExpired");
		String accountNonLockedFieldName = findFieldAnnotatedWith(entityAuthenticatorClass,
				AuthenticatorAccountNonLocked.class, Boolean.class, "AuthenticatorAccountNonLocked");
		String credentialsNonExpiredFieldName = findFieldAnnotatedWith(entityAuthenticatorClass,
				AuthenticatorCredentialsNonExpired.class, Boolean.class, "AuthenticatorCredentialsNonExpired");
		String enabledFieldName = findFieldAnnotatedWith(entityAuthenticatorClass,
				AuthenticatorEnabled.class, Boolean.class, "AuthenticatorEnabled");
		String authoritiesFieldName = findFieldAnnotatedWith(entityAuthenticatorClass,
				AuthenticatorAuthorities.class, List.class, "AuthenticatorAuthorities");

		try {
			IObjectQuery<?> q = ObjectQueryFactory.objectQuery(
					REFLECTION.extractClass(entityAuthenticatorClass), new RuntimeReflectionProvider());

			AuthenticatorInfos authenticatorinfos = new AuthenticatorInfos(REFLECTION.extractClass(entityAuthenticatorClass),
					authentications, authenticationInterfaces, authorizationType, keyType, keyUsage,
					autoCreateKey, keyAlgorithm, signatureAlgorithm, keyLifeTime, keyLifeTimeUnit,
					annotation.authorizationLifeTime(),
					annotation.authorizationLifeTimeUnit(), annotation.authorizationRefreshTokenLifeTime(),
					annotation.authorizationRefreshTokenLifeTimeUnit(), q.address(authoritiesFieldName),
					q.address(accountNonExpiredFieldName), q.address(accountNonLockedFieldName),
					q.address(credentialsNonExpiredFieldName), q.address(enabledFieldName), annotation.scope());

			EntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
			return authenticatorinfos;
		} catch (ReflectionException e) {
			throw new SecurityException(e);
		}
	}

	public static AuthenticatorInfos checkEntityAuthenticator(Object entity) throws CoreException {
		return checkEntityAuthenticatorClass(entity.getClass());
	}

	private static String findFieldAnnotatedWith(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotation,
			Class<?> expectedType, String annotationName) throws SecurityException {
		for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(annotation) != null) {
				if (expectedType != null && !expectedType.isAssignableFrom(field.getType())
						&& !field.getType().isPrimitive()) {
					throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
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
		throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
				"Entity Authenticator " + clazz.getSimpleName()
						+ " does not have any field annotated with @" + annotationName);
	}
}
