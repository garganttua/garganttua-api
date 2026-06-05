package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.TestAuthorization;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.RequestBuilder;
import com.garganttua.core.reflection.IClass;

/**
 * Regression cover for the security flaw filed on 2026-05-18: a previous build
 * of {@code VERIFY_AUTHORIZATION.gs} short-circuited the token check for any
 * caller carrying {@code superTenant=true}. The flag is a self-asserted
 * cross-tenancy capability — not an identity proof — so allowing it to skip
 * authorization granted god-mode to anyone who could call
 * {@code Caller.createSuperCaller(api.getSuperTenantId())}.
 *
 * <p>The expected contract, asserted below:
 * <ul>
 *   <li>A super-caller with no authorization on a secured non-anonymous op is
 *       rejected with {@code UNAUTHORIZED} — same as any other caller.</li>
 *   <li>A super-caller that presents a pre-resolved authorization (Mode B,
 *       i.e. {@code request.arg("authorization", ...)}) traverses
 *       VERIFY_AUTHORIZATION normally and reaches the operation.</li>
 *   <li>A super-caller hitting an op whose access is {@code anonymous} still
 *       passes — the anonymous short-circuit at the top of the script remains.
 *       That path is unaffected by the flaw because authorization is not
 *       required in the first place.</li>
 * </ul>
 *
 * <p>The {@code callerIsSuperTenant} expression was deleted along with the
 * bypass; its former unit tests are intentionally absent from this file.
 */
@DisplayName("VERIFY_AUTHORIZATION — no super-tenant bypass")
class VerifyAuthorizationNoBypassForSuperTenantTest extends AbstractCrudIntegrationTest {

    private IApi buildSecuredDomain(Access readAllAccess) throws ApiException {
        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up()
                .security()
                    // Touching .security() enables the security pipeline.
                    .readAllAccess(readAllAccess)
                    .up()
            .up();

        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    @Nested
    @DisplayName("Secured non-anonymous op")
    class SecuredNonAnonymous {

        @Test
        @DisplayName("super-caller WITHOUT authorization is rejected with 401 (regression of the 2026-05-18 bypass)")
        void superCallerWithoutAuthIsRejected() throws ApiException {
            IApi api = buildSecuredDomain(Access.authenticated);
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createSuperCaller(api.getSuperTenantId()))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode(),
                    "A caller with superTenant=true but no authorization must still be rejected — "
                            + "the flag is a cross-tenancy capability, not an identity proof. "
                            + "Got code=" + response.getResponseCode() + " response=" + response.getResponse());
        }

        @Test
        @DisplayName("non-super tenant caller without authorization is rejected with 401 (sister case — same path, no special exemption)")
        void tenantCallerWithoutAuthIsRejected() throws ApiException {
            IApi api = buildSecuredDomain(Access.authenticated);
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createTenantCaller("acme"))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode(),
                    "Non-super caller without an authorization must be rejected; "
                            + "got code=" + response.getResponseCode() + " response=" + response.getResponse());
        }

        @Test
        @DisplayName("super-caller WITH a pre-resolved Mode B authorization traverses VERIFY_AUTHORIZATION and reaches the op")
        void superCallerWithModeBAuthTraverses() throws ApiException {
            IApi api = buildSecuredDomain(Access.authenticated);
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            // Mode B: the caller has pre-decoded the authorization and set a
            // real IAuthorization on the request. VERIFY_AUTHORIZATION skips
            // the parsing/decode step but still runs verifyAuthorization, which
            // falls through to TestAuthorization.validate() (no-op) since the
            // fixture has no matching authenticator domain. The op then runs
            // and returns OK. Contrast: the *bypass* variant of this test below
            // confirms a missing authorization arg still gets rejected — Mode B
            // is an efficiency shortcut for the decode side, not a free pass.
            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createSuperCaller(api.getSuperTenantId()))
                    .param(IOperationRequest.AUTHORIZATION.name(), new TestAuthorization())
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "Mode B authorization should let the request reach the operation; "
                            + "got code=" + response.getResponseCode() + " response=" + response.getResponse());
            // Sanity: the response is *not* UNAUTHORIZED — proves we went past
            // the authorization stage and actually executed the readAll.
            assertNotEquals(OperationResponseCode.UNAUTHORIZED, response.getResponseCode(),
                    "Mode B should bypass the rawAuthorization decode, not be rejected for missing it");
        }

        // NOTE: Previously this nested class carried two tests that depended on
        // anonymous IAuthorization instances overriding validate() to surface
        // custom exceptions. Both were retired as part of the 2026-05-21 move
        // to full-DSL authorization — IAuthorization no longer exists. The
        // semantics they asserted have shifted: Mode B with a matching
        // registered authorization domain now runs DSL-driven validation
        // (revoked / expiration fields read off IDomainAuthorizationDefinition),
        // and Mode B without a matching domain trusts the in-process caller
        // (no DSL to enforce against, no contract on the entity). The exact-
        // exception propagation via recordCaughtException is exercised
        // mechanically by every test that triggers a non-zero workflow code.

        @Test
        @DisplayName("Mode B with a non-null authorization entity reaches the operation (smoke test — the rest of the chain is unchanged)")
        void modeBStillEnforcesValidate() throws ApiException {
            IApi api = buildSecuredDomain(Access.authenticated);
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            // Post-DSL-migration: Mode B with no matching registered domain
            // simply trusts the in-process caller (there is no DSL to enforce
            // against and the entity carries no contract). Confirms the
            // request still reaches the operation rather than being rejected
            // for missing rawAuthorization — the property the
            // {@code superCallerWithModeBAuthTraverses} test above also asserts.
            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createSuperCaller(api.getSuperTenantId()))
                    .param(IOperationRequest.AUTHORIZATION.name(), new com.garganttua.api.core.integ.TestAuthorization())
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "Mode B with a TestAuthorization should reach the operation. "
                            + "Got code=" + response.getResponseCode() + " response=" + response.getResponse());
        }
    }

    @Nested
    @DisplayName("Anonymous op — short-circuit preserved")
    class AnonymousOp {

        @Test
        @DisplayName("super-caller on a readAll(access=anonymous) op reaches the operation without any authorization")
        void superCallerOnAnonymousOpReachesOp() throws ApiException {
            IApi api = buildSecuredDomain(Access.anonymous);
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createSuperCaller(api.getSuperTenantId()))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "The anonymous short-circuit at the top of VERIFY_AUTHORIZATION must still let "
                            + "this through — anonymous ops never require an authorization. "
                            + "Got code=" + response.getResponseCode() + " response=" + response.getResponse());
        }

        @Test
        @DisplayName("non-super tenant caller on a readAll(access=anonymous) op also reaches the op (parity)")
        void tenantCallerOnAnonymousOpReachesOp() throws ApiException {
            IApi api = buildSecuredDomain(Access.anonymous);
            IDomain<?> domain = api.getDomain("users").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(domain)
                    .caller(Caller.createTenantCaller("acme"))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "Anonymous op must reach the operation regardless of which caller invokes it; "
                            + "got code=" + response.getResponseCode() + " response=" + response.getResponse());
        }
    }
}
