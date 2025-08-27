package com.garganttua.api.spec.caller;

public interface ICallerFactoriesRegistry {

	ICallerFactory getCallerFactory(String domainName);

}
