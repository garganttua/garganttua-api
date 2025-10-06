package com.garganttua.api.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.garganttua.api.core.builder.binder.ApplicationContextStartupBinderBuilder;
import com.garganttua.api.core.context.application.ApplicationContext;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.ContextBuildingStage;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.engine.IApplicationContextStartupBinderBuilder;
import com.garganttua.api.spec.engine.IContextSecurityBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IDomainContext;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public class ApplicationContextBuilder implements IApplicationContextBuilder {

	public static IApplicationContextBuilder builder() {
		return new ApplicationContextBuilder();
	}

	private static IGGBeanLoader loader;

	private List<String> packages;
	private IGGPropertyLoader propLoader;
	private IGGInjector injector;
	private boolean autoDetect = true;
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
	public ApplicationContextBuilder autoDetect(boolean b) throws BuilderException {
		if (this.packages == null && b) {
			throw new BuilderException("Packages must be set before setting autoDetect");
		}
		this.autoDetect = b;
		return this;
	}

	@Override
	public ApplicationContextBuilder superTenantId(String uuid) {
		this.superTenantId = Objects.requireNonNull(uuid, "Uuid cannot be null");

		return this;
	}

	@Override
	public IApplicationContextStartupBinderBuilder startup(ContextBuildingStage stage, IObjectSupplier<?> supplier)
			throws BuilderException {
		IApplicationContextStartupBinderBuilder binder = new ApplicationContextStartupBinderBuilder(this,
				Objects.requireNonNull(supplier, "Supplier cannot be null"));
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public IApplicationContextStartupBinderBuilder startup(ContextBuildingStage stage, Object object)
			throws BuilderException {
		IApplicationContextStartupBinderBuilder binder = new ApplicationContextStartupBinderBuilder(this,
				Objects.requireNonNull(object, "Object cannot be null"));
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public IDomainBuilder domain(String domainName) throws BuilderException {
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
	public ApplicationContextBuilder superTenantAutoCreate(boolean b) throws BuilderException {
		if (this.superTenantId == null) {
			throw new BuilderException("Super tenant ID must be set before setting auto create");
		}
		this.autoCreateSuperTenant = b;
		return this;
	}

	@Override
	public IApplicationContext build() throws CoreException {

		List<IDomainContext> builtDomains = new ArrayList<>();
		for (IDomainBuilder builder : this.domains.values()) {
			builtDomains.add(builder.build());
		}

		return new ApplicationContext(
				this.loader,
				this.packages,
				this.propLoader,
				this.injector,
				this.superTenantId,
				this.startupBinderBuilders,
				this.autoCreateSuperTenant,
				this.security.build(),
				builtDomains);
	}

	@Override
	public IDomainBuilder domain(Class<?> entityClass) throws CoreException {
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

}
