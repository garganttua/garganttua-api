package com.garganttua.api.spec.service;

import java.lang.reflect.Method;

import com.garganttua.api.spec.GGAPIEntityOperation;

public interface IGGAPIServiceInfos {
	
	String getDomainName();

	String getMethodName();
	
	Class<?> getInterface();

	Class<?>[] getParameters();
	
	Method getMethod();
	
	String getPath();

	String getDescription();

	GGAPIEntityOperation getOperation();

}
