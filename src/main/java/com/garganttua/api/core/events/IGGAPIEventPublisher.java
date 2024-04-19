package com.garganttua.api.core.events;

import com.garganttua.api.core.engine.IGGAPIEngineObject;

public interface IGGAPIEventPublisher extends IGGAPIEngineObject{
	
	public void publishEvent(GGAPIEvent event);
		
}
