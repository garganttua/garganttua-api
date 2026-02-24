package com.garganttua.api.spec.security.authorization;

import com.garganttua.api.spec.ApiException;

public interface IAuthorizationProtocol {

	byte[] getAuthorization(Object request) throws ApiException;

	void setAuthorization(byte[] authorization, Object response) throws ApiException;

	String getProtocol();

}
