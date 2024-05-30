package com.garganttua.api.spec.event;

import com.garganttua.api.spec.engine.IGGAPIEngineObject;

public interface IGGAPIEventPublisher extends IGGAPIEngineObject{
	
	public void publishEvent(IGGAPIEvent event);
		
}
