package com.garganttua.api.events;

import com.garganttua.api.engine.IGGAPIEngineObject;
import com.garganttua.api.spec.IGGAPIEntity;

public interface IGGAPIEventPublisher<Entity extends IGGAPIEntity> extends IGGAPIEngineObject{
	
	public void publishEvent(GGAPIEvent<Entity> event);
		
}
