package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.security.authorization.IGGAPIAccessRule;

import jakarta.servlet.http.HttpServletRequest;

public interface IGGAPIAccessRulesRegistry {

	List<IGGAPIAccessRule> getAccessRules();

	IGGAPIAccessRule getAccessRule(HttpServletRequest request);

}
