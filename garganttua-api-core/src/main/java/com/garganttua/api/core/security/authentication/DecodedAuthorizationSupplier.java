package com.garganttua.api.core.security.authentication;
import com.garganttua.api.core.security.key.DomainKeySupplier;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.observability.Logger;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

/**
 * Supplies the decoded authorization (token) entity to a token's
 * {@code @AuthenticationAuthenticate} verification method.
 *
 * <p>Since {@code verifyAuthorization} was unified with the authenticate
 * pipeline, a token verifies itself: the decoded token travels as the
 * authenticate request's {@code credentials} (an Object). The standard
 * {@link AuthenticateCredentialsSupplier} only hands over {@code byte[]}
 * credentials (the login+password flow) and yields for any other shape, so a
 * verification method needs THIS supplier to receive the token itself. It is the
 * token-side companion of {@link DomainKeySupplier} (which supplies the signing
 * key): together they give a custom verify method everything it needs to check a
 * signature by hand.
 *
 * <p>Wire it into the token authenticator method with
 * {@code .withParam(i, new DecodedAuthorizationSupplierBuilder())} (DSL); the
 * matching method parameter is the authorization entity type (or {@code Object}).
 */
@SuppressWarnings("rawtypes")
public class DecodedAuthorizationSupplier implements IContextualSupplier<Object, IRuntimeContext> {
	private static final Logger log = Logger.getLogger(DecodedAuthorizationSupplier.class);

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
		log.trace("Entering DecodedAuthorizationSupplier.supply");

		if (context == null) {
			throw new SupplyException("IRuntimeContext cannot be null");
		}

		Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
		if (requestOpt.isEmpty()) {
			throw new SupplyException("Variable 'request' not found in runtime context");
		}
		IOperationRequest request = (IOperationRequest) requestOpt.get();

		Object entity = request.arg("entity").orElse(null);
		if (!(entity instanceof IAuthenticationRequest authReq)) {
			// Not a verify-flow request (no AuthenticationRequest) — yield so other
			// strategies/suppliers designed for this shape can take over.
			return Optional.empty();
		}
		Object credentials = authReq.credentials();
		// The token is whatever non-byte[] credentials shape the verify flow carries.
		// A byte[] is the login+password flow — not ours; yield to those strategies.
		if (credentials == null || credentials instanceof byte[]) {
			return Optional.empty();
		}
		log.debug("DecodedAuthorizationSupplier resolved token of type {}", credentials.getClass().getName());
		return Optional.of(credentials);
	}

}
