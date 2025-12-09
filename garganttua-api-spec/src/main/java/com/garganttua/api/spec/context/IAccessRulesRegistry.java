package com.garganttua.api.spec.context;

import java.util.List;

import com.garganttua.api.spec.security.IAccessRule;

public interface IAccessRulesRegistry {

	List<IAccessRule> getAccessRules();

	IAccessRule getAccessRule(Operation operation, String endpoint);
	
	List<String> getAuthorities();
	
	void addAccessRule(IAccessRule accessRule);

	String getAuthority(Operation operation);

}
