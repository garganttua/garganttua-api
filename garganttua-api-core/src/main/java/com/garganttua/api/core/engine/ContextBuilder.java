package com.garganttua.api.core.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.ContextBuildingStage;
import com.garganttua.api.spec.engine.IContext;
import com.garganttua.api.spec.engine.IContextBuilder;
import com.garganttua.api.spec.engine.IContextStartupBinderBuilder;
import com.garganttua.api.spec.engine.IDomainBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public class ContextBuilder implements IContextBuilder {

	public static IContextBuilder builder() {
		return new ContextBuilder();
	}

	public static IObjectSupplier<?> bean(Class<?> beanClass) throws BuilderException {
		Objects.requireNonNull(beanClass, "Bean class cannot be null");
        if( ContextBuilder.loader == null ) {
			throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "BeanLoader cannnot be null");
		}
		return new TypedBeanSupplier<>(ContextBuilder.loader, beanClass);
    }

    /* public static IObjectSupplier<?> bean(String beanName) throws BuilderException {
        Objects.requireNonNull(beanName, "Bean name class cannot be null");
		if( ContextBuilder.loader == null ) {
			throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "BeanLoader cannnot be null");
		}
		return new NamedBeanSupplier(loader, beanName);
    } */

    /* public static IObjectSupplier<?> bean(String supplier, String beanName) throws BuilderException {
		Objects.requireNonNull(beanName, "Bean name class cannot be null");
         if( ContextBuilder.loader == null ) {
			throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "BeanLoader cannnot be null");
		}
				return new NamedBeanSupplier(loader, beanName, Optional.ofNullable(supplier));
    } */

    public static IObjectSupplier<?> bean(String supplier, Class<?> beanClass) throws BuilderException {
        Objects.requireNonNull(beanClass, "Bean class cannot be null");
		if( ContextBuilder.loader == null ) {
			throw new BuilderException(CoreExceptionCode.BUILDER_CODE, "BeanLoader cannnot be null");
		}
		return new TypedBeanSupplier<>(ContextBuilder.loader, beanClass, Optional.ofNullable(supplier));
    }

	private static IGGBeanLoader loader;
	private List<String> packages;
	IGGPropertyLoader propLoader;
	private IGGInjector injector;
	private boolean autoDetect = true;
	private String superTenantId = "0";
	private List<IContextStartupBinderBuilder> startupBinderBuilders = new ArrayList<IContextStartupBinderBuilder>();
	private boolean autoCreateSuperTenant;
	
	@Override
	public IContextBuilder beanLoader(IGGBeanLoader loader) {
		ContextBuilder.loader = loader;
		return this;
	}

	/* @Override
	public IEngine build() throws CoreException {
		if( this.loader == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "BeanLoader cannnot be null");
		}
		if( this.packages == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "Packages cannnot be null");
		}
		if( this.propLoader == null ) {
			throw new EngineException(CoreExceptionCode.CORE_GENERIC_CODE, "PropLoader cannnot be null");
		}
		return new Engine(Optional.ofNullable(this.injector), this.packages, this.propLoader, this.loader);
	}
 */
	@Override
	public IContextBuilder packages(List<String> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public IContextBuilder propertyLoader(IGGPropertyLoader loader) {
		propLoader = loader;
		return this;
	}

	@Override
	public IContextBuilder injector(IGGInjector injector) {
		this.injector = injector;
		return this;
	}

	@Override
	public IContextBuilder autoDetect(boolean b) {
		this.autoDetect = b;
		return this;
	}

	@Override
	public IContextBuilder superTenantId(String uuid) {
		this.superTenantId = uuid;
		return this;
	}

	@Override
	public IContextStartupBinderBuilder startup(ContextBuildingStage stage, IObjectSupplier<?> supplier) {
		ContextStartupBinderBuilder binder = new ContextStartupBinderBuilder(this, supplier);
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public IContextStartupBinderBuilder startup(ContextBuildingStage stage, Object object) {
		ContextStartupBinderBuilder binder = new ContextStartupBinderBuilder(this, object);
		this.startupBinderBuilders.add(binder);
		return binder;
	}

	@Override
	public IDomainBuilder domain(String domainName) throws BuilderException {
		return new DomainBuilder(this, domainName);
	}

	@Override
	public IContextBuilder superTenantAutoCreate(boolean b) {
		this.autoCreateSuperTenant = b;
		return this;
	}

	@Override
	public IContextBuilder up() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'up'");
	}

	@Override
	public IContext build() throws CoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'build'");
	}

}
