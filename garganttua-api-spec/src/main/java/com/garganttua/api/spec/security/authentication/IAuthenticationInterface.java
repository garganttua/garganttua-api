package com.garganttua.api.spec.security.authentication;

import java.lang.reflect.Method;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.interfasse.ICustomizableInterface;

public interface IAuthenticationInterface extends ICustomizableInterface {

	void setAuthenticationService(IAuthenticationService authenticationService);

	void addAuthenticationInfos(AuthenticationInfos authenticationInfos);

	void start() throws CoreException;

	String getName();

	void setDomain(IDomain domain);

	Method getAuthenticateMethod();

}
