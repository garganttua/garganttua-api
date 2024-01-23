package com.garganttua.api.security.authorization;

import org.springframework.http.HttpMethod;

import com.garganttua.api.core.GGAPICrudAccess;

public interface IGGAPIAccessRule {
	
	String getEndpoint();
	
	String getAuthority();
	
	HttpMethod getHttpMethod();
	
	String toString();
	
	GGAPICrudAccess getAccess();

}
