package com.garganttua.api.spec.service;

import java.lang.reflect.Method;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.garganttua.api.spec.operation.Operation;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IClass;

public interface IServiceInfos {

	String getDomainName();

	String getMethodName();

	IClass<?> getInterface();

	IClass<?>[] getParameters();
	
	@JsonIgnore
	Method getMethod();
	
	String getPath();

	String getDescription();

	Operation getOperation();
	
	Object invoke(Object[] parameters) throws ApiException;

}
