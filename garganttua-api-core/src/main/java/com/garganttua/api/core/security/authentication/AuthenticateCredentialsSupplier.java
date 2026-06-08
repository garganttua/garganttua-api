package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

/**
 * Supplies the credentials for the authenticate method.
 * Reads credentials from the AuthenticationRequest entity stored in the request
 * under the "entity" key (set by the workflow before AUTHENTICATE.gs).
 */
@SuppressWarnings("rawtypes")
public class AuthenticateCredentialsSupplier implements IContextualSupplier<byte[], IRuntimeContext> {

    private static final IClass<byte[]> SUPPLIED_CLASS = IClass.getClass(byte[].class);
    private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

    @Override
    public Type getSuppliedType() { return SUPPLIED_CLASS.getType(); }

    @Override
    public IClass<byte[]> getSuppliedClass() { return SUPPLIED_CLASS; }

    @Override
    public IClass<IRuntimeContext> getOwnerContextType() { return CONTEXT_CLASS; }

    @Override
    public Optional<byte[]> supply(IRuntimeContext context, Object... otherContexts) throws SupplyException {
        if (context == null) {
            throw new SupplyException("IRuntimeContext cannot be null");
        }

        Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
        if (requestOpt.isEmpty()) {
            throw new SupplyException("Variable 'request' not found in runtime context");
        }
        IOperationRequest request = (IOperationRequest) requestOpt.get();

        // Read the entity (AuthenticationRequest) from the request
        Optional<?> entityOpt = request.arg("entity");
        if (entityOpt.isEmpty()) {
            throw new SupplyException("Variable 'entity' not found in request");
        }

        Object entity = entityOpt.get();
        if (entity instanceof IAuthenticationRequest authReq) {
            Object creds = authReq.credentials();
            if (creds == null) return Optional.empty();
            if (creds instanceof byte[] bytes) return Optional.of(bytes);
            // Over HTTP the JSON "credentials" deserializes into the Object field as a
            // String — encode it as UTF-8, matching how login+password authenticators
            // read the bytes (new String(credentials, UTF_8)). Without this, an HTTP
            // login could never satisfy this non-nullable byte[] parameter.
            if (creds instanceof String s) return Optional.of(s.getBytes(StandardCharsets.UTF_8));
            // Any other shape (e.g. a decoded token entity in the verify flow): this
            // supplier only handles login+password credentials. Yield to other
            // suppliers/strategies designed for the runtime shape at hand.
            return Optional.empty();
        }

        throw new SupplyException("Entity is not an IAuthenticationRequest: " + entity.getClass().getName());
    }
}
