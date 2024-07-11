package com.garganttua.api.security.core.entity.checker;

import com.garganttua.api.spec.security.GGAPIEntitySecurityInfos;
import com.garganttua.api.spec.security.annotations.GGAPIEntitySecurity;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GGAPIEntitySecurityChecker {

	public static GGAPIEntitySecurityInfos checkEntityClass(Class<?> entityClass) {
		if (log.isDebugEnabled()) {
			log.debug("Checking entity security infos from class " + entityClass.getName());
		}
		GGAPIEntitySecurity annotation = entityClass.getAnnotation(GGAPIEntitySecurity.class);
		if( annotation == null ) {
			return new GGAPIEntitySecurityInfos(
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
					true);
		}

		return new GGAPIEntitySecurityInfos(
				annotation.creation_access(), 
				annotation.read_all_access(), 
				annotation.read_one_access(), 
				annotation.update_one_access(), 
				annotation.delete_all_access(), 
				annotation.delete_one_access(), 
				annotation.count_access(), 
				annotation.creation_authority(), 
				annotation.read_all_authority(),
				annotation.read_one_authority(), 
				annotation.update_one_authority(), 
				annotation.delete_all_authority(), 
				annotation.delete_one_authority(), 
				annotation.count_authority()
				);
	}

}
