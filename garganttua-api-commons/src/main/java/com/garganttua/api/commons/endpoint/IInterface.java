package com.garganttua.api.commons.endpoint;

import com.garganttua.api.commons.context.IDomain;
import com.garganttua.core.lifecycle.ILifecycle;

public interface IInterface extends ILifecycle {

	void handle(IDomain<?> context);

}
