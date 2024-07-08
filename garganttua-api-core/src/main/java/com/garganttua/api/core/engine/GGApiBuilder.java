package com.garganttua.api.core.engine;

import java.util.List;

import com.garganttua.api.core.security.engine.GGAPISecurityEngine;
import com.garganttua.api.spec.engine.IGGAPIBuilder;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPISecurityEngine;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public class GGApiBuilder implements IGGAPIBuilder {

	
	public static IGGAPIBuilder builder() {
		return new GGApiBuilder();
	}

	private IGGAPISecurityEngine security;
	private IGGBeanLoader loader;
	private List<String> packages;
	private IGGPropertyLoader propLoader;

	@Override
	public IGGAPIBuilder setSecurity(IGGAPISecurityEngine security) {
		this.security = security;
		return this;
	}

	@Override
	public IGGAPIBuilder setBeanLoader(IGGBeanLoader loader) {
		this.loader = loader;
		return this;
	}

	@Override
	public IGGAPIEngine build() {
		
		if( security == null ) {
			this.security = new GGAPISecurityEngine();
		}
		
		return new GGApiEngine(this.security, this.loader, this.packages, this.propLoader);
	}

	@Override
	public IGGAPIBuilder setPackages(List<String> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public IGGAPIBuilder setPropertyLoader(IGGPropertyLoader loader) {
		propLoader = loader;
		return this;
	}

}
