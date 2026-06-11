package com.garganttua.api.core.integ.security;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.security.authentication.Authentication;
import com.garganttua.api.commons.security.authentication.IAuthentication;

/**
 * Simulates an authentication method (like LoginPasswordAuthentication).
 * Compares credentials against a hardcoded password.
 */
public class StubAuthentication {

    public IAuthentication authenticate(Object principal, byte[] credentials, IAuthenticatorDefinition definition) {
        String password = new String(credentials, StandardCharsets.UTF_8);
        boolean success = "valid-password".equals(password);
        return new Authentication(
                success,
                success ? principal : null,
                credentials,
                success ? "auth-token" : null,
                success ? List.of("ROLE_USER") : null,
                null, null, false, false, // tenantId, ownerId, isSuperTenant, isSuperOwner
                true, true, true, true);
    }
}
