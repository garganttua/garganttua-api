package com.garganttua.api.core.engine;

import java.util.List;

import com.garganttua.api.spec.engine.IGGAPIBuilder;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.IGGAPIAuthenticationManager;
import com.garganttua.api.spec.security.IGGAPIAuthorizationManager;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.properties.IGGPropertyLoader;

public class GGApiBuilder implements IGGAPIBuilder {

	public static IGGAPIBuilder builder() {
		return new GGApiBuilder();
	}
	private IGGBeanLoader loader;
	private List<String> packages;
	private IGGPropertyLoader propLoader;
	private String superTenantId = "0";
	private String superOwnerId = "0";
	private IGGAPIAuthorizationManager authorizationManager;
	private IGGAPIAuthenticationManager authenticationManager;

	@Override
	public IGGAPIBuilder setBeanLoader(IGGBeanLoader loader) {
		this.loader = loader;
		return this;
	}

	@Override
	public IGGAPIEngine build() {
		return new GGApiEngine(this.loader, this.packages, this.propLoader, this.superTenantId, this.superOwnerId, this.authenticationManager, this.authorizationManager);
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

	@Override
	public IGGAPIBuilder superTenantId(String superTenantId) {
		this.superTenantId = superTenantId;
		return this;
	}

	@Override
	public IGGAPIBuilder superOwnerId(String superOwnerId) {
		this.superOwnerId = superOwnerId;
		return this;
	}

	@Override
	public IGGAPIBuilder setAuthorizationManager(IGGAPIAuthorizationManager manager) {
		this.authorizationManager = manager;
		return this;
	}

	@Override
	public IGGAPIBuilder setAuthenticationManager(IGGAPIAuthenticationManager manager) {
		this.authenticationManager = manager;
		return this;
	}

}
