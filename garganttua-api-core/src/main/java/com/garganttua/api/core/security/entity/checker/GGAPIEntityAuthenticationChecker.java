package com.garganttua.api.core.security.entity.checker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.garganttua.api.core.entity.checker.GGAPIEntityChecker;
import com.garganttua.api.core.entity.exceptions.GGAPIEntityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.entity.annotations.GGAPIEntityTenantId;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticate;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticated;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticatorInfos;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthenticatorService;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthorities;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationAuthorization;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationCredentials;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationFindPrincipal;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticationPrincipal;
import com.garganttua.api.spec.security.authentication.GGAPIAuthenticationInfos;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityAuthenticationChecker {
	
	private static Map<Class<?>, GGAPIAuthenticationInfos> infos = new HashMap<Class<?>, GGAPIAuthenticationInfos>();

	public static GGAPIAuthenticationInfos checkEntityAuthenticationClass(Class<?> authenticationClass) throws GGAPIException {
		if (GGAPIEntityAuthenticationChecker.infos.containsKey(authenticationClass)) {
			return GGAPIEntityAuthenticationChecker.infos.get(authenticationClass);
		}

		if (log.isDebugEnabled()) {
			log.debug("Checking entity authentication infos from class " + authenticationClass.getName());
		}
		
		GGAPIAuthentication annotation = authenticationClass.getDeclaredAnnotation(GGAPIAuthentication.class);
				
		String autoritiesFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, GGAPIAuthenticationAuthorities.class, GGObjectReflectionHelper.getParameterizedType(List.class,  String.class), true);
		String serviceFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, GGAPIAuthenticationAuthenticatorService.class, IGGAPIService.class, true);
		String authenticatorInfosFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, GGAPIAuthenticationAuthenticatorInfos.class, GGAPIAuthenticatorInfos.class, true);
		
		String authorizationFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, GGAPIAuthenticationAuthorization.class, Object.class, true);
		
		String authenticatedFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, GGAPIAuthenticationAuthenticated.class, boolean.class, true);
		String principalFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, GGAPIAuthenticationPrincipal.class, Object.class, true);
		String credentialsFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, GGAPIAuthenticationCredentials.class, Object.class, true);
		String tenantIdFieldName = GGAPIEntityChecker.getFieldAddressAnnotatedWithAndCheckType(authenticationClass, GGAPIEntityTenantId.class, String.class, true);
		String authenticateMethodName = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(authenticationClass, GGAPIAuthenticationAuthenticate.class, true, void.class);
		String findPrincipalMethodName = GGAPIEntityChecker.getMethodAnnotationAndMethodParamsHaveGoodTypes(authenticationClass, GGAPIAuthenticationFindPrincipal.class, true, void.class);

		try {
			IGGObjectQuery q = GGObjectQueryFactory.objectQuery(authenticationClass);
			GGAPIAuthenticationInfos infos = new GGAPIAuthenticationInfos(
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
					q.address(findPrincipalMethodName));
			
			GGAPIEntityAuthenticationChecker.infos.put(authenticationClass, infos);
			return infos;		
		} catch (GGReflectionException e) {
			log.atWarn().log("Error ", e);
			throw new GGAPIEntityException(e);
		}
	}

}
