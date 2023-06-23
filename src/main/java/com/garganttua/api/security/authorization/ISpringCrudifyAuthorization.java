package com.garganttua.api.security.authorization;

import org.springframework.http.HttpMethod;

public interface ISpringCrudifyAuthorization {
	
	String getEndpoint();
	
	String getRole();
	
	HttpMethod getHttpMethod();
	
	String toString();

}
