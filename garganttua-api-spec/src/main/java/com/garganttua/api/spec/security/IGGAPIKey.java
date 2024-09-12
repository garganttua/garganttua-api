package com.garganttua.api.spec.security;

import java.security.Key;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIKey {
	
	Key getSigningKey() throws GGAPIException;
	
}
