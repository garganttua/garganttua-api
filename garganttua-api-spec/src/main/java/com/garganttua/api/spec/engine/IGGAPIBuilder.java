package com.garganttua.api.spec.engine;

import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.reflection.beans.IGGBeanLoader;

public interface IGGAPIBuilder {

	IGGAPIBuilder setSecurity(IGGAPISecurity provider);

	IGGAPIBuilder setBeanLoader(IGGBeanLoader l);

	IGGAPIEngine build();

}
