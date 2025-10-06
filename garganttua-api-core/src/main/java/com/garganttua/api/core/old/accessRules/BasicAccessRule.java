package com.garganttua.api.core.accessRules;

import com.garganttua.api.spec.EntityOperation;
import com.garganttua.api.spec.security.IAccessRule;
import com.garganttua.api.spec.service.ServiceAccess;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BasicAccessRule implements IAccessRule {
	
	private String endpoint;
	private String authority; 
	private EntityOperation operation;
	private ServiceAccess access;
	
	@Override
	public String toString() {	
		return "[endpoint ["+this.endpoint+"] access ["+this.access+"] authorization ["+this.authority+"] operation ["+this.operation+"]]";
	}
}
