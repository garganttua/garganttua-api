package com.garganttua.api.core.security.authentication;

import java.util.List;

import com.garganttua.api.spec.security.authentication.IAuthentication;

public record AbstractAuthentication(
	boolean authenticated,
	boolean alwaysEnabled,
	Object principal,
	Object credentials,
	Object authorization,
	List<String> authorities,
	boolean credentialsNonExpired,
	boolean enabled,
	boolean accountNonLocked,
	boolean accountNonExpired) implements IAuthentication {

}
