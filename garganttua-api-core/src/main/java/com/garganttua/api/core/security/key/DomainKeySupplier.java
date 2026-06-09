package com.garganttua.api.core.security.key;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.OwnerIds;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainKeyDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.core.observability.Logger;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;

/**
 * Specialisation of {@link KeySupplier} whose <b>scope</b> is a key realm that is
 * materialised as a <b>domain</b> (a {@code @Key} domain). It resolves the EXACT
 * {@code @Key} entity that SIGNED the token being verified — read from the token's
 * qualified {@code signedBy} reference ({@code ${keyDomain}:${uuid}}) — so a
 * token's verify method checks the signature against the entity that actually
 * produced it, identified by its domain address rather than "the current key".
 *
 * <p>Inherits the key business rules (scope / autocreate / rotation) from
 * {@link KeySupplier} as the fallback when the token carries no qualified
 * {@code signedBy} (e.g. a stateless self-describing token). Supplies the user's
 * own key object (the {@code @Key} entity), never a framework {@code IKeyRealm}.
 *
 * <p><b>Trust gate.</b> Because this resolves a precise persisted key, it also
 * enforces that key's status: a {@code signedBy} key that is <b>revoked</b> or
 * <b>expired</b> makes the resolution fail with an explicit message — the
 * authorization it signed is no longer trusted. (The base "current key" fallback
 * never returns a revoked/expired key either; it rotates instead.)
 *
 * <p>Wire it into a token verify method with
 * {@code .withParam(i, new DomainKeySupplierBuilder())}.
 */
public class DomainKeySupplier extends KeySupplier {
	private static final Logger log = Logger.getLogger(DomainKeySupplier.class);

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
				log.debug("DomainKeySupplier resolved the exact signing key from signedBy");
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
	 * base "current key" rules). Throws when the named key is missing, revoked or
	 * expired — a token signed by a key in any of those states is not trusted.
	 */
	private Object resolveBySignedBy(IDomain<?> authzDomain, Object token) {
		SignerKey signer = resolveSignerKey(authzDomain, token);
		return signer != null ? signer.entity() : null;
	}

	/**
	 * The resolved signing {@code @Key} entity together with its {@code @Key} domain.
	 * Exposed so the verify path can read the entity's verification {@code IKey} directly
	 * (the entity already exposes it — no need to materialise a realm just to verify).
	 */
	public record SignerKey(Object entity, IDomain<?> keyDomain) {
	}

	/**
	 * Resolves the EXACT persisted {@code @Key} entity (and its domain) named by the
	 * token's qualified {@code signedBy}. Returns null when the token carries no
	 * qualified signedBy (verify then fails closed). Throws when the named key is
	 * missing, revoked or expired — a token signed by such a key is not trusted.
	 */
	public SignerKey resolveSignerKey(IDomain<?> authzDomain, Object token) {
		return doResolveSignerKey(authzDomain, token);
	}

	/**
	 * (internal) Resolves the EXACT persisted {@code @Key} entity (and its domain) named by the
	 * token's qualified {@code signedBy}. Returns null when the token carries no
	 * qualified signedBy. Throws when the named key is missing, revoked or expired.
	 */
	private SignerKey doResolveSignerKey(IDomain<?> authzDomain, Object token) {
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
			throw new ApiException("DomainKeySupplier: key domain '" + keyDomainName
					+ "' (from signedBy '" + signedBy + "') is not registered");
		}
		ObjectAddress uuidField = keyDomain.getEntityDefinition() != null
				? keyDomain.getEntityDefinition().uuid() : null;
		if (uuidField == null) {
			throw new ApiException("DomainKeySupplier: key domain '" + keyDomain.getDomainName()
					+ "' has no uuid field");
		}
		IFilter filter = Filter.eq(uuidField.toString(), keyUuid);
		List<Object> results = keyDomain.getRepository()
				.getEntities(Optional.empty(), Optional.of(filter), Optional.empty());
		if (results == null || results.isEmpty()) {
			throw new ApiException("DomainKeySupplier: signing key not found for signedBy '" + signedBy + "'");
		}
		Object keyEntity = results.get(0);
		assertUsable(keyEntity, keyDomain, signedBy);
		return new SignerKey(keyEntity, keyDomain);
	}

	/**
	 * Hard trust gate on the resolved signing key. A token may only be accepted
	 * while the key that signed it is still usable: a revoked key (kill switch) or
	 * an expired key both fail verification, each with its own explicit message.
	 */
	private void assertUsable(Object keyEntity, IDomain<?> keyDomain, String signedBy) {
		IDomainKeyDefinition keyEntDef = keyDomain.getDomainDefinition() != null
				? keyDomain.getDomainDefinition().keyDefinition() : null;
		if (keyEntDef == null) {
			return;
		}
		IReflection reflection = DefaultMapper.reflection();
		if (keyEntDef.revoked() != null) {
			Object revoked = reflection.getFieldValue(keyEntity, keyEntDef.revoked().toString());
			if (Boolean.TRUE.equals(revoked)) {
				throw new ApiException("DomainKeySupplier: the signing key '" + signedBy
						+ "' has been REVOKED — the authorization it signed is no longer trusted; "
						+ "signature verification is refused.");
			}
		}
		if (keyEntDef.expiration() != null) {
			Object exp = reflection.getFieldValue(keyEntity, keyEntDef.expiration().toString());
			if (exp != null && isExpired(exp)) {
				throw new ApiException("DomainKeySupplier: the signing key '" + signedBy
						+ "' has EXPIRED — the authorization it signed can no longer be verified; "
						+ "signature verification is refused.");
			}
		}
	}

}
