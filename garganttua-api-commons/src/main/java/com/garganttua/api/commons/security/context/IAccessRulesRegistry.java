package com.garganttua.api.commons.security.context;

import java.util.List;

import com.garganttua.api.commons.security.IAccessRule;
import com.garganttua.api.commons.operation.OperationDefinition;

public interface IAccessRulesRegistry {

	List<IAccessRule> getAccessRules();

	IAccessRule getAccessRule(OperationDefinition operation, String endpoint);
	
	List<String> getAuthorities();
	
	void addAccessRule(IAccessRule accessRule);

	String getAuthority(OperationDefinition operation);

}
