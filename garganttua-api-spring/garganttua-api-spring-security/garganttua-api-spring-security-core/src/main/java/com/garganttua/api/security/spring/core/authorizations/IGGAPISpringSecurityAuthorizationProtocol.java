package com.garganttua.api.security.spring.core.authorizations;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.IGGAPIAuthorization;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface IGGAPISpringSecurityAuthorizationProtocol {

	byte[] getAuthorization(ServletRequest request) throws GGAPIException;

	void setAuthorization(IGGAPIAuthorization authorization, HttpServletResponse response) throws GGAPIException;

	String getProtocol();

}
