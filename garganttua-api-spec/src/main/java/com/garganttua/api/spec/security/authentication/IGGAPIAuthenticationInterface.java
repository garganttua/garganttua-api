package com.garganttua.api.spec.security.authentication;

import java.lang.reflect.Method;

import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.service.IGGAPIServiceInfos;

public interface IGGAPIAuthenticationInterface {

	void setAuthenticationService(IGGAPIAuthenticationService authenticationService);

	void addAuthenticationInfos(GGAPIAuthenticationInfos authenticationInfos);

	void start() throws GGAPIException;

	String getName();

	void setDomain(IGGAPIDomain domain);

	Method getAuthenticateMethod();

	void addCustomService(IGGAPIServiceInfos service);

}
