package com.garganttua.api.spec.security.authorization;

import java.util.List;

import com.garganttua.core.reflection.IClass;

public interface IAuthorizationInfosRegistry {

	List<IClass<?>> getAuthorizationsTypes();

	List<AuthorizationInfos> getAuthorizationsInfos();

}
