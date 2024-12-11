package com.garganttua.api.core.security.entity.tools;

import java.util.List;

import com.garganttua.api.core.GGAPIInfosHelper;
import com.garganttua.api.core.security.entity.checker.GGAPIEntityAuthenticatorChecker;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.authenticator.GGAPIAuthenticatorInfos;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntityAuthenticatorHelper {
	
	public static IGGAPIDomain getAuthenticatorDomain(IGGAPIDomainsRegistry domainsRegistry) {
		return domainsRegistry.getDomains().parallelStream().filter(e -> 
			e.getEntity().getValue0().getAnnotation(GGAPIAuthenticator.class)==null?false:true
		).findFirst().get();
	}

	public static List<String> getAuthorities(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				GGAPIAuthenticatorInfos::authoritiesFieldAddress);
	}

	public static boolean isAccountNonExpired(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				GGAPIAuthenticatorInfos::isAccountNonExpiredFieldAddress);
	}

	public static boolean isAccountNonLocked(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				GGAPIAuthenticatorInfos::isAccountNonLockedFieldAddress);
	}

	public static boolean isCredentialsNonExpired(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				GGAPIAuthenticatorInfos::isCredentialsNonExpiredFieldAddress);
	}

	public static boolean isEnabled(Object entity) throws GGAPIException {
		return GGAPIInfosHelper.getValue(entity, GGAPIEntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				GGAPIAuthenticatorInfos::isEnabledFieldAddress);
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
	
	public static boolean isAuthenticator(Class<?> entityClass) {
		GGAPIAuthenticatorInfos infos = null;
		try {
			infos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entityClass);
		} catch (GGAPIException e) {
			log.atDebug().log("Error during determining if entity of type "+entityClass.getSimpleName()+" is authenticator or not : "+e.getMessage());
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

	public static void setCredentialsNonExpired(Object entity, boolean b) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				GGAPIAuthenticatorInfos::isCredentialsNonExpiredFieldAddress, b);
	}

	public static void setAccountNonlocked(Object entity, boolean b) throws GGAPIException {
		GGAPIInfosHelper.setValue(entity, GGAPIEntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				GGAPIAuthenticatorInfos::isAccountNonLockedFieldAddress, b);
	}

}
