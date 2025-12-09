package com.garganttua.api.core.builder;

import com.garganttua.api.spec.context.ContextBuildingStage;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.api.spec.context.dsl.IApiContextStartupBinderBuilder;
import com.garganttua.api.spec.context.dsl.IDomainBuilder;
import com.garganttua.api.spec.context.dsl.security.IApiContextSecurityBuilder;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.injection.context.dsl.DiContextBuilder;
import com.garganttua.core.supply.IObjectSupplier;
import com.garganttua.core.supply.dsl.IObjectSupplierBuilder;

public class ApiContextBuilder extends DiContextBuilder implements IApiContextBuilder {

	public static IApiContextBuilder builder() {
		return new ApiContextBuilder();
	}

	@Override
	public IApiContextBuilder superTenantId(String string) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'superTenantId'");
	}

	@Override
	public IApiContextStartupBinderBuilder startup(ContextBuildingStage stage,
			IObjectSupplierBuilder<?, ? extends IObjectSupplier<?>> supplier) throws DslException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'startup'");
	}

	@Override
	public IApiContextStartupBinderBuilder startup(ContextBuildingStage stage, Object object) throws DslException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'startup'");
	}

	@Override
	public IDomainBuilder domain(String domainName) throws DslException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'domain'");
	}

	@Override
	public IDomainBuilder domain(Class<?> entityClass) throws DslException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'domain'");
	}

	@Override
	public IApiContextBuilder superTenantAutoCreate(boolean b) throws DslException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'superTenantAutoCreate'");
	}

	@Override
	public IApiContextSecurityBuilder security() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'security'");
	}

	/* private static IGGBeanLoader loader;

	private List<String> packages;
	private IGGPropertyLoader propLoader;
	private IGGInjector injector;
	private String superTenantId = "0";
	private List<IApplicationContextStartupBinderBuilder> startupBinderBuilders = new ArrayList<IApplicationContextStartupBinderBuilder>();
	private boolean autoCreateSuperTenant;
	private IContextSecurityBuilder security;
	private Map<String, IDomainBuilder> domains = new HashMap<String, IDomainBuilder>();

	@Override
	public ApplicationContextBuilder beanLoader(IGGBeanLoader loader) {
		ApplicationContextBuilder.loader = Objects.requireNonNull(loader, "Loader cannot be null");
		return this;
	}

	@Override
	public ApplicationContextBuilder packages(List<String> packages) {
		this.packages = Objects.requireNonNull(packages, "Packages cannot be null");

		return this;
	}

	@Override
	public ApplicationContextBuilder propertyLoader(IGGPropertyLoader loader) {
		propLoader = Objects.requireNonNull(loader, "Loader cannot be null");

		return this;
	}

	@Override
	public ApplicationContextBuilder injector(IGGInjector injector) {
		this.injector = Objects.requireNonNull(injector, "Injector cannot be null");

		return this;
	}

	@Override
	public ApplicationContextBuilder superTenantId(String uuid) {
		this.superTenantId = Objects.requireNonNull(uuid, "Uuid cannot be null");

		return this;
	}

	@Override
	public IApplicationContextStartupBinderBuilder startup(ContextBuildingStage stage, IObjectSupplier<?> supplier)
			throws DslException {
		IApplicationContextStartupBinderBuilder binder = new ApplicationContextStartupBinderBuilder(this,
				Objects.requireNonNull(supplier, "Supplier cannot be null"));
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public IApplicationContextStartupBinderBuilder startup(ContextBuildingStage stage, Object object)
			throws DslException {
		IApplicationContextStartupBinderBuilder binder = new ApplicationContextStartupBinderBuilder(this,
				Objects.requireNonNull(object, "Object cannot be null"));
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public IDomainBuilder domain(String domainName) throws DslException {
		Objects.requireNonNull(domainName, "Domain name cannot be null");
		Objects.requireNonNull(security, "Security is not configured");
		IDomainBuilder domain;
		if (!this.domains.containsKey(domainName)) {
			domain = new DomainBuilder(this, domainName);
			this.domains.put(domainName, domain);
		} else {
			domain = this.domains.get(domainName);
		}
		return domain;
	}

	@Override
	public ApplicationContextBuilder superTenantAutoCreate(boolean b) throws DslException {
		if (this.superTenantId == null) {
			throw new DslException("Super tenant ID must be set before setting auto create");
		}
		this.autoCreateSuperTenant = b;
		return this;
	}

	@Override
	public IApiContext build() throws DslException {

		List<IDomainContext> builtDomains = new ArrayList<>();
		for (IDomainBuilder builder : this.domains.values()) {
			builtDomains.add(builder.build());
		}

		return null;/* new ApplicationContext(
				this.loader,
				this.packages,
				this.propLoader,
				this.injector,
				this.superTenantId,
				this.startupBinderBuilders,
				this.autoCreateSuperTenant,
				this.security.build(),
				builtDomains); */
/* 	}

	@Override
	public IDomainBuilder domain(Class<?> entityClass) throws DslException {
		Objects.requireNonNull(entityClass, "Entity class cannot be null");
		Objects.requireNonNull(security, "Security is not configured");
		IDomainBuilder domain;
		if (!this.domains.containsKey(entityClass.getSimpleName())) {
			domain = new DomainBuilder(this, entityClass);
			this.domains.put(entityClass.getSimpleName(), domain);
		} else {
			domain = this.domains.get(entityClass.getSimpleName());
		}
		return domain;
	}

	@Override
	public IContextSecurityBuilder security() {
		if (this.security != null) {
			return this.security;
		}
		return this.security = new ContextSecurityBuilder(this.packages, this);
	}

	@Override
	protected IApplicationContext doBuild() throws DslException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'doBuild'");
	} */


}
