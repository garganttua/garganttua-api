package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.api.core.service.RequestBuilder;
import com.garganttua.core.reflection.IClass;

/**
 * Verifies {@link Caller#createSuperCaller(String)} + the workflow path that
 * was broken by the old no-arg factory (which produced a caller with
 * {@code tenantId=null} that {@code Domain.invoke} rejects with 400).
 */
@DisplayName("Super caller")
class SuperCallerIntegrationTest extends AbstractCrudIntegrationTest {

    private static final String MASTER = "MASTER";

    private IApi buildApiWithTenantDomain(CapturingDao dao) throws ApiException {
        IApiBuilder builder = newBaseBuilder();
        builder.multiTenant(true)
               .superTenantId(MASTER)
               .superTenantAutoCreate(true);

        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(dao)
                .up()
                .security().disable(true).up()
                .readAll(true)
            .up();

        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    private static String uuidOf(Object dto) {
        try {
            Field f = dto.getClass().getDeclaredField("uuid");
            f.setAccessible(true);
            Object v = f.get(dto);
            return v == null ? null : v.toString();
        } catch (Exception e) {
            throw new AssertionError("Could not read uuid field on " + dto.getClass().getName(), e);
        }
    }

    @Nested
    @DisplayName("Caller.createSuperCaller(String)")
    class FactorySemantics {

        @Test
        @DisplayName("pins tenantId and requestedTenantId to the supplied super-tenant id")
        void boundToSuperTenantId() {
            ICaller caller = Caller.createSuperCaller(MASTER);
            assertEquals(MASTER, caller.tenantId(), "tenantId should be the super-tenant id");
            assertEquals(MASTER, caller.requestedTenantId(),
                    "requestedTenantId should mirror tenantId so the request reads from MASTER");
            assertTrue(caller.superTenant(), "superTenant flag must be set");
            assertTrue(caller.superOwner(), "superOwner flag must be set");
            assertNull(caller.callerId(), "callerId is not bound for a system super caller");
            assertNull(caller.ownerId(), "ownerId is not bound for a system super caller");
            assertNull(caller.authorities(), "authorities are not bound for a system super caller");
        }

        @Test
        @DisplayName("throws NPE on null superTenantId — fails fast")
        void rejectsNull() {
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> Caller.createSuperCaller(null));
            assertNotNull(ex.getMessage(), "NPE should carry an explanatory message");
            assertTrue(ex.getMessage().contains("superTenantId"),
                    "NPE message should name the missing parameter; got: " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("End-to-end: super caller is accepted by Domain.invoke")
    class EndToEnd {

        @Test
        @DisplayName("readAll on the tenant domain succeeds and returns the auto-created master tenant")
        void readAllReturnsMasterTenant() throws ApiException {
            CapturingDao dao = new CapturingDao();
            IApi api = buildApiWithTenantDomain(dao);

            // Sanity: auto-create did its job.
            assertEquals(1, dao.getStorage().size(),
                    "master tenant should have been auto-created before the super-caller readAll runs");

            IDomain<?> tenantDomain = api.getDomain("users").orElseThrow();
            IOperationResponse response = RequestBuilder.builder(tenantDomain)
                    .caller(Caller.createSuperCaller(api.getSuperTenantId()))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                    "super-caller readAll must succeed; got code=" + response.getResponseCode()
                            + " response=" + response.getResponse());

            Object payload = response.getResponse();
            assertNotNull(payload, "readAll OK response must carry a payload");
            assertTrue(payload instanceof List<?>,
                    "readAll payload should be a List; got " + payload.getClass().getName());
            List<?> results = (List<?>) payload;
            assertEquals(1, results.size(), "expected exactly one tenant (the auto-created master)");
            assertEquals(MASTER, uuidOf(results.get(0)),
                    "the master tenant (uuid=" + MASTER + ") should be in the response");
        }
    }

    @Nested
    @DisplayName("Deprecated no-arg factory")
    class DeprecatedFactory {

        @Test
        @DisplayName("still produces a null-tenantId super caller that Domain.invoke rejects with 400 (parlant 'missing tenantId' message)")
        @SuppressWarnings("deprecation")
        void noArgGetsRejected() throws ApiException {
            CapturingDao dao = new CapturingDao();
            IApi api = buildApiWithTenantDomain(dao);
            IDomain<?> tenantDomain = api.getDomain("users").orElseThrow();

            IOperationResponse response = RequestBuilder.builder(tenantDomain)
                    .caller(Caller.createSuperCaller())
                    .readAll()
                    .build()
                    .execute();

            // Pins the deprecation rationale: the no-arg factory sets
            // superTenant=true but tenantId=null. That's a malformed caller
            // (super flags require a tenantId binding), distinct from "no
            // caller provided" which now auto-creates an anonymous caller.
            // Since 2026-05-19, the rejection message names the missing
            // tenantId and points at the correct factory.
            assertEquals(OperationResponseCode.CLIENT_ERROR, response.getResponseCode(),
                    "deprecated no-arg super caller must still be rejected; got " + response.getResponseCode());
            String msg = String.valueOf(response.getResponse());
            assertTrue(msg.contains("missing tenantId"),
                    "rejection message must name the actual problem (missing tenantId); got: " + msg);
            assertTrue(msg.contains("createSuperCaller(superTenantId)"),
                    "rejection message must point at the correct factory; got: " + msg);
            assertFalse(dao.getStorage().size() > 1,
                    "DAO must not have grown — the request was rejected before reaching the operation");
        }
    }
}
