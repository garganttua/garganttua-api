package com.garganttua.api.spec.endpoint;

import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.core.lifecycle.ILifecycle;

public interface IEndpoint extends ILifecycle {

	void handle(IDomainContext<?> context);

}
