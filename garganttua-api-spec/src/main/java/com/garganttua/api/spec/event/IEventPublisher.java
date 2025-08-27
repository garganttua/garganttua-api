package com.garganttua.api.spec.event;

import com.garganttua.api.spec.engine.IEngineObject;

public interface IEventPublisher extends IEngineObject{
	
	public void publishEvent(IEvent event);
		
}
