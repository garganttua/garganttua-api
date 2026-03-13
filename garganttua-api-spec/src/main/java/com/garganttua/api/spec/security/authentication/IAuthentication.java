package com.garganttua.api.spec.security.authentication;

import java.util.List;

import com.garganttua.core.CoreException;

public interface IAuthentication {

	void authenticate() throws CoreException;

	void findPrincipal() throws CoreException;

	boolean isAuthenticated();

	Object getPrincipal();

	Object getCredential();

	Object getAuthorization();

	List<String> getAuthorities();

	String getTenantId();

	String getOwnerId();

}
