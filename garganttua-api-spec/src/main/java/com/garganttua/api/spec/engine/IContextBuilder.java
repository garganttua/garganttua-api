package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public interface IContextBuilder extends IBuilder<IContext, IContextBuilder> {

	IContextBuilder injector(IGGInjector injector);

	IContextBuilder beanLoader(IGGBeanLoader loadder);

	IContextBuilder packages(List<String> packages);

	IContextBuilder propertyLoader(IGGPropertyLoader loader);

	IContextBuilder autoDetect(boolean b);

	IContextBuilder superTenantId(String string);

	IMethodBinderBuilder<IContextStartupBinderBuilder, Object, IContextBuilder> startup(ContextBuildingStage stage, IObjectSupplier<?> supplier);

	IMethodBinderBuilder<IContextStartupBinderBuilder, Object, IContextBuilder> startup(ContextBuildingStage stage, Object object);

	IDomainBuilder domain(String string) throws CoreException;

	IContextBuilder superTenantAutoCreate(boolean b);

}
