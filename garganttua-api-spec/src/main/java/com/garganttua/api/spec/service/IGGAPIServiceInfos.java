package com.garganttua.api.spec.service;

import com.garganttua.api.spec.GGAPIEntityOperation;

public interface IGGAPIServiceInfos {
	
	String getDomainName();

	String getMethodName();
	
	Class<?> getInterface();

	Class<?>[] getParameters();
	
	String getPath();

	String getDescription();

	GGAPIEntityOperation getOperation();

}
