package com.garganttua.api.core.security.authorization;

import com.garganttua.api.core.GGAPICrudOperation;
import com.garganttua.api.core.GGAPIServiceAccess;
import com.garganttua.api.core.service.GGAPIServiceMethod;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicGGAPIAccessRule implements IGGAPIAccessRule {

	private String endpoint;
	private String authority; 
	private GGAPIServiceMethod method;
	private GGAPIServiceAccess access;
	
	@Override
	public String toString() {	
		return "[endpoint ["+this.endpoint+"] access ["+this.access+"] authorization ["+this.authority+"] method ["+this.method+"]]";
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
