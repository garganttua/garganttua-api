package com.garganttua.api.core.service;

import com.garganttua.api.spec.event.IGGAPIEvent;

@FunctionalInterface
public interface IGGAPIServiceCommand {
	
	public IGGAPIEvent execute(IGGAPIEvent event) throws Exception;

}
