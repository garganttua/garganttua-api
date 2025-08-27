package com.garganttua.api.spec.security.authorization;

import java.util.List;

public interface IAuthorizationInfosRegistry {

	List<Class<?>> getAuthorizationsTypes();

	List<AuthorizationInfos> getAuthorizationsInfos();

}
