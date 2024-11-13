package com.garganttua.api.security.core.entity.checker;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.GGAPIAuthenticatorInfos;
import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.security.annotations.GGAPIEntitySecurity;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntitySecurityChecker {
	
	private static Map<Class<?>, GGAPIEntitySecurityInfos> infos = new HashMap<Class<?>, GGAPIEntitySecurityInfos>();

	public static GGAPIEntitySecurityInfos checkEntityClass(Class<?> entityClass, String domainName) throws GGAPIException {
		if( GGAPIEntitySecurityChecker.infos.containsKey(entityClass) ) {
			return GGAPIEntitySecurityChecker.infos.get(entityClass);
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Checking entity security infos from class " + entityClass.getSimpleName());
		}
		GGAPIEntitySecurityInfos infos = null;
		GGAPIEntitySecurity annotation = entityClass.getAnnotation(GGAPIEntitySecurity.class);
		GGAPIAuthenticatorInfos authenticatorInfos = null;
		if( entityClass.getAnnotation(GGAPIAuthenticator.class) != null ) {
			authenticatorInfos = GGAPIEntityAuthenticatorChecker.checkEntityAuthenticatorClass(entityClass);
		}

		if( annotation == null ) {
			infos = new GGAPIEntitySecurityInfos(
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				GGAPIServiceAccess.tenant, 
				true, 
				true, 
				true, 
				true, 
				true, 
				true, 
				true,
				authenticatorInfos,
				domainName
			);
		} else {
			infos = new GGAPIEntitySecurityInfos(
				annotation.creation_access(), 
				annotation.read_all_access(), 
				annotation.read_one_access(), 
				annotation.update_one_access(), 
				annotation.delete_all_access(), 
				annotation.delete_one_access(), 
				annotation.count_access(), 
				annotation.creation_access()==GGAPIServiceAccess.anonymous?false:annotation.creation_authority(), 
				annotation.read_all_access()==GGAPIServiceAccess.anonymous?false:annotation.read_all_authority(),
				annotation.read_one_access()==GGAPIServiceAccess.anonymous?false:annotation.read_one_authority(), 
				annotation.update_one_access()==GGAPIServiceAccess.anonymous?false:annotation.update_one_authority(), 
				annotation.delete_all_access()==GGAPIServiceAccess.anonymous?false:annotation.delete_all_authority(), 
				annotation.delete_one_access()==GGAPIServiceAccess.anonymous?false:annotation.delete_one_authority(), 
				annotation.count_access()==GGAPIServiceAccess.anonymous?false:annotation.count_authority(),
				authenticatorInfos,
				domainName
			);
		}
		GGAPIEntitySecurityChecker.infos.put(entityClass, infos);
		return infos;
	}

}
