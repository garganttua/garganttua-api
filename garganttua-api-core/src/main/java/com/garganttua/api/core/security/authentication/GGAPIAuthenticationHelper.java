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

public class GGAPIAuthenticationHelper {

	public static void authenticate(Object authentication) throws GGAPIException {
		GGAPIInfosHelper.invoke(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::authenticateMethodAddress);
	}

	public static boolean isAuthenticated(Object authentication) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::authenticatedFieldAddress);
	}

	public static void setAuthorization(Object authentication, Object authorization) throws GGAPIException {
		GGAPIInfosHelper.setValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::authorizationFieldAddress, authorization);
	}

	public static Object getPrincipal(Object authentication) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::principalFieldAddress);
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

	public static List<String> getAuthorities(Object authentication) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::autoritiesFieldAddress);
	}

	public static Object getCredentials(Object authentication) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::credentialsFieldAddress);
	}

	public static Object getAuthorization(Object authentication) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::authorizationFieldAddress);
	}

	public static void setAuthenticated(Object authentication, boolean isAuthenticated) throws GGAPIException {
		GGAPIInfosHelper.setValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::authenticatedFieldAddress, isAuthenticated);
	}

	public static void setAuthenticatorService(Object authentication, IGGAPIService authenticatorService)
			throws GGAPIException {
		GGAPIInfosHelper.setValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::authenticatorServiceFieldAddress, authenticatorService);
	}

	public static void setCredentials(Object authentication, Object credentials) throws GGAPIException {
		GGAPIInfosHelper.setValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::credentialsFieldAddress, credentials);
	}

	public static void setPrincipal(Object authentication, Object principal) throws GGAPIException {
		GGAPIInfosHelper.setValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::principalFieldAddress, principal);
	}

	public static void setTenantId(Object authentication, String tenantId) throws GGAPIException {
		GGAPIInfosHelper.setValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::tenantIdFieldAddress, tenantId);
	}

	public static void setAuthenticatorInfos(Object authentication, GGAPIAuthenticatorInfos authenticatorInfos)
			throws GGAPIException {
		GGAPIInfosHelper.setValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::authenticatorInfosFieldAddress, authenticatorInfos);
	}

	public static String getTenantId(Object authentication) throws GGAPIException {
		return GGAPIInfosHelper.getValue(authentication, GGAPIEntityAuthenticationChecker::checkEntityAuthenticationClass,
				GGAPIAuthenticationInfos::tenantIdFieldAddress);
	}
}
