package com.garganttua.api.security.authorization;

import org.springframework.http.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicSpringCrudifyAuthorization implements ISpringCrudifyAuthorization {

	private String endpoint;
	private String role; 
	private HttpMethod httpMethod;
	
	@Override
	public String toString() {
		
		return "[endpoint ["+this.endpoint+"] role ["+this.role+"] httpMethod ["+this.httpMethod+"]]";
	}

}
