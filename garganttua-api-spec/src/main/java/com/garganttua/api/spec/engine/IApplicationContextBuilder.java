package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public interface IApplicationContextBuilder extends IAutomaticBuilder<IApplicationContextBuilder, IApplicationContext> {

	IApplicationContextBuilder injector(IGGInjector injector);

	IApplicationContextBuilder beanLoader(IGGBeanLoader loadder);

	IApplicationContextBuilder packages(List<String> packages);

	IApplicationContextBuilder propertyLoader(IGGPropertyLoader loader);

	IApplicationContextBuilder superTenantId(String string);

	IMethodBinderBuilder<IApplicationContextStartupBinderBuilder, Object, IApplicationContextBuilder> startup(ContextBuildingStage stage, IObjectSupplier<?> supplier) throws CoreException;

	IMethodBinderBuilder<IApplicationContextStartupBinderBuilder, Object, IApplicationContextBuilder> startup(ContextBuildingStage stage, Object object) throws CoreException;

	IDomainBuilder domain(String domainName) throws CoreException;

	IDomainBuilder domain(Class<?> entityClass) throws CoreException;

	IApplicationContextBuilder superTenantAutoCreate(boolean b) throws CoreException;

    IContextSecurityBuilder security();

}
