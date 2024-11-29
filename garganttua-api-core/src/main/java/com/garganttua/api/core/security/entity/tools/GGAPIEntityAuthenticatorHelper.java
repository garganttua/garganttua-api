package com.garganttua.api.core.security.entity.tools;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityAuthenticatorHelper {
	
	public static IGGAPIDomain getAuthenticatorDomain(IGGAPIDomainsRegistry domainsRegistry) {
		return domainsRegistry.getDomains().parallelStream().filter(e -> 
			e.getEntity().getValue0().getAnnotation(GGAPIAuthenticator.class)==null?false:true
		).findFirst().get();
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAuthorities(Object entity) throws GGAPIException {
		GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		try {
			return (List<String>) GGObjectQueryFactory.objectQuery(entity).getValue(infos.authoritiesFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
		// Should never be reached
		return null; 
	}

	public static boolean isAccountNonExpired(Object entity) throws GGAPIException {
		GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		try {
			return (boolean) GGObjectQueryFactory.objectQuery(entity).getValue(infos.isAccountNonExpiredFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
		// Should never be reached
		return false; 
	}

	public static boolean isAccountNonLocked(Object entity) throws GGAPIException {
		GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		try {
			return (boolean) GGObjectQueryFactory.objectQuery(entity).getValue(infos.isAccountNonLockedFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
		// Should never be reached
		return false; 
	}

	public static boolean isCredentialsNonExpired(Object entity) throws GGAPIException {
		GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		try {
			return (boolean) GGObjectQueryFactory.objectQuery(entity).getValue(infos.isCredentialsNonExpiredFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
		// Should never be reached
		return false; 
	}

	public static boolean isEnabled(Object entity) throws GGAPIException {
		GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		try {
			return (boolean) GGObjectQueryFactory.objectQuery(entity).getValue(infos.isEnabledFieldAddress());
		} catch (GGReflectionException e) {
			GGAPIException.processException(e);
		}
		// Should never be reached
		return false; 
	}

	public static boolean isAuthenticator(Object entity) {
		GGAPIAuthenticatorInfos infos = null;
		try {
			infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		} catch (GGAPIException e) {
			log.atDebug().log("Error during determining if entity of type "+entity.getClass().getSimpleName()+" is authenticator or not : "+e.getMessage());
			return false;
		}
		return infos!=null;
	}

	public static boolean isCreateAuthorization(Object entity) throws GGAPIException {
		GGAPIAuthenticatorInfos infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		if( infos.authorizationType() != void.class ) {
			return true;
		}
		return false;
	}

}
