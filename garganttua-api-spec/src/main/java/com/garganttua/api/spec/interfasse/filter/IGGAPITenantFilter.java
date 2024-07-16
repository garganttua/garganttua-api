package com.garganttua.api.spec.interfasse.filter;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;

public interface IGGAPITenantFilter {

	void doTenantIdFiltering(IGGAPICaller caller, String tenantId, String requestedtenantId) throws GGAPIException;

}
