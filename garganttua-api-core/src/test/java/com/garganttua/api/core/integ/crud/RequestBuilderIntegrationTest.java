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
import com.garganttua.api.spec.service.IRequest;
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

    // --- CRUD shortcuts on IDomainContext.request() ---

    @Test
    @DisplayName("createOne via request builder persists entity")
    void createOneViaBuilder() {
        Product p = product("Widget", 9.99);

        IOperationResponse response = productCtx.request()
                .createOne(p)
                .caller(caller)
                .execute();

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertNotNull(response.getResponse());
        assertTrue(response.getResponse() instanceof Product);
        Product created = (Product) response.getResponse();
        assertEquals("Widget", created.getLabel());
        assertNotNull(created.getUuid());
        assertEquals(1, productDao.getStorage().size());
    }

    @Test
    @DisplayName("readAll via request builder returns entities")
    void readAllViaBuilder() {
        productCtx.request().createOne(product("A", 1.0)).caller(caller).execute();
        productCtx.request().createOne(product("B", 2.0)).caller(caller).execute();

        IOperationResponse response = productCtx.request()
                .readAll()
                .caller(caller)
                .execute();

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
    }

    @Test
    @DisplayName("readOne via request builder returns single entity")
    void readOneViaBuilder() {
        Product p = product("Gadget", 5.0);
        IOperationResponse createResp = productCtx.request()
                .createOne(p)
                .caller(caller)
                .execute();
        Product created = (Product) createResp.getResponse();

        IOperationResponse response = productCtx.request()
                .readOne(created.getUuid())
                .caller(caller)
                .execute();

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
    }

    @Test
    @DisplayName("updateOne via request builder updates entity")
    void updateOneViaBuilder() {
        Product p = product("Old", 1.0);
        IOperationResponse createResp = productCtx.request()
                .createOne(p)
                .caller(caller)
                .execute();
        Product created = (Product) createResp.getResponse();

        Product updated = new Product();
        updated.setLabel("New");

        IOperationResponse response = productCtx.request()
                .updateOne(created.getUuid(), updated)
                .caller(caller)
                .execute();

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
    }

    @Test
    @DisplayName("deleteOne via request builder deletes entity")
    void deleteOneViaBuilder() {
        Product p = product("ToDelete", 3.0);
        IOperationResponse createResp = productCtx.request()
                .createOne(p)
                .caller(caller)
                .execute();
        Product created = (Product) createResp.getResponse();

        IOperationResponse response = productCtx.request()
                .deleteOne(created.getUuid())
                .caller(caller)
                .execute();

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
    }

    @Test
    @DisplayName("deleteAll via request builder deletes all entities")
    void deleteAllViaBuilder() {
        productCtx.request().createOne(product("X", 1.0)).caller(caller).execute();
        productCtx.request().createOne(product("Y", 2.0)).caller(caller).execute();

        IOperationResponse response = productCtx.request()
                .deleteAll()
                .caller(caller)
                .execute();

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        assertEquals(0, productDao.getStorage().size());
    }

    // --- build().execute() vs execute() ---

    @Test
    @DisplayName("build() returns an IRequest that can be executed separately")
    void buildThenExecute() {
        Product p = product("Deferred", 7.0);

        IRequest request = productCtx.request()
                .createOne(p)
                .caller(caller)
                .build();

        assertNotNull(request);
        assertNotNull(request.operationRequest());

        IOperationResponse response = request.execute();
        assertEquals(OperationResponseCode.OK, response.getResponseCode());
    }

    // --- IApiContext.request(domainName) ---

    @Test
    @DisplayName("context.request(domainName) creates entity via fluent API")
    void createViaApiContext() {
        Product p = product("FromContext", 4.0);

        IOperationResponse response = context.request("products")
                .createOne(p)
                .caller(caller)
                .execute();

        assertEquals(OperationResponseCode.OK, response.getResponseCode());
        Product created = (Product) response.getResponse();
        assertEquals("FromContext", created.getLabel());
    }

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
