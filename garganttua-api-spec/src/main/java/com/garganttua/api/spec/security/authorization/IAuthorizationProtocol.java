package com.garganttua.api.spec.security.authorization;

import com.garganttua.core.CoreException;

public interface IAuthorizationProtocol {

	byte[] getAuthorization(Object request) throws CoreException;

	void setAuthorization(byte[] authorization, Object response) throws CoreException;

	String getProtocol();

}
