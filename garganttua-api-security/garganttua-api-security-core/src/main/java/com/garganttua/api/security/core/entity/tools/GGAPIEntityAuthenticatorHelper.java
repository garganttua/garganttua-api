package com.garganttua.api.security.core.entity.tools;

import java.util.List;

import com.garganttua.api.security.core.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.security.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;

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

}
