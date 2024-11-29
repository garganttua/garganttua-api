package com.garganttua.api.spec.security.key;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIKeyRealm {

	String getName();
	
	GGAPIKeyAlgorithm getAlgorithm();
	
	boolean equals(IGGAPIKeyRealm object);

	IGGAPIKey getKeyForUnciphering() throws GGAPIException;

	IGGAPIKey getKeyForCiphering() throws GGAPIException;

	String getUuid();

}
