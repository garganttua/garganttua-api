package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.workflow.WorkflowResult;

@DisplayName("UpdateOne Script Tests")
class UpdateOneIntegrationTest extends AbstractCrudScriptTest {

    private IApi context;
    private IDomain<?> userCtx;
    private CapturingDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new CapturingDao();

        IApiBuilder builder = newBuilder();
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .mandatory("name")
                    .update("name")
                    .update("email")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
                .security().disable(true).up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomain("users").orElseThrow();
    }

    @Test
    @DisplayName("updateOne updates entity by uuid and returns it")
    void updateOneByUuid() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Updated");
        updatedUser.setEmail("alice.updated@example.com");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertTrue(result.output() instanceof User);

        User output = (User) result.output();
        assertEquals("Alice Updated", output.getName());
        assertEquals("alice.updated@example.com", output.getEmail());
        assertEquals("uuid-alice", output.getUuid(), "UUID should not change");
    }

    @Test
    @DisplayName("updateOne updates entity by id")
    void updateOneById() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice By Id");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "id");
        request.arg("identifier", "1");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        User output = (User) result.output();
        assertEquals("Alice By Id", output.getName());
    }

    @Test
    @DisplayName("updateOne defaults to uuid type when not specified")
    void updateOneDefaultsToUuid() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Default");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        User output = (User) result.output();
        assertEquals("Alice Default", output.getName());
    }

    @Test
    @DisplayName("updateOne returns 404 when entity does not exist")
    void updateOneReturns404() throws ApiException {
        User updatedUser = new User();
        updatedUser.setName("Ghost");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-nonexistent");

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(404, result.code());
    }

    @Test
    @DisplayName("updateOne returns 400 when no caller is provided")
    void updateOneReturns400WhenNoCaller() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Updated");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION, updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("updateOne returns 400 when no entity is provided")
    void updateOneReturns400WhenNoEntity() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(400, result.code());
    }

    @Test
    @DisplayName("updateOne returns 500 when repository throws an exception")
    void updateOneReturns500OnRepositoryException() throws ApiException {
        IApiBuilder failingBuilder = newBuilder();

        failingBuilder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .update("name")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new FailingDao())
                .up()
                .security().disable(true).up()
            .up();

        IApi failingContext = buildAndStart(failingBuilder);
        IDomain<?> failingUserCtx = failingContext.getDomain("users").orElseThrow();

        User updatedUser = new User();
        updatedUser.setName("Fail");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(failingUserCtx, request);

        assertFalse(result.isSuccess());
        assertEquals(500, result.code());
    }

    @Test
    @DisplayName("updateOne persists the updated entity to the DAO")
    void updateOnePersistsUpdatedEntity() throws ApiException {
        seedUser("1", "uuid-alice", "Alice", "alice@example.com");

        User updatedUser = new User();
        updatedUser.setName("Alice Persisted");
        updatedUser.setEmail("persisted@example.com");

        OperationDefinition updateOp = OperationDefinition.updateOneWithStandardSecurity("users", IClass.getClass(User.class));
        OperationRequest request = superTenantScriptRequest(updateOp);
        request.arg("entity", updatedUser);
        request.arg("type", "uuid");
        request.arg("identifier", "uuid-alice");

        WorkflowResult result = executeScript(userCtx, request);

        assertTrue(result.isSuccess());
        assertNotNull(userDao.getLastSaved(), "Updated entity should have been saved to DAO");
    }

    private void seedUser(String id, String uuid, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setUuid(uuid);
        dto.setTenantId("SUPER_TENANT");
        dto.setName(name);
        dto.setEmail(email);
        userDao.getStorage().add(dto);
    }

    // ─── Field-level authority enforcement through the pipeline ────────────
    //
    // The setUp() build above only declares .update("name") / .update("email")
    // — no authority gate. These tests rebuild the API with an authority gate
    // on the 'name' field and assert that UPDATE_ONE.gs honours it end-to-end
    // (the script delegates to updateEntity expression → EntityUpdater).

    @org.junit.jupiter.api.Nested
    @DisplayName("Field-level update authority enforcement (entity().update(field, \"auth\"))")
    class FieldLevelAuthority {

        private IApi guardedContext;
        private IDomain<?> guardedUserCtx;
        private CapturingDao guardedDao;

        @BeforeEach
        void buildGuardedApi() throws ApiException {
            guardedDao = new CapturingDao();
            IApiBuilder b = newBuilder();
            b.domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                        // 'name' requires the "user-update-name" authority
                        .update("name", "user-update-name")
                        // 'email' is freely updatable — no authority gate
                        .update("email")
                    .up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(guardedDao)
                    .up()
                    .security().disable(true).up()
                .up();
            guardedContext = buildAndStart(b);
            guardedUserCtx = guardedContext.getDomain("users").orElseThrow();
            UserDto seed = new UserDto();
            seed.setId("1");
            seed.setUuid("uuid-bob");
            seed.setTenantId("TENANT_A");
            seed.setName("Bob");
            seed.setEmail("bob@example.com");
            guardedDao.getStorage().add(seed);
        }

        /**
         * Bare tenant caller with the supplied authority list — bypasses
         * VERIFY_AUTHORIZATION (Mode B: pre-populated authorization arg) so
         * the test focuses on field-level enforcement inside UPDATE_ONE.gs.
         */
        private OperationRequest tenantUpdate(java.util.List<String> authorities, User body) {
            OperationDefinition op = OperationDefinition.updateOneWithStandardSecurity(
                    "users", IClass.getClass(User.class));
            OperationRequest req = new OperationRequest(new HashMap<>());
            req.arg(IOperationRequest.OPERATION, op);
            req.arg(IOperationRequest.TENANT_ID, "TENANT_A");
            req.arg(IOperationRequest.REQUESTED_TENANT_ID, "TENANT_A");
            req.arg(IOperationRequest.CALLER_ID, "user-1");
            req.arg(IOperationRequest.OWNER_ID, "user-1");
            req.arg(IOperationRequest.SUPER_TENANT, false);
            req.arg(IOperationRequest.SUPER_OWNER, false);
            req.arg(IOperationRequest.AUTHORITIES, authorities);
            req.arg("authorization", new com.garganttua.api.core.integ.TestAuthorization()); // Mode B: pre-resolved
            req.arg("caller", new com.garganttua.api.core.caller.Caller(
                    "TENANT_A", "TENANT_A", "user-1", "user-1", false, false, authorities));
            req.arg("entity", body);
            req.arg("type", "uuid");
            req.arg("identifier", "uuid-bob");
            return req;
        }

        @Test
        @DisplayName("caller WITH the required authority can update the guarded field")
        void callerWithAuthorityUpdatesField() throws ApiException {
            User body = new User();
            body.setName("Bob Renamed");
            WorkflowResult result = executeScript(guardedUserCtx,
                    tenantUpdate(java.util.List.of("user-update-name"), body));

            assertTrue(result.isSuccess(),
                    "caller has 'user-update-name' — update must succeed. code=" + result.code());
            User updated = (User) result.output();
            assertEquals("Bob Renamed", updated.getName(),
                    "guarded field must be updated when the caller carries the authority");
        }

        @Test
        @DisplayName("caller WITHOUT the required authority sees the guarded field silently preserved")
        void callerWithoutAuthoritySkipsField() throws ApiException {
            User body = new User();
            body.setName("Bob Hacked");
            body.setEmail("hacked@example.com");

            WorkflowResult result = executeScript(guardedUserCtx,
                    // 'other-role' is not the required authority
                    tenantUpdate(java.util.List.of("other-role"), body));

            assertTrue(result.isSuccess(),
                    "the update operation itself succeeds — only the guarded field is skipped. code=" + result.code());
            User updated = (User) result.output();
            assertEquals("Bob", updated.getName(),
                    "guarded 'name' field must NOT be updated — caller lacks 'user-update-name'");
            assertEquals("hacked@example.com", updated.getEmail(),
                    "ungated 'email' field must still be updated — no authority required");
        }

        @Test
        @DisplayName("caller with null authorities cannot update the guarded field (regression — null is not a bypass)")
        void callerWithNullAuthoritiesSkipsField() throws ApiException {
            User body = new User();
            body.setName("Bob NullAuth");

            WorkflowResult result = executeScript(guardedUserCtx, tenantUpdate(null, body));

            assertTrue(result.isSuccess(), "operation succeeds; only the gated field is skipped");
            User updated = (User) result.output();
            assertEquals("Bob", updated.getName(),
                    "regression guard: a caller with null authorities used to bypass — must now skip the guarded field");
        }

        @Test
        @DisplayName("caller with empty authorities list cannot update the guarded field")
        void callerWithEmptyAuthoritiesSkipsField() throws ApiException {
            User body = new User();
            body.setName("Bob Empty");

            WorkflowResult result = executeScript(guardedUserCtx,
                    tenantUpdate(java.util.List.of(), body));

            assertTrue(result.isSuccess());
            User updated = (User) result.output();
            assertEquals("Bob", updated.getName(),
                    "empty authorities + required gate → skipped");
        }

        @Test
        @DisplayName("super-tenant caller does NOT bypass the field-level gate — the guarded field is preserved")
        void superTenantDoesNotBypassGate() throws ApiException {
            User body = new User();
            body.setName("Bob By Super");
            body.setEmail("super@example.com");

            // Custom super-caller pinned to TENANT_A — the seeded entity lives
            // on TENANT_A and using the canned superTenantScriptRequest (which
            // hardcodes "SUPER_TENANT") would miss the row before the
            // field-level gate even runs.
            OperationDefinition op = OperationDefinition.updateOneWithStandardSecurity(
                    "users", IClass.getClass(User.class));
            OperationRequest req = new OperationRequest(new HashMap<>());
            req.arg(IOperationRequest.OPERATION, op);
            req.arg(IOperationRequest.TENANT_ID, "TENANT_A");
            req.arg(IOperationRequest.REQUESTED_TENANT_ID, "TENANT_A");
            req.arg(IOperationRequest.SUPER_TENANT, true);
            req.arg(IOperationRequest.SUPER_OWNER, true);
            // No authorities — super status must NOT be enough; the guarded field is skipped.
            req.arg("caller", new com.garganttua.api.core.caller.Caller(
                    "TENANT_A", "TENANT_A", null, null, true, true, null));
            req.arg("authorization", new com.garganttua.api.core.integ.TestAuthorization()); // Mode B
            req.arg("entity", body);
            req.arg("type", "uuid");
            req.arg("identifier", "uuid-bob");

            WorkflowResult result = executeScript(guardedUserCtx, req);
            assertTrue(result.isSuccess(),
                    "the update operation itself succeeds — only the guarded field is skipped. code=" + result.code()
                            + " response=" + result.variables());
            User updated = (User) result.output();
            assertEquals("Bob", updated.getName(),
                    "guarded 'name' must NOT change — a super-tenant without 'user-update-name' gets no bypass");
            assertEquals("super@example.com", updated.getEmail(),
                    "ungated 'email' still updates — no authority required");
        }
    }
}
