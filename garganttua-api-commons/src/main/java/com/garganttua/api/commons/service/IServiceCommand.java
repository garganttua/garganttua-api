package com.garganttua.api.commons.service;

import com.garganttua.api.commons.event.IEvent;

@FunctionalInterface
public interface IServiceCommand {
	
	public IEvent execute(IEvent event) throws Exception;

}
