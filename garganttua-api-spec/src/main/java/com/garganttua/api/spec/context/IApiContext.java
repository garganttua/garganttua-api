package com.garganttua.api.spec.context;

import java.util.Optional;

import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.workflow.WorkflowExecutionOptions;

public interface IApiContext extends ILifecycle {

    Optional<IDomainContext<?>> getDomainContext(String domainName);

    String getSuperTenantId();

    boolean isMultiTenant();

    // --- Workflow invocation ---

    default IOperationResponse invoke(String domainName, IOperationRequest request) {
        return getDomainContextOrThrow(domainName).invoke(request);
    }

    default IOperationResponse invoke(String domainName, IOperationRequest request, WorkflowExecutionOptions options) {
        return getDomainContextOrThrow(domainName).invoke(request, options);
    }

    // --- CRUD convenience methods ---

    default IOperationResponse createOne(String domainName, Object body, ICaller caller) {
        return getDomainContextOrThrow(domainName).createOne(body, caller);
    }

    default IOperationResponse readOne(String domainName, String uuid, ICaller caller) {
        return getDomainContextOrThrow(domainName).readOne(uuid, caller);
    }

    default IOperationResponse readAll(String domainName, ICaller caller) {
        return getDomainContextOrThrow(domainName).readAll(caller);
    }

    default IOperationResponse readAll(String domainName, IFilter filter, IPageable page, ISort sort, ICaller caller) {
        return getDomainContextOrThrow(domainName).readAll(filter, page, sort, caller);
    }

    default IOperationResponse updateOne(String domainName, String uuid, Object body, ICaller caller) {
        return getDomainContextOrThrow(domainName).updateOne(uuid, body, caller);
    }

    default IOperationResponse deleteOne(String domainName, String uuid, ICaller caller) {
        return getDomainContextOrThrow(domainName).deleteOne(uuid, caller);
    }

    default IOperationResponse deleteAll(String domainName, ICaller caller) {
        return getDomainContextOrThrow(domainName).deleteAll(caller);
    }

    // --- Internal helper ---

    private IDomainContext<?> getDomainContextOrThrow(String domainName) {
        return getDomainContext(domainName)
                .orElseThrow(() -> new ApiException("Domain not found: " + domainName));
    }

}
