package com.garganttua.api.spec.engine;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.factory.IEntityFactory;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.service.IService;
import com.garganttua.api.spec.service.IServiceInfos;

public interface IEngine {
    
    IEngine start() throws CoreException;

	IEngine stop() throws CoreException;

	IEngine reload() throws CoreException;

	IEngine flush() throws CoreException;

	IEngine init() throws CoreException;

	List<String> getAuthorities();

	String getAuthority(EntityOperation method);

	IDomain getTenantsDomain();

	Set<IDomain> getDomains();

	void addServicesInfos(IDomain domain, List<IServiceInfos> authenticationServiceInfos);

	void addAccessRule(IAccessRule accessRule);

	IService getService(String domain);

	String getTenantDomainName();

	List<IServiceInfos> getServicesInfos();

	ICaller getCaller(String domainName, EntityOperation operation, String path, String tenantId, String ownerId,
			String requestedtenantId, Object object) throws CoreException;

	Optional<IDomain> getDomain(String domain);

	List<IAccessRule> getAccessRules();

	IService getTenantService();

	Collection<IService> getServices();

	IEntityFactory<?> getFactory(String domainName);

	IRepository getRepository(String domainName);

}
