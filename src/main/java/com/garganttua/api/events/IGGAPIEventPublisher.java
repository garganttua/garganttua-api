package com.garganttua.api.events;

import com.garganttua.api.engine.IGGAPIEngineObject;

public interface IGGAPIEventPublisher extends IGGAPIEngineObject{
	
	public void publishEvent(GGAPIEvent event);
		
}
