package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.api.Api;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleStatus;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;

@DisplayName("API Integration Tests")
class ApiTest extends AbstractCrudIntegrationTest {

    // ───── Test IInterface ─────

    public static class TestInterface implements IInterface {
        private boolean handleCalled = false;
        private boolean initCalled = false;
        private boolean startCalled = false;
        private boolean stopCalled = false;
        private LifecycleStatus currentStatus = LifecycleStatus.NEW;
        private IDomain<?> domainContext;

        @Override
        public void handle(IDomain<?> context) {
            this.handleCalled = true;
            this.domainContext = context;
        }

        @Override
        public ILifecycle onInit() {
            this.initCalled = true;
            this.currentStatus = LifecycleStatus.INITIALIZED;
            return this;
        }

        @Override
        public ILifecycle onStart() {
            this.startCalled = true;
            this.currentStatus = LifecycleStatus.STARTED;
            return this;
        }

        @Override
        public ILifecycle onStop() {
            this.stopCalled = true;
            this.currentStatus = LifecycleStatus.STOPPED;
            return this;
        }

        @Override
        public ILifecycle onFlush() {
            this.currentStatus = LifecycleStatus.FLUSHED;
            return this;
        }

        @Override
        public ILifecycle onReload() {
            return this;
        }

        @Override
        public LifecycleStatus status() {
            return this.currentStatus;
        }

        public boolean isHandleCalled() { return handleCalled; }
        public boolean isInitCalled() { return initCalled; }
        public boolean isStartCalled() { return startCalled; }
        public boolean isStopCalled() { return stopCalled; }
        public IDomain<?> getDomain() { return domainContext; }
    }

    // ───── Fixtures ─────

    private IApiBuilder builder;
    private StubDao userDao;
    private StubDao productDao;
    private TestInterface userInterface;

    @BeforeEach
    void setUp() throws ApiException {
        builder = newBuilder();

        userDao = new StubDao();
        productDao = new StubDao();
        userInterface = new TestInterface();

        // Domain 1: User (tenant entity with interface)
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .interfasse(FixedSupplierBuilder.of(userInterface))
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
                .workflow("deleteAll").security().disable(true).up().up()
            .up();

        // Domain 2: Product (non-tenant entity)
        builder.domain(IClass.getClass(Product.class))
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(ProductDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(productDao)
                .up()
            .up();
    }

    @Nested
    @DisplayName("Context building")
    class ContextBuilding {

        @Test
        @DisplayName("builds a valid API context with two domains")
        void buildsValidContext() throws ApiException {
            IApi context = builder.build();

            assertNotNull(context);
            assertTrue(context instanceof Api);
        }

        @Test
        @DisplayName("context contains the user domain")
        void contextContainsUserDomain() throws ApiException {
            IApi context = builder.build();

            Optional<IDomain<?>> userCtx = context.getDomain("users");
            assertTrue(userCtx.isPresent());
            assertEquals(IClass.getClass(User.class), userCtx.get().getEntityClass());
        }

        @Test
        @DisplayName("context contains the product domain")
        void contextContainsProductDomain() throws ApiException {
            IApi context = builder.build();

            Optional<IDomain<?>> productCtx = context.getDomain("products");
            assertTrue(productCtx.isPresent());
            assertEquals(IClass.getClass(Product.class), productCtx.get().getEntityClass());
        }

        @Test
        @DisplayName("user domain is a tenant entity")
        void userDomainIsTenant() throws ApiException {
            IApi context = builder.build();

            IDomain<?> userCtx = context.getDomain("users").orElseThrow();
            assertTrue(userCtx.isTenantEntity());
        }

        @Test
        @DisplayName("product domain is not a tenant entity")
        void productDomainIsNotTenant() throws ApiException {
            IApi context = builder.build();

            IDomain<?> productCtx = context.getDomain("products").orElseThrow();
            assertFalse(productCtx.isTenantEntity());
        }

        @Test
        @DisplayName("each domain has a repository")
        void domainsHaveRepositories() throws ApiException {
            IApi context = builder.build();

            IDomain<?> userCtx = context.getDomain("users").orElseThrow();
            IDomain<?> productCtx = context.getDomain("products").orElseThrow();

            assertNotNull(userCtx.getRepository());
            assertNotNull(productCtx.getRepository());
        }

        @Test
        @DisplayName("each domain has workflows")
        void domainsHaveWorkflows() throws ApiException {
            IApi context = builder.build();

            IDomain<?> userCtx = context.getDomain("users").orElseThrow();
            IDomain<?> productCtx = context.getDomain("products").orElseThrow();

            assertNotNull(userCtx.getWorkflow());
            assertNotNull(productCtx.getWorkflow());
        }
    }

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("init and start the API context")
        void initAndStart() throws ApiException {
            IApi context = builder.build();

            assertDoesNotThrow(() -> context.onInit());
            assertDoesNotThrow(() -> context.onStart());
        }

        @Test
        @DisplayName("interface receives handle and lifecycle callbacks")
        void interfaceReceivesCallbacks() throws ApiException {
            IApi context = builder.build();

            context.onInit();
            context.onStart();

            assertTrue(userInterface.isHandleCalled(), "handle() should be called on init");
            assertTrue(userInterface.isInitCalled(), "onInit() should be called");
            assertTrue(userInterface.isStartCalled(), "onStart() should be called");
            assertNotNull(userInterface.getDomain(), "domain context should be passed to handle()");
            assertEquals("users", userInterface.getDomain().getDomain());
        }

        @Test
        @DisplayName("stop the API context")
        void stop() throws ApiException {
            IApi context = builder.build();

            context.onInit();
            context.onStart();
            assertDoesNotThrow(() -> context.onStop());

            assertTrue(userInterface.isStopCalled(), "onStop() should be called on interface");
        }

        @Test
        @DisplayName("full lifecycle: init -> start -> stop")
        void fullLifecycle() throws ApiException {
            IApi context = builder.build();

            context.onInit();
            context.onStart();
            context.onStop();

            // After full lifecycle, all domains should still be accessible
            assertTrue(context.getDomain("users").isPresent());
            assertTrue(context.getDomain("products").isPresent());
        }
    }
}
