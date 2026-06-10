package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.OperationRequest;
import com.garganttua.core.reflection.IClass;

/**
 * Non-tenant mode ({@code multiTenant(false)}): an owner-scoped caller legitimately
 * carries no {@code tenantId}. {@code Domain.invoke} must not reject it — the
 * tenantId-binding guard is for multi-tenant misuse only — and owner isolation is
 * still enforced by the repository owner filter.
 */
@DisplayName("Non-tenant mode — owner-scoped caller without tenantId")
class NonTenantOwnerCallerTest extends AbstractCrudIntegrationTest {

    /** An owned, non-tenant entity (no tenantId field). */
    public static class Note {
        private String id;
        private String uuid;
        private String ownerId;
        private String text;

        public Note() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    public static class NoteDto {
        @com.garganttua.core.mapper.annotations.FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @com.garganttua.core.mapper.annotations.FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @com.garganttua.core.mapper.annotations.FieldMappingRule(sourceFieldAddress = "ownerId")
        private String ownerId;
        @com.garganttua.core.mapper.annotations.FieldMappingRule(sourceFieldAddress = "text")
        private String text;

        public NoteDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    private static NoteDto note(String uuid, String ownerId, String text) {
        NoteDto dto = new NoteDto();
        dto.setId(uuid);
        dto.setUuid(uuid);
        dto.setOwnerId(ownerId);
        dto.setText(text);
        return dto;
    }

    @Test
    @DisplayName("an owner caller (no tenantId) reads its OWN data, not a 400")
    void ownerCallerWithoutTenantIdReadsOwnData() throws ApiException {
        CapturingDao dao = new CapturingDao();
        dao.getStorage().add(note("n-alice", "alice", "alice's note"));
        dao.getStorage().add(note("n-bob", "bob", "bob's note"));

        IApiBuilder builder = newBaseBuilder().multiTenant(false);
        builder.domain(IClass.getClass(Note.class))
                .owned("ownerId")
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(NoteDto.class))
                    .id("id").uuid("uuid").db(dao)
                .up()
                .readAll(true)
            .up();
        IApi api = builder.build();
        api.onInit();
        api.onStart();
        IDomain<?> notes = api.getDomain("notes").orElseThrow();

        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION,
                OperationDefinition.readAll("notes", IClass.getClass(Note.class), false, null, Access.anonymous));
        // Owner-scoped caller: ownerId set, NO tenantId (legitimate in non-tenant mode).
        request.arg(IOperationRequest.CALLER_ID, "alice");
        request.arg(IOperationRequest.OWNER_ID, "alice");

        IOperationResponse response = notes.invoke(request);

        assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                () -> "owner caller without tenantId must be accepted in non-tenant mode, got: "
                        + response.getResponseCode() + " / " + response.getResponse());
        Object body = response.getResponse();
        assertTrue(body instanceof List<?>, "readAll must return a list");
        List<?> rows = (List<?>) body;
        assertEquals(1, rows.size(), "owner isolation must restrict the result to Alice's note");
        assertEquals("alice's note", ((Note) rows.get(0)).getText());
        assertEquals("alice", ((Note) rows.get(0)).getOwnerId());
    }

    @Test
    @DisplayName("multi-tenant mode still rejects a super caller with no tenantId (guard preserved)")
    void multiTenantStillRejectsTenantlessSuperCaller() throws ApiException {
        CapturingDao dao = new CapturingDao();

        IApiBuilder builder = newBuilder(); // multi-tenant (superTenantId set, autoCreate false)
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId").db(dao)
                .up()
                .readAll(true)
            .up();
        IApi api = builder.build();
        api.onInit();
        api.onStart();
        IDomain<?> users = api.getDomain("users").orElseThrow();

        OperationRequest request = new OperationRequest(new HashMap<>());
        request.arg(IOperationRequest.OPERATION,
                OperationDefinition.readAll("users", IClass.getClass(User.class), false, null, Access.anonymous));
        // The deprecated misuse: super flag set, but no tenantId binding.
        request.arg(IOperationRequest.SUPER_TENANT, Boolean.TRUE);

        IOperationResponse response = users.invoke(request);

        assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode(),
                "in multi-tenant mode a tenantless super caller must still be rejected (400)");
        assertTrue(String.valueOf(response.getResponse()).contains("missing tenantId"),
                "the rejection must be the parlant tenantId message; got: " + response.getResponse());
    }
}
