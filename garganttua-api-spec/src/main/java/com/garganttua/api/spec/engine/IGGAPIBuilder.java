package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public interface IGGAPIBuilder {

	IGGAPIBuilder injector(IGGInjector injector);
	
	IGGAPIBuilder beanLoader(IGGBeanLoader loadder);
	
	IGGAPIBuilder packages(List<String> packages);

	IGGAPIEngine build() throws GGAPIException;

	IGGAPIBuilder propertyLoader(IGGPropertyLoader loader);

}
