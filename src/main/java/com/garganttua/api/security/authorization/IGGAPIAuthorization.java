package com.garganttua.api.security.authorization;

import org.springframework.http.HttpMethod;

import com.garganttua.api.spec.GGAPICrudAccess;

public interface IGGAPIAuthorization {
	
	String getEndpoint();
	
	String getAuthorization();
	
	HttpMethod getHttpMethod();
	
	String toString();
	
	GGAPICrudAccess getAccess();

}
