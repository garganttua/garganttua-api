package com.garganttua.api.core.engine;

import java.util.List;

import com.garganttua.api.spec.engine.IGGAPIBuilder;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPISecurity;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public class GGApiBuilder implements IGGAPIBuilder {

	
	public static IGGAPIBuilder builder() {
		return new GGApiBuilder();
	}

	private IGGAPISecurity provider;
	private IGGBeanLoader loader;
	private List<String> packages;
	private IGGInjector injector;
	private IGGPropertyLoader propLoader;

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
		return new GGApiEngine(this.provider, this.loader, this.packages, this.injector, this.propLoader);
	}

	@Override
	public IGGAPIBuilder setPackages(List<String> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public IGGAPIBuilder setInjector(IGGInjector injector) {
		this.injector = injector;
		return this;
	}

	@Override
	public IGGAPIBuilder setPropertyLoader(IGGPropertyLoader loader) {
		propLoader = loader;
		return this;
	}

}
