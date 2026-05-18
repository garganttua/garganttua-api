package com.garganttua.api.commons.context;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.IRequestBuilder;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.workflow.WorkflowExecutionOptions;

public interface IApi extends ILifecycle {

    Optional<IDomain<?>> getDomain(String domainName);

    String getSuperTenantId();

    boolean isMultiTenant();

    List<ISerializer> getSerializers();

    List<IProtocol<?, ?>> getProtocols();

    List<IAuthorizationProtocol> getAuthorizationProtocols();


    // --- Request builder ---

    default IRequestBuilder request(String domainName) {
        return getDomainOrThrow(domainName).request();
    }

    // --- Workflow invocation ---

    default IOperationResponse invoke(IOperationRequest request) {
        return getDomainOrThrow(request.domain()).invoke(request);
    }

    default IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options) {
        return getDomainOrThrow(request.domain()).invoke(request, options);
    }

    // --- CRUD convenience methods ---

    default IOperationResponse createOne(String domainName, Object body, ICaller caller) {
        return getDomainOrThrow(domainName).createOne(body, caller);
    }

    default IOperationResponse readOne(String domainName, String uuid, ICaller caller) {
        return getDomainOrThrow(domainName).readOne(uuid, caller);
    }

    default IOperationResponse readAll(String domainName, ICaller caller) {
        return getDomainOrThrow(domainName).readAll(caller);
    }

    default IOperationResponse readAll(String domainName, IFilter filter, IPageable page, ISort sort, ICaller caller) {
        return getDomainOrThrow(domainName).readAll(filter, page, sort, caller);
    }

    default IOperationResponse updateOne(String domainName, String uuid, Object body, ICaller caller) {
        return getDomainOrThrow(domainName).updateOne(uuid, body, caller);
    }

    default IOperationResponse deleteOne(String domainName, String uuid, ICaller caller) {
        return getDomainOrThrow(domainName).deleteOne(uuid, caller);
    }

    default IOperationResponse deleteAll(String domainName, ICaller caller) {
        return getDomainOrThrow(domainName).deleteAll(caller);
    }

    // --- Internal helper ---

    private IDomain<?> getDomainOrThrow(String domainName) {
        return getDomain(domainName)
                .orElseThrow(() -> new ApiException("Domain not found: " + domainName));
    }

}
