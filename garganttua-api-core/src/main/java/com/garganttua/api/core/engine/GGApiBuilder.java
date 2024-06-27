package com.garganttua.api.core.engine;

import com.garganttua.api.spec.engine.IGGAPIBuilder;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.reflection.beans.IGGBeanLoader;

public class GGApiBuilder implements IGGAPIBuilder {

	
	public static IGGAPIBuilder builder() {
		return new GGApiBuilder();
	}

	private IGGAPISecurity provider;
	private IGGBeanLoader loader;

	@Override
	public IGGAPIBuilder setSecurity(IGGAPISecurity provider) {
		this.provider = provider;
		return this;
	}

	@Override
	public IGGAPIBuilder setBeanLoader(IGGBeanLoader loader) {
		this.loader = loader;
		return this;
	}

	@Override
	public IGGAPIEngine build() {
		return new GGApiEngine(this.provider, this.loader);
	}

}
