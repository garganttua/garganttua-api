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
}
