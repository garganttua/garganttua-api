package com.garganttua.api.spec.service;

import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garganttua.api.spec.context.Operation;
import com.garganttua.core.CoreException;

public interface IServiceInfos {
	
	String getDomainName();

	String getMethodName();
	
	Class<?> getInterface();

	Class<?>[] getParameters();
	
	@JsonIgnore
	Method getMethod();
	
	String getPath();

	String getDescription();

	Operation getOperation();
	
	Object invoke(Object[] parameters) throws CoreException;

}
