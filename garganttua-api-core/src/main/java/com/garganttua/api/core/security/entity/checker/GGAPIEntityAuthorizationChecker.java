package com.garganttua.api.core.security.entity.checker;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwned;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwnerId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityUuid;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorization;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationCreation;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationExpiration;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationRevoked;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationToByteArray;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationType;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationValidate;
import com.garganttua.api.spec.security.annotations.GGAPIAuthorizationValidateAgainst;
import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityAuthorizationChecker {

	private static Map<Class<?>, GGAPIAuthorizationInfos> infos = new HashMap<Class<?>, GGAPIAuthorizationInfos>();

	public static GGAPIAuthorizationInfos checkEntityAuthorizationClass(Class<?> entityClass) throws GGAPIException {
		if (GGAPIEntityAuthorizationChecker.infos.containsKey(entityClass)) {
			return GGAPIEntityAuthorizationChecker.infos.get(entityClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity authorization infos from class " + entityClass.getSimpleName());
		}

		GGAPIAuthorization authorizationAnnotation = (GGAPIAuthorization) GGAPIEntityChecker
				.checkIfAnnotatedEntity(entityClass, GGAPIAuthorization.class);

		// must be owned
		Annotation ownedAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIEntityOwned.class);
		if (ownedAnnotation == null) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Authorization entity "
					+ entityClass.getSimpleName() + " must be annotated with @GGAPIEntityOnwed");
		}

		// must be authenticator
		Annotation authenticatorAnnotation = GGAPIEntityChecker.checkIfAnnotatedEntity(entityClass, GGAPIAuthenticator.class);
		if (authenticatorAnnotation == null) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Authorization entity "
					+ entityClass.getSimpleName() + " must be annotated with @AuthenticatorAnnotation");
		}

		String uuidFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIEntityUuid.class, String.class, true);
		String idFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIEntityId.class, String.class, true);
		String tenantIdFieldAddress = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIEntityTenantId.class, String.class, true);
		String authorizationTypeFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIAuthorizationType.class, String.class, true);

		String revoked = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIAuthorizationRevoked.class, Boolean.class, true);
		String ownerUuid = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIEntityOwnerId.class, String.class, true);
		String authorities = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIAuthorizationAuthorities.class, List.class, true);
		String creationDate = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIAuthorizationCreation.class, Date.class, true);
		String expirationDate = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(entityClass,
				GGAPIAuthorizationExpiration.class, Date.class, true);

		// is signable ?
		boolean signable = authorizationAnnotation.signable();
		Constructor<?> rawConstructor = null;
		Constructor<?> completeConstructor = null;
		String validateMethod = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				GGAPIAuthorizationValidate.class, true, void.class);
		String validateAgainstMethod = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				GGAPIAuthorizationValidateAgainst.class, true, void.class, entityClass);
		String toByteArrayMethod = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(entityClass,
				GGAPIAuthorizationToByteArray.class, true, byte[].class);

		if (signable) {
			try {
				rawConstructor = entityClass.getDeclaredConstructor(byte[].class, IGGAPIKeyRealm.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
						"Authorization entity " + entityClass.getSimpleName()
								+ " must have one constructor with parameters (byte[] raw, IGGAPIKeyRealm key)");
			}
			try {
				completeConstructor = entityClass.getDeclaredConstructor(String.class, String.class, String.class,
						String.class, List.class, Date.class, Date.class, IGGAPIKeyRealm.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Authorization entity "
						+ entityClass.getSimpleName()
						+ " must have one constructor with parameters (String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate, IGGAPIKeyRealm key)");
			}
		} else {
			try {
				rawConstructor = entityClass.getDeclaredConstructor(Byte[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Authorization entity "
						+ entityClass.getSimpleName() + " must have one constructor with parameters (byte[] raw)");
			}
			try {
				completeConstructor = entityClass.getDeclaredConstructor(String.class, String.class, String.class,
						String.class, List.class, Date.class, Date.class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Authorization entity "
						+ entityClass.getSimpleName()
						+ " must have one constructor with parameters (String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate)");
			}
		}

		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityClass);

			GGAPIAuthorizationInfos authorizationInfos = new GGAPIAuthorizationInfos(signable, completeConstructor,
					rawConstructor, q.address(uuidFieldAddress), q.address(idFieldAddress),
					q.address(tenantIdFieldAddress), q.address(ownerUuid), q.address(authorities),
					q.address(creationDate), q.address(expirationDate), q.address(revoked),
					q.address(validateAgainstMethod), q.address(validateMethod), q.address(authorizationTypeFieldName),
					q.address(toByteArrayMethod));

			GGAPIEntityAuthorizationChecker.infos.put(entityClass, authorizationInfos);

			return authorizationInfos;
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(e);
		}
	}
}
