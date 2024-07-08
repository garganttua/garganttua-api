package com.garganttua.api.spec.service;

public interface IGGAPIServiceInfos {

	String getMethodName();

	Class<?>[] getParameters();
	
	String getPath();
	
//	String getAuthority();
	
//	IGGAPIAccessRule getAccessRule();

	String getDescription();

	GGAPIServiceMethod getMethod();

}
