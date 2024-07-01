package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public interface IGGAPIBuilder {

	IGGAPIBuilder setSecurity(IGGAPISecurity provider);

	IGGAPIBuilder setBeanLoader(IGGBeanLoader loadder);
	
	IGGAPIBuilder setPackages(List<String> packages);

	IGGAPIEngine build();

	IGGAPIBuilder setInjector(IGGInjector injector);

	IGGAPIBuilder setPropertyLoader(IGGPropertyLoader loader);

}
