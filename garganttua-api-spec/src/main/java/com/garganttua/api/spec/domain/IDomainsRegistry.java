package com.garganttua.api.spec.domain;

import java.util.Set;

import com.garganttua.api.spec.engine.IEngineObject;

public interface IDomainsRegistry extends IEngineObject {

	Set<IDomain> getDomains();

	IDomain getDomain(String string);
	
	IDomain getOwnerDomain();
	
	IDomain getTenantDomain();
}
