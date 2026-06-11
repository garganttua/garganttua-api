package com.garganttua.api.core.integ.security;

import java.util.List;

import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.security.authentication.Authentication;
import com.garganttua.api.commons.security.authentication.IAuthentication;

/**
 * Test authentication strategy for an <em>authorization</em> (token) domain.
 *
 * <p>Since {@code verifyAuthorization} was unified with the authenticate
 * pipeline, a token verifies <em>itself</em>: its domain is an authenticator
 * whose authenticate method receives the looked-up token as {@code principal}
 * and the decoded token as {@code credentials}. The framework already enforces
 * expiration / revocation before this runs and resolves the owner as the final
 * principal afterwards, so for the happy path this stub simply succeeds. The
 * {@code signaturePresent} switch lets a verification test exercise the
 * "user-side signature check rejects" branch (returns a failed authentication →
 * 401) without standing up real crypto.
 */
public class StubTokenAuthentication {

    private final boolean reject;

    public StubTokenAuthentication() {
        this(false);
    }

    public StubTokenAuthentication(boolean reject) {
        this.reject = reject;
    }

    public IAuthentication authenticate(Object principal, Object credentials, IAuthenticatorDefinition definition) {
        boolean success = !reject;
        return new Authentication(
                success,
                success ? principal : null,
                credentials,
                success ? "token" : null,
                success ? List.of() : null,
                null, null, false, false, // tenantId, ownerId, isSuperTenant, isSuperOwner
                true, true, true, true);
    }
}
