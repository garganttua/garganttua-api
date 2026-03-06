package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.context.OperationRequest;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.reflection.runtime.RuntimeClass;

@DisplayName("DeleteAll Integration Tests")
class DeleteAllIntegrationTest extends AbstractCrudIntegrationTest {

    private IApiContext context;
    private IDomainContext<?> userCtx;
    private StubDao userDao;

    @BeforeEach
    void setUp() throws ApiException {
        userDao = new StubDao();

        IApiContextBuilder builder = newBuilder();
        builder.domain(RuntimeClass.of(User.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(RuntimeClass.of(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
            .up();

        context = buildAndStart(builder);
        userCtx = context.getDomainContext("users").orElseThrow();
    }

    @Test
    @DisplayName("invoke deleteAll workflow returns a successful response")
    void invokeDeleteAllWorkflow() throws ApiException {
        Operation deleteAllOp = Operation.deleteAllWithStandardSecurity("users", RuntimeClass.of(User.class));
        OperationRequest request = superTenantRequest(deleteAllOp);

        IOperationResponse response = userCtx.invoke(request);

        assertNotNull(response);
        assertEquals(OperationResponseCode.OK, response.getResponseCode());
    }
}
