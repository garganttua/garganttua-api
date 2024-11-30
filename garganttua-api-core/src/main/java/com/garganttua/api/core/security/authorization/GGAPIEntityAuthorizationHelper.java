package com.garganttua.api.core.security.authorization;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthorizationChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class GGAPIEntityAuthorizationHelper {

	public static String getUuid(Object authorization) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::uuidFieldAddress);
	}

	public static boolean isSignable(Class<?> authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		return infos.signable();		
	}

	public static boolean isAuthorization(Object object) {
		try {
			GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(object.getClass());
			if( infos != null )
				return true;
		} catch (GGAPIException e) {
			return false;
		}
		return false;
	}

	public static void validateAgainst(Object authorization, Object authorizationRef) throws GGAPIException {
		GGAPIInfosHelper.invoke(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::validateAgainstMethodAddress, authorizationRef);
	}

	public static void validate(Object authorization) throws GGAPIException {
		GGAPIInfosHelper.invoke(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
				GGAPIAuthorizationInfos::validateMethodAddress);
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
		return (byte[]) GGAPIInfosHelper.invoke(authorization, GGAPIEntityAuthorizationChecker::checkEntityAuthorizationClass,
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

	public static Object newObject(Class<?> authorization, byte[] authorizationRaw, IGGAPIKeyRealm keyRealm) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		try {
			return infos.rawConstructor().newInstance(authorizationRaw, keyRealm);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			GGAPIException.processException(e);
			return null;
		}
	}
	
	public static Object newObject(Class<?> authorization, String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		try {
			return infos.completeConstructor().newInstance(uuid, id, tenantId, ownerUuid, authorities, creationDate, expirationDate);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static Object newObject(Class<?> authorization, String uuid, String id, String tenantId, String ownerUuid, List<String> authorities, Date creationDate, Date expirationDate, IGGAPIKeyRealm keyRealm) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization);
		try {
			return infos.completeConstructor().newInstance(uuid, id, tenantId, ownerUuid, authorities, creationDate, expirationDate, keyRealm);
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
}
