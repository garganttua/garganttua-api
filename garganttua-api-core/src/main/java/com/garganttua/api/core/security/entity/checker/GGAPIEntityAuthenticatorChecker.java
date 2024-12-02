package com.garganttua.api.core.security.entity.checker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityOwner;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAccountNonLocked;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorCredentialsNonExpired;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorEnabled;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorKeyUsage;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityAuthenticatorChecker {

	private static Map<Class<?>, GGAPIAuthenticatorInfos> infos = new HashMap<Class<?>, GGAPIAuthenticatorInfos>();

	public static GGAPIAuthenticatorInfos checkEntityAuthenticatorClass(Class<?> entityAuthenticatorClass)
			throws GGAPIException {
		if (GGAPIEntityAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass)) {
			return GGAPIEntityAuthenticatorChecker.infos.get(entityAuthenticatorClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity authenticator infos from class " + entityAuthenticatorClass.getName());
		}

		GGAPIAuthenticator annotation = entityAuthenticatorClass.getDeclaredAnnotation(GGAPIAuthenticator.class);

		if (annotation == null) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "
					+ entityAuthenticatorClass.getSimpleName() + " is not annotated with @GGAPIAuthenticator");
		}
		
		Class<?> authorizationType = annotation.authorization();

		if (authorizationType != void.class && entityAuthenticatorClass.getDeclaredAnnotation(GGAPIEntityOwner.class) == null) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION, "Entity Authenticator "
					+ entityAuthenticatorClass.getSimpleName() + " is not annotated with @GGAPIEntityOwner");
		}
		
		Class<?> authentication = annotation.authentication();
		String[] authenticationInterfaces = annotation.interfaces();
		Class<?> keyType = annotation.key();
		boolean autoCreateKey = annotation.autoCreateKey();
		GGAPIKeyAlgorithm keyAlgorithm = annotation.keyAlgorithm();
		int keyLifeTime = annotation.keyLifeTime();
		TimeUnit keyLifeTimeUnit = annotation.keyLifeTimeUnit();

		GGAPIAuthenticatorKeyUsage keyUsage = annotation.keyUsage();

		if (keyType != void.class && keyType.isAssignableFrom(IGGAPIKeyRealm.class)) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " must have key "
							+ keyType.getSimpleName() + " that implements IGGAPIKeyRealm interface");
		}

		if (keyType != void.class && GGAPIEntityHelper.getDomain(keyType) == null) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " must have key "
							+ keyType.getSimpleName() + " annotated with @GGAPIEntity");
		}

		if (keyType != void.class && !GGAPIEntityChecker.checkEntityClass(keyType).ownedEntity()) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName() + " must have key "
							+ keyType.getSimpleName() + " annotated with @GGAPIEntityOwned");
		}

		if (authenticationInterfaces != null && authenticationInterfaces.length > 0 && authentication == void.class) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " cannot have interface configured but no authentication");
		}

		String accountNonExpiredFieldName = GGAPIEntityAuthenticatorChecker
				.checkAccountNonExpiredAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String accountNonLockedFieldName = GGAPIEntityAuthenticatorChecker
				.checkAccountNonLockedAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String credentialsNonExpiredFieldName = GGAPIEntityAuthenticatorChecker
				.checkCredentialsNonLockedAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String enabledFieldName = GGAPIEntityAuthenticatorChecker
				.checkEnabledAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
		String autoritiesFieldName = GGAPIEntityAuthenticatorChecker
				.checkAuthoritiesAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);

		IGGObjectQuery q;
		try {
			q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);


			GGAPIAuthenticatorInfos authenticatorinfos = new GGAPIAuthenticatorInfos(entityAuthenticatorClass,
					authentication, authenticationInterfaces, authorizationType, keyType, keyUsage,
					autoCreateKey, keyAlgorithm, keyLifeTime, keyLifeTimeUnit, annotation.authorizationLifeTime(),
					annotation.authorizationLifeTimeUnit(), q.address(autoritiesFieldName),
					q.address(accountNonExpiredFieldName), q.address(accountNonLockedFieldName),
					q.address(credentialsNonExpiredFieldName), q.address(enabledFieldName));

			GGAPIEntityAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);

			return authenticatorinfos;
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(e);
		}
	}

	private static String checkAuthoritiesAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass)
			throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					GGAPIAuthenticatorAuthorities.class, List.class);
			if (fieldAddress == null) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @GGAPIAuthenticatorAutorities");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @GGAPIAuthenticatorAutorities",
					e);
		}
		return fieldAddress;
	}

	private static String checkEnabledAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass)
			throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					GGAPIAuthenticatorEnabled.class, boolean.class);
			if (fieldAddress == null) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @GGAPIAuthenticatorEnabled");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @GGAPIAuthenticatorEnabled",
					e);
		}
		return fieldAddress;
	}

	private static String checkCredentialsNonLockedAnnotationPresentAndFieldHasGoodType(
			Class<?> entityAuthenticatorClass) throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					GGAPIAuthenticatorCredentialsNonExpired.class, boolean.class);
			if (fieldAddress == null) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @GGAPIAuthenticatorCredentialsNonExpired");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @GGAPIAuthenticatorCredentialsNonExpired",
					e);
		}
		return fieldAddress;
	}

	private static String checkAccountNonLockedAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass)
			throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					GGAPIAuthenticatorAccountNonLocked.class, boolean.class);
			if (fieldAddress == null) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @GGAPIAuthenticatorAccountNonLocked");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @GGAPIAuthenticatorAccountNonLocked",
					e);
		}
		return fieldAddress;
	}

	private static String checkAccountNonExpiredAnnotationPresentAndFieldHasGoodType(Class<?> entityAuthenticatorClass)
			throws GGAPIException {
		String fieldAddress;
		try {
			fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
					GGAPIAuthenticatorAccountNonExpired.class, boolean.class);
			if (fieldAddress == null) {
				throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
						"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
								+ " does not have any field annotated with @GGAPIAuthenticatorAccountNonExpired");
			}
		} catch (GGReflectionException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
					"Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
							+ " does not have any field annotated with @GGAPIAuthenticatorAccountNonExpired",
					e);
		}
		return fieldAddress;
	}

	public static GGAPIAuthenticatorInfos checkEntityAuthenticator(Object entity) throws GGAPIException {
		return checkEntityAuthenticatorClass(entity.getClass());
	}
}
