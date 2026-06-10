package com.garganttua.api.core.security.key;

import static com.garganttua.api.core.expression.ExpressionUtils.toDomain;
import static com.garganttua.api.core.expression.ExpressionUtils.toDomainDefinition;
import static com.garganttua.api.core.expression.ExpressionUtils.unwrapOptional;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition;
import com.garganttua.api.commons.definition.IDomainKeyDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.expression.SecurityExpressions;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.observability.Logger;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.supply.IContextualSupplier;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.SupplyException;

/**
 * Single home of the framework's KEY BUSINESS RULES, as an injectable supplier.
 *
 * <p>Resolves the key OBJECT the framework should use for an authenticator's
 * authorization, applying every key policy in one place:
 * <ul>
 *   <li><b>scope</b> — {@code AuthenticatorKeyUsage}: {@code oneForAll} (one
 *       global key), {@code oneForTenant} (one per tenant), {@code oneForEach}
 *       (one per caller), encoded into the realmName so a single lookup returns
 *       the right key;</li>
 *   <li><b>autocreate</b> — generate + persist a fresh key when none exists
 *       (unless {@code autoGenerate(false)});</li>
 *   <li><b>auto re-creation (rotation)</b> — replace an expired/revoked key
 *       (unless {@code autoRotate(false)}).</li>
 * </ul>
 *
 * <p>It returns the user's OWN key object — the {@code @Key} entity in
 * {@code .key(domain)} mode, or whatever {@code .key(supplier)} provides. The
 * framework does not impose its {@code IKeyRealm} shape; the sign path
 * materializes it when it needs to sign (see {@link #resolveSigning}).
 *
 * <p>As a supplier it injects the current key into a method via
 * {@code .withParam(i, new KeySupplierBuilder())}. {@link DomainKeySupplier}
 * extends it to resolve, instead, the EXACT domain-defined key that SIGNED a
 * token being verified.
 */
@SuppressWarnings("rawtypes")
public class KeySupplier implements IContextualSupplier<Object, IRuntimeContext> {
	private static final Logger log = Logger.getLogger(KeySupplier.class);

	private static final IClass<Object> SUPPLIED_CLASS = IClass.getClass(Object.class);
	private static final IClass<IRuntimeContext> CONTEXT_CLASS = IClass.getClass(IRuntimeContext.class);

	/** A materialized signing realm together with its qualified signer id. */
	public record Signing(IKeyRealm realm, String signerId) {
	}

	// ───────────────────────── business rules ─────────────────────────

	/**
	 * Resolves the key OBJECT for the authenticator's authorization in the
	 * current context (supplier object, or the scoped {@code @Key} entity with
	 * autocreate/rotation). Subclasses may override to pick a different key.
	 */
	public Object resolveKey(IDomain<?> authzDomain, IOperationRequest request) {
		requireAuthzAuthDef(authzDomain);
		IDomainAuthenticatorAuthorizationKeyDefinition keyConfig = keyConfig(authzDomain);
		Object supplied = trySupplierMode(authzDomain);
		if (supplied != null) {
			return supplied;
		}
		if (keyConfig != null && keyConfig.keyDomain() != null) {
			return resolvePersisted(authzDomain, keyConfig, request).entity();
		}
		throw new ApiException("KeySupplier: domain '" + domainName(authzDomain)
				+ "' declares a signable authorization but neither .key(supplier) nor .key(domain) was configured");
	}

	/**
	 * Resolves the key for SIGNING: a materialized {@link IKeyRealm} (so the
	 * framework can sign) plus the qualified signer id to stamp on the token.
	 * Uses the same business rules as {@link #resolveKey}.
	 */
	public Signing resolveSigning(IDomain<?> authzDomain, IOperationRequest request) {
		return resolveSigning(authzDomain, request, null);
	}

	/**
	 * Sign-path key resolution scoped by an explicit principal. {@code signingCaller}
	 * is the identity the key realm is bound to: at authenticate / refresh time it is
	 * the just-authenticated PRINCIPAL (the token's own owner/tenant), NOT the anonymous
	 * login request — so {@code oneForEach} yields one key per principal rather than the
	 * shared {@code …:caller:anonymous:anonymous}. A null {@code signingCaller} falls
	 * back to the request caller (the legacy behaviour, e.g. resolveKeyRealm).
	 */
	public Signing resolveSigning(IDomain<?> authzDomain, IOperationRequest request, ICaller signingCaller) {
		requireAuthzAuthDef(authzDomain);
		Object supplied = trySupplierMode(authzDomain);
		if (supplied != null) {
			if (supplied instanceof IKeyRealm realm) {
				// Supplier mode has no @Key entity uuid; the realm name is the signer id.
				return new Signing(realm, realm.getName());
			}
			// A .key(supplier) that yields a non-IKeyRealm object: the framework has
			// no way to sign with it (signing needs IKeyRealm.getKeyForSigning()).
			throw new ApiException("KeySupplier: the .key(supplier) on domain '" + domainName(authzDomain)
					+ "' provides a " + supplied.getClass().getName() + ", which is not an IKeyRealm — the framework "
					+ "cannot sign with it. Supply an IKeyRealm for framework signing, or take over token production "
					+ "(shape + signature) with a custom .authenticator().authorization(issuer, \"method\").");
		}
		IDomainAuthenticatorAuthorizationKeyDefinition keyConfig = keyConfig(authzDomain);
		if (keyConfig != null && keyConfig.keyDomain() != null) {
			Persisted p = resolvePersisted(authzDomain, keyConfig, request, signingCaller);
			IReflection reflection = DefaultMapper.reflection();
			return new Signing(
					SecurityExpressions.materializeKeyRealm(p.entity(), p.keyDef(), reflection),
					SecurityExpressions.keySignerId(p.keyDomain(), p.entity(), reflection));
		}
		throw new ApiException("KeySupplier: domain '" + domainName(authzDomain)
				+ "' declares a signable authorization but neither .key(supplier) nor .key(domain) was configured");
	}

	/** Resolved persisted key: the @Key entity plus its domain/definition. */
	protected record Persisted(Object entity, IDomain<?> keyDomain, IDomainKeyDefinition keyDef) {
	}

	protected Persisted resolvePersisted(IDomain<?> authzDomain,
			IDomainAuthenticatorAuthorizationKeyDefinition keyConfig, IOperationRequest request) {
		return resolvePersisted(authzDomain, keyConfig, request, null);
	}

	/**
	 * The core persisted-key business rules: scope → lookup → policy → create/rotate.
	 * The realm scope is computed from {@code signingCaller} when provided (the
	 * authenticated principal on the sign path), else from the request caller.
	 */
	protected Persisted resolvePersisted(IDomain<?> authzDomain,
			IDomainAuthenticatorAuthorizationKeyDefinition keyConfig, IOperationRequest request, ICaller signingCaller) {
		IDomain<?> keyDomain = SecurityExpressions.resolveKeyDomain(authzDomain, keyConfig);
		if (keyDomain == null) {
			throw new ApiException("KeySupplier: the configured .key(domain) entity class '"
					+ keyConfig.keyDomain().getClass().getName() + "' did not resolve to a registered domain");
		}
		IDomainKeyDefinition keyEntDef = keyDomain.getDomainDefinition().keyDefinition();
		if (keyEntDef == null) {
			throw new ApiException("KeySupplier: key domain '" + keyDomain.getDomainName()
					+ "' is not marked as a @Key domain");
		}

		ICaller caller = signingCaller != null ? signingCaller : SecurityExpressions.extractCaller(request);
		String realmName = buildRealmName(keyConfig.usage(), caller, keyDomain.getDomainName());

		IFilter filter = Filter.eq(keyEntDef.name().toString(), realmName);
		List<Object> existing;
		try {
			existing = SecurityExpressions.invokeReadAll(keyDomain, filter);
		} catch (Exception e) {
			throw new ApiException("KeySupplier: failed to query key domain '" + keyDomain.getDomainName()
					+ "' for realmName '" + realmName + "': " + e.getMessage(), e);
		}
		IReflection reflection = DefaultMapper.reflection();
		boolean hadExistingButUnusable = false;
		if (existing != null && !existing.isEmpty()) {
			Object entity = pickUsable(existing, keyEntDef, reflection);
			if (entity != null) {
				return new Persisted(entity, keyDomain, keyEntDef);
			}
			hadExistingButUnusable = true;
		}

		// Policy gates: autoRotate (unusable key) / autoGenerate (nothing in storage).
		if (hadExistingButUnusable && !keyConfig.autoRotate()) {
			throw new ApiException("KeySupplier: the only key on domain '" + keyDomain.getDomainName()
					+ "' matching realmName '" + realmName + "' is expired or revoked, and .autoRotate(false) "
					+ "was configured. Rotate out of band, or enable .autoRotate(true).");
		}
		if (!hadExistingButUnusable && !keyConfig.autoGenerate()) {
			throw new ApiException("KeySupplier: no key found on domain '" + keyDomain.getDomainName()
					+ "' for realmName '" + realmName + "', and .autoGenerate(false) was configured. Seed the key "
					+ "out of band, or enable .autoGenerate(true).");
		}

		Object newEntity = SecurityExpressions.generateAndStampKeyEntity(
				keyDomain.getEntityClass(), keyEntDef,
				keyConfig.algorithm(), keyConfig.signatureAlgorithm(),
				realmName, keyConfig.duration(), keyConfig.unit(), reflection);
		SecurityExpressions.stampIdentityAndTenancy(newEntity, keyDomain, keyConfig.usage(), caller, reflection);
		try {
			SecurityExpressions.invokeCreate(keyDomain, newEntity);
		} catch (Exception e) {
			throw new ApiException("KeySupplier: failed to persist freshly-generated key on domain '"
					+ keyDomain.getDomainName() + "' for realmName '" + realmName + "': " + e.getMessage(), e);
		}
		return new Persisted(newEntity, keyDomain, keyEntDef);
	}

	/**
	 * Mode A: a user {@code .key(supplier)} — returns the supplier's key object
	 * (ANY shape, not necessarily an IKeyRealm), or null when no supplier is
	 * configured. The framework does not impose its IKeyRealm shape here.
	 */
	protected final Object trySupplierMode(IDomain<?> authzDomain) {
		var authzAuthDef = authzAuthDef(authzDomain);
		if (authzAuthDef == null) {
			return null;
		}
		var supplierBuilder = authzAuthDef.keyRealm();
		if (supplierBuilder == null) {
			return null;
		}
		try {
			ISupplier<?> supplier = supplierBuilder.build();
			Optional<?> keyOpt = supplier.supply();
			return keyOpt.orElseThrow(() -> new ApiException("KeySupplier: key supplier returned empty for domain '"
					+ domainName(authzDomain) + "'"));
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("KeySupplier: failed to obtain key from supplier: " + e.getMessage(), e);
		}
	}

	// ───────────────────────── scope + usability rules ─────────────────────────

	protected final String buildRealmName(AuthenticatorKeyUsage usage, ICaller caller, String keyDomainName) {
		String base = keyDomainName != null ? keyDomainName : "key";
		if (usage == null || usage == AuthenticatorKeyUsage.oneForAll) {
			return base + ":global";
		}
		String tenant = caller.tenantId() != null ? caller.tenantId() : "anonymous";
		return switch (usage) {
			case oneForAll -> base + ":global";
			case oneForTenant -> base + ":tenant:" + tenant;
			case oneForEach -> {
				String scoped = caller.ownerId() != null ? caller.ownerId()
						: (caller.callerId() != null ? caller.callerId() : "anonymous");
				yield base + ":caller:" + tenant + ":" + scoped;
			}
		};
	}

	protected final Object pickUsable(List<Object> candidates, IDomainKeyDefinition keyEntDef, IReflection reflection) {
		for (Object entity : candidates) {
			if (keyEntDef.revoked() != null) {
				Object revoked = reflection.getFieldValue(entity, keyEntDef.revoked().toString());
				if (Boolean.TRUE.equals(revoked)) continue;
			}
			if (keyEntDef.expiration() != null) {
				Object exp = reflection.getFieldValue(entity, keyEntDef.expiration().toString());
				if (exp != null && isExpired(exp)) continue;
			}
			return entity;
		}
		return null;
	}

	protected static boolean isExpired(Object value) {
		long now = System.currentTimeMillis();
		if (value instanceof java.util.Date d) return d.getTime() <= now;
		if (value instanceof java.time.Instant i) return i.toEpochMilli() <= now;
		if (value instanceof Long l) return l <= now;
		return false;
	}

	// ───────────────────────── definition navigation ─────────────────────────

	private com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition authzAuthDef(IDomain<?> authzDomain) {
		DomainDefinition<?> domDef = toDomainDefinition(authzDomain);
		if (domDef == null) return null;
		var secDef = domDef.domainSecurityDefinition();
		if (secDef == null || secDef.authenticatorDefinition() == null) return null;
		return secDef.authenticatorDefinition().authorizationDefinition();
	}

	private com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationDefinition requireAuthzAuthDef(
			IDomain<?> authzDomain) {
		var authzAuthDef = authzAuthDef(authzDomain);
		if (authzAuthDef == null) {
			throw new ApiException("resolveKeyRealm: no authenticator authorization configured on domain '"
					+ domainName(authzDomain) + "'");
		}
		return authzAuthDef;
	}

	protected final IDomainAuthenticatorAuthorizationKeyDefinition keyConfig(IDomain<?> authzDomain) {
		var authzAuthDef = authzAuthDef(authzDomain);
		return authzAuthDef != null ? authzAuthDef.keyDefinition() : null;
	}

	protected static String domainName(IDomain<?> domain) {
		return domain != null ? domain.getDomainName() : "<null>";
	}

	// ───────────────────────── supplier surface ─────────────────────────

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
		log.trace("Entering {}.supply", getClass().getSimpleName());
		if (context == null) {
			throw new SupplyException("IRuntimeContext cannot be null");
		}
		Optional<?> domainOpt = context.getVariable("domainContext", IClass.getClass(IDomain.class));
		if (domainOpt.isEmpty()) {
			throw new SupplyException("Variable 'domainContext' not found in runtime context");
		}
		IDomain<?> authzDomain = (IDomain<?>) domainOpt.get();
		IOperationRequest request = (IOperationRequest) context
				.getVariable("request", IClass.getClass(IOperationRequest.class)).orElse(null);
		try {
			return Optional.ofNullable(resolveKey(authzDomain, request));
		} catch (Exception e) {
			throw new SupplyException("KeySupplier: failed to resolve the key: " + e.getMessage(), e);
		}
	}

	/** Helper for callers holding an Optional/raw operation request. */
	protected static IOperationRequest asRequest(Object request) {
		Object r = unwrapOptional(request);
		return (r instanceof IOperationRequest req) ? req : null;
	}

}
