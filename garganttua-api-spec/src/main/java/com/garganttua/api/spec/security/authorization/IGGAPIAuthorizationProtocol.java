package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.GGAPIException;

public interface IGGAPIAuthorizationProtocol {

	byte[] getAuthorization(Object request) throws GGAPIException;

	void setAuthorization(byte[] authorization, Object response) throws GGAPIException;

	String getProtocol();

}
