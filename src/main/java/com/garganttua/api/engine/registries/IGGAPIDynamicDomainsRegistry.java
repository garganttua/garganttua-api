package com.garganttua.api.engine.registries;

import java.util.List;

import com.garganttua.api.engine.GGAPIDynamicDomain;

import jakarta.servlet.http.HttpServletRequest;

public interface IGGAPIDynamicDomainsRegistry {

	List<GGAPIDynamicDomain> getDynamicDomains();

	GGAPIDynamicDomain getDomain(HttpServletRequest request);

	GGAPIDynamicDomain getDomain(String string);

}
