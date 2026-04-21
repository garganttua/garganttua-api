package com.garganttua.api.commons.endpoint;

import java.util.List;

public interface IEndpointsRegistry {
	
	List<IEndpoint> getInterfaces(String domainName);

	List<IEndpoint> getInterfaces();

}
