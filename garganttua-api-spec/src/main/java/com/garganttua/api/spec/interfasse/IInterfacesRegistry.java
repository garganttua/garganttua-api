package com.garganttua.api.spec.interfasse;

import java.util.List;

import com.garganttua.api.spec.engine.IEngineObject;

public interface IInterfacesRegistry extends IEngineObject {
	
	List<IInterface> getInterfaces(String domainName);

	List<IInterface> getInterfaces();

}
