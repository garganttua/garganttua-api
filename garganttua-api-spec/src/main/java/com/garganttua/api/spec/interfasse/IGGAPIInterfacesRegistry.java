package com.garganttua.api.spec.interfasse;

import java.util.List;

import com.garganttua.api.spec.engine.IGGAPIEngineObject;

public interface IGGAPIInterfacesRegistry extends IGGAPIEngineObject {
	
	List<IGGAPIInterface> getInterfaces(String domainName);

	List<IGGAPIInterface> getInterfaces();

}
