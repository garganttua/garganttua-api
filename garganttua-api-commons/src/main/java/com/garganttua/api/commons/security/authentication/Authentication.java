package com.garganttua.api.commons.security.authentication;

import java.util.List;

import com.garganttua.core.reflection.annotations.Reflected;

/**
 * Authentication result carrier. Reflectively accessed at runtime (its
 * components are read back through the reflection layer), so it must ship full
 * AOT descriptors for native-image — equivalent to a manual reflect-config.json
 * entry with allDeclaredMethods / allDeclaredConstructors / allDeclaredFields.
 * The {@code @Reflected} flags below let the AOT annotation processor generate
 * those descriptors automatically; {@code ApiCommonsInfrastructureSeed}
 * registers the class for pure-AOT resolution.
 */
@Reflected(queryAllDeclaredMethods = true, queryAllDeclaredConstructors = true, allDeclaredFields = true)
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
