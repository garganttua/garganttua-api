package com.garganttua.api.core.integ;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.api.commons.endpoint.Interface;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.lifecycle.LifecycleStatus;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;

/**
 * Strong tests for the {@code .interfasse(IClass)} DSL overload — attaching an
 * {@link IInterface} by class (the framework instantiates it via its public
 * no-arg constructor and wraps it in a fixed supplier), as opposed to handing in
 * a pre-built supplier.
 */
@DisplayName("Interface-by-class (.interfasse(IClass)) Integration Tests")
class InterfaceByClassTest extends AbstractCrudIntegrationTest {

    /**
     * A no-arg, {@code @Interface}-annotated implementation. The framework owns the
     * single instance, so we observe it back through the domain's supplier list.
     */
    @Interface
    public static class ByClassInterface implements IInterface {
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

    /** An interface with no public no-arg constructor — must be rejected at build time. */
    public static class NoNoArgInterface implements IInterface {
        @SuppressWarnings("unused")
        private final String required;
        public NoNoArgInterface(String required) { this.required = required; }
        @Override public void handle(IDomain<?> context) {}
        @Override public ILifecycle onInit() { return this; }
        @Override public ILifecycle onStart() { return this; }
        @Override public ILifecycle onStop() { return this; }
        @Override public ILifecycle onFlush() { return this; }
        @Override public ILifecycle onReload() { return this; }
        @Override public LifecycleStatus status() { return LifecycleStatus.NEW; }
    }

    private StubDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new StubDao();
    }

    private IApi buildUserApiWith(Class<? extends IInterface> interfaceClass) throws ApiException {
        var builder = newBuilder();
        builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .interfasse(IClass.getClass(interfaceClass))
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(userDao)
                .up()
                .workflow("deleteAll").security().disable(true).up().up()
            .up();
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private static ByClassInterface theAttachedInstance(IDomain<?> domain) throws Exception {
        List<ISupplier<IInterface>> interfaces = ((com.garganttua.api.core.domain.Domain<?>) domain).getInterfaces();
        assertEquals(1, interfaces.size(), "exactly one interface must be attached");
        IInterface instance = interfaces.get(0).supply()
                .orElseThrow(() -> new AssertionError("interface supplier returned empty"));
        assertInstanceOf(ByClassInterface.class, instance,
                "the framework must instantiate the declared @Interface class");
        return (ByClassInterface) instance;
    }

    @Test
    @DisplayName(".interfasse(IClass) instantiates the class and registers exactly one supplier")
    void registersOneInstantiatedInterface() throws Exception {
        IApi api = buildUserApiWith(ByClassInterface.class);
        IDomain<?> userDomain = api.getDomain("users").orElseThrow();

        ByClassInterface attached = theAttachedInstance(userDomain);
        assertNotNull(attached, "the attached instance must be retrievable");
        assertFalse(attached.isHandleCalled(),
                "before lifecycle starts, handle() must not have been called yet");
    }

    @Test
    @DisplayName(".interfasse(IClass) instance receives handle() and lifecycle callbacks on init/start")
    void instanceReceivesLifecycleCallbacks() throws Exception {
        IApi api = buildUserApiWith(ByClassInterface.class);

        api.onInit();
        api.onStart();

        ByClassInterface attached = theAttachedInstance(api.getDomain("users").orElseThrow());

        assertTrue(attached.isHandleCalled(), "handle() must be called on init");
        assertTrue(attached.isInitCalled(), "onInit() must be called");
        assertTrue(attached.isStartCalled(), "onStart() must be called");
        assertEquals(LifecycleStatus.STARTED, attached.status(), "interface must be in STARTED state");
        assertNotNull(attached.getDomain(), "the domain context must be passed to handle()");
        assertEquals("users", attached.getDomain().getDomain(),
                "handle() must receive the very domain it is attached to");
    }

    @Test
    @DisplayName(".interfasse(IClass) instance receives onStop() on context stop")
    void instanceReceivesStop() throws Exception {
        IApi api = buildUserApiWith(ByClassInterface.class);

        api.onInit();
        api.onStart();
        ByClassInterface attached = theAttachedInstance(api.getDomain("users").orElseThrow());
        assertFalse(attached.isStopCalled(), "onStop() must not be called before stop");

        api.onStop();

        assertTrue(attached.isStopCalled(), "onStop() must be called on context stop");
        assertEquals(LifecycleStatus.STOPPED, attached.status(), "interface must be in STOPPED state");
    }

    @Test
    @DisplayName(".interfasse(IClass) supplier is stable — supplies the same instance every time")
    void supplierIsStable() throws Exception {
        IApi api = buildUserApiWith(ByClassInterface.class);
        IDomain<?> userDomain = api.getDomain("users").orElseThrow();

        @SuppressWarnings("unchecked")
        List<ISupplier<IInterface>> interfaces =
                ((com.garganttua.api.core.domain.Domain<?>) userDomain).getInterfaces();
        IInterface first = interfaces.get(0).supply().orElseThrow();
        IInterface second = interfaces.get(0).supply().orElseThrow();

        assertSame(first, second,
                "the by-class interface must be a singleton fixed supplier, not re-instantiated per supply()");
    }

    @Test
    @DisplayName(".interfasse(IClass) rejects a class without a public no-arg constructor")
    void rejectsClassWithoutNoArgConstructor() {
        ApiException ex = assertThrows(ApiException.class,
                () -> buildUserApiWith(NoNoArgInterface.class));
        assertTrue(ex.getMessage() == null
                        || ex.getMessage().contains("no-arg")
                        || ex.getMessage().toLowerCase().contains("constructor")
                        || ex.getCause() != null,
                "rejection must point at the missing no-arg constructor; got: " + ex.getMessage());
    }
}
