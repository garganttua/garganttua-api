package com.garganttua.api.security.keys;

public interface IGGAPIKeyRealm {

	GGAPIKey getCipheringKey() throws GGAPIKeyExpiredException;

	GGAPIKey getUncipheringKey() throws GGAPIKeyExpiredException;

	String getName();

}
