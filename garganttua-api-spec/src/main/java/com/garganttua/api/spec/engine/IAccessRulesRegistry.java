package com.garganttua.api.spec.engine;

import java.util.List;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.security.IAccessRule;

public interface IAccessRulesRegistry {

	List<IAccessRule> getAccessRules();

	IAccessRule getAccessRule(EntityOperation operation, String endpoint);
	
	List<String> getAuthorities();
	
	void addAccessRule(IAccessRule accessRule);

	String getAuthority(EntityOperation operation);

}
