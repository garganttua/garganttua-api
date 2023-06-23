package com.garganttua.api.events;

import com.garganttua.api.spec.ISpringCrudifyEntity;

public interface ISpringCrudifyEventPublisher {
	
	public void publishEntityEvent(SpringCrudifyEntityEvent event, ISpringCrudifyEntity entity);
		
}
