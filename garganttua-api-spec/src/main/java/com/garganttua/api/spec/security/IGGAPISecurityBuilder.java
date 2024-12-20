package com.garganttua.api.spec.security;

import java.util.List;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;

public interface IGGAPISecurityBuilder {

	IGGAPISecurityBuilder scanPackages(List<String> packages);
	
	IGGAPISecurityBuilder injector(IGGInjector injector); 

	IGGAPISecurityEngine build() throws GGAPIException;

	IGGAPISecurityBuilder loader(IGGBeanLoader loader);

	IGGAPISecurityBuilder engine(IGGAPIEngine engine);
}
