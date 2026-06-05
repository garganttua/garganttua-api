package com.garganttua.api.core.security.authentication;

import java.lang.reflect.Type;
import java.util.Optional;

import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.core.observability.Logger;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.SupplyException;

/**
 * Supplies the KEY OBJECT that SIGNED the authorization currently being
 * verified, so a token's {@code @AuthenticationAuthenticate} method can verify
 * its own signature by hand.
 *
 * <p>Returns {@code Object}, not a framework {@code IKeyRealm}: the framework
 * does not impose how a key is represented — it is whatever the user declared
 * (typically a {@code @Key} entity). The verify method casts the supplied object
 * to its own key type and extracts the verification material however it sees fit.
 *
 * <p>Since {@code verifyAuthorization} was unified with the authenticate
 * pipeline, a token verifies itself: the decoded token is the authenticate
 * request's {@code credentials}. This supplier reads that token, looks up the
 * key it was signed with (via its {@code signedBy} field — the EXACT persisted
 * {@code @Key} entity in {@code .key(domain)} mode, robust to key rotation; the
 * object the configured {@code .key(supplier)} provides otherwise) and hands it
 * to the method.
 *
 * <p>Wire it into the token authenticator method with
 * {@code .withParam(i, new SigningKeySupplierBuilder())} (DSL).
 */
@SuppressWarnings("rawtypes")
public class SigningKeySupplier implements IContextualSupplier<Object, IRuntimeContext> {
	private static final Logger log = Logger.getLogger(SigningKeySupplier.class);

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
		log.trace("Entering SigningKeySupplier.supply");

		if (context == null) {
			throw new SupplyException("IRuntimeContext cannot be null");
		}

		Optional<?> requestOpt = context.getVariable("request", IClass.getClass(IOperationRequest.class));
		if (requestOpt.isEmpty()) {
			throw new SupplyException("Variable 'request' not found in runtime context");
		}
		IOperationRequest request = (IOperationRequest) requestOpt.get();

		Optional<?> domainOpt = context.getVariable("domainContext", IClass.getClass(IDomain.class));
		if (domainOpt.isEmpty()) {
			throw new SupplyException("Variable 'domainContext' not found in runtime context");
		}
		IDomain<?> authzDomain = (IDomain<?>) domainOpt.get();

		// The authorization being verified is the authenticate request's credentials.
		Object entity = request.arg("entity").orElse(null);
		if (!(entity instanceof IAuthenticationRequest authReq)) {
			throw new SupplyException("Variable 'entity' is not an IAuthenticationRequest");
		}
		Object authz = authReq.credentials();
		if (authz == null) {
			log.debug("SigningKeySupplier: no credentials (authorization) on the request — supplying empty");
			return Optional.empty();
		}

		try {
			Object key = SecurityExpressions.resolveSigningKey(authz, authzDomain, request);
			log.debug("SigningKeySupplier resolved signing key of type {}", key != null ? key.getClass().getName() : null);
			return Optional.ofNullable(key);
		} catch (Exception e) {
			throw new SupplyException("SigningKeySupplier: failed to resolve the signing key: " + e.getMessage(), e);
		}
	}

}
