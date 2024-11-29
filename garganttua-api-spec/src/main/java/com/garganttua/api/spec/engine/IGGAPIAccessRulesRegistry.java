package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.security.IGGAPIAccessRule;

public interface IGGAPIAccessRulesRegistry {

	List<IGGAPIAccessRule> getAccessRules();

	IGGAPIAccessRule getAccessRule(GGAPIEntityOperation operation, String endpoint);
	
	List<String> getAuthorities();
	
	void addAccessRule(IGGAPIAccessRule accessRule);

}
