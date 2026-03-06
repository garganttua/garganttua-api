package com.garganttua.api.spec.security.authentication;

import java.util.Map;

import com.garganttua.core.reflection.IClass;

public interface IAuthenticationFactoriesRegistry {

	IAuthenticationFactory getFactory(IClass<?> authenticationType);

	Map<IClass<?>, IAuthenticationFactory> getFactories();

}
