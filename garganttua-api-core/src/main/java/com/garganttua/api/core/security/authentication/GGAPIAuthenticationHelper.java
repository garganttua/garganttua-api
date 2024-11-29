package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticationChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.reflection.query.GGObjectQueryFactory;

public class GGAPIAuthenticationHelper {

	public static void authenticate(Object authentication) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			GGObjectQueryFactory.objectQuery(authentication).invoke(infos.authenticateMethodAddress(),
					infos.findPrincipal());
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}

	public static boolean isAuthenticated(Object authentication) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			return (boolean) GGObjectQueryFactory.objectQuery(authentication)
					.getValue(infos.authenticatedFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return false;
		}
	}

	public static void setAuthorization(Object authentication, Object authorization) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			GGObjectQueryFactory.objectQuery(authentication).setValue(infos.authorizationFieldAddress(), authorization);
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}

	public static Object getPrincipal(Object authentication) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			return GGObjectQueryFactory.objectQuery(authentication).getValue(infos.principalFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return false;
		}
	}

	public static Object instanciateNewOject(Class<?> authenticationClass) throws GGAPIEntityException {
		Constructor<?> ctor;
		try {
			ctor = authenticationClass.getDeclaredConstructor();
			Object authentication = ctor.newInstance();

			return authentication;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new GGAPIEntityException(GGAPIExceptionCode.INVOKE_METHOD,
					"Cannot instanciate new authentication of type " + authenticationClass.getSimpleName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAuthorities(Object authentication) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			return (List<String>) GGObjectQueryFactory.objectQuery(authentication)
					.getValue(infos.autoritiesFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static Object getCredentials(Object authentication) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::credentialsFieldAddress);
	}

	public static Object getAuthorization(Object authentication) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			return GGObjectQueryFactory.objectQuery(authentication).getValue(infos.authorizationFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}

	public static void setAuthenticated(Object authentication, boolean isAuthenticated) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			GGObjectQueryFactory.objectQuery(authentication).setValue(infos.authenticatedFieldAddress(),
					isAuthenticated);
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}

	public static void setAuthenticatorService(Object authentication, IGGAPIService authenticatorService)
			throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			GGObjectQueryFactory.objectQuery(authentication).setValue(infos.authenticatorServiceFieldAddress(),
					authenticatorService);
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}

	public static void setCredentials(Object authentication, Object credentials) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			GGObjectQueryFactory.objectQuery(authentication).setValue(infos.credentialsFieldAddress(), credentials);
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}

	public static void setPrincipal(Object authentication, Object principal) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			GGObjectQueryFactory.objectQuery(authentication).setValue(infos.principalFieldAddress(), principal);
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}

	public static void setTenantId(Object authentication, String tenantId) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			GGObjectQueryFactory.objectQuery(authentication).setValue(infos.tenantIdFieldAddress(), tenantId);
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}

	public static void setAuthenticatorInfos(Object authentication, GGAPIAuthenticatorInfos authenticatorInfos)
			throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			GGObjectQueryFactory.objectQuery(authentication).setValue(infos.authenticatorInfosFieldAddress(),
					authenticatorInfos);
		} catch (Exception e) {
			GGAPIException.processException(e);
		}
	}

	public static String getTenantId(Object authentication) throws GGAPIException {
		GGAPIAuthenticationInfos infos;
		try {
			infos = GGAPIEntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
			return (String) GGObjectQueryFactory.objectQuery(authentication).getValue(infos.tenantIdFieldAddress());
		} catch (Exception e) {
			GGAPIException.processException(e);
			return null;
		}
	}
}
