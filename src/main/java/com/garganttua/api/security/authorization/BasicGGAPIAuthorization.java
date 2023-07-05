package com.garganttua.api.security.authorization;

import org.springframework.http.HttpMethod;

import com.garganttua.api.spec.GGAPICrudAccess;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicGGAPIAuthorization implements IGGAPIAuthorization {

	private String endpoint;
	private String authorization; 
	private HttpMethod httpMethod;
	private GGAPICrudAccess access;
	
	@Override
	public String toString() {	
		return "[endpoint ["+this.endpoint+"] access ["+this.access+"] authorization ["+this.authorization+"] httpMethod ["+this.httpMethod+"]]";
	}

}
