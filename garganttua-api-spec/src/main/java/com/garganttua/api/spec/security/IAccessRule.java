package com.garganttua.api.spec.security;

import com.garganttua.api.spec.context.Access;
import com.garganttua.api.spec.context.Operation;

public interface IAccessRule {
	
	default String endpoint(){
		return operation().getPath();
	}
	
	String authority();
	
	Operation operation();
	
	Access access();

	default String key() {
		return operation().key();
	}
}
