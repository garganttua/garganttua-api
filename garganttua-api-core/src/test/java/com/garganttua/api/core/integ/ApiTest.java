package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.ApiContextBuilder;
import com.garganttua.api.core.context.OperationRequest;
import com.garganttua.api.core.context.application.ApiContext;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.interfasse.IInterface;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.expression.dsl.ExpressionContextBuilder;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.injection.context.dsl.InjectionContextBuilder;
import com.garganttua.core.runtime.RuntimeContextFactory;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleStatus;
import com.garganttua.core.mapper.annotations.FieldMappingRule;
import com.garganttua.core.supply.dsl.FixedSupplierBuilder;

@DisplayName("API Integration Tests")
class ApiTest {

    // ───── Tenant entity: User ─────

    public static class User {
        private String id;
        private String uuid;
        private String tenantId;
        private String name;
        private String email;

        public User() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class UserDto {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "name")
        private String name;
        @FieldMappingRule(sourceFieldAddress = "email")
        private String email;

        public UserDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ───── Non-tenant entity: Product ─────

    public static class Product {
        private String id;
        private String uuid;
        private String tenantId;
        private String label;
        private double price;

        public Product() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    public static class ProductDto {
        @FieldMappingRule(sourceFieldAddress = "id")
        private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid")
        private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId")
        private String tenantId;
        @FieldMappingRule(sourceFieldAddress = "label")
        private String label;
        @FieldMappingRule(sourceFieldAddress = "price")
        private double price;

        public ProductDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    // ───── Stub DAO ─────

    public static class StubDao implements IDao {
        private final List<Object> storage = new ArrayList<>();
        private Class<?> dtoClass;

        @Override
        public void setDtoClass(Class<?> dtoClass) {
            this.dtoClass = dtoClass;
        }

        @Override
        public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
                throws ApiException {
            return new ArrayList<>(storage);
        }

        @Override
        public Object save(Object object) throws ApiException {
            storage.add(object);
            return object;
        }

        @Override
        public void delete(Object object) throws ApiException {
            storage.remove(object);
        }

        @Override
        public long count(IFilter filter) throws ApiException {
            return storage.size();
        }

        public List<Object> getStorage() {
            return storage;
        }

        public Class<?> getDtoClass() {
            return dtoClass;
        }
    }

    // ───── Test IInterface ─────

    public static class TestInterface implements IInterface {
        private boolean handleCalled = false;
        private boolean initCalled = false;
        private boolean startCalled = false;
        private boolean stopCalled = false;
        private LifecycleStatus currentStatus = LifecycleStatus.NEW;
        private IDomainContext<?> domainContext;

        @Override
        public void handle(IDomainContext<?> context) {
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
        public IDomainContext<?> getDomainContext() { return domainContext; }
    }

    // ───── Fixtures ─────

    private IApiContextBuilder builder;
    private StubDao userDao;
    private StubDao productDao;
    private TestInterface userInterface;

    @BeforeEach
    void setUp() throws ApiException {
        builder = ApiContextBuilder.builder();

        // Real injection and expression contexts
        IInjectionContextBuilder injectionContextBuilder = InjectionContextBuilder.builder()
                .childContextFactory(new RuntimeContextFactory());
        IExpressionContextBuilder expressionContextBuilder = ExpressionContextBuilder.builder();

        // Provide dependencies
        ((IDependentBuilder<IApiContextBuilder, IApiContext>) builder).provide(injectionContextBuilder);
        ((IDependentBuilder<IApiContextBuilder, IApiContext>) builder).provide(expressionContextBuilder);

        // Create DAOs and interface
        userDao = new StubDao();
        productDao = new StubDao();
        userInterface = new TestInterface();

        // Configure builder with two domains
        builder.superTenantId("SUPER_TENANT")
               .superTenantAutoCreate(true);

        // Domain 1: User (tenant entity)
        builder.domain(User.class)
                .tenant(true)
                .interfasse(new FixedSupplierBuilder<>(userInterface))
                .entity()
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                .up()
                .dto(UserDto.class)
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                    .db(userDao)
                .up()
                .workflow("deleteAll").security().disable(true).up().up()
            .up();

        // Domain 2: Product (non-tenant entity)
        builder.domain(Product.class)
                .entity()
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                .up()
                .dto(ProductDto.class)
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
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
            IApiContext context = builder.build();

            assertNotNull(context);
            assertTrue(context instanceof ApiContext);
        }

        @Test
        @DisplayName("context contains the user domain")
        void contextContainsUserDomain() throws ApiException {
            IApiContext context = builder.build();

            Optional<IDomainContext<?>> userCtx = context.getDomainContext("users");
            assertTrue(userCtx.isPresent());
            assertEquals(User.class, userCtx.get().getEntityClass());
        }

        @Test
        @DisplayName("context contains the product domain")
        void contextContainsProductDomain() throws ApiException {
            IApiContext context = builder.build();

            Optional<IDomainContext<?>> productCtx = context.getDomainContext("products");
            assertTrue(productCtx.isPresent());
            assertEquals(Product.class, productCtx.get().getEntityClass());
        }

        @Test
        @DisplayName("user domain is a tenant entity")
        void userDomainIsTenant() throws ApiException {
            IApiContext context = builder.build();

            IDomainContext<?> userCtx = context.getDomainContext("users").orElseThrow();
            assertTrue(userCtx.isTenantEntity());
        }

        @Test
        @DisplayName("product domain is not a tenant entity")
        void productDomainIsNotTenant() throws ApiException {
            IApiContext context = builder.build();

            IDomainContext<?> productCtx = context.getDomainContext("products").orElseThrow();
            assertFalse(productCtx.isTenantEntity());
        }

        @Test
        @DisplayName("each domain has a repository")
        void domainsHaveRepositories() throws ApiException {
            IApiContext context = builder.build();

            IDomainContext<?> userCtx = context.getDomainContext("users").orElseThrow();
            IDomainContext<?> productCtx = context.getDomainContext("products").orElseThrow();

            assertNotNull(userCtx.getRepository());
            assertNotNull(productCtx.getRepository());
        }

        @Test
        @DisplayName("each domain has workflows")
        void domainsHaveWorkflows() throws ApiException {
            IApiContext context = builder.build();

            IDomainContext<?> userCtx = context.getDomainContext("users").orElseThrow();
            IDomainContext<?> productCtx = context.getDomainContext("products").orElseThrow();

            assertFalse(userCtx.getWorkflows().isEmpty());
            assertFalse(productCtx.getWorkflows().isEmpty());
        }
    }

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("init and start the API context")
        void initAndStart() throws ApiException {
            IApiContext context = builder.build();

            assertDoesNotThrow(() -> context.onInit());
            assertDoesNotThrow(() -> context.onStart());
        }

        @Test
        @DisplayName("interface receives handle and lifecycle callbacks")
        void interfaceReceivesCallbacks() throws ApiException {
            IApiContext context = builder.build();

            context.onInit();
            context.onStart();

            assertTrue(userInterface.isHandleCalled(), "handle() should be called on init");
            assertTrue(userInterface.isInitCalled(), "onInit() should be called");
            assertTrue(userInterface.isStartCalled(), "onStart() should be called");
            assertNotNull(userInterface.getDomainContext(), "domain context should be passed to handle()");
            assertEquals("users", userInterface.getDomainContext().getDomain());
        }

        @Test
        @DisplayName("stop the API context")
        void stop() throws ApiException {
            IApiContext context = builder.build();

            context.onInit();
            context.onStart();
            assertDoesNotThrow(() -> context.onStop());

            assertTrue(userInterface.isStopCalled(), "onStop() should be called on interface");
        }

        @Test
        @DisplayName("full lifecycle: init -> start -> stop")
        void fullLifecycle() throws ApiException {
            IApiContext context = builder.build();

            context.onInit();
            context.onStart();
            context.onStop();

            // After full lifecycle, all domains should still be accessible
            assertTrue(context.getDomainContext("users").isPresent());
            assertTrue(context.getDomainContext("products").isPresent());
        }
    }

    @Nested
    @DisplayName("Workflow invocation")
    class WorkflowInvocation {

        @Test
        @DisplayName("invoke readAll workflow returns a successful response")
        void invokeReadAllWorkflow() throws ApiException {
            IApiContext context = builder.build();
            context.onInit();
            context.onStart();

            IDomainContext<?> userCtx = context.getDomainContext("users").orElseThrow();

            // Pre-populate the DAO with some UserDto objects
            UserDto alice = new UserDto();
            alice.setId("1");
            alice.setUuid("uuid-alice");
            alice.setTenantId("SUPER_TENANT");
            alice.setName("Alice");
            alice.setEmail("alice@example.com");

            UserDto bob = new UserDto();
            bob.setId("2");
            bob.setUuid("uuid-bob");
            bob.setTenantId("SUPER_TENANT");
            bob.setName("Bob");
            bob.setEmail("bob@example.com");

            UserDto charlie = new UserDto();
            charlie.setId("3");
            charlie.setUuid("uuid-charlie");
            charlie.setTenantId("SUPER_TENANT");
            charlie.setName("Charlie");
            charlie.setEmail("charlie@example.com");

            userDao.getStorage().add(alice);
            userDao.getStorage().add(bob);
            userDao.getStorage().add(charlie);

            Operation readAllOp = Operation.readAllWithStandardSecurity("users", User.class);
            OperationRequest request = new OperationRequest(new HashMap<>());
            request.arg(IOperationRequest.OPERATION, readAllOp);

            IOperationResponse response = userCtx.invoke(request);

            assertNotNull(response);
            assertEquals(OperationResponseCode.OK, response.getResponseCode());

            // Verify the response contains the entities
            assertNotNull(response.getResponse());
            assertTrue(response.getResponse() instanceof List);
            List<Object> entities = (List<Object>) response.getResponse();
            assertEquals(3, entities.size());
        }
    }
}
