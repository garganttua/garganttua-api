package com.garganttua.api.spec.service;

import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.CoreException;

public interface IServiceInfos {
	
	String getDomainName();

	String getMethodName();
	
	Class<?> getInterface();

	Class<?>[] getParameters();
	
	@JsonIgnore
	Method getMethod();
	
	String getPath();

	String getDescription();

	EntityOperation getOperation();
	
	Object invoke(Object[] parameters) throws CoreException;

}
