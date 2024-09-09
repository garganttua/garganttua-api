package com.garganttua.api.core.accessRules;

import com.garganttua.api.spec.GGAPIEntityOperation;
import com.garganttua.api.spec.security.IGGAPIAccessRule;
import com.garganttua.api.spec.service.GGAPIServiceAccess;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicGGAPIAccessRule implements IGGAPIAccessRule {
	
	private String endpoint;
	private String authority; 
	private GGAPIEntityOperation operation;
	private GGAPIServiceAccess access;
	
	@Override
	public String toString() {	
		return "[endpoint ["+this.endpoint+"] access ["+this.access+"] authorization ["+this.authority+"] operation ["+this.operation+"]]";
	}
	
	public static String getAuthority(String domain, GGAPIEntityOperation operation) {
		
		String authority = null;
		switch (operation) {
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
