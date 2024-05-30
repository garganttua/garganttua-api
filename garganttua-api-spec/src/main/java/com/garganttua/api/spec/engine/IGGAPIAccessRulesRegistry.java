package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceMethod;

public interface IGGAPIAccessRulesRegistry {

	List<IGGAPIAccessRule> getAccessRules();

	IGGAPIAccessRule getAccessRule(GGAPIServiceMethod method, String endpoint);

}
