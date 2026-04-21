package com.garganttua.api.commons.security.authentication;

import java.util.List;

public record Authentication(
	boolean authenticated,
	Object principal,
	Object credentials,
	Object authorization,
	List<String> authorities,
	boolean credentialsNonExpired,
	boolean enabled,
	boolean accountNonLocked,
	boolean accountNonExpired) implements IAuthentication {

}
