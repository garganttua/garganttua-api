package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIKeyRealm {

	String getName();
	
	String getAlgorithm();
	
	boolean equals(IGGAPIKeyRealm object);

	IGGAPIKey getKeyForUnciphering() throws GGAPIException;

	IGGAPIKey getKeyForCiphering() throws GGAPIException;

	String getUuid();

}
