package com.garganttua.api.core.security.authentication;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.OwnerIds;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.core.observability.Logger;
import com.garganttua.core.reflection.ObjectAddress;

/**
 * Specialisation of {@link KeySupplier} for the VERIFY side: instead of the
 * current key for the scope, it resolves the EXACT key that SIGNED the token
 * being verified — read from the token's {@code signedBy} field. So a token's
 * {@code @AuthenticationAuthenticate} method verifies the signature against the
 * key that actually produced it, even after that key has rotated.
 *
 * <p>Inherits the key business rules (scope / autocreate / rotation) from
 * {@link KeySupplier} as the fallback when the token carries no qualified
 * {@code signedBy} (e.g. a stateless self-describing token). Supplies the user's
 * own key object (the {@code @Key} entity), never a framework {@code IKeyRealm}.
 *
 * <p>Wire it into a token verify method with
 * {@code .withParam(i, new SigningKeySupplierBuilder())}.
 */
@SuppressWarnings("rawtypes")
public class SigningKeySupplier extends KeySupplier {
	private static final Logger log = Logger.getLogger(SigningKeySupplier.class);

	@Override
	public Object resolveKey(IDomain<?> authzDomain, IOperationRequest request) {
		return resolveKeyForToken(authzDomain, tokenFrom(request), request);
	}

	/**
	 * Resolves the key for an explicitly supplied token: the EXACT key it was
	 * signed with ({@code signedBy}), or — when the token carries no qualified
	 * signedBy — the base "current key for the scope". Lets callers that already
	 * hold the decoded token (rather than a request) reuse the same logic.
	 */
	public Object resolveKeyForToken(IDomain<?> authzDomain, Object token, IOperationRequest request) {
		if (token != null) {
			Object exact = resolveBySignedBy(authzDomain, token);
			if (exact != null) {
				log.debug("SigningKeySupplier resolved the exact signing key from signedBy");
				return exact;
			}
		}
		// No qualified signedBy → fall back to the current key for the scope.
		return super.resolveKey(authzDomain, request);
	}

	/** The decoded token travels as the authenticate request's credentials. */
	private static Object tokenFrom(IOperationRequest request) {
		if (request == null) {
			return null;
		}
		Object entity = request.arg("entity").orElse(null);
		return (entity instanceof IAuthenticationRequest authReq) ? authReq.credentials() : null;
	}

	/**
	 * Resolves the EXACT persisted {@code @Key} entity named by the token's
	 * qualified {@code signedBy} ({@code ${keyDomain}:${uuid}}). Returns null when
	 * the token carries no qualified signedBy (the caller then falls back to the
	 * base "current key" rules).
	 */
	private Object resolveBySignedBy(IDomain<?> authzDomain, Object token) {
		Object defObj = SecurityExpressions.authorizationDefinition(authzDomain);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef)) {
			return null;
		}
		ObjectAddress signedByAddr = authzDef.signedBy();
		String signedBy = signedByAddr != null ? SecurityExpressions.readField(token, signedByAddr) : null;
		if (signedBy == null || !OwnerIds.isQualified(signedBy)) {
			return null;
		}
		IApi api = SecurityExpressions.apiOf(authzDomain);
		String keyDomainName = OwnerIds.domainOf(signedBy);
		String keyUuid = OwnerIds.idOf(signedBy);
		IDomain<?> keyDomain = (api != null && keyDomainName != null)
				? api.getDomain(keyDomainName).orElse(null) : null;
		if (keyDomain == null) {
			throw new ApiException("SigningKeySupplier: key domain '" + keyDomainName
					+ "' (from signedBy '" + signedBy + "') is not registered");
		}
		ObjectAddress uuidField = keyDomain.getEntityDefinition() != null
				? keyDomain.getEntityDefinition().uuid() : null;
		if (uuidField == null) {
			throw new ApiException("SigningKeySupplier: key domain '" + keyDomain.getDomainName()
					+ "' has no uuid field");
		}
		IFilter filter = Filter.eq(uuidField.toString(), keyUuid);
		List<Object> results = keyDomain.getRepository()
				.getEntities(Optional.empty(), Optional.of(filter), Optional.empty());
		if (results == null || results.isEmpty()) {
			throw new ApiException("SigningKeySupplier: signing key not found for signedBy '" + signedBy + "'");
		}
		return results.get(0);
	}

}
