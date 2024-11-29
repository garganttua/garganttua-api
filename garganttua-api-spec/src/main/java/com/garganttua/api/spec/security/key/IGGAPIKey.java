package com.garganttua.api.spec.security.key;

import java.security.Key;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIKey {
	
	byte[] cipher(byte[] clear) throws GGAPIException;
	
	byte[] uncipher(byte[] encoded) throws GGAPIException;

	Key getKey() throws GGAPIException;
	
}
