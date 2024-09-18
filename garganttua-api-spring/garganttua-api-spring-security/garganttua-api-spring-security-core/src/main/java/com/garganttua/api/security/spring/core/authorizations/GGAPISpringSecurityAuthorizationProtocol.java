package com.garganttua.api.security.spring.core.authorizations;

import com.garganttua.api.spec.GGAPIException;

import jakarta.servlet.ServletRequest;

public interface GGAPISpringSecurityAuthorizationProtocol {

	byte[] getAuthorization(ServletRequest request) throws GGAPIException;

}
