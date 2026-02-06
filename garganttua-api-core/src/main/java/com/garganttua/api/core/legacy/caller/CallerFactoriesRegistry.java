package com.garganttua.api.core.legacy.caller;

import java.util.Map;

import com.garganttua.api.spec.caller.ICallerFactoriesRegistry;
import com.garganttua.api.spec.caller.ICallerFactory;

public class CallerFactoriesRegistry implements ICallerFactoriesRegistry {

	private Map<String, ICallerFactory> callerFactories;

	public CallerFactoriesRegistry(Map<String, ICallerFactory> callerFactories) {
		this.callerFactories = callerFactories;
	}

	@Override
	public ICallerFactory getCallerFactory(String domainName) {
		return this.callerFactories.get(domainName);
	}

}
