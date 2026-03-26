package com.garganttua.api.spec.security.authentication;

import java.util.List;

public interface IAuthentication {

	boolean authenticated();

	Object principal();

	Object credentials();

	Object authorization();

	List<String> authorities();

	boolean credentialsNonExpired();

	boolean enabled();

	boolean accountNonLocked();

	boolean accountNonExpired();

}
