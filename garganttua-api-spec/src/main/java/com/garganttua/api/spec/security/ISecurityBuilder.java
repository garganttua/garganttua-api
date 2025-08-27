package com.garganttua.api.spec.security;

import java.util.List;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;

public interface ISecurityBuilder {

	ISecurityBuilder scanPackages(List<String> packages);
	
	ISecurityBuilder injector(IGGInjector injector); 

	ISecurityEngine build() throws CoreException;

	ISecurityBuilder loader(IGGBeanLoader loader);

	ISecurityBuilder engine(IEngine engine);
}
