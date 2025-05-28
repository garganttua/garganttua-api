package com.garganttua.api.core.security.authorization;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthorizationChecker;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public class GGAPIEntityAuthorizationHelper {

	public static String getUuid(Object authorization) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::uuidFieldAddress);
	}

	public static boolean isSignable(Class<?> authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		return infos.signable();
	}

	public static boolean isRenewable(Class<?> authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		return infos.renewable();
	}

	public static boolean isAuthorization(Object object) {
		try {
			GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker
					.checkEntityAuthorizationClass(object.getClass());
			if (infos != null)
				return true;
		} catch (GGAPIException e) {
			return false;
		}
		return false;
	}

	public static void validateAgainst(Object authorization, Object authorizationRef, Object... args)
			throws GGAPIException {
		GGAPIInfosHelper.invoke(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::validateAgainstMethodAddress, authorizationRef, new Object[] { args });
	}

	public static void validate(Object authorization, Object... args) throws GGAPIException {
		GGAPIInfosHelper.invoke(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::validateMethodAddress, new Object[] { args });
	}

	public static void sign(Object authorization, IGGAPIKeyRealm key) throws GGAPIException {
		if (!isSignable(authorization.getClass()))
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not signable");

		GGAPIInfosHelper.invoke(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::signMethodAddress, key);
	}

	public static List<String> getAuthorities(Object authorization) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::authoritiesFieldAddress);
	}

	public static String getOwnerId(Object authorization) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::ownerIdFieldAddress);
	}

	public static String getTenantId(Object authorization) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::tenantIdFieldAddress);
	}

	public static byte[] toByteArray(Object authorization) throws GGAPIException {
		return (byte[]) GGAPIInfosHelper.invoke(authorization,
				GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::toByteArrayMethodAddress);
	}

	public static Object newObject(Class<?> authorization, byte[] authorizationRaw) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		try {
			return infos.rawConstructor().newInstance(authorizationRaw);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static Object newObject(Class<?> authorization, String uuid, String tenantId, String ownerUuid,
			List<String> authorities, Date creationDate, Date expirationDate) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		try {
			return infos.completeConstructor().newInstance(uuid, tenantId, ownerUuid, authorities, creationDate,
					expirationDate);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static String getType(Object authorization) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::authorizationTypeFieldAddress);
	}

	public static void createRefreshToken(Object authorization, IGGAPIKeyRealm key, Date expirationDate)
			throws GGAPIException {
		if (!isRenewable(authorization.getClass()))
			throw new GGAPISecurityException(GGAPIExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");
		GGAPIInfosHelper.invoke(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::createRefreshTokenMethodAddress, key, expirationDate);
	}

	public static byte[] getRefreshToken(Object authorization) throws GGAPIException {
		if (!isRenewable(authorization.getClass()))
			throw new GGAPISecurityException(GGAPIExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");
		return (byte[]) GGAPIInfosHelper.invoke(authorization,
				GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::getRefreshTokenMethodAddress);
	}

	public static void validatefreshToken(Object authorization, IGGAPIKeyRealm key, byte[] refreshToken)
			throws GGAPIException {
		if (!isRenewable(authorization.getClass()))
			throw new GGAPISecurityException(GGAPIExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");
		GGAPIInfosHelper.invoke(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::validateRefreshTokenMethodAddress, key, refreshToken);
	}

	public static Date getRefreshTokenExpirationDate(Object authorization) throws GGAPIException {
		if (!isRenewable(authorization.getClass()))
			throw new GGAPISecurityException(GGAPIExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");
		return GGAPIInfosHelper.getValue(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::refreshTokenExpirationFielddAddress);
	}

	public static boolean isExpired(Object authorization) throws GGAPIException {
		Date authorizationExpirationDate = GGAPIInfosHelper.getValue(authorization,
				GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::expirationFieldAddress);

		if (Instant.now().isAfter(authorizationExpirationDate.toInstant())) {
			return true;
		}
		return false;
	}

	public static boolean isRefreshTokenExpired(Object authorization) throws GGAPIException {
		if (!isRenewable(authorization.getClass()))
			throw new GGAPISecurityException(GGAPIExceptionCode.CORE_GENERIC_CODE,
					"Authorization class " + authorization.getClass().getSimpleName() + " is not renewable");

		Date refreshTokenExpirationDate = GGAPIInfosHelper.getValue(authorization,
				GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::refreshTokenExpirationFielddAddress);

		if (Instant.now().isAfter(refreshTokenExpirationDate.toInstant())) {
			return true;
		}
		return false;
	}

	public static boolean isRevoked(Object authorization) throws GGAPIException {
		if ((boolean) GGAPIInfosHelper.getValue(authorization,
				GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::revokedFieldAddress)) {
			return true;
		}
		return false;
	}

	public static void revoke(Object authorization) throws GGAPIException {
		GGAPIInfosHelper.setValue(authorization,
				GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::revokedFieldAddress, true);
	}
}
