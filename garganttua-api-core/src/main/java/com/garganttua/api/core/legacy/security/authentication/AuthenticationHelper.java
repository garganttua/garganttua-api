package com.garganttua.api.core.legacy.security.authentication;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.context.InfosHelper;
import com.garganttua.api.core.legacy.entity.exceptions.EntityException;
import com.garganttua.api.core.legacy.security.entity.checker.EntityAuthenticationChecker;
import com.garganttua.api.core.legacy.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.core.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.security.authentication.AuthenticationInfos;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.service.IService;

public class AuthenticationHelper {

	public static void authenticate(Object authentication) throws CoreException {
		InfosHelper.invoke(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authenticateMethodAddress);
	}

	public static boolean isAuthenticated(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authenticatedFieldAddress);
	}

	public static void setAuthorization(Object authentication, Object authorization) throws CoreException {
		InfosHelper.setValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authorizationFieldAddress, authorization);
	}

	public static Object getPrincipal(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::principalFieldAddress);
	}

	public static Object instanciateNewOject(Class<?> authenticationClass, IDomain domain) throws EntityException {
		Constructor<?> ctor;
		try {
			ctor = authenticationClass.getDeclaredConstructor(IDomain.class);
			Object authentication = ctor.newInstance(domain);

			return authentication;
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new EntityException(CoreExceptionCode.INVOKE_METHOD,
					"Cannot instanciate new authentication of type " + authenticationClass.getSimpleName(), e);
		}
	}

	public static List<String> getAuthorities(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::autoritiesFieldAddress);
	}

	public static Object getCredentials(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::credentialsFieldAddress);
	}

	public static Object getAuthorization(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authorizationFieldAddress);
	}

	public static void setAuthenticated(Object authentication, boolean isAuthenticated) throws CoreException {
		InfosHelper.setValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authenticatedFieldAddress, isAuthenticated);
	}

	public static void setAuthenticatorService(Object authentication, IService authenticatorService)
			throws CoreException {
		InfosHelper.setValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authenticatorServiceFieldAddress, authenticatorService);
	}

	public static void setCredentials(Object authentication, Object credentials) throws CoreException {
		InfosHelper.setValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::credentialsFieldAddress, credentials);
	}

	public static void setPrincipal(Object authentication, Object principal) throws CoreException {
		InfosHelper.setValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::principalFieldAddress, principal);
	}

	public static void setTenantId(Object authentication, String tenantId) throws CoreException {
		InfosHelper.setValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::tenantIdFieldAddress, tenantId);
	}

	public static void setAuthenticatorInfos(Object authentication, AuthenticatorInfos authenticatorInfos)
			throws CoreException {
		InfosHelper.setValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authenticatorInfosFieldAddress, authenticatorInfos);
	}

	public static String getTenantId(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::tenantIdFieldAddress);
	}
	
	public static String getOwnerId(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::ownerIdFieldAddress);
	}
	
	public static void setOwnerInfos(Object authentication, String ownerId)
			throws CoreException {
		InfosHelper.setValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::ownerIdFieldAddress, ownerId);
	}

	public static boolean isFindPrincipal(Object authentication) throws CoreException {
		AuthenticationInfos infos = EntityAuthenticationChecker.checkEntityAuthenticationClass(authentication.getClass());
		if( infos == null ) {
			throw new SecurityException(CoreExceptionCode.UNKNOWN_ERROR, authentication.getClass().getSimpleName()+" not an authentication");
		}
		return infos.findPrincipal();
	}

	public static void findPrincipal(Object authentication) throws CoreException {
		if( AuthenticationHelper.isFindPrincipal(authentication) ) {
			InfosHelper.invoke(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::findPrincipalMethodAddress);
		}
	}

	public static IService getAuthenticatorService(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authenticatorServiceFieldAddress);
	}

	public static AuthenticatorInfos getAuthenticatorInfos(Object authentication) throws CoreException {
		return InfosHelper.getValue(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::authenticatorInfosFieldAddress);
	}

	public static void applyPreProcessingSecurity(Object authentication, ICaller caller, Object entity, Map<String, String> params) throws CoreException {
		InfosHelper.invoke(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::securityPreProcessingMethodAddress, caller, entity, params);
	}
	
	public static void applyPostProcessingSecurity(Object authentication, ICaller caller, Object entity, Map<String, String> params) throws CoreException {
		InfosHelper.invoke(authentication, EntityAuthenticationChecker::checkEntityAuthenticationClass,
				AuthenticationInfos::securityPostProcessingMethodAddress, caller, entity, params);
	}
}
