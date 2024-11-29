package com.garganttua.api.spec.security.authenticator;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.service.IGGAPIService;

public interface IGGAPIAuthenticatorServicesRegistry {

	List<IGGAPIService> getServices();

	List<IGGAPIDomain> getDomains();

	Pair<GGAPIAuthenticatorInfos, IGGAPIService> getService(String domain);

}
