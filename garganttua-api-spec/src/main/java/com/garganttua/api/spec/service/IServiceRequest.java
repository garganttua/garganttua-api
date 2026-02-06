package com.garganttua.api.spec.service;

import com.garganttua.api.spec.context.Operation;

public interface IServiceRequest {

	Operation operation();

	Object[] args();

}
