package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.IRequestBuilder;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.api.commons.service.ReadAllOutputMode;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

/**
 * Pins {@link IApi#request(String)}: the fluent shortcut that lets callers
 * skip the {@code api.getDomain(name).orElseThrow().request()} dance.
 * Mirrors {@link com.garganttua.api.commons.context.IDomain#request()} but
 * resolves the domain by name at the API level.
 */
@DisplayName("IApi.request(String) — domain-name fluent shortcut")
class ApiRequestBuilderTest extends AbstractCrudIntegrationTest {

    private IApi buildApiWithProduct(StubDao dao) throws ApiException {
        IApiBuilder builder = newBaseBuilder().multiTenant(false);
        builder.domain(IClass.getClass(Product.class))
                .entity().id("id").uuid("uuid").up()
                .dto(IClass.getClass(ProductDto.class))
                    .id("id").uuid("uuid")
                    .db(dao)
                .up()
                .security().disable(true).up()
                .readAll(true).readOne(true).creation(true)
            .up();
        IApi api = builder.build();
        api.onInit();
        api.onStart();
        return api;
    }

    @Nested
    @DisplayName("Resolution")
    class Resolution {

        @Test
        @DisplayName("returns a builder bound to the requested domain")
        void returnsBuilderForExistingDomain() throws ApiException {
            IApi api = buildApiWithProduct(new StubDao());

            IRequestBuilder builder = api.request("products");

            assertNotNull(builder, "request(String) must never return null for a known domain");
        }

        @Test
        @DisplayName("returns a FRESH builder on every call — callers can't accidentally share state")
        void freshBuilderEveryCall() throws ApiException {
            IApi api = buildApiWithProduct(new StubDao());

            IRequestBuilder a = api.request("products");
            IRequestBuilder b = api.request("products");

            assertNotSame(a, b,
                    "each request(String) call must return a distinct builder; "
                            + "otherwise concurrent callers would clobber each other's state");
        }

        @Test
        @DisplayName("throws ApiException naming the missing domain when the name is unknown")
        void throwsOnMissingDomain() throws ApiException {
            IApi api = buildApiWithProduct(new StubDao());

            ApiException ex = assertThrows(ApiException.class,
                    () -> api.request("does-not-exist"));
            assertTrue(ex.getMessage().contains("does-not-exist"),
                    "error message should name the missing domain; got: " + ex.getMessage());
        }
    }

    @Nested
    @DisplayName("Equivalence with the long form")
    class Equivalence {

        @Test
        @DisplayName("api.request(name).readAll().execute() produces the same response as getDomain(name).orElseThrow().request().readAll().execute()")
        void readAllPathsAgree() throws ApiException {
            StubDao dao = new StubDao();
            ProductDto seed = new ProductDto();
            seed.setId("p-1");
            seed.setUuid("p-1");
            seed.setLabel("Coffee");
            seed.setPrice(3.5);
            dao.getStorage().add(seed);

            IApi api = buildApiWithProduct(dao);

            IOperationResponse viaShortcut = api.request("products")
                    .caller(Caller.createTenantCaller("acme"))
                    .readAll()
                    .build()
                    .execute();

            IOperationResponse viaLongForm = api.getDomain("products").orElseThrow()
                    .request()
                    .caller(Caller.createTenantCaller("acme"))
                    .readAll()
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, viaShortcut.getResponseCode(),
                    "shortcut readAll must succeed; got " + viaShortcut);
            assertEquals(viaLongForm.getResponseCode(), viaShortcut.getResponseCode(),
                    "the shortcut must yield the same response code as the long form");

            List<?> a = (List<?>) viaShortcut.getResponse();
            List<?> b = (List<?>) viaLongForm.getResponse();
            assertEquals(1, a.size(), "shortcut should return the one seeded row; got " + a);
            assertEquals(b.size(), a.size(), "shortcut and long form must return the same number of rows");
        }

        @Test
        @DisplayName("readAll().mode(uuid).execute() reduces the result to uuids — the DSL .mode() reaches the pipeline")
        void readAllModeUuidThroughDsl() throws ApiException {
            StubDao dao = new StubDao();
            ProductDto seed = new ProductDto();
            seed.setId("p-1");
            seed.setUuid("uuid-1");
            seed.setLabel("Coffee");
            seed.setPrice(3.5);
            dao.getStorage().add(seed);

            IApi api = buildApiWithProduct(dao);

            IOperationResponse resp = api.request("products")
                    .caller(Caller.createTenantCaller("acme"))
                    .readAll()
                    .mode(ReadAllOutputMode.uuid)
                    .build()
                    .execute();

            assertEquals(OperationResponseCode.OK, resp.getResponseCode(), () -> "got " + resp);
            assertEquals(List.of("uuid-1"), resp.getResponse(),
                    "mode=uuid must reduce the readAll result to the list of uuids");
        }

        @Test
        @DisplayName("the shortcut hits the actual repository — a createOne via request(name) lands in the DAO")
        void createOnePersistsThroughShortcut() throws ApiException {
            StubDao dao = new StubDao();
            IApi api = buildApiWithProduct(dao);

            Product entity = new Product();
            entity.setLabel("Espresso");
            entity.setPrice(2.5);

            IOperationResponse response = api.request("products")
                    .caller(Caller.createTenantCaller("acme"))
                    .createOne(entity)
                    .build()
                    .execute();

            // Domain.invoke maps a successful create to CREATED (mapSuccessCode keys
            // off the business operation label), not a flat OK.
            assertEquals(OperationResponseCode.CREATED, response.getResponseCode(),
                    "createOne via shortcut must succeed with CREATED; got " + response);
            assertEquals(1, dao.getStorage().size(),
                    "exactly one row should have been persisted through the shortcut; got " + dao.getStorage());
        }
    }
}
