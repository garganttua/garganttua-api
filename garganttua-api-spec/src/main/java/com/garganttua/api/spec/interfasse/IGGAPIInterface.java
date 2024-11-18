package com.garganttua.api.spec.interfasse;

import java.lang.reflect.Method;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.service.IGGAPIService;

public interface IGGAPIInterface extends IGGAPIEngineObject {

	void start() throws GGAPIException;

	void setDomain(IGGAPIDomain domain);

	void setService(IGGAPIService service);

	String getName();

	Method getMethod(GGAPIInterfaceMethod method);

}
