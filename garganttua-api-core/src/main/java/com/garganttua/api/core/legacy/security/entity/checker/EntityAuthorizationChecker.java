package com.garganttua.api.core.legacy.security.entity.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.legacy.entity.checker.EntityChecker;
import com.garganttua.api.core.legacy.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.entity.annotations.EntityId;
import com.garganttua.api.spec.entity.annotations.EntityOwned;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.entity.annotations.EntityUuid;
import com.garganttua.api.spec.security.annotations.Authenticator;
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
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.query.IObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityAuthorizationChecker {

	private static Map<Class<?>, AuthorizationInfos> infos = new HashMap<Class<?>, AuthorizationInfos>();

	public static AuthorizationInfos checkEntityAuthorizationClass(Class<?> entityClass) throws CoreException {
		if (EntityAuthorizationChecker.infos.containsKey(entityClass)) {
			return EntityAuthorizationChecker.infos.get(entityClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity authorization infos from class " + entityClass.getSimpleName());
		}

		Authorization authorizationAnnotation = (Authorization) EntityChecker
				.checkIfAnnotatedEntity(entityClass, Authorization.class);

		// must be owned
		Annotation ownedAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass, EntityOwned.class);
		if (ownedAnnotation == null) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Authorization entity "
					+ entityClass.getSimpleName() + " must be annotated with @EntityOnwed");
		}

		// must be authenticator
		Annotation authenticatorAnnotation = EntityChecker.checkIfAnnotatedEntity(entityClass,
				Authenticator.class);
		if (authenticatorAnnotation == null) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Authorization entity "
					+ entityClass.getSimpleName() + " must be annotated with @AuthenticatorAnnotation");
		}

		String uuidFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityUuid.class, String.class, true);
		String idFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityId.class, String.class, true);
		String tenantIdFieldAddress = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityTenantId.class, String.class, true);
		String authorizationTypeFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				AuthorizationType.class, String.class, true);

		String revoked = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				AuthorizationRevoked.class, Boolean.class, true);
		String ownerUuid = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				EntityOwnerId.class, String.class, true);
		String authorities = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				AuthorizationAuthorities.class, List.class, true);
		String creationDate = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				AuthorizationCreation.class, Date.class, true);
		String expirationDate = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				AuthorizationExpiration.class, Date.class, true);

		// is renewable ?
		boolean renewable = authorizationAnnotation.renewable();

		// is signable ?
		boolean signable = authorizationAnnotation.signable();
		Constructor<?> rawConstructor = null;
		Constructor<?> completeConstructor = null;
		String validateMethod = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				AuthorizationValidate.class, true, void.class, Object[].class);
		String validateAgainstMethod = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				AuthorizationValidateAgainst.class, true, void.class, entityClass, Object[].class);
		String toByteArrayMethod = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				AuthorizationToByteArray.class, true, byte[].class);
		String signMethod = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				AuthorizationSign.class, true, void.class, IKeyRealm.class);

		String getRefreshTokenMethod = null;
		String validateRefreshTokenMethod = null;
		String createRefreshTokenMethod = null;
		String refreshTokenExpirationField = null;

		if (renewable) {
			if (!signable)
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Authorization entity "
						+ entityClass.getSimpleName() + " must be signable if it is renewable");

			getRefreshTokenMethod = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
					AuthorizationRefreshToken.class, true, byte[].class);
			validateRefreshTokenMethod = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
					AuthorizationValidateRefreshToken.class, true, void.class, IKeyRealm.class, byte[].class);
			createRefreshTokenMethod = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
					AuthorizationCreateRefreshToken.class, true, void.class, IKeyRealm.class, Date.class);
			refreshTokenExpirationField = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				AuthorizationRefreshTokenExpiration.class, Date.class, true);
		}

		try {
			rawConstructor = entityClass.getDeclaredConstructor(byte[].class);
		} catch (NoSuchMethodException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Authorization entity "
					+ entityClass.getSimpleName() + " must have one constructor with parameters (byte[] raw)");
		}
		try {
			completeConstructor = entityClass.getDeclaredConstructor(String.class, String.class,
					String.class, List.class, Date.class, Date.class);
		} catch (NoSuchMethodException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Authorization entity "
					+ entityClass.getSimpleName()
					+ " must have one constructor with parameters (String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate)");
		}

		IObjectQuery q;
		try {
			q = ObjectQueryFactory.objectQuery(entityClass);

			AuthorizationInfos authorizationInfos = new AuthorizationInfos(signable, renewable,
					completeConstructor,
					rawConstructor, q.address(uuidFieldAddress), q.address(idFieldAddress),
					q.address(tenantIdFieldAddress), q.address(ownerUuid), q.address(authorities),
					q.address(creationDate), q.address(expirationDate), q.address(revoked),
					q.address(validateAgainstMethod), q.address(validateMethod), q.address(authorizationTypeFieldName),
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
}
