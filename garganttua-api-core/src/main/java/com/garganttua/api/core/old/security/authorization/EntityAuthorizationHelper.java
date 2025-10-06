package com.garganttua.api.core.security.authorization;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.garganttua.api.core.context.InfosHelper;
import com.garganttua.api.core.security.entity.checker.EntityAuthorizationChecker;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.security.authorization.AuthorizationInfos;
import com.garganttua.api.spec.security.key.IKeyRealm;

public class EntityAuthorizationHelper {

	public static String getUuid(Object authorization) throws CoreException {
		return InfosHelper.getValue(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::uuidFieldAddress);
	}

	public static boolean isSignable(Class<?> authorization) throws CoreException {
		AuthorizationInfos infos = EntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		return infos.signable();
	}

	public static boolean isRenewable(Class<?> authorization) throws CoreException {
		AuthorizationInfos infos = EntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		return infos.renewable();
	}

	public static boolean isAuthorization(Object object) {
		try {
			AuthorizationInfos infos = EntityAuthorizationChecker
					.checkEntityAuthorizationClass(object.getClass());
			if (infos != null)
				return true;
		} catch (CoreException e) {
			return false;
		}
		return false;
	}

	public static void validateAgainst(Object authorization, Object authorizationRef, Object... args)
			throws CoreException {
		InfosHelper.invoke(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::validateAgainstMethodAddress, authorizationRef, new Object[] { args });
	}

	public static void validate(Object authorization, Object... args) throws CoreException {
		InfosHelper.invoke(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::validateMethodAddress, new Object[] { args });
	}

	public static void sign(Object authorization, IKeyRealm key) throws CoreException {
		if (!isSignable(authorization.getClass()))
			throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not signable");

		InfosHelper.invoke(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::signMethodAddress, key);
	}

	public static List<String> getAuthorities(Object authorization) throws CoreException {
		return InfosHelper.getValue(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::authoritiesFieldAddress);
	}

	public static String getOwnerId(Object authorization) throws CoreException {
		return InfosHelper.getValue(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::ownerIdFieldAddress);
	}

	public static String getTenantId(Object authorization) throws CoreException {
		return InfosHelper.getValue(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::tenantIdFieldAddress);
	}

	public static byte[] toByteArray(Object authorization) throws CoreException {
		return (byte[]) InfosHelper.invoke(authorization,
				EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::toByteArrayMethodAddress);
	}

	public static Object newObject(Class<?> authorization, byte[] authorizationRaw) throws CoreException {
		AuthorizationInfos infos = EntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		try {
			return infos.rawConstructor().newInstance(authorizationRaw);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			CoreException.processException(e);
			return null;
		}
	}

	public static Object newObject(Class<?> authorization, String uuid, String tenantId, String ownerUuid,
			List<String> authorities, Date creationDate, Date expirationDate) throws CoreException {
		AuthorizationInfos infos = EntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		try {
			return infos.completeConstructor().newInstance(uuid, tenantId, ownerUuid, authorities, creationDate,
					expirationDate);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			CoreException.processException(e);
			return null;
		}
	}

	public static String getType(Object authorization) throws CoreException {
		return InfosHelper.getValue(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::authorizationTypeFieldAddress);
	}

	public static void createRefreshToken(Object authorization, IKeyRealm key, Date expirationDate)
			throws CoreException {
		if (!isRenewable(authorization.getClass()))
			throw new SecurityException(CoreExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");
		InfosHelper.invoke(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::createRefreshTokenMethodAddress, key, expirationDate);
	}

	public static byte[] getRefreshToken(Object authorization) throws CoreException {
		if (!isRenewable(authorization.getClass()))
			throw new SecurityException(CoreExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");
		return (byte[]) InfosHelper.invoke(authorization,
				EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::getRefreshTokenMethodAddress);
	}

	public static void validatefreshToken(Object authorization, IKeyRealm key, byte[] refreshToken)
			throws CoreException {
		if (!isRenewable(authorization.getClass()))
			throw new SecurityException(CoreExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");
		InfosHelper.invoke(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::validateRefreshTokenMethodAddress, key, refreshToken);
	}

	public static Date getRefreshTokenExpirationDate(Object authorization) throws CoreException {
		if (!isRenewable(authorization.getClass()))
			throw new SecurityException(CoreExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");
		return InfosHelper.getValue(authorization, EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::refreshTokenExpirationFielddAddress);
	}

	public static boolean isExpired(Object authorization) throws CoreException {
		Date authorizationExpirationDate = InfosHelper.getValue(authorization,
				EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::expirationFieldAddress);

		if (Instant.now().isAfter(authorizationExpirationDate.toInstant())) {
			return true;
		}
		return false;
	}

	public static boolean isRefreshTokenExpired(Object authorization) throws CoreException {
		if (!isRenewable(authorization.getClass()))
			throw new SecurityException(CoreExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");

		Date refreshTokenExpirationDate = InfosHelper.getValue(authorization,
				EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::refreshTokenExpirationFielddAddress);

		if (Instant.now().isAfter(refreshTokenExpirationDate.toInstant())) {
			return true;
		}
		return false;
	}

	public static boolean isRevoked(Object authorization) throws CoreException {
		if ((boolean) InfosHelper.getValue(authorization,
				EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::revokedFieldAddress)) {
			return true;
		}
		return false;
	}

	public static void revoke(Object authorization) throws CoreException {
		InfosHelper.setValue(authorization,
				EntityAuthorizationChecker::checkEntityAuthorizationClass,
				AuthorizationInfos::revokedFieldAddress, true);
	}
}
