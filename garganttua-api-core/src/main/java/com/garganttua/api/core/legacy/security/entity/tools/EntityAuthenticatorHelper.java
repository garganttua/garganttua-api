package com.garganttua.api.core.legacy.security.entity.tools;

import java.util.List;

import com.garganttua.api.core.context.InfosHelper;
import com.garganttua.api.core.legacy.security.entity.checker.EntityAuthenticatorChecker;
import com.garganttua.core.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.domain.IDomainsRegistry;
import com.garganttua.api.spec.security.annotations.Authenticator;
import com.garganttua.api.spec.security.authenticator.AuthenticatorInfos;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntityAuthenticatorHelper {
	
	public static IDomain getAuthenticatorDomain(IDomainsRegistry domainsRegistry) {
		return domainsRegistry.getDomains().parallelStream().filter(e -> 
			e.getEntityClass().getAnnotation(Authenticator.class)==null?false:true
		).findFirst().get();
	}

	public static List<String> getAuthorities(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				AuthenticatorInfos::authoritiesFieldAddress);
	}

	public static Boolean isAccountNonExpired(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				AuthenticatorInfos::isAccountNonExpiredFieldAddress);
	}

	public static Boolean isAccountNonLocked(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				AuthenticatorInfos::isAccountNonLockedFieldAddress);
	}

	public static Boolean isCredentialsNonExpired(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				AuthenticatorInfos::isCredentialsNonExpiredFieldAddress);
	}

	public static Boolean isEnabled(Object entity) throws CoreException {
		return InfosHelper.getValue(entity, EntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				AuthenticatorInfos::isEnabledFieldAddress);
	}

	public static boolean isAuthenticator(Object entity) {
		AuthenticatorInfos infos = null;
		try {
			infos = EntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		} catch (CoreException e) {
			log.atDebug().log("Error during determining if entity of type "+entity.getClass().getSimpleName()+" is authenticator or not : "+e.getMessage());
			return false;
		}
		return infos!=null;
	}
	
	public static boolean isAuthenticator(Class<?> entityClass) {
		AuthenticatorInfos infos = null;
		try {
			infos = EntityAuthenticatorChecker.checkEntityAuthenticatorClass(entityClass);
		} catch (CoreException e) {
			log.atDebug().log("Error during determining if entity of type "+entityClass.getSimpleName()+" is authenticator or not : "+e.getMessage());
			return false;
		}
		return infos!=null;
	}

	public static boolean isCreateAuthorization(Object entity) throws CoreException {
		AuthenticatorInfos infos = EntityAuthenticatorChecker.checkEntityAuthenticator(entity);
		if( infos.authorizationType() != void.class ) {
			return true;
		}
		return false;
	}

	public static void setCredentialsNonExpired(Object entity, Boolean b) throws CoreException {
		InfosHelper.setValue(entity, EntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				AuthenticatorInfos::isCredentialsNonExpiredFieldAddress, b);
	}

	public static void setAccountNonlocked(Object entity, Boolean b) throws CoreException {
		InfosHelper.setValue(entity, EntityAuthenticatorChecker::checkEntityAuthenticatorClass,
				AuthenticatorInfos::isAccountNonLockedFieldAddress, b);
	}

}
