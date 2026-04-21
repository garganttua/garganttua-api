package com.garganttua.api.commons.definition;

import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.Scope;
import com.garganttua.api.commons.operation.TechnicalOperation;

public interface IUseCaseDefinition {

	Scope scope();

	TechnicalOperation operation();

	Access access();

	boolean authority();
}
