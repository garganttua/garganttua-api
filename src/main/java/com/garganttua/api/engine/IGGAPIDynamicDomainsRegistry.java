package com.garganttua.api.engine;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

public interface IGGAPIDynamicDomainsRegistry {

	List<GGAPIDynamicDomain> getDynamicDomains();

	GGAPIDynamicDomain getDomain(HttpServletRequest request);

	GGAPIDynamicDomain getDomain(String string);

}
