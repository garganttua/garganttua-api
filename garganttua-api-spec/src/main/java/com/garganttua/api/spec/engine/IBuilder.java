package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.CoreException;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public interface IBuilder {

	IBuilder injector(IGGInjector injector);
	
	IBuilder beanLoader(IGGBeanLoader loadder);
	
	IBuilder packages(List<String> packages);

	IEngine build() throws CoreException;

	IBuilder propertyLoader(IGGPropertyLoader loader);

    IBuilder autoDetect(boolean b);

    IBuilder superTenantId(String string);

	IContextStartupBinderBuilder startup(IObjectSupplier<?> supplier);
    
	IDomainBuilder domain(String string);

	IBuilder supertenantAutoCreate(boolean b);

}
