package com.garganttua.api.spec.security.authentication;

import java.lang.reflect.Method;

import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.endpoint.ICustomizableEndpoint;
import com.garganttua.api.spec.ApiException;

public interface IAuthenticationInterface extends ICustomizableEndpoint {

	void setAuthenticationService(IAuthenticationService authenticationService);

	void addAuthenticationInfos(AuthenticationInfos authenticationInfos);

	void start() throws ApiException;

	String getName();

	void setDomainDefinition(IDomainDefinition<?> domainDefinition);

	Method getAuthenticateMethod();

}
