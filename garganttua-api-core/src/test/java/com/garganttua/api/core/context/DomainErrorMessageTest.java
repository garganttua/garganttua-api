package com.garganttua.api.core.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @DisplayName("functionalMessage end-to-end on a synthetic WorkflowResult")
    class FunctionalMessage {

        @Test
        @DisplayName("when a stage variable is non-zero, the message names the FUNCTIONAL cause (not 'rejected by validation')")
        void picksUpStageHint() {
            // Synthetic WorkflowResult that mirrors what Workflow.execute would
            // produce when OWNER_RULES.gs does `! -> 400` after the
            // requireOwnerId guard fails. The variable name format
            // (_<stage>_<script>_code) is set by garganttua-core's
            // Workflow.collectVariables.
            java.util.Map<String, Object> vars = new java.util.HashMap<>();
            vars.put("_owner_rules_owner_rules_code", 400);
            java.time.Instant now = java.time.Instant.now();
            com.garganttua.core.workflow.WorkflowResult result =
                    new com.garganttua.core.workflow.WorkflowResult(
                            java.util.UUID.randomUUID(), null, 400, vars,
                            java.util.Map.of(), now, now,
                            java.util.Optional.empty(), java.util.Optional.empty());

            String msg = Domain.functionalMessage(result, "deleteAll", "authorizations");

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
        @DisplayName("when NO stage variable is non-zero, falls back to defaultMessageForCode")
        void fallsBackWhenNoStageMarker() {
            // Empty variables — engine produced a code but no stage code surfaced.
            java.time.Instant now = java.time.Instant.now();
            com.garganttua.core.workflow.WorkflowResult result =
                    new com.garganttua.core.workflow.WorkflowResult(
                            java.util.UUID.randomUUID(), null, 404, java.util.Map.of(),
                            java.util.Map.of(), now, now,
                            java.util.Optional.empty(), java.util.Optional.empty());

            String msg = Domain.functionalMessage(result, "readOne", "users");

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
