package com.garganttua.api.spec.security.authenticator;

import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.service.IService;

public interface IAuthenticatorServicesRegistry {

	List<IService> getServices();

	List<IDomain> getDomains();

	Pair<AuthenticatorInfos, IService> getService(String domain);

}
