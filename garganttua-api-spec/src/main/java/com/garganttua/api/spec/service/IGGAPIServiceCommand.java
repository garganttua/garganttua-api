package com.garganttua.api.spec.service;

import com.garganttua.api.spec.event.IGGAPIEvent;

@FunctionalInterface
public interface IGGAPIServiceCommand {
	
	public IGGAPIEvent execute(IGGAPIEvent event) throws Exception;

}
