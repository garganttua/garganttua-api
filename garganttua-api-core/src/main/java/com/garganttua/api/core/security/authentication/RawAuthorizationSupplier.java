package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

import com.garganttua.core.observability.Logger;

@SuppressWarnings("rawtypes")
public class RawAuthorizationSupplier implements IContextualSupplier<byte[], IRuntimeContext> {
	private static final Logger log = Logger.getLogger(RawAuthorizationSupplier.class);


    private static final IClass<byte[]> SUPPLIED_CLASS = IClass.getClass(byte[].class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() {
        return SUPPLIED_CLASS.getType();
    }

    @Override
    public IClass<byte[]> getSuppliedClass() {
        return SUPPLIED_CLASS;
    }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() {
        return CONTEXT_CLASS;
    }

    @Override
    public Optional<byte[]> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        log.trace("Entering RawAuthorizationSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        Optional<Byte[]> rawOpt = request.arg(IOperationRequest.RAW_AUTHORIZATION);
        if (rawOpt.isEmpty()) {
            log.debug("No RAW_AUTHORIZATION in request");
            return Optional.empty();
        }

        Byte[] boxed = rawOpt.get();
        byte[] raw = new byte[boxed.length];
        for (int i = 0; i < boxed.length; i++) {
            raw[i] = boxed[i];
        }

        log.debug("RawAuthorizationSupplier resolved raw authorization ({} bytes)", raw.length);
        return Optional.of(raw);
    }

}
