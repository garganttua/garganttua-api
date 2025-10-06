package com.garganttua.api.core.runtime;

import com.garganttua.api.spec.event.IEvent;

public interface IEventRuntime {

    void publishEvent(IEvent event);

}
