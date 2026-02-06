package com.garganttua.api.spec.interfasse;

import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.core.lifecycle.ILifecycle;

public interface IInterface extends ILifecycle {

	void handle(IDomainContext<?> context);

}
