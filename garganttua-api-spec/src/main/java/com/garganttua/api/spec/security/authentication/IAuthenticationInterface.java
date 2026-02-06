package com.garganttua.api.spec.security.authentication;

import java.lang.reflect.Method;

import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.interfasse.ICustomizableInterface;
import com.garganttua.core.CoreException;

public interface IAuthenticationInterface extends ICustomizableInterface {

	void setAuthenticationService(IAuthenticationService authenticationService);

	void addAuthenticationInfos(AuthenticationInfos authenticationInfos);

	void start() throws CoreException;

	String getName();

	void setDomainDefinition(IDomainDefinition<?> domainDefinition);

	Method getAuthenticateMethod();

}
