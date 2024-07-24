package com.garganttua.api.spec.caller;

public interface IGGAPICallerFactoriesRegistry {

	IGGAPICallerFactory getCallerFactory(String domainName);

}
