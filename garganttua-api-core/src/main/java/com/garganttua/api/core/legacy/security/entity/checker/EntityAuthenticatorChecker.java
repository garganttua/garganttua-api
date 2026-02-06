package com.garganttua.api.core.legacy.security.entity.checker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.legacy.entity.checker.EntityChecker;
import com.garganttua.api.core.legacy.entity.tools.EntityHelper;
import com.garganttua.api.core.legacy.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.entity.annotations.EntityOwner;
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
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.query.IObjectQuery;
import com.garganttua.core.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityAuthenticatorChecker {

	private static Map<Class<?>, AuthenticatorInfos> infos = new HashMap<Class<?>, AuthenticatorInfos>();

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
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "
					+ entityAuthenticatorClass.getSimpleName() + " is not annotated with @Authenticator");
		}

		Class<?> authorizationType = annotation.authorization();

		if (authorizationType != void.class
				&& entityAuthenticatorClass.getDeclaredAnnotation(EntityOwner.class) == null) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "
					+ entityAuthenticatorClass.getSimpleName() + " is not annotated with @EntityOwner");
		}

		Class<?>[] authentications = annotation.authentications();
		String[] authenticationInterfaces = annotation.interfaces();
		Class<?> keyType = annotation.authorizationKey();
		boolean autoCreateKey = annotation.autoCreateAuthorizationKey();
		KeyAlgorithm keyAlgorithm = annotation.authorizationKeyAlgorithm();
		SignatureAlgorithm signatureAlgorithm = annotation.authorizationSignatureAlgorithm();
		int keyLifeTime = annotation.authorizationKeyLifeTime();
		TimeUnit keyLifeTimeUnit = annotation.authorizationKeyLifeTimeUnit();

		AuthenticatorKeyUsage keyUsage = annotation.authorizationKeyUsage();

		if (keyType != void.class && keyType.isAssignableFrom(IKeyRealm.class)) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " must have key "
							+ keyType.getSimpleName() + " that implements IKeyRealm interface");
		}

		if (keyType != void.class && EntityHelper.getDomain(keyType) == null) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " must have key "
							+ keyType.getSimpleName() + " annotated with @Entity");
		}

		if (keyType != void.class && !EntityChecker.checkEntityClass(keyType).ownedEntity()) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " must have key "
							+ keyType.getSimpleName() + " annotated with @EntityOwned");
		}

		if (authenticationInterfaces != null && authenticationInterfaces.length > 0
				&& authentications[0] == void.class) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " cannot have interface configured but no authentication");
		}

		String accountNonExpiredFieldName = EntityAuthenticatorChecker
				.checkAccountNonExpiredAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String accountNonLockedFieldName = EntityAuthenticatorChecker
				.checkAccountNonLockedAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String credentialsNonExpiredFieldName = EntityAuthenticatorChecker
				.checkCredentialsNonLockedAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String enabledFieldName = EntityAuthenticatorChecker
				.checkEnabledAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String autoritiesFieldName = EntityAuthenticatorChecker
				.checkAuthoritiesAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);

		IObjectQuery q;
		try {
			q = ObjectQueryFactory.objectQuery(entityAuthenticatorClass);

			AuthenticatorInfos authenticatorinfos = new AuthenticatorInfos(entityAuthenticatorClass,
					authentications, authenticationInterfaces, authorizationType, keyType, keyUsage,
					autoCreateKey, keyAlgorithm, signatureAlgorithm, keyLifeTime, keyLifeTimeUnit,
					annotation.authorizationLifeTime(),
					annotation.authorizationLifeTimeUnit(), annotation.authorizationRefreshTokenLifeTime(),
					annotation.authorizationRefreshTokenLifeTimeUnit(), q.address(autoritiesFieldName),
					q.address(accountNonExpiredFieldName), q.address(accountNonLockedFieldName),
					q.address(credentialsNonExpiredFieldName), q.address(enabledFieldName), annotation.scope());

			EntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);

			return authenticatorinfos;
		} catch (ReflectionException e) {
			throw new SecurityException(e);
		}
	}

	private static String checkAuthoritiesAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass)
			throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					AuthenticatorAuthorities.class, List.class);
			if (fieldAddress == null) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @AuthenticatorAutorities");
			}
		} catch (ReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @AuthenticatorAutorities",
					e);
		}
		return fieldAddress;
	}

	private static String checkEnabledAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass)
			throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					AuthenticatorEnabled.class, Boolean.class);
			if (fieldAddress == null) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @AuthenticatorEnabled");
			}
		} catch (ReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @AuthenticatorEnabled",
					e);
		}
		return fieldAddress;
	}

	private static String checkCredentialsNonLockedAnnotationPresentAndFieldHasGoodType(
			Class<?> entityAuthenticatorClass) throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					AuthenticatorCredentialsNonExpired.class, Boolean.class);
			if (fieldAddress == null) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @AuthenticatorCredentialsNonExpired");
			}
		} catch (ReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @AuthenticatorCredentialsNonExpired",
					e);
		}
		return fieldAddress;
	}

	private static String checkAccountNonLockedAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass)
			throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					AuthenticatorAccountNonLocked.class, Boolean.class);
			if (fieldAddress == null) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @AuthenticatorAccountNonLocked");
			}
		} catch (ReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @AuthenticatorAccountNonLocked",
					e);
		}
		return fieldAddress;
	}

	private static String checkAccountNonExpiredAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass)
			throws CoreException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					AuthenticatorAccountNonExpired.class, Boolean.class);
			if (fieldAddress == null) {
				throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @AuthenticatorAccountNonExpired");
			}
		} catch (ReflectionException e) {
			throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @AuthenticatorAccountNonExpired",
					e);
		}
		return fieldAddress;
	}

	public static AuthenticatorInfos checkEntityAuthenticator(Object entity) throws CoreException {
		return checkEntityAuthenticatorClass(entity.getClass());
	}
}
