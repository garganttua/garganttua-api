package com.garganttua.api.core.security.keys;

public interface IGGAPIKeyRealm {

	GGAPIKey getCipheringKey() throws GGAPIKeyExpiredException;

	GGAPIKey getUncipheringKey() throws GGAPIKeyExpiredException;

	String getName();
	
	String getAlgo();
	
	boolean equals(IGGAPIKeyRealm object);

}
