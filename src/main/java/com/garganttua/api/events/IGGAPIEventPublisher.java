package com.garganttua.api.events;

import com.garganttua.api.core.IGGAPIEntity;
import com.garganttua.api.engine.IGGAPIEngineObject;

public interface IGGAPIEventPublisher<Entity extends IGGAPIEntity> extends IGGAPIEngineObject{
	
	public void publishEvent(GGAPIEvent<Entity> event);
		
}
