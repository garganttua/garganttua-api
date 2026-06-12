package com.garganttua.api.core.security.authentication;

import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.core.reflection.annotations.Reflected;

/**
 * The wire record for an authentication request. {@code @Reflected} so the AOT
 * pipeline emits a descriptor for its canonical constructor and accessors — the
 * authenticate body is deserialized/instantiated by name, which under
 * native-image otherwise fails with "Cannot resolve class: AuthenticationRequest"
 * unless the consumer writes reflect-config by hand.
 */
@Reflected(queryAllDeclaredConstructors = true, queryAllDeclaredMethods = true, allDeclaredFields = true)
public record AuthenticationRequest(
	String login,
	Object credentials) implements IAuthenticationRequest {

}
