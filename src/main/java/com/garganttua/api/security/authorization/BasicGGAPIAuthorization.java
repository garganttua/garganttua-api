package com.garganttua.api.security.authorization;

import org.springframework.http.HttpMethod;

import com.garganttua.api.spec.GGAPICrudAccess;
import com.garganttua.api.spec.GGAPICrudOperation;

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
	
	public static String getAuthorization(String domain, GGAPICrudOperation operation) {
		
		String authority = null;
		switch (operation) {
		case count:
			authority = domain + "-get-count";
			break;
		case create_one:
			authority = domain + "-create";
			break;
		case delete_all:
			authority = domain + "-delete-all";
			break;
		case delete_one:
			authority = domain + "-delete-one";
			break;
		default:
		case read_all:
			authority = domain + "-read-all";
			break;
		case read_one:
			authority = domain + "-read-one";
			break;
		case update_one:
			authority = domain + "-update-one";
			break;
		}
		
		return authority;
	}

}
