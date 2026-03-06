package com.garganttua.api.spec.security.authentication;

import java.util.List;

import com.garganttua.core.reflection.IClass;

public interface IAuthenticationInfosRegistry {

	List<AuthenticationInfos> getAuthenticationInfos();

	List<IClass<?>> getAuthentications();

	AuthenticationInfos getAuthenticationInfos(IClass<?> authenticationType);

}
