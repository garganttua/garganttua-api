package com.garganttua.api.core.integ;

import com.garganttua.api.commons.security.authorization.IAuthorization;

/**
 * Minimal {@link IAuthorization} fixture for Mode B test paths.
 *
 * <p>Acts as a "trusted pre-decoded token" that {@code verifyAuthorization}
 * will accept: there is no matching registered domain in the test fixtures,
 * which is fine — {@code resolveOptionalAuthenticatorDomain} returns null
 * and {@code verifyAuthorization} falls through to {@code validate()}. Both
 * {@code validate} methods are deliberate no-ops so the test focuses on
 * what comes after VERIFY_AUTHORIZATION (authority enforcement, field-level
 * updates, etc.) without requiring full crypto / authenticator scaffolding.
 *
 * <p>Replace with a real implementation when a test specifically exercises
 * signature verification or the authenticate pipeline.
 */
public final class TestAuthorization implements IAuthorization {

	@Override public void revoke() {}
	@Override public void isRevoked() {}
	@Override public void isExpired() {}
	@Override public void validateAgainst(IAuthorization reference, Object... args) {}
	@Override public void validate(Object... args) {}
}
