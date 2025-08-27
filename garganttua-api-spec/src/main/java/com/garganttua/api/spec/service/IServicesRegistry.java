package com.garganttua.api.spec.service;

import java.util.List;

import com.garganttua.api.spec.engine.IEngineObject;

public interface IServicesRegistry extends IEngineObject {
	
	IService getService(String name);

	List<IService> getServices();

}
