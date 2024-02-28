package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.service.GGAPIServiceMethod;

public interface IGGAPIAccessRulesRegistry {

	List<IGGAPIAccessRule> getAccessRules();

	IGGAPIAccessRule getAccessRule(GGAPIServiceMethod method, String endpoint);

}
