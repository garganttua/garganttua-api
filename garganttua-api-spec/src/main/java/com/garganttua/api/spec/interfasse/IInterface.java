package com.garganttua.api.spec.interfasse;

import java.lang.reflect.Method;

import com.garganttua.api.spec.context.BusinessOperation;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.core.CoreException;

public interface IInterface {

	void start() throws CoreException;

	void setDomain(IDomain domain);

/* 	void setService(Service service);
 */
	String getName();

	Method getMethod(BusinessOperation method) throws CoreException;

}
