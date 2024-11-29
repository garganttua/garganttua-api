package com.garganttua.api.core.engine;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.engine.IGGAPIBuilder;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public class GGApiBuilder implements IGGAPIBuilder {

	public static IGGAPIBuilder builder() {
		return new GGApiBuilder();
	}
	private IGGBeanLoader loader;
	private List<String> packages;
	private IGGPropertyLoader propLoader;
	private IGGInjector injector;
	
	@Override
	public IGGAPIBuilder beanLoader(IGGBeanLoader loader) {
		this.loader = loader;
		return this;
	}

	@Override
	public IGGAPIEngine build() throws GGAPIException {
		if( this.loader == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "BeanLoader cannnot be null");
		}
		if( this.packages == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "Packages cannnot be null");
		}
		if( this.propLoader == null ) {
			throw new GGAPIEngineException(GGAPIExceptionCode.CORE_GENERIC_CODE, "PropLoader cannnot be null");
		}
		return new GGApiEngine(Optional.ofNullable(this.injector), this.packages, this.propLoader, this.loader);
	}

	@Override
	public IGGAPIBuilder packages(List<String> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public IGGAPIBuilder propertyLoader(IGGPropertyLoader loader) {
		propLoader = loader;
		return this;
	}

	@Override
	public IGGAPIBuilder injector(IGGInjector injector) {
		this.injector = injector;
		return this;
	}

}
