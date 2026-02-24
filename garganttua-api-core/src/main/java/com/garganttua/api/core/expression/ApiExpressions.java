package com.garganttua.api.core.expression;

import com.garganttua.api.core.context.RepositoryFilterTools;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.service.IOperationRequest;

public class ApiExpressions {

	public static IFilter buildFilter(Object request) {
		IOperationRequest opRequest = (IOperationRequest) request;
		ICaller caller = opRequest.caller();
		IDomainContext<?> dc = opRequest.arg(IOperationRequest.DOMAIN_CONTEXT).orElse(null);
		IDomainDefinition<?> domainDef = dc.getDomainDefinition();
		IFilter baseFilter = opRequest.arg(IOperationRequest.FILTER).orElse(null);
		return RepositoryFilterTools.buildFilter(caller, domainDef, baseFilter);
	}

}
