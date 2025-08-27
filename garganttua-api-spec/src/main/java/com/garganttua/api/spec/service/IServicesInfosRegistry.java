package com.garganttua.api.spec.service;

import java.util.List;

import com.garganttua.api.spec.domain.IDomain;

public interface IServicesInfosRegistry {

	List<IServiceInfos> getServiceInfos(String domainName);

	List<IServiceInfos> getServicesInfos();

	void addServicesInfos(IDomain domain, List<IServiceInfos> authenticationServiceInfos);

}
