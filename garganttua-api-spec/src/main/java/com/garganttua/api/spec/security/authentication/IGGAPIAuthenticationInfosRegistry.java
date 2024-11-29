package com.garganttua.api.spec.security.authentication;

import java.util.List;

public interface IGGAPIAuthenticationInfosRegistry {

	List<GGAPIAuthenticationInfos> getAuthenticationInfos();
	
	List<Class<?>> getAuthentications();

	GGAPIAuthenticationInfos getAuthenticationInfos(Class<?> authenticationType);

}
