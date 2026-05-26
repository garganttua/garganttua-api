package com.garganttua.api.core.unit.builder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.ApiBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.dao.IDao;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.bootstrap.dsl.Bootstrap;
import com.garganttua.core.bootstrap.dsl.IBoostrap;
import com.garganttua.core.expression.dsl.ExpressionContextBuilder;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.injection.context.dsl.InjectionContextBuilder;
import com.garganttua.core.mapper.annotations.FieldMappingRule;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.dsl.IReflectionBuilder;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;
import com.garganttua.core.runtime.RuntimeContextFactory;

/**
 * Demonstrates the PoC ergonomics: {@code ApiBuilder.builder()} returns a
 * builder pre-wired into a private {@link IBoostrap}, but the framework does
 * <em>not</em> pick the reflection/injection/expression implementations —
 * that is the user's choice. Two patterns are covered:
 * <ul>
 *   <li>Owned bootstrap — the default. User adds their preferred core builders
 *       via {@code apiBuilder.bootstrap().withBuilder(...)}.</li>
 *   <li>External bootstrap — for multi-framework apps. User constructs a
 *       shared {@link Bootstrap}, plugs everything in including the
 *       ApiBuilder, and drives the build from the bootstrap directly.</li>
 * </ul>
 *
 * <p>Companion ticket: {@code docs/CORE_EVOLUTION_bootstrap_reflection_defaults.md}
 * — once {@code ReflectionBuilder.doAutoDetection()} ships sensible defaults,
 * the "user wires reflection" step disappears for the common case and a real
 * one-liner becomes possible.
 */
@DisplayName("ApiBuilder auto-bootstrap (3.0 PoC)")
class AutoBootstrapApiBuilderTest {

    /**
     * Install a JVM-wide IReflection before any ApiBuilder is constructed.
     * The {@code ApiBuilder} private constructor calls
     * {@code IClass.getClass(...)} to declare its dependencies, which needs a
     * registered IReflection — a pre-existing constraint, unrelated to the
     * PoC. In a real app this happens once in {@code main()}; in tests we do
     * it in {@code @BeforeAll} so the test class is order-independent within
     * a Surefire JVM.
     */
    @BeforeAll
    static void installReflectionForTheJvm() throws ApiException {
        try {
            ReflectionBuilder.builder()
                    .withProvider(new RuntimeReflectionProvider())
                    .withScanner(new ReflectionsAnnotationScanner())
                    .build();
        } catch (com.garganttua.core.dsl.DslException e) {
            throw new ApiException("Failed to install IReflection: " + e.getMessage(), e);
        }
    }

    public static class TestEntity {
        private String id;
        private String uuid;
        private String tenantId;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }

    public static class TestDto {
        @FieldMappingRule(sourceFieldAddress = "id") private String id;
        @FieldMappingRule(sourceFieldAddress = "uuid") private String uuid;
        @FieldMappingRule(sourceFieldAddress = "tenantId") private String tenantId;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    }

    public static class InMemoryDao implements IDao {
        private final List<Object> storage = new ArrayList<>();
        @Override public void registerDomain(IDomainDefinition d) {}
        @Override public List<Object> find(Optional<IPageable> p, Optional<IFilter> f, Optional<ISort> s) { return new ArrayList<>(storage); }
        @Override public Object save(Object o) { storage.add(o); return o; }
        @Override public void delete(Object o) { storage.remove(o); }
        @Override public long count(IFilter f) { return storage.size(); }
    }

    /**
     * Builds the trio (reflection, injection, expression) the user has to bring
     * themselves. Centralised in this helper to keep the test bodies focused on
     * the ApiBuilder shape. We use runtime-reflection + the Reflections scanner
     * here, but the whole point of the PoC is that the user picks: an AOT
     * stack or an entirely custom one would be equally valid.
     *
     * <p>The reflection builder is built eagerly because the constructors of
     * {@code InjectionContextBuilder} and {@code ExpressionContextBuilder}
     * call {@code IClass.getClass(...)} — that requires a registered
     * {@code IReflection}.
     */
    private static IBoostrap wireUserStack(IApiBuilder ab) throws ApiException {
        try {
            IReflectionBuilder refl = ReflectionBuilder.builder()
                    .withProvider(new RuntimeReflectionProvider())
                    .withScanner(new ReflectionsAnnotationScanner());
            refl.build();

            IInjectionContextBuilder inj = InjectionContextBuilder.builder()
                    .childContextFactory(new RuntimeContextFactory());

            IExpressionContextBuilder expr = ExpressionContextBuilder.builder();
            expr.autoDetect(true);
            expr.withPackage("com.garganttua.core.expression.functions");
            expr.withPackage("com.garganttua.core.script.functions");
            expr.withPackage("com.garganttua.api.core.expression");

            // Bootstrap declares a `require(IReflectionBuilder, AUTO_DETECT)` on
            // itself — satisfy it via provide() in addition to withBuilder().
            return ab.bootstrap()
                    .provide(refl)
                    .withBuilder(refl)
                    .withBuilder(inj)
                    .withBuilder(expr);
        } catch (com.garganttua.core.dsl.DslException e) {
            throw new ApiException("Failed to wire test stack: " + e.getMessage(), e);
        }
    }

    @Nested
    @DisplayName("Owned bootstrap (default path)")
    class OwnedBootstrap {

        @Test
        @DisplayName("builder() exposes its private Bootstrap via bootstrap()")
        void exposes_internal_bootstrap() {
            IApiBuilder ab = ApiBuilder.builder();
            IBoostrap b = ab.bootstrap();
            assertNotNull(b, "factory must always wire an internal Bootstrap");
            assertSame(b, ab.bootstrap(), "bootstrap() is stable across calls");
        }

        @Test
        @DisplayName("packages(...) propagates to the underlying Bootstrap")
        void packages_propagate_to_bootstrap() throws ApiException {
            IApiBuilder ab = ApiBuilder.builder();
            ab.packages("com.example.app", "com.example.security");

            String[] pkgs = ab.bootstrap().getPackages();
            List<String> asList = List.of(pkgs);
            assertTrue(asList.contains("com.example.app"),
                    "packages registered on ApiBuilder must flow to the Bootstrap so reflection/injection/expression see them too");
            assertTrue(asList.contains("com.example.security"));
        }

        @Test
        @DisplayName("build() returns a working IApi once the user wires their core stack")
        void build_works_after_user_wires_dependencies() throws ApiException {
            IApiBuilder ab = ApiBuilder.builder();
            wireUserStack(ab);

            IApi api = ab.multiTenant(false)
                    .domain(IClass.getClass(TestEntity.class))
                        .entity()
                            .id("id").uuid("uuid").tenantId("tenantId")
                        .up()
                        .dto(IClass.getClass(TestDto.class))
                            .id("id").uuid("uuid").tenantId("tenantId")
                            .db(new InMemoryDao())
                        .up()
                    .up()
                    .build();

            assertNotNull(api, "Api must be built end-to-end when the user supplied the core stack");
            Optional<IDomain<?>> domain = api.getDomain("testentities");
            assertTrue(domain.isPresent(),
                    "auto-derived domain name from TestEntity must resolve via IApi.getDomain(\"testentities\")");
            assertEquals(IClass.getClass(TestEntity.class), domain.get().getEntityClass(),
                    "the domain context must bind back to the user's entity class");
        }

        @Test
        @DisplayName("build() is idempotent (second call returns the same IApi)")
        void build_is_idempotent() throws ApiException {
            IApiBuilder ab = ApiBuilder.builder();
            wireUserStack(ab);
            ab.multiTenant(false)
                    .domain(IClass.getClass(TestEntity.class))
                        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                        .dto(IClass.getClass(TestDto.class))
                            .id("id").uuid("uuid").tenantId("tenantId").db(new InMemoryDao())
                        .up()
                    .up();

            IApi first = ab.build();
            IApi second = ab.build();
            assertSame(first, second,
                    "AbstractAutomaticBuilder caches the built result — same instance returned on every build() call");
        }
    }

    @Nested
    @DisplayName("Error guidance")
    class ErrorGuidance {

        @Test
        @DisplayName("builder() without prior IClass.setReflection() but with reflection providers on the classpath: SPI cold-start succeeds, no exception")
        void builder_with_spi_providers_succeeds_without_manual_setup() throws Exception {
            // garganttua-core c19c7d66 made Bootstrap discover IReflectionProvider
            // / IAnnotationScanner via ServiceLoader at cold start. Our test
            // classpath transitively includes garganttua-runtime-reflection and
            // garganttua-reflections, which both publish their META-INF/services
            // descriptor — so even when we nullify the global holder, calling
            // ApiBuilder.builder() now repopulates IReflection via SPI rather
            // than throwing.
            //
            // The "no reflection at all" guidance path (manual setup absent AND
            // SPI providers absent) is still wired in ApiBuilder.builder() — it
            // catches the underlying "No IReflection ..." IllegalStateException
            // and surfaces NO_REFLECTION_GUIDANCE — but exercising it would
            // require running Surefire without any provider jar on the
            // classpath, which is not feasible from within this test suite.
            IReflection saved = IClass.getReflection();
            java.lang.reflect.Field f =
                    Class.forName("com.garganttua.core.reflection.IClass$ReflectionHolder")
                            .getDeclaredField("globalDefault");
            f.setAccessible(true);
            try {
                f.set(null, null);
                assertDoesNotThrow(ApiBuilder::builder,
                        "ApiBuilder.builder() must succeed via SPI cold-start when reflection providers are on the classpath");
                assertNotNull(IClass.getReflection(),
                        "SPI cold-start must have repopulated IClass.getReflection()");
            } finally {
                f.set(null, saved);
            }
        }

        @Test
        @DisplayName("build() without wiring reports concrete next steps (SPI fallback disabled, simulates a deps-less env)")
        void build_without_wiring_includes_guidance() {
            // garganttua-core's SPI fallback would normally auto-load reflection,
            // injection and expression builders from META-INF/services on the
            // classpath — turning this "nothing wired" scenario into a successful
            // build. To still exercise the guidance text under regression, we
            // explicitly disable the SPI fallback on the private bootstrap so
            // the require()'d core builders end up unresolved.
            IApiBuilder ab = ApiBuilder.builder();
            ((com.garganttua.core.bootstrap.dsl.Bootstrap) ab.bootstrap()).disableSpiFallback();
            ab.multiTenant(false)
                    .domain(IClass.getClass(TestEntity.class))
                        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
                        .dto(IClass.getClass(TestDto.class))
                            .id("id").uuid("uuid").tenantId("tenantId").db(new InMemoryDao())
                        .up()
                    .up();

            ApiException e = assertThrows(ApiException.class, ab::build,
                    "build() with an unwired bootstrap and SPI disabled must fail loudly");
            String msg = e.getMessage();
            assertNotNull(msg);
            // The message must surface the underlying core error AND add the
            // api-side recipe so a fresh user can act without leaving the log.
            assertTrue(msg.contains("Required dependency"),
                    "must preserve the core diagnostic: <" + msg + ">");
            assertTrue(msg.contains("apiBuilder.bootstrap()"),
                    "must include the bootstrap.provide()/withBuilder() snippet, got: <" + msg + ">");
            assertTrue(msg.contains("intoBootstrap"),
                    "must mention intoBootstrap as the multi-framework alternative, got: <" + msg + ">");
        }
    }

    @Nested
    @DisplayName("External bootstrap (multi-framework path)")
    class ExternalBootstrap {

        @Test
        @DisplayName("intoBootstrap(external) reroutes the builder to the supplied Bootstrap")
        void intoBootstrap_swaps_orchestrator() throws ApiException {
            IBoostrap external = Bootstrap.builder();
            IApiBuilder ab = ApiBuilder.builder().intoBootstrap(external);
            assertSame(external, ab.bootstrap(),
                    "after intoBootstrap(external), bootstrap() must return the external instance");
        }

        @Test
        @DisplayName("Calling intoBootstrap with the same instance is a no-op")
        void intoBootstrap_same_instance_is_noop() throws ApiException {
            IApiBuilder ab = ApiBuilder.builder();
            IBoostrap original = ab.bootstrap();
            ab.intoBootstrap(original);
            assertSame(original, ab.bootstrap());
        }
    }
}
