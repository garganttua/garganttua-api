package com.garganttua.api.core.security.entity.checker;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.api.spec.entity.annotations.EntityOwned;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.entity.annotations.EntityUuid;
import com.garganttua.api.spec.entity.annotations.EntityId;
import com.garganttua.api.spec.security.annotations.Authorization;
import com.garganttua.api.spec.security.annotations.AuthorizationAuthorities;
import com.garganttua.api.spec.security.annotations.AuthorizationCreateRefreshToken;
import com.garganttua.api.spec.security.annotations.AuthorizationCreation;
import com.garganttua.api.spec.security.annotations.AuthorizationExpiration;
import com.garganttua.api.spec.security.annotations.AuthorizationRefreshToken;
import com.garganttua.api.spec.security.annotations.AuthorizationRefreshTokenExpiration;
import com.garganttua.api.spec.security.annotations.AuthorizationRevoked;
import com.garganttua.api.spec.security.annotations.AuthorizationSign;
import com.garganttua.api.spec.security.annotations.AuthorizationToByteArray;
import com.garganttua.api.spec.security.annotations.AuthorizationType;
import com.garganttua.api.spec.security.annotations.AuthorizationValidate;
import com.garganttua.api.spec.security.annotations.AuthorizationValidateAgainst;
import com.garganttua.api.spec.security.annotations.AuthorizationValidateRefreshToken;
import com.garganttua.api.spec.security.authorization.AuthorizationInfos;
import com.garganttua.api.spec.security.key.IKeyRealm;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityAuthorizationChecker {

	private static Map<Class<?>, AuthorizationInfos> infos = new HashMap<>();

	public static AuthorizationInfos checkEntityAuthorizationClass(Class<?> entityClass) throws CoreException {
		if (EntityAuthorizationChecker.infos.containsKey(entityClass)) {
			return EntityAuthorizationChecker.infos.get(entityClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity authorization infos from class " + entityClass.getSimpleName());
		}

		Authorization authorizationAnnotation = entityClass.getDeclaredAnnotation(Authorization.class);
		if (authorizationAnnotation == null) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
					"Entity " + entityClass.getSimpleName() + " is not annotated with @Authorization");
		}

		String uuidFieldAddress = findFieldAnnotatedWith(entityClass, EntityUuid.class, String.class);
		String idFieldAddress = findFieldAnnotatedWithOptional(entityClass, EntityId.class, String.class);
		String tenantIdFieldAddress = findFieldAnnotatedWithOptional(entityClass, EntityTenantId.class, String.class);
		String authorizationTypeFieldName = findFieldAnnotatedWith(entityClass, AuthorizationType.class, String.class);
		String revoked = findFieldAnnotatedWith(entityClass, AuthorizationRevoked.class, Boolean.class);
		String ownerUuid = findFieldAnnotatedWith(entityClass, EntityOwnerId.class, String.class);
		String authorities = findFieldAnnotatedWith(entityClass, AuthorizationAuthorities.class, List.class);
		String creationDate = findFieldAnnotatedWith(entityClass, AuthorizationCreation.class, Date.class);
		String expirationDate = findFieldAnnotatedWith(entityClass, AuthorizationExpiration.class, Date.class);

		boolean renewable = authorizationAnnotation.renewable();
		boolean signable = authorizationAnnotation.signable();

		String validateMethod = findMethodAnnotatedWith(entityClass, AuthorizationValidate.class);
		String validateAgainstMethod = findMethodAnnotatedWith(entityClass, AuthorizationValidateAgainst.class);
		String toByteArrayMethod = findMethodAnnotatedWith(entityClass, AuthorizationToByteArray.class);
		String signMethod = findMethodAnnotatedWith(entityClass, AuthorizationSign.class);

		String getRefreshTokenMethod = null;
		String validateRefreshTokenMethod = null;
		String createRefreshTokenMethod = null;
		String refreshTokenExpirationField = null;

		if (renewable) {
			if (!signable)
				throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authorization entity "
						+ entityClass.getSimpleName() + " must be signable if it is renewable");

			getRefreshTokenMethod = findMethodAnnotatedWith(entityClass, AuthorizationRefreshToken.class);
			validateRefreshTokenMethod = findMethodAnnotatedWith(entityClass, AuthorizationValidateRefreshToken.class);
			createRefreshTokenMethod = findMethodAnnotatedWith(entityClass, AuthorizationCreateRefreshToken.class);
			refreshTokenExpirationField = findFieldAnnotatedWith(entityClass,
					AuthorizationRefreshTokenExpiration.class, Date.class);
		}

		Constructor<?> rawConstructor;
		Constructor<?> completeConstructor;
		try {
			rawConstructor = entityClass.getDeclaredConstructor(byte[].class);
		} catch (NoSuchMethodException e) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authorization entity "
					+ entityClass.getSimpleName() + " must have one constructor with parameters (byte[] raw)");
		}
		try {
			completeConstructor = entityClass.getDeclaredConstructor(String.class, String.class,
					String.class, List.class, Date.class, Date.class);
		} catch (NoSuchMethodException e) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authorization entity "
					+ entityClass.getSimpleName()
					+ " must have one constructor with parameters (String uuid, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate)");
		}

		try {
			IObjectQuery<?> q = ObjectQueryFactory.objectQuery(
					DefaultMapper.reflection().extractClass(entityClass), new RuntimeReflectionProvider());

			AuthorizationInfos authorizationInfos = new AuthorizationInfos(signable, renewable,
					completeConstructor, rawConstructor,
					q.address(uuidFieldAddress),
					idFieldAddress == null ? null : q.address(idFieldAddress),
					tenantIdFieldAddress == null ? null : q.address(tenantIdFieldAddress),
					q.address(ownerUuid), q.address(authorities),
					q.address(creationDate), q.address(expirationDate), q.address(revoked),
					q.address(validateAgainstMethod), q.address(validateMethod),
					q.address(authorizationTypeFieldName),
					q.address(toByteArrayMethod), q.address(signMethod),
					getRefreshTokenMethod == null ? null : q.address(getRefreshTokenMethod),
					createRefreshTokenMethod == null ? null : q.address(createRefreshTokenMethod),
					validateRefreshTokenMethod == null ? null : q.address(validateRefreshTokenMethod),
					refreshTokenExpirationField == null ? null : q.address(refreshTokenExpirationField));

			EntityAuthorizationChecker.infos.put(entityClass, authorizationInfos);
			return authorizationInfos;
		} catch (ReflectionException e) {
			throw new SecurityException(e);
		}
	}

	private static String findFieldAnnotatedWith(Class<?> clazz,
			Class<? extends java.lang.annotation.Annotation> annotation, Class<?> expectedType)
			throws SecurityException {
		String result = findFieldAnnotatedWithOptional(clazz, annotation, expectedType);
		if (result == null) {
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
					"Entity " + clazz.getSimpleName() + " does not have any field annotated with @"
							+ annotation.getSimpleName());
		}
		return result;
	}

	private static String findFieldAnnotatedWithOptional(Class<?> clazz,
			Class<? extends java.lang.annotation.Annotation> annotation, Class<?> expectedType) {
		for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(annotation) != null) {
				return field.getName();
			}
		}
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
			return findFieldAnnotatedWithOptional(clazz.getSuperclass(), annotation, expectedType);
		}
		return null;
	}

	private static String findMethodAnnotatedWith(Class<?> clazz,
			Class<? extends java.lang.annotation.Annotation> annotation) throws SecurityException {
		for (java.lang.reflect.Method method : clazz.getDeclaredMethods()) {
			if (method.getAnnotation(annotation) != null) {
				return method.getName();
			}
		}
		if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
			return findMethodAnnotatedWith(clazz.getSuperclass(), annotation);
		}
		throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
				"Entity " + clazz.getSimpleName() + " does not have any method annotated with @"
						+ annotation.getSimpleName());
	}
}
