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
public class CredentialsSupplier implements IContextualSupplier<byte[], IRuntimeContext> {
	private static final Logger log = Logger.getLogger(CredentialsSupplier.class);


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
        log.trace("Entering CredentialsSupplier.supply");

        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        Optional<Byte[]> rawBodyOpt = request.arg(IOperationRequest.RAW_BODY);
        if (rawBodyOpt.isEmpty()) {
            log.debug("No RAW_BODY in request, no credentials available");
            return Optional.empty();
        }

        Byte[] boxed = rawBodyOpt.get();
        byte[] credentials = new byte[boxed.length];
        for (int i = 0; i < boxed.length; i++) {
            credentials[i] = boxed[i];
        }

        log.debug("CredentialsSupplier resolved credentials ({} bytes)", credentials.length);
        return Optional.of(credentials);
    }

}
