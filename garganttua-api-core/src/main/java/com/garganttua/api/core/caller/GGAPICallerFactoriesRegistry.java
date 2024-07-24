package com.garganttua.api.core.caller;

import java.util.Map;

import com.garganttua.api.spec.caller.IGGAPICallerFactoriesRegistry;
import com.garganttua.api.spec.caller.IGGAPICallerFactory;

public class GGAPICallerFactoriesRegistry implements IGGAPICallerFactoriesRegistry {

	private Map<String, IGGAPICallerFactory> callerFactories;

	public GGAPICallerFactoriesRegistry(Map<String, IGGAPICallerFactory> callerFactories) {
		this.callerFactories = callerFactories;
	}

	@Override
	public IGGAPICallerFactory getCallerFactory(String domainName) {
		return this.callerFactories.get(domainName);
	}

}
