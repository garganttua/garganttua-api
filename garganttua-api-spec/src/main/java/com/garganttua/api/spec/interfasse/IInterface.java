package com.garganttua.api.spec.interfasse;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngineObject;
import com.garganttua.api.spec.service.IService;

public interface IInterface extends IEngineObject {

	void start() throws CoreException;

	void setDomain(IDomain domain);

	void setService(IService service);

	String getName();

	Method getMethod(InterfaceMethod method) throws CoreException;

}
