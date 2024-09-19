package com.garganttua.api.security.spring.core.keys;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.security.IGGAPIKeyRealm;

public interface IGGAPISpringKeyProvider {

	IGGAPIKeyRealm getRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException;

	IGGAPIKeyRealm createRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException;

	IGGAPIKeyRealm revokeRealm(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException;

	IGGAPIKeyRealm revokeAllRealms(IGGAPICaller caller, GGAPISpringSecurityKeyEntityRequest request) throws GGAPIException;

	IGGAPIKeyRealm getRealm(IGGAPICaller caller, String realmUuid) throws GGAPIException;
}
