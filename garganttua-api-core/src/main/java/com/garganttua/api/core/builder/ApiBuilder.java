package com.garganttua.api.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.garganttua.api.core.builder.binder.ApiStartupBinderBuilder;
import com.garganttua.api.core.context.Api;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.BuildingStage;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.context.dsl.IApiStartupBinderBuilder;
import com.garganttua.api.commons.context.dsl.IDomainBuilder;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.context.dsl.security.IAuthenticationBuilder;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.protocol.Protocol;
import com.garganttua.api.commons.security.authorization.AuthorizationProtocol;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.security.context.IAuthenticationContext;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.serialization.Serializer;
import com.garganttua.core.bootstrap.annotations.Bootstrap;
import com.garganttua.core.dsl.IObservableBuilder;
import com.garganttua.core.dsl.annotations.Scan;
import com.garganttua.core.dsl.dependency.AbstractAutomaticDependentBuilder;
import com.garganttua.core.dsl.dependency.DependencyPhase;
import com.garganttua.core.dsl.dependency.DependencySpec;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.injection.BeanReference;
import com.garganttua.core.injection.BeanStrategy;
import com.garganttua.core.injection.IInjectionContext;
import com.garganttua.core.injection.Predefined;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.dsl.IReflectionBuilder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Bootstrap
@Scan(scan = "com.garganttua.api.core")
public class ApiBuilder extends AbstractAutomaticDependentBuilder<IApiBuilder, IApi>
		implements IApiBuilder {

	private ApiBuilder() {
		super(Set.of(
						DependencySpec.require(IClass.getClass(IInjectionContextBuilder.class), DependencyPhase.BUILD),
						DependencySpec.require(IClass.getClass(IExpressionContextBuilder.class), DependencyPhase.BUILD),
						DependencySpec.require(IClass.getClass(IReflectionBuilder.class), DependencyPhase.BUILD),
						// Optional: when present (bootstrap-discovered), each Domain
						// is attached to the binding so @Observer scans flow through.
						// When absent, observability is just disabled.
						DependencySpec.use(IClass.getClass(
								com.garganttua.core.observability.dsl.IObservabilityBuilder.class),
								DependencyPhase.BUILD)));
	}

	private final Set<String> packages = ConcurrentHashMap.newKeySet();

	/**
	 * Framework packages that {@code doAutoDetection()} auto-injects into the
	 * scan surface so built-in assets (protocols, serializers, security
	 * primitives shipped with the framework) are discoverable without the
	 * user repeating these names in every {@code ApiBuilder.builder()} call.
	 * Opt-out via {@link #includeFrameworkPackages(boolean)}.
	 */
	static final String[] FRAMEWORK_PACKAGES = {"com.garganttua.api", "com.garganttua.core"};

	private volatile boolean includeFrameworkPackages = true;

	private volatile String superTenantId;

	private volatile boolean superTenantAutoCreate = false;

	private volatile boolean multiTenant = true;

	private final Map<IClass<?>, DomainBuilder<?>> domainBuilders = new ConcurrentHashMap<>();
	private volatile SecurityBuilder securityBuilder;
	private final List<ApiStartupBinderBuilder> startupBinderBuilders = new CopyOnWriteArrayList<>();
	private final List<ISerializer> serializers = new CopyOnWriteArrayList<>();
	private final List<ISupplierBuilder<?, ? extends ISupplier<?>>> serializerBuilders = new CopyOnWriteArrayList<>();
	private final List<IProtocol<?, ?>> protocols = new CopyOnWriteArrayList<>();
	private final List<ISupplierBuilder<?, ? extends ISupplier<?>>> protocolBuilders = new CopyOnWriteArrayList<>();
	private final List<IAuthorizationProtocol> authorizationProtocols = new CopyOnWriteArrayList<>();
	private final List<ISupplierBuilder<?, ? extends ISupplier<?>>> authorizationProtocolBuilders = new CopyOnWriteArrayList<>();

	private volatile IInjectionContextBuilder injectionContextBuilder;
	private volatile IExpressionContextBuilder expressionContextBuilder;
	private volatile com.garganttua.core.observability.dsl.IObservabilityBuilder observabilityBuilder;
	private volatile IInjectionContext injectionContext;
	private volatile AuthoritiesEndpointBuilder authoritiesEndpointBuilder;

	/**
	 * Default entry point — returns a fresh {@code ApiBuilder}. The caller is
	 * responsible for wiring the reflection / injection / expression stack and
	 * orchestrating the build (typically through a
	 * {@code com.garganttua.core.bootstrap.dsl.IBoostrap} driven from the
	 * application's {@code main()}). The {@code @Bootstrap} annotation on this
	 * class makes it auto-discoverable when the user registers an external
	 * {@code Bootstrap.autoDetect(true)}.
	 *
	 * <p>The framework deliberately does <em>not</em> instantiate a Bootstrap
	 * of its own, nor pick an implementation for reflection, injection or
	 * expression — those are the user's calls.
	 *
	 * <p>If the user has not installed an {@code IReflection} before calling
	 * this factory, the {@code ApiBuilder} constructor — which uses
	 * {@code IClass.getClass(...)} to declare its dependencies — throws an
	 * {@code IllegalStateException} from core. We re-throw it as an
	 * {@link ApiException} with concrete guidance.
	 */
	public static IApiBuilder builder() {
		try {
			return new ApiBuilder();
		} catch (IllegalStateException e) {
			if (e.getMessage() != null && e.getMessage().contains("No IReflection")) {
				throw new ApiException(NO_REFLECTION_GUIDANCE, e);
			}
			throw e;
		}
	}

	private static final String NO_REFLECTION_GUIDANCE =
			"No IReflection installed — ApiBuilder needs one to declare its dependencies. "
			+ "Install a reflection stack ONCE in your app's main() before calling ApiBuilder.builder(), e.g.:\n"
			+ "\n"
			+ "    IClass.setReflection(ReflectionBuilder.builder()\n"
			+ "        .withProvider(new RuntimeReflectionProvider())     // garganttua-runtime-reflection\n"
			+ "        .withScanner(new ReflectionsAnnotationScanner())   // garganttua-reflections\n"
			+ "        .build());\n"
			+ "\n"
			+ "Or rely on garganttua-core's ServiceLoader cold-start: put the provider/scanner jars on\n"
			+ "the classpath and a Bootstrap.builder() call anywhere in your bootstrap setup picks them up.";

	@Override
	public IApiBuilder superTenantId(String superTenantId) {
		if (!this.multiTenant) {
			throw new ApiException("Cannot set superTenantId — multi-tenancy is disabled (.multiTenant(false) was called or set as default). "
					+ "Either keep multi-tenancy enabled (do not call .multiTenant(false)) if you need a super-tenant, "
					+ "or drop the .superTenantId(...) call for a single-tenant app.");
		}
		this.superTenantId = Objects.requireNonNull(superTenantId, "Super tenant ID cannot be null");
		return this;
	}

	@Override
	public IApiStartupBinderBuilder startup(BuildingStage stage,
			ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException {
		ApiStartupBinderBuilder binder = new ApiStartupBinderBuilder(this, supplier);
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public IApiStartupBinderBuilder startup(BuildingStage stage, Object object) throws ApiException {
		ApiStartupBinderBuilder binder = new ApiStartupBinderBuilder(this, object);
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public <E> IDomainBuilder<E> domain(IClass<E> entityClass) throws ApiException {
		Objects.requireNonNull(entityClass, "Entity class cannot be null");

		return (IDomainBuilder<E>) this.domainBuilders.computeIfAbsent(entityClass, clazz -> {
			return new DomainBuilder<>(this, clazz);
		});
	}

	@Override
	public IApiBuilder superTenantAutoCreate(boolean b) throws ApiException {
		if (!this.multiTenant) {
			throw new ApiException("Cannot set superTenantAutoCreate — multi-tenancy is disabled. "
					+ "Drop the .superTenantAutoCreate(...) call for single-tenant apps, or keep .multiTenant(true).");
		}
		this.superTenantAutoCreate = b;
		return this;
	}

	@Override
	public IApiBuilder multiTenant(boolean enabled) throws ApiException {
		if (!enabled && this.superTenantId != null) {
			throw new ApiException("Cannot call .multiTenant(false) after .superTenantId(...) has been set. "
					+ "A super-tenant only makes sense in multi-tenant mode — either remove the .superTenantId(...) call "
					+ "or keep multi-tenancy enabled. (DSL is order-sensitive: set .multiTenant(false) first if that is what you want.)");
		}
		if (!enabled && this.superTenantAutoCreate) {
			throw new ApiException("Cannot call .multiTenant(false) after .superTenantAutoCreate(true) has been set. "
					+ "Same reason as .superTenantId — remove the .superTenantAutoCreate(...) call or keep multi-tenancy enabled.");
		}
		this.multiTenant = enabled;
		return this;
	}

	@Override
	public synchronized IApiSecurityBuilder security() {
		if (this.securityBuilder == null) {
			this.securityBuilder = new SecurityBuilder(this.packages, this);
		}
		return this.securityBuilder;
	}

	@Override
	public IApiBuilder serializer(ISerializer serializer) throws ApiException {
		Objects.requireNonNull(serializer, "Serializer cannot be null");
		this.serializers.add(serializer);
		return this;
	}

	@Override
	public IApiBuilder serializer(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException {
		Objects.requireNonNull(bean, "Serializer supplier builder cannot be null");
		this.serializerBuilders.add(bean);
		return this;
	}

	@Override
	public IApiBuilder protocol(IProtocol<?, ?> protocol) throws ApiException {
		Objects.requireNonNull(protocol, "Protocol cannot be null");
		this.protocols.add(protocol);
		return this;
	}

	@Override
	public IApiBuilder protocol(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException {
		Objects.requireNonNull(bean, "Protocol supplier builder cannot be null");
		this.protocolBuilders.add(bean);
		return this;
	}

	@Override
	public IApiBuilder authorizationProtocol(IAuthorizationProtocol protocol) throws ApiException {
		Objects.requireNonNull(protocol, "Authorization protocol cannot be null");
		this.authorizationProtocols.add(protocol);
		return this;
	}

	@Override
	public IApiBuilder authorizationProtocol(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException {
		Objects.requireNonNull(bean, "Authorization protocol supplier builder cannot be null");
		this.authorizationProtocolBuilders.add(bean);
		return this;
	}

	@Override
	public com.garganttua.api.commons.context.dsl.IAuthoritiesEndpointBuilder exposeAuthorities() throws ApiException {
		if (this.authoritiesEndpointBuilder == null) {
			this.authoritiesEndpointBuilder = new AuthoritiesEndpointBuilder(this);
		}
		return this.authoritiesEndpointBuilder;
	}

	public String[] getPackages() {
		return this.packages.toArray(new String[0]);
	}

	public IApiBuilder withPackage(String packageName) {
		log.atDebug().log("Adding package: {}", packageName);
		this.packages.add(Objects.requireNonNull(packageName, "Package name cannot be null"));
		return this;
	}

	public IApiBuilder withPackages(String[] packageNames) {
		log.atDebug().log("Adding {} packages", packageNames.length);
		Objects.requireNonNull(packageNames, "Package names cannot be null");
		for (String pkg : packageNames) {
			this.withPackage(pkg);
		}
		return this;
	}

	@Override
	public IApiBuilder includeFrameworkPackages(boolean include) {
		this.includeFrameworkPackages = include;
		return this;
	}

	@Override
	protected void doAutoDetectionWithDependency(Object dependency) throws ApiException {
		log.atTrace().log("Entering doAutoDetectionWithDependency() with dependency: {}", dependency);

		if (dependency instanceof IInjectionContext context) {
			// Auto-detect entities and domains from injection context
			// This could scan for @Entity annotated classes
			log.atDebug().log("Auto-detecting domains from InjectionContext");
			// TODO: Implement entity/domain auto-detection from injection context
		}

		log.atTrace().log("Exiting doAutoDetectionWithDependency() method");
	}

	@Override
	protected void doPreBuildWithDependency(Object dependency) {
		log.atTrace().log("Entering doPreBuildWithDependency() with dependency: {}", dependency);

		if (dependency instanceof IInjectionContext context) {
			this.injectionContext = context;
			log.atDebug().log("InjectionContext captured in pre-build phase");
		}

		log.atTrace().log("Exiting doPreBuildWithDependency() method");
	}

	@Override
	protected void doPostBuildWithDependency(Object dependency) {
		log.atTrace().log("Entering doPostBuildWithDependency() with dependency: {}", dependency);

		if (dependency instanceof IInjectionContext context) {
			registerBuiltObjectInContext(context, this.built);
		}

		log.atTrace().log("Exiting doPostBuildWithDependency() method");
	}

	private void registerBuiltObjectInContext(IInjectionContext context, IApi apiContext) {
		log.atDebug().log("Registering IApi as bean in InjectionContext");
		String providerName = Predefined.BeanProviders.garganttua.toString();

		BeanReference<IApi> beanRef = new BeanReference<>(
				IClass.getClass(IApi.class),
				Optional.of(BeanStrategy.singleton),
				Optional.of("Api"),
				Set.of());
		context.addBean(providerName, beanRef, apiContext);
		log.atDebug().log("IApi successfully registered as bean with 'Api' name");

		// Register each domain context
		for (Map.Entry<String, IDomain<?>> entry : ((Api) apiContext).getDomains().entrySet()) {
			String domainName = entry.getKey();
			IDomain<?> domainContext = entry.getValue();

			BeanReference<IDomain<?>> domainBeanRef = new BeanReference<>(
					(IClass<IDomain<?>>) (IClass<?>) IClass.getClass(IDomain.class),
					Optional.of(BeanStrategy.singleton),
					Optional.of("domain." + domainName),
					Set.of());
			context.addBean(providerName, domainBeanRef, domainContext);
			log.atDebug().log("IDomain successfully registered as bean with 'domain.{}' name", domainName);

			// Register the tenant domain context with a well-known bean name
			if (domainContext.isTenantEntity()) {
				BeanReference<IDomain<?>> tenantBeanRef = new BeanReference<>(
						(IClass<IDomain<?>>) (IClass<?>) IClass.getClass(IDomain.class),
						Optional.of(BeanStrategy.singleton),
						Optional.of("tenantDomain"),
						Set.of());
				context.addBean(providerName, tenantBeanRef, domainContext);
				log.atInfo().log("Tenant domain context registered as bean 'tenantDomain' (domain: {})", domainName);
			}
		}

		// Register authentication contexts as named beans
		if (this.securityBuilder != null) {
			for (Map.Entry<IClass<?>, IAuthenticationBuilder> entry : this.securityBuilder.getAuthenticationBuilders().entrySet()) {
				IClass<?> authClass = entry.getKey();
				IAuthenticationContext authContext = entry.getValue().build();
				String beanName = "authentication." + authClass.getSimpleName();

				@SuppressWarnings("unchecked")
				BeanReference<IAuthenticationContext> authBeanRef = new BeanReference<>(
						(IClass<IAuthenticationContext>) (IClass<?>) IClass.getClass(IAuthenticationContext.class),
						Optional.of(BeanStrategy.singleton),
						Optional.of(beanName),
						Set.of());
				context.addBean(providerName, authBeanRef, authContext);
				log.atDebug().log("IAuthenticationContext registered as bean '{}'", beanName);
			}
		}
	}

	private void registerMapperBean() {
		log.atDebug().log("Registering IMapper as bean in InjectionContext");
		String providerName = Predefined.BeanProviders.garganttua.toString();

		BeanReference<IMapper> beanRef = new BeanReference<>(
				IClass.getClass(IMapper.class),
				Optional.of(BeanStrategy.singleton),
				Optional.of("mapper"),
				Set.of());
		this.injectionContext.addBean(providerName, beanRef, DefaultMapper.mapper());
		log.atDebug().log("IMapper successfully registered as bean with 'mapper' name");
	}

	@Override
	protected synchronized IApi doBuild() throws ApiException {
		log.atTrace().log("Entering doBuild() method");

		try {
			// Ensure we have an injection context. The caller is responsible for wiring it,
			// either by registering an IInjectionContextBuilder into the Bootstrap that
			// drives this ApiBuilder, or by calling
			// ((IDependentBuilder) apiBuilder).provide(builder) before .build().
			if (this.injectionContext == null) {
				throw new ApiException(
						"InjectionContext is required but no IInjectionContextBuilder was provided.\n"
						+ "\n"
						+ "Register one on the Bootstrap that drives ApiBuilder:\n"
						+ "    bootstrap.withBuilder(injectionContextBuilder);\n"
						+ "\n"
						+ "Or wire it directly on the ApiBuilder:\n"
						+ "    ((IDependentBuilder) apiBuilder).provide(injectionContextBuilder);\n");
			}

			// Register default mapper as bean
			registerMapperBean();

			// Build all domain contexts
			Map<String, IDomain<?>> domainContexts = new HashMap<>();
			for (DomainBuilder<?> domainBuilder : this.domainBuilders.values()) {
				domainBuilder.setDependencyBuilders(this.injectionContextBuilder, this.expressionContextBuilder);
				IDomain<?> domainContext = domainBuilder.build();
				domainContexts.put(domainContext.getDomain(), domainContext);
				log.atDebug().log("Built domain context: {}", domainContext.getDomain());
			}

			// Workflow-level ObservableEvent observers are wired by core's
			// ObservabilityBuilder via @Observer scan (bootstrap-discovered).
			// We do not duplicate that wiring here — the api just produces
			// domain workflows; they self-register as emitters at build time
			// (cf. core 2.0.0-ALPHA02 commit 43a2d1dc).

			// Validate tenant domain presence when multi-tenancy is enabled
			if (this.multiTenant) {
				boolean hasTenantDomain = domainContexts.values().stream()
						.anyMatch(IDomain::isTenantEntity);
				if (!hasTenantDomain) {
					throw new ApiException(
							"Multi-tenancy is enabled but no domain is marked as tenant. "
							+ "Use .tenant(true) on a domain or disable multi-tenancy with .multiTenant(false)");
				}
			}

			// Validate that every domain whose linked authorization is signable
			// also has a key configured (either .key(supplier) or .key(domain)).
			// Surfaces misconfiguration at build time rather than failing at the
			// first sign call. Mirrors the runtime check in
			// SecurityExpressions.resolveKeyRealm.
			validateSignableKeyConfig(domainContexts);

			// Build security context if configured
			if (this.securityBuilder != null) {
				this.securityBuilder.build();
				log.atDebug().log("Built security context");
			}

			// Build startup binders
			List<IMethodBinder<Void>> startupBinders = new ArrayList<>();
			for (ApiStartupBinderBuilder binder : this.startupBinderBuilders) {
				startupBinders.add(binder.build());
			}
			log.atDebug().log("Built {} startup binders", startupBinders.size());

			// Build serializers
			List<ISerializer> builtSerializers = new ArrayList<>(this.serializers);
			for (ISupplierBuilder<?, ? extends ISupplier<?>> sb : this.serializerBuilders) {
				ISupplier<?> supplier = sb.build();
				Object serializer = supplier.supply();
				builtSerializers.add((ISerializer) serializer);
			}
			log.atDebug().log("Built {} serializers", builtSerializers.size());

			// Build protocols
			List<IProtocol<?, ?>> builtProtocols = new ArrayList<>(this.protocols);
			for (ISupplierBuilder<?, ? extends ISupplier<?>> pb : this.protocolBuilders) {
				ISupplier<?> supplier = pb.build();
				Object protocol = supplier.supply();
				builtProtocols.add((IProtocol<?, ?>) protocol);
			}
			log.atDebug().log("Built {} protocols", builtProtocols.size());

			// Build authorization protocols
			List<IAuthorizationProtocol> builtAuthzProtocols = new ArrayList<>(this.authorizationProtocols);
			for (ISupplierBuilder<?, ? extends ISupplier<?>> ab : this.authorizationProtocolBuilders) {
				ISupplier<?> supplier = ab.build();
				Object authzProtocol = supplier.supply();
				builtAuthzProtocols.add((IAuthorizationProtocol) authzProtocol);
			}
			log.atDebug().log("Built {} authorization protocols", builtAuthzProtocols.size());

			// Build the authorities-endpoint descriptor when opted-in. Null when
			// the user did not call .exposeAuthorities() — the Api context
			// surfaces that as IApi.getAuthoritiesEndpoint() == null and
			// IApi.getAuthoritiesForCaller refuses every call.
			com.garganttua.api.commons.context.IAuthoritiesEndpoint authoritiesEndpoint = null;
			if (this.authoritiesEndpointBuilder != null) {
				authoritiesEndpoint = this.authoritiesEndpointBuilder.build();
			}

			// Create and return API context
			IApi apiContext = new Api(this.injectionContext, domainContexts,
					this.superTenantId, this.superTenantAutoCreate, this.multiTenant,
					startupBinders, builtSerializers, builtProtocols, builtAuthzProtocols,
					authoritiesEndpoint);

			// Attach each Domain to the bootstrap-wired ObservabilityBinding so
			// every @Observer scanned by core's ObservabilityBuilder sees
			// api:operation:<domain>:<op> Start/End/Error events. No-op when
			// no observability builder was provided (manual setups).
			if (this.observabilityBuilder != null) {
				com.garganttua.core.observability.ObservabilityBinding binding =
						this.observabilityBuilder.getBinding();
				if (binding != null) {
					for (IDomain<?> domain : domainContexts.values()) {
						binding.attachSource(domain);
					}
					log.atDebug().log("Attached {} domain(s) to the ObservabilityBinding",
							domainContexts.size());
				}
			}

			log.atDebug().log("Built Api with {} domains", domainContexts.size());
			log.atTrace().log("Exiting doBuild() method");

			return apiContext;

		} catch (ApiException e) {
			throw new ApiException("Failed to build API context: " + e.getMessage(), e);
		}
	}

	@Override
	protected void doAutoDetection() throws ApiException {
		log.atTrace().log("Entering doAutoDetection() method");
		// Framework packages contribute their built-in *assets* (serializers,
		// protocols, authorization protocols) — never user-domain entities or
		// user security configs, which live exclusively in user-declared
		// packages. The split avoids dragging in @Entity*-annotated test
		// fixtures of the framework's own test classpath when scanning
		// com.garganttua.api / com.garganttua.core.
		Set<String> assetScanPackages = assetScanSurface();
		autoDetectSerializers(assetScanPackages);
		autoDetectProtocols(assetScanPackages);
		autoDetectAuthorizationProtocols(assetScanPackages);
		new com.garganttua.api.core.builder.scan.EntityAnnotationScanner(this, this.packages).scan();
		new com.garganttua.api.core.builder.scan.SecurityAnnotationScanner(this, this.packages).scan();
		log.atTrace().log("Exiting doAutoDetection() method");
	}

	/**
	 * Union of user-declared packages and the framework's own packages (when
	 * {@link #includeFrameworkPackages(boolean)} is on — the default). Used
	 * only by asset auto-detection ({@code @Serializer}, {@code @Protocol},
	 * {@code @AuthorizationProtocol}) so the framework can ship built-in
	 * implementations and have them picked up out of the box.
	 */
	private Set<String> assetScanSurface() {
		Set<String> surface = new java.util.HashSet<>(this.packages);
		if (this.includeFrameworkPackages) {
			for (String pkg : FRAMEWORK_PACKAGES) {
				surface.add(pkg);
			}
		}
		return surface;
	}

	/**
	 * Walks every authenticator domain, finds its linked authorization
	 * definition, and refuses the build when the authorization is signable
	 * but no key was configured — either via {@code .key(supplier)} on the
	 * authenticator's authorization DSL, or via {@code .key(domain)} pointing
	 * at a {@code @Key}-marked entity domain.
	 *
	 * <p>Also rejects {@code .key(domain)} when the referenced domain has not
	 * actually been marked as a key domain (no {@code .key()} sub-builder, no
	 * {@code @Key} annotation), keeping symmetry with how {@code .authenticator()}
	 * requires its target to be marked.
	 */
	private void validateSignableKeyConfig(Map<String, IDomain<?>> domainContexts) throws ApiException {
		for (IDomain<?> domain : domainContexts.values()) {
			if (!(domain.getDomainDefinition() instanceof com.garganttua.api.core.definition.DomainDefinition<?> domDef)) {
				continue;
			}
			com.garganttua.api.commons.definition.IDomainSecurityDefinition secDef = domDef.domainSecurityDefinition();
			if (secDef == null) continue;
			com.garganttua.api.commons.definition.IAuthenticatorDefinition authDef = secDef.authenticatorDefinition();
			if (authDef == null) continue;
			com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition authzAuthDef =
					authDef.authorizationDefinition();
			if (authzAuthDef == null) continue;

			// Resolve the linked authorization domain to inspect its signable flag.
			com.garganttua.api.commons.context.dsl.IDomainBuilder<?> authzDomainBuilder =
					authzAuthDef.authorizationDomainBuilder();
			if (authzDomainBuilder == null) continue;

			IDomain<?> authzDomain = findDomainByEntityClass(domainContexts, authzDomainBuilder.getEntityClass());
			if (authzDomain == null) continue;
			com.garganttua.api.commons.definition.IDomainSecurityDefinition authzSecDef =
					authzDomain.getDomainDefinition() instanceof com.garganttua.api.core.definition.DomainDefinition<?> authzDef
							? authzDef.domainSecurityDefinition() : null;
			com.garganttua.api.commons.definition.IDomainAuthorizationDefinition signableDef =
					authzSecDef != null ? authzSecDef.authorizationDefinition() : null;
			if (signableDef == null || !signableDef.signable()) continue;

			boolean hasSupplier = authzAuthDef.keyRealm() != null;
			boolean hasKeyDomain = authzAuthDef.keyDefinition() != null
					&& authzAuthDef.keyDefinition().keyDomain() != null;
			if (!hasSupplier && !hasKeyDomain) {
				throw new ApiException("Domain '" + domain.getDomainName()
						+ "' declares a signable authorization (linked to domain '"
						+ authzDomain.getDomainName()
						+ "') but neither .key(supplier) nor .key(domain) is wired on its "
						+ "authenticator's authorization DSL. Add one before .build().");
			}

			if (hasKeyDomain) {
				IDomain<?> keyDomain = findDomainByEntityClass(domainContexts,
						authzAuthDef.keyDefinition().keyDomain().getEntityClass());
				if (keyDomain == null) {
					throw new ApiException("Domain '" + domain.getDomainName()
							+ "' references a .key(domain) whose entity class '"
							+ authzAuthDef.keyDefinition().keyDomain().getEntityClass().getName()
							+ "' did not resolve to a registered domain on the API. Make sure "
							+ ".domain(KeyEntity.class) was declared before .build().");
				}
				if (keyDomain.getDomainDefinition().keyDefinition() == null) {
					throw new ApiException("Domain '" + domain.getDomainName()
							+ "' references key domain '" + keyDomain.getDomainName()
							+ "' which is not marked as a @Key domain. Annotate the entity with @Key "
							+ "and its fields with @KeyName / @KeyAlgorithm / @KeySignatureAlgorithm / "
							+ "@KeyForSigning / @KeyForSignatureVerification, or call .key().name(...)... "
							+ "on its domain builder.");
				}

				// Rotation creates new keys — it implies generation. Refuse the
				// inconsistent combination at build time so the user catches it
				// before the first sign call.
				var keyConfig = authzAuthDef.keyDefinition();
				if (keyConfig.autoRotate() && !keyConfig.autoGenerate()) {
					throw new ApiException("Domain '" + domain.getDomainName()
							+ "' configures .autoRotate(true) with .autoGenerate(false) on its key DSL — "
							+ "rotation creates a new key, which is a generation. Either flip autoGenerate "
							+ "to true, or flip autoRotate to false.");
				}
			}
		}
	}

	private static IDomain<?> findDomainByEntityClass(Map<String, IDomain<?>> domains, IClass<?> target) {
		if (target == null) return null;
		for (IDomain<?> domain : domains.values()) {
			IClass<?> entityClass = domain.getEntityClass();
			if (entityClass != null && entityClass.equals(target)) return domain;
		}
		return null;
	}

	/**
	 * Scans the configured packages for classes annotated with {@link Serializer}
	 * and registers their instances on the global serializer pool. Silently
	 * no-ops when no packages are configured or when no reflection scanner is
	 * available (e.g. native image without pre-computed metadata).
	 */
	private void autoDetectSerializers(Set<String> scanSurface) {
		if (scanSurface.isEmpty()) {
			return;
		}
		com.garganttua.core.reflection.IReflection reflection;
		try {
			reflection = IClass.getReflection();
		} catch (Exception e) {
			log.atWarn().log("No IReflection available for @Serializer auto-detection: {}", e.getMessage());
			return;
		}

		IClass<Serializer> annotation = IClass.getClass(Serializer.class);
		java.util.Set<Class<?>> seen = new java.util.HashSet<>();
		for (ISerializer registered : this.serializers) {
			seen.add(registered.getClass());
		}

		int discovered = 0;
		for (String pkg : scanSurface) {
			List<IClass<?>> found = reflection.getClassesWithAnnotation(pkg, annotation);
			for (IClass<?> clazz : found) {
				ISerializer instance = instantiateSerializer(clazz);
				if (!seen.add(instance.getClass())) {
					continue;
				}
				this.serializers.add(instance);
				discovered++;
			}
		}
		if (discovered > 0) {
			log.atDebug().log("Auto-detected {} @Serializer class(es) across {} package(s)",
					discovered, scanSurface.size());
		}
	}

	/**
	 * Scans the configured packages for classes annotated with {@link Protocol}
	 * and registers their instances on the global protocol pool. Behaves like
	 * {@link #autoDetectSerializers()}: no-op without packages or without a
	 * reflection scanner; dedup by class against manually-registered protocols.
	 */
	private void autoDetectProtocols(Set<String> scanSurface) {
		if (scanSurface.isEmpty()) {
			return;
		}
		com.garganttua.core.reflection.IReflection reflection;
		try {
			reflection = IClass.getReflection();
		} catch (Exception e) {
			log.atWarn().log("No IReflection available for @Protocol auto-detection: {}", e.getMessage());
			return;
		}

		IClass<Protocol> annotation = IClass.getClass(Protocol.class);
		java.util.Set<Class<?>> seen = new java.util.HashSet<>();
		for (IProtocol<?, ?> registered : this.protocols) {
			seen.add(registered.getClass());
		}

		int discovered = 0;
		for (String pkg : scanSurface) {
			List<IClass<?>> found = reflection.getClassesWithAnnotation(pkg, annotation);
			for (IClass<?> clazz : found) {
				IProtocol<?, ?> instance = instantiateProtocol(clazz);
				if (!seen.add(instance.getClass())) {
					continue;
				}
				this.protocols.add(instance);
				discovered++;
			}
		}
		if (discovered > 0) {
			log.atDebug().log("Auto-detected {} @Protocol class(es) across {} package(s)",
					discovered, scanSurface.size());
		}
	}

	/**
	 * Scans the configured packages for classes annotated with {@link AuthorizationProtocol}
	 * and registers their instances on the global authorization-protocol pool.
	 * Mirrors {@link #autoDetectSerializers()} / {@link #autoDetectProtocols()}.
	 */
	private void autoDetectAuthorizationProtocols(Set<String> scanSurface) {
		if (scanSurface.isEmpty()) {
			return;
		}
		com.garganttua.core.reflection.IReflection reflection;
		try {
			reflection = IClass.getReflection();
		} catch (Exception e) {
			log.atWarn().log("No IReflection available for @AuthorizationProtocol auto-detection: {}", e.getMessage());
			return;
		}

		IClass<AuthorizationProtocol> annotation = IClass.getClass(AuthorizationProtocol.class);
		java.util.Set<Class<?>> seen = new java.util.HashSet<>();
		for (IAuthorizationProtocol registered : this.authorizationProtocols) {
			seen.add(registered.getClass());
		}

		int discovered = 0;
		for (String pkg : scanSurface) {
			List<IClass<?>> found = reflection.getClassesWithAnnotation(pkg, annotation);
			for (IClass<?> clazz : found) {
				IAuthorizationProtocol instance = instantiateAuthorizationProtocol(clazz);
				if (!seen.add(instance.getClass())) {
					continue;
				}
				this.authorizationProtocols.add(instance);
				discovered++;
			}
		}
		if (discovered > 0) {
			log.atDebug().log("Auto-detected {} @AuthorizationProtocol class(es) across {} package(s)",
					discovered, scanSurface.size());
		}
	}

	// package-private for unit testing
	static IAuthorizationProtocol instantiateAuthorizationProtocol(IClass<?> clazz) {
		Object instance;
		try {
			instance = clazz.getConstructor().newInstance();
		} catch (Exception e) {
			throw new ApiException(
					"Failed to instantiate @AuthorizationProtocol class '" + clazz.getName()
					+ "'. A public no-arg constructor is required.", e);
		}
		if (!(instance instanceof IAuthorizationProtocol protocol)) {
			throw new ApiException(
					"Class '" + clazz.getName() + "' is annotated with @AuthorizationProtocol "
					+ "but does not implement " + IAuthorizationProtocol.class.getName());
		}
		return protocol;
	}

	// package-private for unit testing
	static IProtocol<?, ?> instantiateProtocol(IClass<?> clazz) {
		Object instance;
		try {
			instance = clazz.getConstructor().newInstance();
		} catch (Exception e) {
			throw new ApiException(
					"Failed to instantiate @Protocol class '" + clazz.getName()
					+ "'. A public no-arg constructor is required.", e);
		}
		if (!(instance instanceof IProtocol<?, ?> protocol)) {
			throw new ApiException(
					"Class '" + clazz.getName() + "' is annotated with @Protocol "
					+ "but does not implement " + IProtocol.class.getName());
		}
		return protocol;
	}

	// package-private for unit testing
	static ISerializer instantiateSerializer(IClass<?> clazz) {
		Object instance;
		try {
			instance = clazz.getConstructor().newInstance();
		} catch (Exception e) {
			throw new ApiException(
					"Failed to instantiate @Serializer class '" + clazz.getName()
					+ "'. A public no-arg constructor is required.", e);
		}
		if (!(instance instanceof ISerializer serializer)) {
			throw new ApiException(
					"Class '" + clazz.getName() + "' is annotated with @Serializer "
					+ "but does not implement " + ISerializer.class.getName());
		}
		return serializer;
	}

	@Override
	public IApiBuilder provide(IObservableBuilder<?, ?> dependency) throws ApiException {
		if (dependency instanceof IInjectionContextBuilder builder) {
			this.injectionContextBuilder = builder;
			log.atDebug().log("IInjectionContextBuilder captured via provide()");
		} else if (dependency instanceof IExpressionContextBuilder builder) {
			this.expressionContextBuilder = builder;
			if (!this.expressionContextBuilder.isAutoDetected()) {
				this.expressionContextBuilder.autoDetect(true);
			}
			this.expressionContextBuilder.withPackage("com.garganttua.core.expression.functions");
			this.expressionContextBuilder.withPackage("com.garganttua.core.script.functions");
			// Required so script-side `observe("start"|"end", source)` markers
			// emitted by ScriptGenerator when workflowTiming is enabled resolve
			// against ObservabilityExpressions (garganttua-core 2.0.0-ALPHA02).
			// Without this, the calls become unresolved expressions and the
			// stage:/script: events never reach observers.
			this.expressionContextBuilder.withPackage("com.garganttua.core.observability");
			this.expressionContextBuilder.withPackage("com.garganttua.api.core.expression");
			log.atDebug().log("IExpressionContextBuilder captured via provide()");
		} else if (dependency instanceof com.garganttua.core.observability.dsl.IObservabilityBuilder builder) {
			this.observabilityBuilder = builder;
			log.atDebug().log("IObservabilityBuilder captured via provide()");
		}
		return super.provide(dependency);
	}

	IInjectionContextBuilder getInjectionContextBuilder() {
		return this.injectionContextBuilder;
	}

	IExpressionContextBuilder getExpressionContextBuilder() {
		return this.expressionContextBuilder;
	}

	boolean isMultiTenant() {
		return this.multiTenant;
	}

	@Override
	public IApiBuilder packages(String... packageNames) throws ApiException {
		Objects.requireNonNull(packageNames, "packageNames cannot be null");
		for (String pkg : packageNames) {
			this.withPackage(Objects.requireNonNull(pkg, "package name cannot be null"));
		}
		return this;
	}

}
