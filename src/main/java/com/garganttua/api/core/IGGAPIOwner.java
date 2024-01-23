package com.garganttua.api.core;

public interface IGGAPIOwner extends IGGAPIEntity {

	String getOwnerId();
	
	boolean isSuperOnwer();
	
}
