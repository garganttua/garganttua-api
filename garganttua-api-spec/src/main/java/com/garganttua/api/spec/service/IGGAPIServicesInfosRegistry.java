package com.garganttua.api.spec.service;

import java.util.List;

import com.garganttua.api.spec.domain.IGGAPIDomain;

public interface IGGAPIServicesInfosRegistry {

	List<IGGAPIServiceInfos> getServiceInfos(String domainName);

	List<IGGAPIServiceInfos> getServicesInfos();

	void addServicesInfos(IGGAPIDomain domain, List<IGGAPIServiceInfos> authenticationServiceInfos);

}
