package com.garganttua.api.spec.interfasse;

import java.util.List;

public interface IInterfacesRegistry {
	
	List<IInterface> getInterfaces(String domainName);

	List<IInterface> getInterfaces();

}
