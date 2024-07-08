package com.garganttua.api.spec.interfasse;

import java.util.List;

public interface IGGAPIInterfacesRegistry {
	
	List<IGGAPIInterface> getInterfaces(String name);

	List<IGGAPIInterface> getInterfaces();

}
