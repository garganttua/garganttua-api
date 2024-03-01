package com.garganttua.api.events;

import com.garganttua.api.engine.IGGAPIEngineObject;

public interface IGGAPIEventPublisher<Entity> extends IGGAPIEngineObject{
	
	public void publishEvent(GGAPIEvent<Entity> event);
		
}
