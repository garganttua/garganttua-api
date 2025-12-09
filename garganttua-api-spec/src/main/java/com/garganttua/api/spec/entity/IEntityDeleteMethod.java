package com.garganttua.api.spec.entity;

import java.util.Map;

import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.core.CoreException;

@FunctionalInterface
public interface IEntityDeleteMethod {

	void delete(ICaller caller, Map<String, String> parameters, Object entity)
			throws CoreException;
	
}
