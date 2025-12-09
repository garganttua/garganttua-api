package com.garganttua.api.core.security.entity.checker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.entity.checker.EntityChecker;
import com.garganttua.api.core.entity.exceptions.EntityException;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.entity.annotations.EntityOwnerId;
import com.garganttua.api.spec.entity.annotations.EntityTenantId;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticate;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticated;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticatorInfos;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthenticatorService;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthorities;
import com.garganttua.api.spec.security.annotations.AuthenticationAuthorization;
import com.garganttua.api.spec.security.annotations.AuthenticationCredentials;
import com.garganttua.api.spec.security.annotations.AuthenticationFindPrincipal;
import com.garganttua.api.spec.security.annotations.AuthenticationPrincipal;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.security.authentication.AuthenticationInfos;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;
import com.garganttua.api.spec.service.IService;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.query.IObjectQuery;
import com.garganttua.core.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityAuthenticationChecker {
	
	private static Map<Class<?>, AuthenticationInfos> infos = new HashMap<Class<?>, AuthenticationInfos>();

	public static AuthenticationInfos checkEntityAuthenticationClass(Class<?> authenticationClass) throws CoreException {
		if (EntityAuthenticationChecker.infos.containsKey(authenticationClass)) {
			return EntityAuthenticationChecker.infos.get(authenticationClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity authentication infos from class " + authenticationClass.getName());
		}
		
		Authentication annotation = authenticationClass.getDeclaredAnnotation(Authentication.class);
				
		String autoritiesFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, AuthenticationAuthorities.class, GGObjectReflectionHelper.getParameterizedType(List.class, String.class), true);
		String serviceFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, AuthenticationAuthenticatorService.class, IService.class, true);
		String authenticatorInfosFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, AuthenticationAuthenticatorInfos.class, AuthenticatorInfos.class, true);
		
		String authorizationFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, AuthenticationAuthorization.class, Object.class, true);
		
		String authenticatedFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, AuthenticationAuthenticated.class, boolean.class, true);
		String principalFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, AuthenticationPrincipal.class, Object.class, true);
		String credentialsFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, AuthenticationCredentials.class, Object.class, true);
		String tenantIdFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, EntityTenantId.class, String.class, true);
		String ownerIdFieldName = EntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, EntityOwnerId.class, String.class, true);
		String authenticateMethodName = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(authenticationClass, AuthenticationAuthenticate.class, true, void.class);
		String findPrincipalMethodName = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(authenticationClass, AuthenticationFindPrincipal.class, true, void.class);
		String securityPostProcessingMethodName = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(authenticationClass, AuthenticatorSecurityPostProcessing.class, true, void.class, ICaller.class, Object.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		String securityPreProcessingMethodName = EntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(authenticationClass, AuthenticatorSecurityPreProcessing.class, true, void.class, ICaller.class, Object.class, GGObjectReflectionHelper.getParameterizedType(Map.class, String.class, String.class));
		
		try {
			IObjectQuery q = ObjectQueryFactory.objectQuery(authenticationClass);
			AuthenticationInfos infos = new AuthenticationInfos(
					authenticationClass,
					q.address(autoritiesFieldName),
					q.address(serviceFieldName),
					q.address(authorizationFieldName),
					q.address(authenticatedFieldName),
					q.address(principalFieldName),
					q.address(credentialsFieldName),
					q.address(tenantIdFieldName),
					q.address(authenticateMethodName),
					q.address(authenticatorInfosFieldName),
					annotation.findPrincipal(),
					q.address(findPrincipalMethodName),
					q.address(ownerIdFieldName),
					q.address(securityPreProcessingMethodName),
					q.address(securityPostProcessingMethodName));
			
			EntityAuthenticationChecker.infos.put(authenticationClass, infos);
			return infos;		
		} catch (ReflectionException e) {
			log.atWarn().log("Error ", e);
			throw new EntityException(e);
		}
	}

}
