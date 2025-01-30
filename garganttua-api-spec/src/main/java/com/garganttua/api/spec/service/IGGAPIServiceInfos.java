package com.garganttua.api.spec.service;

import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIServiceInfos {
	
	String getDomainName();

	String getMethodName();
	
	Class<?> getInterface();

	Class<?>[] getParameters();
	
	@JsonIgnore
	Method getMethod();
	
	String getPath();

	String getDescription();

	GGAPIEntityOperation getOperation();
	
	Object invoke(Object[] parameters) throws GGAPIException;

}
