package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IDomain;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.IOperationResponse;
import com.garganttua.api.spec.service.OperationResponseCode;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("rawtypes")
public class PrincipalSupplier implements IContextualSupplier<Object, IRuntimeContext> {

    private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<Object> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<Object> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.atTrace().log("Entering PrincipalSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        Optional<?> domainContextOpt = context.getVariable("domainContext", IClass.getClass(IDomain.class));
        if (domainContextOpt.isEmpty()) {
            throw new SupplyException("Variable 'domainContext' not found in runtime context");
        }
        IDomain domainContext = (IDomain) domainContextOpt.get();
        String domainName = domainContext.getDomainName();

        ObjectAddress idFieldAddress = domainContext.getEntityDefinition().id();
        if (idFieldAddress == null) {
            log.atWarn().log("No id field configured for domain={}", domainName);
            return Optional.empty();
        }

        String callerId = request.caller() != null ? request.caller().callerId() : null;
        if (callerId == null) {
            log.atDebug().log("No callerId in request, cannot find principal");
            return Optional.empty();
        }

        String tenantId = request.caller() != null ? request.caller().tenantId() : null;
        ICaller caller = tenantId != null
                ? Caller.createTenantCaller(tenantId)
                : Caller.createSuperCaller();

        Filter idFilter = Filter.eq(idFieldAddress.toString(), callerId);
        IOperationResponse response = domainContext.readAll(idFilter, null, null, caller);

        if (response == null || response.getResponseCode() != OperationResponseCode.OK) {
            log.atDebug().log("Principal not found for callerId={} in domain={}", callerId, domainName);
            return Optional.empty();
        }

        Object result = response.getResponse();
        if (result instanceof List<?> list && !list.isEmpty()) {
            log.atDebug().log("Principal found for callerId={} in domain={}", callerId, domainName);
            return Optional.of(list.get(0));
        }

        log.atDebug().log("No principal entity found for callerId={}", callerId);
        return Optional.empty();
    }

}
