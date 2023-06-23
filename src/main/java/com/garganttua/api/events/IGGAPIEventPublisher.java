package com.garganttua.api.events;

import com.garganttua.api.spec.IGGAPIEntity;

public interface IGGAPIEventPublisher {
	
	public void publishEntityEvent(GGAPIEntityEvent event, IGGAPIEntity entity);
		
}
