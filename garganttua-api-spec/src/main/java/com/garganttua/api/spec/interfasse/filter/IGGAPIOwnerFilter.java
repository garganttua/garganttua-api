package com.garganttua.api.spec.interfasse.filter;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.IGGAPICaller;

public interface IGGAPIOwnerFilter {

	void doOwnerIdFiltering(IGGAPICaller caller, String ownerId, String requestedtenantId) throws GGAPIException;
}
