package com.garganttua.api.core.security.authorization;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthorizationChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.authorization.GGAPIAuthorizationInfos;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class GGAPIEntityAuthorizationHelper {

	public static String getUuid(Object authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos;
		try {
			infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
			return (String) GGObjectQueryFactory.objectQuery(authorization).getValue(infos.uuidFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
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
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
		try {
			GGObjectQueryFactory.objectQuery(authorization).invoke(infos.validateAgainstMethodAddress(), authorizationRef);
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);

		}	
	}

	public static void validate(Object authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
		try {
			GGObjectQueryFactory.objectQuery(authorization).invoke(infos.validateMethodAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}	
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAuthorities(Object authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos;
		try {
			infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
			return (List<String>) GGObjectQueryFactory.objectQuery(authorization).getValue(infos.authoritiesFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static String getOwnerId(Object authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos;
		try {
			infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
			return (String) GGObjectQueryFactory.objectQuery(authorization).getValue(infos.ownerIdFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}
	
	public static String getFieldValue(Object authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos;
		try {
			infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
			return (String) GGObjectQueryFactory.objectQuery(authorization).getValue(infos.ownerIdFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static String getTenantId(Object authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos;
		try {
			infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
			return (String) GGObjectQueryFactory.objectQuery(authorization).getValue(infos.tenantIdFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static byte[] toByteArray(Object authorization) throws GGAPIException {
		GGAPIAuthorizationInfos infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
		try {
			return (byte[]) GGObjectQueryFactory.objectQuery(authorization).invoke(infos.toByteArrayMethodAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
			return null;
		}
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
		GGAPIAuthorizationInfos infos;
		try {
			infos = GGAPIEntityAuthorizationChecker.checkEntityAuthorizationClass(authorization.getClass());
			return (String) GGObjectQueryFactory.objectQuery(authorization).getValue(infos.authorizationTypeFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}

}
