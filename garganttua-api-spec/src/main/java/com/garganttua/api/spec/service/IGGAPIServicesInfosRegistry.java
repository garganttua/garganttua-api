package com.garganttua.api.spec.service;

import java.util.List;

public interface IGGAPIServicesInfosRegistry {

	List<IGGAPIServiceInfos> getServiceInfos(String domainName);

}
