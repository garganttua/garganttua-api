package com.garganttua.api.spec.service;

import com.garganttua.api.spec.event.IEvent;

@FunctionalInterface
public interface IServiceCommand {
	
	public IEvent execute(IEvent event) throws Exception;

}
