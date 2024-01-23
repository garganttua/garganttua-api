package com.garganttua.api.security.authorization;

import org.springframework.http.HttpMethod;

import com.garganttua.api.core.GGAPICrudAccess;
import com.garganttua.api.core.GGAPICrudOperation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicGGAPIAccessRule implements IGGAPIAccessRule {

	private String endpoint;
	private String authority; 
	private HttpMethod httpMethod;
	private GGAPICrudAccess access;
	
	@Override
	public String toString() {	
		return "[endpoint ["+this.endpoint+"] access ["+this.access+"] authorization ["+this.authority+"] httpMethod ["+this.httpMethod+"]]";
	}
	
	public static String getAuthority(String domain, GGAPICrudOperation operation) {
		
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
