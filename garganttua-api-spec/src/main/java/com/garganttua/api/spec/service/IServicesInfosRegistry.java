package com.garganttua.api.spec.service;

import java.util.List;

import com.garganttua.api.spec.definition.IDomainDefinition;

public interface IServicesInfosRegistry {

	List<IServiceInfos> getServiceInfos(String domainName);

	List<IServiceInfos> getServicesInfos();

	void addServicesInfos(IDomainDefinition<?> domainDefinition, List<IServiceInfos> authenticationServiceInfos);

}
