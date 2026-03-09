package com.garganttua.api.spec.definition;

import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.operation.Scope;
import com.garganttua.api.spec.operation.TechnicalOperation;

public interface IUseCaseDefinition {

	Scope scope();

	TechnicalOperation operation();

	Access access();

	boolean authority();
}
