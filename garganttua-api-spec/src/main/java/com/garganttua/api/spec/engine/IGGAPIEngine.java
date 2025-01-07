package com.garganttua.api.spec.engine;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.factory.IGGAPIEntityFactory;
import com.garganttua.api.spec.repository.IGGAPIRepository;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.IGGAPIService;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public interface IGGAPIEngine {
    
    IGGAPIEngine start() throws GGAPIException;

	IGGAPIEngine stop() throws GGAPIException;

	IGGAPIEngine reload() throws GGAPIException;

	IGGAPIEngine flush() throws GGAPIException;

	IGGAPIEngine init() throws GGAPIException;

	List<String> getAuthorities();

	String getAuthority(GGAPIEntityOperation method);

	IGGAPIDomain getTenantsDomain();

	Set<IGGAPIDomain> getDomains();

	void addServicesInfos(IGGAPIDomain domain, List<IGGAPIServiceInfos> authenticationServiceInfos);

	void addAccessRule(IGGAPIAccessRule accessRule);

	IGGAPIService getService(String domain);

	String getTenantDomainName();

	List<IGGAPIServiceInfos> getServicesInfos();

	IGGAPICaller getCaller(String domainName, GGAPIEntityOperation operation, String path, String tenantId, String ownerId,
			String requestedtenantId, Object object) throws GGAPIException;

	Optional<IGGAPIDomain> getDomain(String domain);

	List<IGGAPIAccessRule> getAccessRules();

	IGGAPIService getTenantService();

	Collection<IGGAPIService> getServices();

	IGGAPIEntityFactory<?> getFactory(String domainName);

	IGGAPIRepository getRepository(String domainName);

}
