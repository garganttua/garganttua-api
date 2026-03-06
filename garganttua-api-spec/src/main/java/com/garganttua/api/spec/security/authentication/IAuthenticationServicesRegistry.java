package com.garganttua.api.spec.security.authentication;

import com.garganttua.core.reflection.IClass;

public interface IAuthenticationServicesRegistry {

	IAuthenticationService getService(IClass<?> authentication);

}
