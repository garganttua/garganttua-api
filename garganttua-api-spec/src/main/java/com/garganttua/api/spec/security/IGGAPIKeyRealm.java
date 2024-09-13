package com.garganttua.api.spec.security;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.reflection.GGObjectAddress;

public interface IGGAPIKeyRealm {

	String getName();
	
	String getAlgorithm();
	
	boolean equals(IGGAPIKeyRealm object);

	IGGAPIKey getKeyForUnciphering() throws GGAPIException;

	IGGAPIKey getKeyForCiphering() throws GGAPIException;

	String getUuid();
	
	byte[] sign(byte[] toBeSigned)throws GGAPIException;
	
	void verifySignature(byte[] signature, byte[] original) throws GGAPIException;

}
