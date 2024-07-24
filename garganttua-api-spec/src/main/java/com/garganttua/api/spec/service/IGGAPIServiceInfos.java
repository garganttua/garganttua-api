package com.garganttua.api.spec.service;

import com.garganttua.api.spec.GGAPIEntityOperation;

public interface IGGAPIServiceInfos {

	String getMethodName();

	Class<?>[] getParameters();
	
	String getPath();
	
//	String getAuthority();
	
//	IGGAPIAccessRule getAccessRule();

	String getDescription();

	GGAPIEntityOperation getOperation();

}
