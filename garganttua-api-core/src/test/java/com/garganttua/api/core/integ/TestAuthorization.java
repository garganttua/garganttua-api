package com.garganttua.api.core.integ;

/**
 * Minimal authorization-entity fixture for Mode B test paths.
 *
 * <p>Acts as a "trusted pre-decoded token" that {@code verifyAuthorization}
 * will accept: there is no matching registered domain in the test fixtures,
 * which is fine — {@code resolveOptionalAuthenticatorDomain} returns null
 * and {@code verifyAuthorization} accepts the entity without any further
 * server-side enforcement (the in-process caller is trusted). The fixture
 * deliberately has no fields so neither expiration nor revocation can
 * trigger, and no signature can be verified — keeps the focus on what
 * comes after VERIFY_AUTHORIZATION (authority enforcement, field-level
 * updates, etc.) without requiring full crypto / authenticator scaffolding.
 *
 * <p>Replace with a real implementation when a test specifically exercises
 * signature verification or the authenticate pipeline.
 */
public final class TestAuthorization {
}
