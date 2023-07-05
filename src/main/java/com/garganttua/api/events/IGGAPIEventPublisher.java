package com.garganttua.api.events;

import com.garganttua.api.spec.IGGAPIEntity;

public interface IGGAPIEventPublisher {
	
	public <Entity extends IGGAPIEntity> void publishEvent(GGAPIEvent<Entity> event);
		
}
