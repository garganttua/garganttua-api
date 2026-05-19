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
