package com.garganttua.api.core.security.authentication.entity.tools;

import java.util.List;

import com.garganttua.api.core.domain.GGAPIDomain;
import com.garganttua.api.core.objects.GGAPIObjectAddress;
import com.garganttua.api.core.security.authentication.entity.annotations.GGAPIAuthenticator;
import com.garganttua.api.spec.domain.IGGAPIDomainsRegistry;

public class GGAPIEntityAuthenticatorHelper {
	
	public static GGAPIDomain getAuthenticatorDomain(IGGAPIDomainsRegistry domainsRegistry) {
		return domainsRegistry.getDomains().parallelStream().filter(e -> 
			e.entity.getValue0().getAnnotation(GGAPIAuthenticator.class)==null?false:true
		).findFirst().get();
	}


	public static String getLogin(GGAPIObjectAddress loginField, Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String getPassword(GGAPIObjectAddress passwordField, Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public static List<String> getAuthorities(GGAPIObjectAddress authoritiesField, Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public static boolean isAccountNonExpired(GGAPIObjectAddress isAccountNonExpiredField, Object entity) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean isAccountNonLocked(GGAPIObjectAddress isAccountNonLockedField, Object entity) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean isCredentialsNonExpired(GGAPIObjectAddress isCredentialsNonExpiredField, Object entity) {
		// TODO Auto-generated method stub
		return false;
	}

	public static boolean isEnabled(GGAPIObjectAddress isEnabledField, Object entity) {
		// TODO Auto-generated method stub
		return false;
	}

}
