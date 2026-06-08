package com.garganttua.api.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pins {@link Domain#defaultMessageForCode(Integer, String, String)} — the
 * parlant fallback that Domain.invoke uses when the workflow returns a
 * non-zero code with no exception message attached. Replaces the old
 * unhelpful "Workflow execution failed" string with a context-aware
 * message naming the operation, the domain, and what the code means.
 *
 * Lives in the {@code .context} package so it can reach the
 * package-private helper directly without reflection.
 */
@DisplayName("Domain.defaultMessageForCode — parlant fallbacks for empty exception messages")
class DomainErrorMessageTest {

    /**
     * IOperationRequest's static initializer wires its ArgKey constants via
     * {@code IClass.getClass(...)} — which requires {@code IClass.setReflection(...)}
     * to have been called. The integration tests do this through their
     * builder fixture; here we set it up directly so the ReplayValidator
     * tests can build a stub request without crashing.
     */
    @BeforeAll
    static void initReflection() {
        com.garganttua.core.reflection.IClass.setReflection(
                com.garganttua.core.reflection.dsl.ReflectionBuilder.builder()
                        .withProvider(new com.garganttua.core.reflection.runtime.RuntimeReflectionProvider())
                        .build());
    }

    @Nested
    @DisplayName("Known HTTP-like codes")
    class KnownCodes {

        @Test
        @DisplayName("400 -> 'Bad request' naming the op and the domain")
        void code400() {
            String msg = Domain.defaultMessageForCode(400, "createOne", "users");
            assertTrue(msg.contains("Bad request"), "must lead with 'Bad request'; got: " + msg);
            assertTrue(msg.contains("createOne"), "must name the operation; got: " + msg);
            assertTrue(msg.contains("users"), "must name the domain; got: " + msg);
            assertNotGeneric(msg);
        }

        @Test
        @DisplayName("401 -> 'Authorization required' naming the op and the domain")
        void code401() {
            String msg = Domain.defaultMessageForCode(401, "readOne", "users");
            assertTrue(msg.contains("Authorization required"),
                    "must mention 'Authorization required'; got: " + msg);
            assertTrue(msg.contains("readOne") && msg.contains("users"),
                    "must name the operation and the domain; got: " + msg);
            assertNotGeneric(msg);
        }

        @Test
        @DisplayName("403 -> 'Forbidden' naming the missing privilege context")
        void code403() {
            String msg = Domain.defaultMessageForCode(403, "deleteOne", "invoices");
            assertTrue(msg.contains("Forbidden"), "must lead with 'Forbidden'; got: " + msg);
            assertTrue(msg.contains("caller lacks the privilege"),
                    "must explain the cause; got: " + msg);
            assertTrue(msg.contains("deleteOne") && msg.contains("invoices"),
                    "must name the operation and the domain; got: " + msg);
            assertNotGeneric(msg);
        }

        @Test
        @DisplayName("404 -> 'Not found' naming the operation context")
        void code404() {
            String msg = Domain.defaultMessageForCode(404, "readOne", "users");
            assertTrue(msg.contains("Not found"), "must lead with 'Not found'; got: " + msg);
            assertTrue(msg.contains("no matching resource"),
                    "must explain the cause; got: " + msg);
            assertTrue(msg.contains("readOne") && msg.contains("users"),
                    "must name the operation and the domain; got: " + msg);
            assertNotGeneric(msg);
        }

        @Test
        @DisplayName("409 -> 'Conflict' naming the operation that could not be applied")
        void code409() {
            String msg = Domain.defaultMessageForCode(409, "createOne", "users");
            assertTrue(msg.contains("Conflict"), "must lead with 'Conflict'; got: " + msg);
            assertTrue(msg.contains("could not be applied"),
                    "must explain the cause; got: " + msg);
            assertTrue(msg.contains("createOne") && msg.contains("users"),
                    "must name the operation and the domain; got: " + msg);
            assertNotGeneric(msg);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class Edges {

        @Test
        @DisplayName("Unknown numeric code -> message includes the code value and the context")
        void unknownCode() {
            String msg = Domain.defaultMessageForCode(418, "useCase", "teapot");
            assertTrue(msg.contains("418"), "must include the actual code; got: " + msg);
            assertTrue(msg.contains("useCase") && msg.contains("teapot"),
                    "must name the operation and the domain; got: " + msg);
            assertNotGeneric(msg);
        }

        @Test
        @DisplayName("Null code -> falls back to a generic-but-still-parlant message")
        void nullCode() {
            String msg = Domain.defaultMessageForCode(null, "readAll", "users");
            assertTrue(msg.contains("readAll") && msg.contains("users"),
                    "even without a code we must still name the operation and the domain; got: " + msg);
            assertNotGeneric(msg);
        }

        @Test
        @DisplayName("All codes produce a non-blank message")
        void allCodesNonBlank() {
            int[] codes = {400, 401, 403, 404, 409, 500, 422, 0};
            for (int code : codes) {
                String msg = Domain.defaultMessageForCode(code, "op", "dom");
                assertFalse(msg == null || msg.isBlank(),
                        "code " + code + " produced a blank message");
            }
        }

        @Test
        @DisplayName("Each code yields a DISTINCT canned text (no copy-paste between branches)")
        void distinctMessagesPerCode() {
            String m400 = Domain.defaultMessageForCode(400, "op", "dom");
            String m401 = Domain.defaultMessageForCode(401, "op", "dom");
            String m403 = Domain.defaultMessageForCode(403, "op", "dom");
            String m404 = Domain.defaultMessageForCode(404, "op", "dom");
            String m409 = Domain.defaultMessageForCode(409, "op", "dom");
            assertEquals(5, java.util.Set.of(m400, m401, m403, m404, m409).size(),
                    "each well-known code must produce its own canned text — found "
                            + "duplicates between 400/401/403/404/409");
        }
    }

    @Nested
    @DisplayName("Stage-aware functional hints (the path mon général flagged on 2026-05-19)")
    class StageHints {

        @Test
        @DisplayName("owner_rules + 400 -> 'Owner rules failed — required ownerId missing'")
        void ownerRulesIs400() {
            String hint = Domain.stageFunctionalHint("owner_rules_owner_rules", 400);
            assertTrue(hint != null && hint.contains("Owner rules failed"),
                    "must produce a parlant owner-rules hint; got: " + hint);
            assertTrue(hint.contains("ownerId missing"),
                    "must explicitly mention the missing ownerId; got: " + hint);
        }

        @Test
        @DisplayName("tenant_rules + 400 -> 'Tenant rules failed — required tenantId missing'")
        void tenantRulesIs400() {
            String hint = Domain.stageFunctionalHint("tenant_rules_tenant_rules", 400);
            assertTrue(hint != null && hint.contains("Tenant rules failed"),
                    "must produce a parlant tenant-rules hint; got: " + hint);
            assertTrue(hint.contains("tenantId missing"),
                    "must explicitly mention the missing tenantId; got: " + hint);
        }

        @Test
        @DisplayName("verify_authorization + 401 -> 'Authorization required' with the missing/malformed hint")
        void verifyAuthorizationIs401() {
            String hint = Domain.stageFunctionalHint("verify_authorization_verify_authorization", 401);
            assertTrue(hint != null && hint.contains("Authorization required"),
                    "must mention authorization being required; got: " + hint);
            assertTrue(hint.contains("missing") || hint.contains("rejected"),
                    "should hint at the actual failure mode; got: " + hint);
        }

        @Test
        @DisplayName("verify_owner -> 'caller is not the owner of the resource'")
        void verifyOwner() {
            String hint = Domain.stageFunctionalHint("verify_owner_verify_owner", 403);
            assertTrue(hint != null && hint.contains("Owner verification"),
                    "must mention owner verification; got: " + hint);
            assertTrue(hint.contains("not the owner"),
                    "must explain WHY (caller is not the owner); got: " + hint);
        }

        @Test
        @DisplayName("verify_authority -> 'caller lacks the required authority'")
        void verifyAuthority() {
            String hint = Domain.stageFunctionalHint("verify_authority_verify_authority", 403);
            assertTrue(hint != null && hint.contains("lacks the required authority"),
                    "must mention the missing authority; got: " + hint);
        }

        @Test
        @DisplayName("null stage key returns null (lets the caller fall back to per-code default)")
        void nullStage() {
            assertEquals(null, Domain.stageFunctionalHint(null, 400),
                    "null stage must return null, not a hint");
        }

        @Test
        @DisplayName("unknown stage key returns null (graceful fall-through)")
        void unknownStage() {
            assertEquals(null, Domain.stageFunctionalHint("some_random_stage", 400),
                    "unknown stages must return null so the caller falls back");
        }
    }

    @Nested
    @DisplayName("functionalMessage (synthesis-only fallback)")
    class FunctionalMessage {

        @Test
        @DisplayName("known stage + code -> message names the FUNCTIONAL cause (not 'rejected by validation')")
        void picksUpStageHint() {
            String msg = Domain.functionalMessage("owner_rules_owner_rules", 400,
                    "deleteAll", "authorizations");

            assertTrue(msg.contains("Owner rules failed"),
                    "must name the functional cause (Owner rules failed); got: " + msg);
            assertTrue(msg.contains("ownerId missing"),
                    "must name what's missing (ownerId); got: " + msg);
            assertTrue(msg.contains("deleteAll") && msg.contains("authorizations"),
                    "must still name the op and the domain for context; got: " + msg);
            assertFalse(msg.contains("rejected by validation"),
                    "must NOT fall back to the generic per-code line when a stage hint is available; got: "
                            + msg);
        }

        @Test
        @DisplayName("unknown stage -> falls back to defaultMessageForCode")
        void fallsBackWhenNoStageMarker() {
            String msg = Domain.functionalMessage(null, 404, "readOne", "users");

            assertTrue(msg.contains("Not found"),
                    "must use the per-code default; got: " + msg);
            assertTrue(msg.contains("readOne") && msg.contains("users"),
                    "must still carry op + domain context; got: " + msg);
        }

        @Test
        @DisplayName("findFailingStage skips zero codes and picks the first non-zero one")
        void findFailingStageSkipsZero() {
            java.util.Map<String, Object> vars = new java.util.LinkedHashMap<>();
            vars.put("_verify_tenant_verify_tenant_code", 0);
            vars.put("_owner_rules_owner_rules_code", 400);
            vars.put("_verify_owner_verify_owner_code", 0);
            java.time.Instant now = java.time.Instant.now();
            com.garganttua.core.workflow.WorkflowResult result =
                    new com.garganttua.core.workflow.WorkflowResult(
                            java.util.UUID.randomUUID(), null, 400, vars,
                            java.util.Map.of(), now, now,
                            java.util.Optional.empty(), java.util.Optional.empty());

            String stage = Domain.findFailingStage(result).orElse(null);
            assertTrue(stage != null && stage.startsWith("owner_rules"),
                    "must pick the non-zero owner_rules stage, not the zero ones; got: " + stage);
        }
    }

    @Nested
    @DisplayName("autoCreateCallerFromBody — body-aware materialization")
    class AutoCreateCallerFromBody {

        private com.garganttua.api.commons.service.IOperationRequest requestWithBody(Object body) {
            return new com.garganttua.api.commons.service.IOperationRequest() {
                @Override public java.util.Map<String, Object> args() { return java.util.Map.of(); }
                @SuppressWarnings("unchecked")
                @Override
                public <T> java.util.Optional<T> arg(
                        com.garganttua.api.commons.service.ArgKey<T> key) {
                    if (key == com.garganttua.api.commons.service.IOperationRequest.BODY) {
                        return java.util.Optional.ofNullable((T) body);
                    }
                    return java.util.Optional.empty();
                }
                @Override public <T> void arg(
                        com.garganttua.api.commons.service.ArgKey<T> key, T value) { /* no-op */ }
                @Override public String domain() { return null; }
                @Override public com.garganttua.api.commons.caller.ICaller caller() { return null; }
                @Override public com.garganttua.api.commons.operation.OperationDefinition operation() { return null; }
                @Override public com.garganttua.api.commons.operation.OperationPath operationPath() { return null; }
                @Override public java.util.UUID executionUuid() { return null; }
                @Override public java.util.UUID correlationUuid() { return null; }
            };
        }

        @Test
        @DisplayName("IAuthenticationRequest body -> anonymous caller (the tenant is on the caller, not the body)")
        void authRequestBodyYieldsAnonymous() {
            com.garganttua.api.commons.security.authentication.IAuthenticationRequest auth =
                    new com.garganttua.api.core.security.authentication.AuthenticationRequest(
                            "alice@acme", new byte[]{1, 2, 3});

            com.garganttua.api.commons.caller.ICaller caller =
                    Domain.autoCreateCallerFromBody(requestWithBody(auth));

            // The tenant of a tenant-scoped login is carried by the caller (over HTTP,
            // the X-Tenant-Id header) — it is no longer read from the request body — so
            // an AuthenticationRequest body yields the bare anonymous caller.
            assertTrue(Domain.isEmptyCaller(caller),
                    "an AuthenticationRequest body must NOT pin a tenant on the caller; got tenantId="
                            + caller.tenantId());
        }

        @Test
        @DisplayName("non-AuthenticationRequest body (eg a User entity) -> anonymous")
        void nonAuthBody() {
            Object userBody = new Object();  // arbitrary, NOT IAuthenticationRequest

            com.garganttua.api.commons.caller.ICaller caller =
                    Domain.autoCreateCallerFromBody(requestWithBody(userBody));

            assertTrue(Domain.isEmptyCaller(caller),
                    "non-auth body must fall through to anonymous — the tenantId-pinning behavior "
                            + "is scoped to IAuthenticationRequest, not all bodies; got: " + caller);
        }

        @Test
        @DisplayName("null body -> anonymous")
        void nullBody() {
            com.garganttua.api.commons.caller.ICaller caller =
                    Domain.autoCreateCallerFromBody(requestWithBody(null));

            assertTrue(Domain.isEmptyCaller(caller),
                    "null body must yield a plain anonymous caller; got: " + caller);
        }

        @Test
        @DisplayName("null request -> anonymous (defensive)")
        void nullRequest() {
            com.garganttua.api.commons.caller.ICaller caller =
                    Domain.autoCreateCallerFromBody(null);

            assertTrue(Domain.isEmptyCaller(caller),
                    "null request must yield a plain anonymous caller; got: " + caller);
        }
    }

    @Nested
    @DisplayName("isEmptyCaller — detects 'no real caller' for the anonymous-swap path")
    class IsEmptyCaller {

        @Test
        @DisplayName("anonymous caller is empty (swap is idempotent)")
        void anonymousIsEmpty() {
            assertTrue(Domain.isEmptyCaller(
                            com.garganttua.api.core.caller.Caller.createAnonymousCaller()),
                    "createAnonymousCaller() must be detected as empty so swapping it back in "
                            + "would be a no-op (no risk of infinite loops or surprise mutation)");
        }

        @Test
        @DisplayName("null caller is empty (defensive)")
        void nullIsEmpty() {
            assertTrue(Domain.isEmptyCaller(null),
                    "null caller must be detected as empty so the auto-swap covers the "
                            + "edge case where caller() itself returns null");
        }

        @Test
        @DisplayName("tenant caller is NOT empty")
        void tenantCallerIsNotEmpty() {
            assertFalse(Domain.isEmptyCaller(
                            com.garganttua.api.core.caller.Caller.createTenantCaller("acme")),
                    "a caller with a tenantId carries meaningful info — must NOT be detected as empty");
        }

        @Test
        @DisplayName("caller with just an ownerId is NOT empty")
        void ownerOnlyIsNotEmpty() {
            com.garganttua.api.commons.caller.ICaller ownerOnly =
                    new com.garganttua.api.core.caller.Caller(
                            null, null, null, "user-1", false, false, null);
            assertFalse(Domain.isEmptyCaller(ownerOnly),
                    "an ownerId by itself is meaningful info — must NOT be detected as empty");
        }

        @Test
        @DisplayName("malformed super caller (null tenantId + superTenant=true) is NOT empty")
        @SuppressWarnings("deprecation")
        void malformedSuperIsNotEmpty() {
            assertFalse(Domain.isEmptyCaller(
                            com.garganttua.api.core.caller.Caller.createSuperCaller()),
                    "the super flag alone marks a non-empty caller — even with null tenantId — so "
                            + "the deprecated factory's misuse path routes to the explicit "
                            + "rejection branch, not to the anonymous swap");
        }

        @Test
        @DisplayName("caller with authorities only is NOT empty")
        void authoritiesOnlyIsNotEmpty() {
            com.garganttua.api.commons.caller.ICaller authoritiesOnly =
                    new com.garganttua.api.core.caller.Caller(
                            null, null, null, null, false, false, java.util.List.of("admin"));
            assertFalse(Domain.isEmptyCaller(authoritiesOnly),
                    "a non-empty authorities list is meaningful — must NOT be detected as empty");
        }
    }

    @Nested
    @DisplayName("tryReplayValidator — recovers the EXACT exception the script would have thrown")
    class ReplayValidator {

        /**
         * Stub {@link com.garganttua.api.commons.service.IOperationRequest}
         * that only implements {@link IOperationRequest#caller()}. Sidesteps
         * the static initialization on the concrete {@code OperationRequest}
         * (which pulls {@code IClass.getClass(...)} via ArgKey and requires
         * {@code IClass.setReflection()} — not set in a pure unit test).
         */
        private com.garganttua.api.commons.service.IOperationRequest requestWith(
                String tenantId, String ownerId) {
            com.garganttua.api.commons.caller.ICaller caller =
                    new com.garganttua.api.core.caller.Caller(
                            tenantId, tenantId, null, ownerId, false, false, null);
            return new com.garganttua.api.commons.service.IOperationRequest() {
                @Override public java.util.Map<String, Object> args() { return java.util.Map.of(); }
                @Override public <T> java.util.Optional<T> arg(
                        com.garganttua.api.commons.service.ArgKey<T> key) { return java.util.Optional.empty(); }
                @Override public <T> void arg(
                        com.garganttua.api.commons.service.ArgKey<T> key, T value) { /* no-op */ }
                @Override public String domain() { return null; }
                @Override public com.garganttua.api.commons.caller.ICaller caller() { return caller; }
                @Override public com.garganttua.api.commons.operation.OperationDefinition operation() { return null; }
                @Override public com.garganttua.api.commons.operation.OperationPath operationPath() { return null; }
                @Override public java.util.UUID executionUuid() { return null; }
                @Override public java.util.UUID correlationUuid() { return null; }
            };
        }

        @Test
        @DisplayName("owner_rules + 400 with caller missing ownerId -> ApiException 'Owner ID is required for this operation' (same as requireOwnerId)")
        void ownerRulesReplayMatchesRequireOwnerId() {
            // Caller has tenantId but NO ownerId — the exact scenario that
            // triggers requireOwnerId() to throw in the script.
            com.garganttua.api.commons.service.IOperationRequest request = requestWith("acme", null);

            Throwable replayed = Domain.tryReplayValidator("owner_rules_owner_rules", 400, request);

            assertNotNull(replayed,
                    "the replay must surface the exception that requireOwnerId would have thrown");
            assertTrue(replayed instanceof com.garganttua.api.commons.ApiException,
                    "the replayed exception must be the same type as the script-side helper would throw "
                            + "(ApiException); got: " + replayed.getClass());
            assertEquals("Owner ID is required for this operation", replayed.getMessage(),
                    "the replayed exception's message must MATCH VERBATIM the wording from "
                            + "SecurityExpressions.requireOwnerId — so the operator sees the same "
                            + "functional explanation, not a synthesized one");
        }

        @Test
        @DisplayName("tenant_rules + 400 with caller missing tenantId -> 'Tenant ID is required for this operation'")
        void tenantRulesReplayMatchesRequireTenantId() {
            com.garganttua.api.commons.service.IOperationRequest request = requestWith(null, null);

            Throwable replayed = Domain.tryReplayValidator("tenant_rules_tenant_rules", 400, request);

            assertNotNull(replayed,
                    "tenant_rules replay must produce an exception when tenantId is missing");
            assertEquals("Tenant ID is required for this operation", replayed.getMessage(),
                    "the replayed exception must mirror SecurityExpressions.requireTenantId wording");
        }

        @Test
        @DisplayName("owner_rules + 400 with a caller that DOES have ownerId -> returns null (no exception to replay)")
        void ownerRulesReplayReturnsNullWhenValid() {
            // Caller HAS both tenantId and ownerId — requireOwnerId will not throw.
            com.garganttua.api.commons.service.IOperationRequest request = requestWith("acme", "user-1");

            Throwable replayed = Domain.tryReplayValidator("owner_rules_owner_rules", 400, request);

            assertEquals(null, replayed,
                    "when the validator does NOT throw, replay must return null so the caller "
                            + "falls back to the synthesized message");
        }

        @Test
        @DisplayName("unknown stage -> returns null (caller falls back)")
        void unknownStageReturnsNull() {
            com.garganttua.api.commons.service.IOperationRequest request = requestWith("acme", null);

            assertEquals(null, Domain.tryReplayValidator("some_random_stage", 400, request),
                    "unknown stages have no replay path; must return null");
        }

        @Test
        @DisplayName("null request -> returns null (defensive)")
        void nullRequestReturnsNull() {
            assertEquals(null, Domain.tryReplayValidator("owner_rules_owner_rules", 400, null),
                    "null request must short-circuit the replay; got non-null");
        }
    }

    /**
     * Asserts the message is not the old useless "Workflow execution failed"
     * sentinel. This is exactly the regression mon général flagged.
     */
    private static void assertNotGeneric(String msg) {
        assertFalse(msg.equals("Workflow execution failed"),
                "must not fall back to the old generic string; got: " + msg);
        assertFalse(msg.toLowerCase().equals("workflow execution failed"),
                "must not fall back to a case-variant of the old generic string; got: " + msg);
    }
}
