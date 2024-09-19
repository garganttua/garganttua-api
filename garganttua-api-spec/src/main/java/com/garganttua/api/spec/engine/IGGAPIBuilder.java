package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public interface IGGAPIBuilder {

	IGGAPIBuilder setBeanLoader(IGGBeanLoader loadder);
	
	IGGAPIBuilder setPackages(List<String> packages);

	IGGAPIEngine build();

	IGGAPIBuilder setPropertyLoader(IGGPropertyLoader loader);

}
