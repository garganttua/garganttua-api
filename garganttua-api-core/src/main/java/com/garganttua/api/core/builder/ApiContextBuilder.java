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

import com.garganttua.api.core.builder.binder.ApiContextStartupBinderBuilder;
import com.garganttua.api.core.context.application.ApiContext;
import com.garganttua.api.core.expression.ApiExpressions;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.context.ContextBuildingStage;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IApiContextStartupBinderBuilder;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.security.IApiContextSecurityBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.core.bootstrap.annotations.Bootstrap;
import com.garganttua.core.dsl.IObservableBuilder;
import com.garganttua.core.dsl.annotations.Scan;
import com.garganttua.core.dsl.dependency.AbstractAutomaticDependentBuilder;
import com.garganttua.core.dsl.dependency.DependencyPhase;
import com.garganttua.core.dsl.dependency.DependencySpec;
import com.garganttua.core.expression.dsl.IExpressionContextBuilder;
import com.garganttua.core.expression.functions.Expressions;
import com.garganttua.core.injection.BeanReference;
import com.garganttua.core.injection.BeanStrategy;
import com.garganttua.core.injection.IInjectionContext;
import com.garganttua.core.injection.Predefined;
import com.garganttua.core.injection.context.dsl.IInjectionContextBuilder;
import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.supply.dsl.NullSupplierBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Bootstrap
@Scan(scan = "com.garganttua.api.core")
public class ApiContextBuilder extends AbstractAutomaticDependentBuilder<IApiContextBuilder, IApiContext>
		implements IApiContextBuilder {

	private ApiContextBuilder(Set<DependencySpec> dependencies) {
		super(dependencies);
	}

	private final Set<String> packages = ConcurrentHashMap.newKeySet();

	private volatile String superTenantId;

	private volatile boolean superTenantAutoCreate = false;

	private final Map<Class<?>, DomainBuilder<?>> domainBuilders = new ConcurrentHashMap<>();
	private volatile ContextSecurityBuilder securityBuilder;
	private final List<ApiContextStartupBinderBuilder> startupBinderBuilders = new CopyOnWriteArrayList<>();

	private volatile IInjectionContextBuilder injectionContextBuilder;
	private volatile IExpressionContextBuilder expressionContextBuilder;
	private volatile IInjectionContext injectionContext;

	public static IApiContextBuilder builder() {
		return new ApiContextBuilder(
				Set.of(
						DependencySpec.require(IInjectionContextBuilder.class, DependencyPhase.BUILD),
						DependencySpec.require(IExpressionContextBuilder.class, DependencyPhase.BUILD)));
	}

	@Override
	public IApiContextBuilder superTenantId(String superTenantId) {
		this.superTenantId = Objects.requireNonNull(superTenantId, "Super tenant ID cannot be null");
		return this;
	}

	@Override
	public IApiContextStartupBinderBuilder startup(ContextBuildingStage stage,
			ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException {
		ApiContextStartupBinderBuilder binder = new ApiContextStartupBinderBuilder(this, supplier);
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public IApiContextStartupBinderBuilder startup(ContextBuildingStage stage, Object object) throws ApiException {
		ApiContextStartupBinderBuilder binder = new ApiContextStartupBinderBuilder(this, object);
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public <E> IDomainBuilder<E> domain(Class<E> entityClass) throws ApiException {
		Objects.requireNonNull(entityClass, "Entity class cannot be null");

		return (IDomainBuilder<E>) this.domainBuilders.computeIfAbsent(entityClass, clazz -> {
			return new DomainBuilder<>(this, clazz);
		});
	}

	@Override
	public IApiContextBuilder superTenantAutoCreate(boolean b) throws ApiException {
		this.superTenantAutoCreate = b;
		return this;
	}

	@Override
	public synchronized IApiContextSecurityBuilder security() {
		if (this.securityBuilder == null) {
			this.securityBuilder = new ContextSecurityBuilder(this.packages, this);
		}
		return this.securityBuilder;
	}

	@Override
	public String[] getPackages() {
		return this.packages.toArray(new String[0]);
	}

	@Override
	public IApiContextBuilder withPackage(String packageName) {
		log.atDebug().log("Adding package: {}", packageName);
		this.packages.add(Objects.requireNonNull(packageName, "Package name cannot be null"));
		return this;
	}

	@Override
	public IApiContextBuilder withPackages(String[] packageNames) {
		log.atDebug().log("Adding {} packages", packageNames.length);
		Objects.requireNonNull(packageNames, "Package names cannot be null");
		for (String pkg : packageNames) {
			this.withPackage(pkg);
		}
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

	private void registerBuiltObjectInContext(IInjectionContext context, IApiContext apiContext) {
		log.atDebug().log("Registering IApiContext as bean in InjectionContext");
		String providerName = Predefined.BeanProviders.garganttua.toString();

		BeanReference<IApiContext> beanRef = new BeanReference<>(
				IApiContext.class,
				Optional.of(BeanStrategy.singleton),
				Optional.of("ApiContext"),
				Set.of());
		context.addBean(providerName, beanRef, apiContext);
		log.atDebug().log("IApiContext successfully registered as bean with 'ApiContext' name");

		// Register each domain context
		for (Map.Entry<String, IDomainContext<?>> entry : ((ApiContext) apiContext).getDomainContexts().entrySet()) {
			String domainName = entry.getKey();
			IDomainContext<?> domainContext = entry.getValue();

			BeanReference<IDomainContext<?>> domainBeanRef = new BeanReference<>(
					(Class<IDomainContext<?>>) (Class<?>) IDomainContext.class,
					Optional.of(BeanStrategy.singleton),
					Optional.of("domain." + domainName),
					Set.of());
			context.addBean(providerName, domainBeanRef, domainContext);
			log.atDebug().log("IDomainContext successfully registered as bean with 'domain.{}' name", domainName);
		}
	}

	private void registerMapperBean() {
		log.atDebug().log("Registering IMapper as bean in InjectionContext");
		String providerName = Predefined.BeanProviders.garganttua.toString();

		BeanReference<IMapper> beanRef = new BeanReference<>(
				IMapper.class,
				Optional.of(BeanStrategy.singleton),
				Optional.of("mapper"),
				Set.of());
		this.injectionContext.addBean(providerName, beanRef, DefaultMapper.mapper());
		log.atDebug().log("IMapper successfully registered as bean with 'mapper' name");
	}

	@Override
	protected synchronized IApiContext doBuild() throws ApiException {
		log.atTrace().log("Entering doBuild() method");

		try {
			// Ensure we have an injection context
			if (this.injectionContext == null) {
				throw new ApiException("InjectionContext is required but not provided");
			}

			// Register default mapper as bean
			registerMapperBean();

			// Build all domain contexts
			Map<String, IDomainContext<?>> domainContexts = new HashMap<>();
			for (DomainBuilder<?> domainBuilder : this.domainBuilders.values()) {
				domainBuilder.setDependencyBuilders(this.injectionContextBuilder, this.expressionContextBuilder);
				IDomainContext<?> domainContext = domainBuilder.build();
				domainContexts.put(domainContext.getDomain(), domainContext);
				log.atDebug().log("Built domain context: {}", domainContext.getDomain());
			}

			// Build security context if configured
			if (this.securityBuilder != null) {
				this.securityBuilder.build();
				log.atDebug().log("Built security context");
			}

			// Build startup binders
			List<IMethodBinder<Void>> startupBinders = new ArrayList<>();
			for (ApiContextStartupBinderBuilder binder : this.startupBinderBuilders) {
				startupBinders.add(binder.build());
			}
			log.atDebug().log("Built {} startup binders", startupBinders.size());

			// Create and return API context
			IApiContext apiContext = new ApiContext(this.injectionContext, domainContexts,
					this.superTenantId, this.superTenantAutoCreate, startupBinders);

			log.atDebug().log("Built ApiContext with {} domains", domainContexts.size());
			log.atTrace().log("Exiting doBuild() method");

			return apiContext;

		} catch (ApiException e) {
			throw new ApiException("Failed to build API context: " + e.getMessage(), e);
		}
	}

	@Override
	protected void doAutoDetection() throws ApiException {
		log.atTrace().log("Entering doAutoDetection() method");
		// Base auto-detection without dependencies
		// Could scan for @Entity annotated classes in packages
		log.atTrace().log("Exiting doAutoDetection() method");
	}

	@Override
	public IApiContextBuilder provide(IObservableBuilder<?, ?> dependency) throws ApiException {
		if (dependency instanceof IInjectionContextBuilder builder) {
			this.injectionContextBuilder = builder;
			log.atDebug().log("IInjectionContextBuilder captured via provide()");
		} else if (dependency instanceof IExpressionContextBuilder builder) {
			this.expressionContextBuilder = builder;
			this.expressionContextBuilder
					.expression(NullSupplierBuilder.of(Expressions.class), String.class)
					.encapsulatedMethod("string", String.class, Object.class)
					.withName("string");
			this.expressionContextBuilder
					.expression(NullSupplierBuilder.of(ApiExpressions.class), IFilter.class)
					.encapsulatedMethod("buildFilter", IFilter.class, Object.class)
					.withName("buildFilter");
			log.atDebug().log("IExpressionContextBuilder captured via provide()");
		}
		return super.provide(dependency);
	}

	IInjectionContextBuilder getInjectionContextBuilder() {
		return this.injectionContextBuilder;
	}

	IExpressionContextBuilder getExpressionContextBuilder() {
		return this.expressionContextBuilder;
	}

}
