package com.garganttua.api.core.integ.crud;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;

@DisplayName("RequestBuilder Integration Tests")
class RequestBuilderIntegrationTest extends AbstractCrudIntegrationTest {

    private IApiContext context;
    private IDomainContext<?> productCtx;
    private CapturingDao productDao;
    private ICaller caller;

    @BeforeEach
    void setUp() throws ApiException {
        productDao = new CapturingDao();
        caller = new Caller("T1", "T1", "caller1", null, true, true, null);

        IApiContextBuilder builder = newBuilder();
        builder.domain(IClass.getClass(Product.class))
                .tenant(true)
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(ProductDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(productDao)
                .up()
            .up();

        context = buildAndStart(builder);
        productCtx = context.getDomainContext("products").orElseThrow();
    }

    // --- CRUD via IDomainContext shortcut methods (all go through invoke()) ---

    @Test
    @DisplayName("createOne via shortcut persists entity")
    void createOneViaShortcut() {
        Product p = product("Widget", 9.99);

        IOperationResponse response = productCtx.createOne(p, caller);

        assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                "response: " + response.getResponse());
    }

    @Test
    @DisplayName("readAll via shortcut returns OK")
    void readAllViaShortcut() {
        productCtx.createOne(product("A", 1.0), caller);
        productCtx.createOne(product("B", 2.0), caller);

        IOperationResponse response = productCtx.readAll(caller);

        assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                "response: " + response.getResponse());
    }

    @Test
    @DisplayName("deleteAll via shortcut returns OK")
    void deleteAllViaShortcut() {
        productCtx.createOne(product("X", 1.0), caller);
        productCtx.createOne(product("Y", 2.0), caller);

        IOperationResponse response = productCtx.deleteAll(caller);

        assertEquals(OperationResponseCode.OK, response.getResponseCode(),
                "response: " + response.getResponse());
        assertEquals(0, productDao.getStorage().size());
    }

    // --- IApiContext.request(domainName) ---

    @Test
    @DisplayName("context.request(unknownDomain) throws ApiException")
    void unknownDomainThrows() {
        assertThrows(ApiException.class, () -> context.request("unknown"));
    }

    private Product product(String label, double price) {
        Product p = new Product();
        p.setLabel(label);
        p.setPrice(price);
        return p;
    }
}
