package com.garganttua.api.spec.entity;

import java.util.Map;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;

@FunctionalInterface
public interface IEntitySaveMethod  {

	Object save(ICaller caller, Map<String, String> parameters, Object entity)
			throws CoreException;
	
}
