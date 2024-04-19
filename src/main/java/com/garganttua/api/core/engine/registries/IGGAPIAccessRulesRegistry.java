package com.garganttua.api.core.engine.registries;

import java.util.List;

import com.garganttua.api.core.security.authorization.IGGAPIAccessRule;
import com.garganttua.api.core.service.GGAPIServiceMethod;

public interface IGGAPIAccessRulesRegistry {

	List<IGGAPIAccessRule> getAccessRules();

	IGGAPIAccessRule getAccessRule(GGAPIServiceMethod method, String endpoint);

}
