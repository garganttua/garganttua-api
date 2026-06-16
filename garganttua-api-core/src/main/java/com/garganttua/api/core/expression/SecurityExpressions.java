package com.garganttua.api.core.expression;
import com.garganttua.core.reflection.annotations.Reflected;

import java.security.KeyPair;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.domain.DomainDefinition;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.caller.OwnerIds;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IAuthenticationDefinition;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.definition.IDomainKeyDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.operation.OperationType;
import com.garganttua.api.commons.Pluralizer;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.crypto.CryptoException;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.IKeyAlgorithm;
import com.garganttua.core.crypto.IKeyRealm;
import com.garganttua.core.crypto.Key;
import com.garganttua.core.crypto.KeyAlgorithm;
import com.garganttua.core.crypto.KeyRealm;
import com.garganttua.core.crypto.KeyType;
import com.garganttua.core.crypto.SignatureAlgorithm;
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IContextualMethodBinder;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.IMethodReturn;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.runtime.RuntimeExpressionContext;
import com.garganttua.core.supply.ISupplier;

import jakarta.annotation.Nullable;

import static com.garganttua.api.core.expression.ExpressionUtils.*;

/**
 * Expressions for security: access control, authentication, and authorization.
 */
@Reflected(queryAllPublicMethods = true)
public class SecurityExpressions {

	// ── Framework-internal pipeline invocations on the key/authorization domains ──
	// These run DURING authentication, before any caller token exists. They use
	// access=anonymous + authority=false operations so the security stages
	// short-circuit (no token/tenant/owner/authority enforcement) while the full
	// business pipeline runs (validation, persistence, lifecycle hooks). No
	// skipStages filtering → the domain's precompiled workflow is reused (no
	// per-invocation recompile on the auth hot path). The CALLER is chosen to make
	// the pipeline behave correctly: a create mirrors the entity's own tenant/owner
	// (so ensureTenantId/ensureOwnerId are idempotent), a lookup is a super caller
	// with NO requested tenant (RepositoryFilterTools returns the explicit filter
	// unchanged → that filter is the sole scope).

	public static IApi apiOf(IDomain<?> domain) {
		return (domain instanceof Domain<?> d) ? d.getApiContext() : null;
	}

	public static String readField(Object entity, ObjectAddress addr) {
		if (addr == null) {
			return null;
		}
		Object v = DefaultMapper.reflection().getFieldValue(entity, addr.toString());
		return v != null ? v.toString() : null;
	}

	private static boolean readBoolean(Object entity, ObjectAddress addr) {
		if (addr == null) {
			return false;
		}
		Object v = DefaultMapper.reflection().getFieldValue(entity, addr.toString());
		return Boolean.TRUE.equals(v);
	}

	/** Caller for an internal CREATE: mirrors the entity's own tenant + owner so CREATE_ONE's ensure-stamping is idempotent. */
	private static ICaller callerFromEntity(IDomain<?> target, Object entity) {
		String tenantId = readField(entity, target.getTenantIdFieldAddress());
		ObjectAddress ownedAddr = target.getDomainDefinition().owned();
		String ownerId = ownedAddr != null ? readField(entity, ownedAddr) : null;
		if (tenantId == null) {
			return Caller.createAnonymousCaller();
		}
		return Caller.createTenantCallerWithOwnerId(tenantId, ownerId);
	}

	/** Caller for an internal lookup (readAll): super + NO requested tenant → RepositoryFilterTools returns the supplied filter unchanged (the explicit filter is the sole scope). */
	private static ICaller lookupCaller(IDomain<?> target) {
		IApi api = apiOf(target);
		String superTenantId = api != null ? api.getSuperTenantId() : null;
		if (superTenantId == null || superTenantId.isBlank()) {
			return Caller.createAnonymousCaller();
		}
		return new Caller(superTenantId, null, null, null, true, true, null);
	}

	/**
	 * Well-known request arg flagging a framework-internal pipeline write (a write
	 * issued by {@link #invokeInternal}, e.g. the authenticate/refresh token persist
	 * or a key auto-create) as opposed to a client-issued CRUD call. Set server-side
	 * only — never read from the wire — so it cannot be forged by a caller. Read by
	 * {@code requireNotDirectAuthorizationCreate} to let the framework mint a signable
	 * authorization while rejecting a direct external create.
	 */
	public static final String FRAMEWORK_INTERNAL_WRITE_ARG = "_frameworkInternalWrite";

	private static Object invokeInternal(IDomain<?> target, OperationDefinition op, ICaller caller,
			java.util.function.Consumer<com.garganttua.api.core.service.OperationRequest> setup) {
		com.garganttua.api.core.service.OperationRequest req =
				new com.garganttua.api.core.service.OperationRequest(new java.util.HashMap<>());
		req.arg(IOperationRequest.OPERATION, op);
		req.arg(IOperationRequest.TENANT_ID, caller.tenantId());
		req.arg(IOperationRequest.REQUESTED_TENANT_ID, caller.requestedTenantId());
		req.arg(IOperationRequest.CALLER_ID, caller.callerId());
		req.arg(IOperationRequest.OWNER_ID, caller.ownerId());
		req.arg(IOperationRequest.SUPER_TENANT, caller.superTenant());
		req.arg(IOperationRequest.SUPER_OWNER, caller.superOwner());
		req.arg(FRAMEWORK_INTERNAL_WRITE_ARG, Boolean.TRUE);
		setup.accept(req);
		IOperationResponse response = target.invoke(req);
		OperationResponseCode code = response.getResponseCode();
		if (code != OperationResponseCode.OK && code != OperationResponseCode.CREATED
				&& code != OperationResponseCode.UPDATED && code != OperationResponseCode.DELETED) {
			Throwable cause = response.getException().orElse(null);
			throw new ApiException("Internal pipeline invocation (" + op.technicalOperation()
					+ ") on domain '" + target.getDomainName() + "' returned " + code, cause);
		}
		return response.getResponse();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object invokeCreate(IDomain<?> target, Object entity) {
		OperationDefinition op = OperationDefinition.createOne(target.getDomainName(),
				((IDomain) target).getEntityClass(), false, null, Access.anonymous);
		return invokeInternal(target, op, callerFromEntity(target, entity), req -> req.arg("entity", entity));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<Object> invokeReadAll(IDomain<?> target, @Nullable IFilter filter) {
		OperationDefinition op = OperationDefinition.readAll(target.getDomainName(),
				((IDomain) target).getEntityClass(), false, null, Access.anonymous);
		Object body = invokeInternal(target, op, lookupCaller(target), req -> {
			if (filter != null) {
				req.arg(IOperationRequest.FILTER, filter);
			}
		});
		if (body instanceof List<?> list) {
			return (List<Object>) list;
		}
		return body == null ? List.of() : List.of(body);
	}

	@Expression(name = "operationAccess", description = "Returns the Access level string from an OperationDefinition")
	public static String operationAccess(@Nullable Object operation) {
		if (operation == null) return "anonymous";
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return "anonymous";
		return opDef.access() != null ? opDef.access().name() : "anonymous";
	}

	@Expression(name = "shouldSkipAuthorization",
			description = "True when VERIFY_AUTHORIZATION can short-circuit. An operation is verified when it is "
					+ "non-anonymous, OR it is anonymous but a token was actually presented (Mode A raw header or "
					+ "Mode B pre-decoded authorization) — that is OPTIONAL authentication: an anonymous op honours a "
					+ "valid token (identity persists) and rejects an invalid one (401). Skips when anonymous with no "
					+ "token. authenticate / refresh ops always skip: they carry their own credentials in the body.")
	public static boolean shouldSkipAuthorization(@Nullable Object request) {
		IOperationRequest req = (unwrapOptional(request) instanceof IOperationRequest r) ? r : null;
		if (req == null) {
			return true;
		}
		OperationDefinition op = req.arg(IOperationRequest.OPERATION).orElse(null);
		if (op == null) {
			return true;
		}
		// authenticate / refresh validate their own token (presented in the body); never gate them here.
		if (op.type() == OperationType.authentication || op.type() == OperationType.refreshAuthorization) {
			return true;
		}
		// A non-anonymous operation always verifies.
		if (op.access() != Access.anonymous) {
			return false;
		}
		// Anonymous: skip only when NO token was presented; a presented token is verified (→ 401 if invalid).
		boolean preDecoded = req.arg(IOperationRequest.AUTHORIZATION).orElse(null) != null;
		String raw = AuthorizationProtocolExpressions
				.rawAuthorizationAsString(req.arg(IOperationRequest.RAW_AUTHORIZATION).orElse(null));
		boolean rawPresent = raw != null && !raw.isBlank();
		return !(preDecoded || rawPresent);
	}

	@Expression(name = "operationAuthority", description = "Returns whether the operation requires an authority check")
	public static boolean operationAuthority(Object operation) {
		if (operation == null) return false;
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return false;
		return opDef.authority();
	}

	@Expression(name = "operationAuthorityName",
			description = "Returns the authority name enforced for the operation: the explicit name configured via "
					+ "authority(String), or the auto-generated default <domain>:<operation> when only authority(true) "
					+ "was set. Returns null when no authority is required.")
	public static @Nullable String operationAuthorityName(@Nullable Object operation) {
		if (operation == null) return null;
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return null;
		return opDef.effectiveAuthorityName();
	}

	@Expression(name = "callerHasAuthority",
			description = "Returns true when the caller carries an authority equal to the supplied name. "
					+ "Safe: returns false when caller is null, has no authorities, or the name is blank. "
					+ "Super-tenant / super-owner status does NOT bypass the check — being super grants "
					+ "cross-tenant / cross-owner reach, not the authority to perform an operation; a super "
					+ "caller must still carry the required authority.")
	public static boolean callerHasAuthority(@Nullable Object caller, @Nullable Object authorityName) {
		ICaller c = (ICaller) unwrapOptional(caller);
		if (c == null) return false;
		Object name = unwrapOptional(authorityName);
		if (!(name instanceof String authority) || authority.isBlank()) return false;
		java.util.List<String> authorities = c.authorities();
		return authorities != null && authorities.contains(authority);
	}

	@Expression(name = "guardSuperStatusOnWrite",
			description = "Before persisting a tenant/owner entity, rejects a LOCKED promotion to super-tenant / "
					+ "super-owner. No-op unless: the domain is a tenant (resp. owner) carrying a superTenant "
					+ "(resp. superOwner) field, that flag is true on the entity, the entity's uuid is NOT already "
					+ "registered as super, and the matching creation lock is on. Returns the entity unchanged.")
	public static Object guardSuperStatusOnWrite(@Nullable Object entity, @Nullable Object domainContext) {
		IDomain<?> domain = toDomain(domainContext);
		Object e = unwrapOptional(entity);
		if (domain == null || e == null) return entity;
		IApi api = apiOf(domain);
		if (api == null) return entity;
		var def = domain.getDomainDefinition();
		if (def == null) return entity;
		String uuid = readField(e, domain.getEntityDefinition().uuid());
		if (domain.isTenantEntity() && def.superTenant() != null
				&& readBoolean(e, def.superTenant())
				&& !api.isSuperTenant(uuid) && api.isSuperTenantCreationLocked()) {
			throw new ApiException("Super-tenant creation is locked: cannot promote tenant '" + uuid
					+ "' to super-tenant at runtime. Only the startup scan and the auto-created master tenant may "
					+ "seed super-tenants. Call .lockSuperTenantCreation(false) on the ApiBuilder to allow runtime promotion.");
		}
		if (def.owner() != null && def.superOwner() != null
				&& readBoolean(e, def.superOwner())
				&& !api.isSuperOwner(uuid) && api.isSuperOwnerCreationLocked()) {
			throw new ApiException("Super-owner creation is locked: cannot promote owner '" + uuid
					+ "' to super-owner at runtime. Call .lockSuperOwnerCreation(false) on the ApiBuilder to allow it.");
		}
		return entity;
	}

	@Expression(name = "syncSuperStatusRegistry",
			description = "After persisting a tenant/owner entity, maintains the server-side super registries: "
					+ "registers the entity's uuid when its superTenant/superOwner flag is true, unregisters it "
					+ "(demotion) when false. No-op for non-tenant/owner domains. Returns the entity unchanged.")
	public static Object syncSuperStatusRegistry(@Nullable Object entity, @Nullable Object domainContext) {
		IDomain<?> domain = toDomain(domainContext);
		Object e = unwrapOptional(entity);
		if (domain == null || e == null) return entity;
		IApi api = apiOf(domain);
		if (api == null) return entity;
		var def = domain.getDomainDefinition();
		if (def == null) return entity;
		String uuid = readField(e, domain.getEntityDefinition().uuid());
		if (uuid == null) return entity;
		if (domain.isTenantEntity() && def.superTenant() != null) {
			if (readBoolean(e, def.superTenant())) api.registerSuperTenant(uuid);
			else api.unregisterSuperTenant(uuid);
		}
		if (def.owner() != null && def.superOwner() != null) {
			if (readBoolean(e, def.superOwner())) api.registerSuperOwner(uuid);
			else api.unregisterSuperOwner(uuid);
		}
		return entity;
	}

	@Expression(name = "reconcileCaller",
			description = "Folds the (untrusted) protocol caller into the verified, trusted IAuthentication. "
					+ "Default (R1-R3): IAuthentication.reconcile — the token's identity (tenant/owner/super) wins over "
					+ "the headers; a header contradicting a non-super token is rejected (403); then the super flags are "
					+ "recomputed from the server registries on the resolved home (authoritative). Custom: when "
					+ ".authorization().reconcile(supplier,\"method\") is declared, that method owns caller resolution "
					+ "entirely (no registry super-recompute) — enabling self-contained tokens whose super status comes "
					+ "from signed claims. Returns the protocol caller unchanged when there is no authentication.")
	public static ICaller reconcileCaller(@Nullable Object authResult, @Nullable Object protocolCaller,
			@Nullable Object request, @Nullable Object domainContext, @Nullable Object apiContext) {
		Object auth = unwrapOptional(authResult);
		ICaller caller = (ICaller) unwrapOptional(protocolCaller);
		if (!(auth instanceof IAuthentication authentication)) {
			return caller;
		}

		// Custom reconcile method (default-or-custom): when .authorization().reconcile(supplier,
		// "method") is declared, delegate caller resolution to it. Its params — (IAuthentication,
		// ICaller) — are resolved from the runtime context by their suppliers, so we publish the
		// authentication / request first (mirrors the mint-side issuer). Otherwise: default R1-R3.
		IMethodBinder<?> binder = resolveReconcileBinder(domainContext);
		if (binder != null) {
			IDomain<?> dc = toDomain(domainContext);
			Object req = unwrapOptional(request);
			IOperationRequest opReq = (req instanceof IOperationRequest r) ? r : null;
			IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
			if (runtimeCtx != null) {
				runtimeCtx.setVariable("authentication", authentication);
				if (dc != null) {
					runtimeCtx.setVariable("domainContext", dc);
				}
				if (opReq != null) {
					runtimeCtx.setVariable("request", opReq);
				}
			}
			Optional<? extends IMethodReturn<?>> result;
			if (binder instanceof IContextualMethodBinder<?, ?> contextualBinder) {
				result = ((IContextualMethodBinder<?, Object>) contextualBinder).execute(runtimeCtx);
			} else {
				result = binder.execute();
			}
			Object resolved = result.isPresent() ? result.get().single() : null;
			if (!(resolved instanceof ICaller resolvedCaller)) {
				throw new ApiException("reconcileCaller: the custom reconcile method must return an ICaller");
			}
			// Custom reconcile owns the result entirely — trust its super flags (self-contained).
			return resolvedCaller;
		}
		// Default R1-R3, then the server-authoritative super-recompute on the resolved home.
		ICaller reconciled = authentication.reconcile(caller);
		return applyServerAuthoritativeSuperStatus(reconciled, apiContext);
	}

	private static IMethodBinder<?> resolveReconcileBinder(Object domainContext) {
		Object defObj = authorizationDefinition(domainContext);
		return (defObj instanceof IDomainAuthorizationDefinition def) ? def.reconcileBinder() : null;
	}

	@Expression(name = "applyServerAuthoritativeSuperStatus",
			description = "Recomputes the caller's superTenant/superOwner flags from the server-side registries "
					+ "(membership of the caller's tenantId / ownerId), OVERRIDING whatever the protocol claimed, and "
					+ "returns a corrected ICaller. The verify script stores it back as the request's 'caller' arg so "
					+ "all downstream stages (filtering, authority, update) trust the registry, not the token.")
	public static ICaller applyServerAuthoritativeSuperStatus(@Nullable Object caller, @Nullable Object apiContext) {
		ICaller c = (ICaller) unwrapOptional(caller);
		if (c == null) return null;
		Object ctx = unwrapOptional(apiContext);
		if (!(ctx instanceof IApi api)) return c;
		boolean superTenant = api.isSuperTenant(c.tenantId());
		boolean superOwner = api.isSuperOwner(c.ownerId());
		if (superTenant == c.superTenant() && superOwner == c.superOwner()) {
			return c;
		}
		return new Caller(c.tenantId(), c.requestedTenantId(), c.callerId(), c.ownerId(),
				superTenant, superOwner, c.authorities());
	}

	@Expression(name = "isSecurityDisabled", description = "Returns true if the domain has security disabled")
	public static boolean isSecurityDisabled(Object context) {
		IDomain<?> dc = toDomain(context);
		DomainDefinition<?> domDef = toDomainDefinition(dc);
		if (domDef != null) {
			return domDef.domainSecurityDefinition() == null || domDef.domainSecurityDefinition().disabled();
		}
		return true;
	}

	@Expression(name = "requireAuthentication", description = "Checks that the caller has been authenticated (authorization present in request)")
	public static boolean requireAuthentication(@Nullable Object request) {
		IOperationRequest opRequest = (IOperationRequest) request;
		Optional<Object> authorization = opRequest.arg(IOperationRequest.AUTHORIZATION);
		if (authorization.isEmpty()) {
			throw new ApiException("Authentication required but no authorization token provided");
		}
		return true;
	}

	@Expression(name = "requireTenantId", description = "Checks that the caller has a tenantId set")
	public static boolean requireTenantId(@Nullable Object caller) {
		ICaller c = (ICaller) unwrapOptional(caller);
		if (c == null || c.requestedTenantId() == null) {
			throw new ApiException("Tenant ID is required for this operation");
		}
		return true;
	}

	@Expression(name = "requireOwnerId", description = "Checks that the caller has an ownerId set")
	public static boolean requireOwnerId(@Nullable Object caller) {
		ICaller c = (ICaller) unwrapOptional(caller);
		if (c == null || c.ownerId() == null) {
			throw new ApiException("Owner ID is required for this operation");
		}
		return true;
	}

	@Expression(name = "callerHasTenantId", description = "Returns true if the caller has a non-null requestedTenantId (safe, never throws)")
	public static boolean callerHasTenantId(@Nullable Object caller) {
		ICaller c = (ICaller) unwrapOptional(caller);
		return c != null && c.requestedTenantId() != null;
	}

	@Expression(name = "callerHasOwnerId", description = "Returns true if the caller has a non-null ownerId (safe, never throws)")
	public static boolean callerHasOwnerId(@Nullable Object caller) {
		ICaller c = (ICaller) unwrapOptional(caller);
		return c != null && c.ownerId() != null;
	}

	@Expression(name = "authRequestLogin", description = "Extracts the login from an IAuthenticationRequest (safe, never throws)")
	public static @Nullable Object authRequestLogin(@Nullable Object entity) {
		Object unwrapped = unwrapOptional(entity);
		if (unwrapped instanceof IAuthenticationRequest req) {
			return req.login();
		}
		return null;
	}

	@Expression(name = "requireCallerTenantForScope", description = "For a tenant-scoped authenticator, requires the caller to carry a tenantId (taken from the caller — over HTTP, the X-Tenant-Id header — never from the AuthenticationRequest body). Throws a parlant ApiException naming what is missing; no-op for non-tenant scopes.")
	public static boolean requireCallerTenantForScope(@Nullable Object operationRequest, @Nullable Object scope) {
		Object scopeUnwrapped = unwrapOptional(scope);
		String scopeName = scopeUnwrapped == null ? null : scopeUnwrapped.toString();
		if (!"tenant".equals(scopeName)) {
			return true;
		}
		IOperationRequest req = (IOperationRequest) unwrapOptional(operationRequest);
		String tenantId = req == null ? null : req.arg(IOperationRequest.TENANT_ID).orElse(null);
		if (tenantId == null || tenantId.isBlank()) {
			throw new ApiException("Tenant-scoped authentication requires the caller's tenant. "
					+ "Provide it on the caller.");
		}
		return true;
	}

	@Expression(name = "isTenantIdMandatory",
			description = "Always false since the token-authoritative redesign: there is no Access.tenant gate. "
					+ "Tenant isolation is folded into IAuthentication.reconcile (the verified token carries the "
					+ "caller's tenant) and the repository filter; a tenantId is never required on the caller based on "
					+ "the access level.")
	public static boolean isTenantIdMandatory(Object operation, Object context) {
		return false;
	}

	@Expression(name = "isOwnerIdMandatory",
			description = "Always false since the token-authoritative redesign: there is no Access.owner gate. "
					+ "Owner isolation is folded into reconcile + the repository filter.")
	public static boolean isOwnerIdMandatory(Object operation, Object context) {
		return false;
	}

	@Expression(name = "authenticatorContext", description = "Returns the IAuthenticatorDefinition from the domain's security definition")
	public static IAuthenticatorDefinition authenticatorContext(Object context) {
		IDomain<?> dc = toDomain(context);
		DomainDefinition<?> domDef = toDomainDefinition(dc);
		if (domDef != null) {
			var secDef = domDef.domainSecurityDefinition();
			if (secDef != null) {
				return secDef.authenticatorDefinition();
			}
		}
		return null;
	}

	@Expression(name = "applySecurityOnEntity",
			description = "Runs the authenticator's custom applySecurityOnEntity method(s) on the entity being "
					+ "created/updated (after validation, before persist) — e.g. hashing a password. Each configured "
					+ "authentication's binder receives the CURRENT entity (SecuredEntitySupplier) and may mutate it in "
					+ "place or return a secured entity (both honored, chained). No-op when the domain is not an "
					+ "authenticator or declares no applySecurityOnEntity. Returns the (possibly secured) entity.")
	public static Object applySecurityOnEntity(@Nullable Object entityObj, @Nullable Object domainContextObj,
			@Nullable Object request) {
		Object entity = unwrapOptional(entityObj);
		if (entity == null) {
			return entity;
		}
		IAuthenticatorDefinition authDef = authenticatorContext(domainContextObj);
		if (authDef == null || authDef.authenticationDefinitions() == null) {
			return entity;
		}
		IDomain<?> domain = toDomain(domainContextObj);
		Object req = unwrapOptional(request);
		IOperationRequest opReq = (req instanceof IOperationRequest r) ? r : null;

		for (IAuthenticationDefinition auth : authDef.authenticationDefinitions()) {
			if (auth == null) {
				continue;
			}
			IMethodBinder<?> binder = auth.applySecurityOnEntityMethodBinder();
			if (binder == null) {
				continue;
			}
			IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
			if (runtimeCtx != null) {
				runtimeCtx.setVariable("entity", entity);
				if (domain != null) {
					runtimeCtx.setVariable("domainContext", domain);
				}
				if (opReq != null) {
					runtimeCtx.setVariable("request", opReq);
				}
			}
			Optional<? extends IMethodReturn<?>> result;
			if (binder instanceof IContextualMethodBinder<?, ?> contextualBinder) {
				result = ((IContextualMethodBinder<?, Object>) contextualBinder).execute(runtimeCtx);
			} else {
				result = binder.execute();
			}
			Object secured = result.isPresent() ? result.get().single() : null;
			if (secured != null) {
				entity = secured; // the method returned a secured entity; void/mutate keeps the same ref
			}
		}
		return entity;
	}

	@Expression(name = "authenticatorScope", description = "Returns the authenticator scope string from IAuthenticatorDefinition")
	public static String authenticatorScope(Object authContext) {
		if (authContext instanceof IAuthenticatorDefinition def) {
			return def.scope() != null ? def.scope().name() : null;
		}
		return null;
	}

	@Expression(name = "hasAuthorizationConfig", description = "Returns true if the authenticator has an authorization definition configured")
	public static boolean hasAuthorizationConfig(@Nullable Object authContextObj) {
		if (authContextObj instanceof IAuthenticatorDefinition def) {
			return def.authorizationDefinition() != null;
		}
		return false;
	}

	@Expression(name = "authorizationDefinition", description = "Returns the IDomainAuthorizationDefinition from the domain's security definition, resolving from the linked authorization domain if needed")
	public static @Nullable Object authorizationDefinition(@Nullable Object context) {
		IDomain<?> dc = toDomain(context);
		DomainDefinition<?> domDef = toDomainDefinition(dc);
		if (domDef != null) {
			var secDef = domDef.domainSecurityDefinition();
			if (secDef != null) {
				if (secDef.authorizationDefinition() != null) {
					return secDef.authorizationDefinition();
				}
				IDomain<?> authzDomain = resolveAuthorizationDomain(dc);
				if (authzDomain != null && authzDomain.getDomainDefinition() instanceof DomainDefinition<?> dd
						&& dd.domainSecurityDefinition() != null) {
					return dd.domainSecurityDefinition().authorizationDefinition();
				}
			}
		}
		return null;
	}

	/**
	 * Returns the authorization definition the domain carries <strong>itself</strong>
	 * (its own {@code .security().authorization()}), with <strong>no</strong> fallback
	 * to a linked authorization domain. This is the token domain's own view — distinct
	 * from {@link #authorizationDefinition(Object)}, whose fallback makes an
	 * <em>authenticator</em> domain (which merely references a token domain) report the
	 * token's definition. CRUD-write guards must key on the OWN definition: a {@code users}
	 * authenticator that references a signable token domain is NOT itself a signable
	 * authorization and its CRUD writes must stay unguarded.
	 */
	public static @Nullable Object ownAuthorizationDefinition(@Nullable Object context) {
		IDomain<?> dc = toDomain(context);
		DomainDefinition<?> domDef = toDomainDefinition(dc);
		if (domDef != null && domDef.domainSecurityDefinition() != null) {
			return domDef.domainSecurityDefinition().authorizationDefinition();
		}
		return null;
	}

	@Expression(name = "isOwnAuthorizationSignable",
			description = "True when the domain ITSELF is a signable authorization (its own .security().authorization().signable()), "
					+ "with NO fallback to a linked token domain. Used by the CRUD-write guards so that an authenticator domain "
					+ "referencing a signable token domain is not mistaken for one — only the token domain itself is guarded.")
	public static boolean isOwnAuthorizationSignable(@Nullable Object domainContext) {
		Object def = ownAuthorizationDefinition(domainContext);
		return def instanceof IDomainAuthorizationDefinition d && d.signable();
	}

	@Expression(name = "createAuthorizationEntity", description = "Creates a new authorization entity with fields populated from authentication result, principal uuid and tenant id")
	public static Object createAuthorizationEntity(@Nullable Object authorizationDefObj,
			@Nullable Object authenticationResult, @Nullable Object domainContextObj,
			@Nullable Object principalUuid, @Nullable Object tenantId) {
		if (authorizationDefObj == null || authenticationResult == null) {
			throw new ApiException("createAuthorizationEntity: authorizationDef and authenticationResult are required");
		}

		IDomainAuthorizationDefinition authzDef = (IDomainAuthorizationDefinition) authorizationDefObj;
		IAuthentication authResult = (IAuthentication) authenticationResult;
		IDomain<?> authenticatorDomain = toDomain(domainContextObj);
		IAuthenticatorDefinition authDef = null;
		DomainDefinition<?> domDef = toDomainDefinition(authenticatorDomain);
		if (domDef != null) {
			var secDef = domDef.domainSecurityDefinition();
			if (secDef != null) {
				authDef = secDef.authenticatorDefinition();
			}
		}
		IReflection reflection = DefaultMapper.reflection();

		try {
			IDomain<?> authzDomain = resolveAuthorizationDomain(authenticatorDomain);
			if (authzDomain == null) {
				throw new ApiException("createAuthorizationEntity: authorization domain not configured");
			}
			if (!authzDomain.isOwnedEntity()) {
				throw new ApiException("Authorization domain '" + authzDomain.getDomainName()
						+ "' must be owned (use .owned(field) on the domain builder)");
			}

			Object entity = authzDomain.getEntityClass().getConstructor().newInstance();

			ObjectAddress uuidAddress = authzDomain.getEntityDefinition().uuid();
			if (uuidAddress != null) {
				reflection.setFieldValue(entity, uuidAddress.toString(),
						com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch().toString());
			}

			// The authorization is owned by the authenticated principal. Store the
			// owner id qualified with the principal's domain (${domainName}:${uuid})
			// so the value is self-describing and consistent with every other
			// ownerId in the framework. The Caller derived from this token (via
			// IProtocol.getCaller reading this field) and the repository owner
			// filter both carry the qualified form, so ownership comparisons match.
			ObjectAddress ownedField = authzDomain.getDomainDefinition().owned();
			if (ownedField != null && principalUuid != null) {
				reflection.setFieldValue(entity, ownedField,
						OwnerIds.qualify(authenticatorDomain.getDomainName(), principalUuid.toString()));
			}

			ObjectAddress tenantField = authzDomain.getTenantIdFieldAddress();
			if (tenantField != null && tenantId != null) {
				reflection.setFieldValue(entity, tenantField, tenantId);
			}

			if (authzDef.type() != null && authResult.authorization() != null) {
				reflection.setFieldValue(entity, authzDef.type(), authResult.authorization());
			}
			if (authzDef.authorities() != null && authResult.authorities() != null) {
				reflection.setFieldValue(entity, authzDef.authorities(), authResult.authorities());
			}
			if (authzDef.creation() != null) {
				reflection.setFieldValue(entity, authzDef.creation(), java.time.Instant.now());
			}
			if (authzDef.expiration() != null && authDef.authorizationDefinition() != null) {
				var authzAuthDef = authDef.authorizationDefinition();
				if (authzAuthDef.unit() != null && authzAuthDef.duration() > 0) {
					long millis = authzAuthDef.unit().toMillis(authzAuthDef.duration());
					reflection.setFieldValue(entity, authzDef.expiration(), java.time.Instant.now().plusMillis(millis));
				}
			}
			if (authzDef.revoked() != null) {
				reflection.setFieldValue(entity, authzDef.revoked(), false);
			}

			// Refresh-token fields — populated when the authorization is refreshable.
			// The expiration window comes from the authenticator's authorization def
			// ({@code refreshLifeTime}). The revoked flag starts at false so the
			// authorization domain's repository can later flip it to invalidate.
			if (authzDef.refreshable()) {
				if (authzDef.refreshExpiration() != null && authDef.authorizationDefinition() != null) {
					var authzAuthDef = authDef.authorizationDefinition();
					if (authzAuthDef.refreshUnit() != null && authzAuthDef.refreshDuration() > 0) {
						long millis = authzAuthDef.refreshUnit().toMillis(authzAuthDef.refreshDuration());
						reflection.setFieldValue(entity, authzDef.refreshExpiration(),
								java.time.Instant.now().plusMillis(millis));
					}
				}
				if (authzDef.refreshRevoked() != null) {
					reflection.setFieldValue(entity, authzDef.refreshRevoked(), false);
				}
			}

			return entity;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to create authorization entity: " + e.getMessage(), e);
		}
	}

	@Expression(name = "lookupValidAuthorization",
			description = "Looks up a valid (non-expired, non-revoked) authorization owned by the principal "
					+ "via a direct repository query on the authorization domain. Bypasses the workflow on "
					+ "purpose: this is a framework-internal lookup, not user-triggered traffic, so the "
					+ "authorization pipeline (which expects a caller-supplied token) does not apply.")
	public static @Nullable Object lookupValidAuthorization(@Nullable Object authorizationDefObj,
			@Nullable Object domainContextObj, @Nullable Object principalUuid, @Nullable Object tenantId) {
		if (authorizationDefObj == null || domainContextObj == null || principalUuid == null) {
			return null;
		}
		try {
			IDomainAuthorizationDefinition authzDef = (IDomainAuthorizationDefinition) authorizationDefObj;
			IDomain<?> authenticatorDomain = toDomain(domainContextObj);

			IDomain<?> authzDomain = resolveAuthorizationDomain(authenticatorDomain);
			if (authzDomain == null) return null;

			java.util.ArrayList<IFilter> filters = new java.util.ArrayList<>();

			// The owned field is stored qualified (${domainName}:${uuid}); query by
			// the same qualified form so the reuse lookup matches what
			// createAuthorizationEntity wrote.
			ObjectAddress ownedField = authzDomain.getDomainDefinition().owned();
			if (ownedField != null) {
				filters.add(Filter.eq(ownedField.toString(),
						OwnerIds.qualify(authenticatorDomain.getDomainName(), principalUuid.toString())));
			}

			if (tenantId != null) {
				ObjectAddress tenantField = authzDomain.getTenantIdFieldAddress();
				if (tenantField != null) {
					filters.add(Filter.eq(tenantField.toString(), tenantId));
				}
			}

			if (authzDef.revoked() != null) {
				filters.add(Filter.eq(authzDef.revoked().toString(), false));
			}

			if (authzDef.expiration() != null) {
				filters.add(Filter.gt(authzDef.expiration().toString(), java.time.Instant.now()));
			}

			IFilter combinedFilter = filters.isEmpty() ? null
					: filters.size() == 1 ? filters.get(0)
					: Filter.and(filters.toArray(new Filter[0]));

			// Run the authorization domain's readAll pipeline (security neutralised
			// via anonymous access; the explicit filter above is the sole scope).
			List<Object> results = invokeReadAll(authzDomain, combinedFilter);
			return (results != null && !results.isEmpty()) ? results.get(0) : null;
		} catch (Exception e) {
			return null;
		}
	}

	static IDomain<?> resolveAuthorizationDomain(IDomain<?> authenticatorDomain) {
		DomainDefinition<?> domDef = toDomainDefinition(authenticatorDomain);
		if (domDef != null) {
			var secDef = domDef.domainSecurityDefinition();
			if (secDef != null && secDef.authenticatorDefinition() != null
					&& secDef.authenticatorDefinition().authorizationDefinition() != null
					&& secDef.authenticatorDefinition().authorizationDefinition().authorizationDomainBuilder() != null) {
				if (authenticatorDomain instanceof Domain<?> domCtx) {
					IApi apiContext = domCtx.getApiContext();
					if (apiContext != null) {
						try {
							var builder = secDef.authenticatorDefinition().authorizationDefinition().authorizationDomainBuilder();
							String simpleName = builder.getEntityClass().getSimpleName();
							String domainName = Pluralizer.toPlural(simpleName.toLowerCase());
							return apiContext.getDomain(domainName).orElse(null);
						} catch (Exception e) {
							return null;
						}
					}
				}
			}
		}
		return null;
	}

	@Expression(name = "createAuthorizationEntity2", description = "Creates an authorization entity from an authentication result and domain context")
	public static Object createAuthorizationEntity2(@Nullable Object authResultObj, @Nullable Object domainContextObj) {
		if (authResultObj == null || domainContextObj == null) {
			throw new ApiException("createAuthorizationEntity2: authResult and domainContext are required");
		}

		IAuthentication authResult = (IAuthentication) authResultObj;
		IDomain<?> authenticatorDomain = toDomain(domainContextObj);
		String principalUuid = readPrincipalUuid(authResult, authenticatorDomain);
		String tenantId = readPrincipalTenantId(authResult, authenticatorDomain);

		IDomainAuthorizationDefinition authzDef = (IDomainAuthorizationDefinition) authorizationDefinition(authenticatorDomain);

		return createAuthorizationEntity(authzDef, authResult, authenticatorDomain, principalUuid, tenantId);
	}

	@Expression(name = "issueAuthorization",
			description = "Produces the authorization (token) after a successful authentication — the mint-side "
					+ "entry point used by CREATE_AUTHORIZATION. When a custom issuer method is declared via "
					+ ".authenticator().authorization(issuer, \"method\").withParam(...), delegates token production "
					+ "(shape + signature) to that bound method — enabling custom tokens or delegation to an external "
					+ "authorization server (Keycloak/OAuth2). The method's params are resolved from the runtime context "
					+ "(authentication result, domainContext, request) by the same supplier mechanism as the verify-side "
					+ "authenticate method. Otherwise runs the framework's standard minting: build the entity from the "
					+ "auth result, then sign it. Persistence + transport encoding still run AROUND this in the script.")
	public static Object issueAuthorization(@Nullable Object authResultObj, @Nullable Object domainContextObj,
			@Nullable Object request) {
		Object authResultUnwrapped = unwrapOptional(authResultObj);
		if (!(authResultUnwrapped instanceof IAuthentication authResult)) {
			throw new ApiException("issueAuthorization: a successful IAuthentication result is required");
		}
		IDomain<?> authenticatorDomain = toDomain(domainContextObj);
		if (authenticatorDomain == null) {
			throw new ApiException("issueAuthorization: domain context is required");
		}

		IMethodBinder<?> issuerBinder = resolveIssuerBinder(authenticatorDomain);

		// Custom issuer method: the user owns token production (shape + signing).
		// Declared on the authenticator (.authenticator().authorization(issuer, "method")),
		// the mint-side dual of the verify-side authenticate. The bound method's
		// params are resolved from the runtime context by their suppliers, so we
		// publish authentication / domainContext / request first.
		if (issuerBinder != null) {
			Object req = unwrapOptional(request);
			IOperationRequest opReq = (req instanceof IOperationRequest r) ? r : null;
			IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
			if (runtimeCtx != null) {
				runtimeCtx.setVariable("authentication", authResult);
				runtimeCtx.setVariable("domainContext", authenticatorDomain);
				if (opReq != null) {
					runtimeCtx.setVariable("request", opReq);
				}
			}
			Optional<? extends IMethodReturn<?>> result;
			if (issuerBinder instanceof IContextualMethodBinder<?, ?> contextualBinder) {
				result = ((IContextualMethodBinder<?, Object>) contextualBinder).execute(runtimeCtx);
			} else {
				result = issuerBinder.execute();
			}
			Object token = result.isPresent() ? result.get().single() : null;
			if (token == null) {
				throw new ApiException("issueAuthorization: the custom issuer method returned no authorization");
			}
			return token;
		}

		// Default: the framework is the authorization server — build + sign.
		Object entity = createAuthorizationEntity2(authResultObj, domainContextObj);
		signIfSignable(entity, domainContextObj, request);
		return entity;
	}

	/**
	 * The custom token-production (mint) binder declared on the authenticator via
	 * {@code .authorization(issuer, "method")}. {@code null} when none is declared
	 * (the framework then mints with its standard build + sign).
	 */
	private static IMethodBinder<?> resolveIssuerBinder(IDomain<?> authenticatorDomain) {
		IAuthenticatorDefinition authDef = authenticatorContext(authenticatorDomain);
		return authDef != null ? authDef.authorizationMethodBinder() : null;
	}

	/**
	 * Returns the storable authorization currently valid for this principal in
	 * the linked authorization domain, or {@code null} when the token is not
	 * storable or no reusable entry exists. CREATE_AUTHORIZATION.gs branches on
	 * the result to skip the sign + persist round when reuse is possible.
	 *
	 * <p>The lookup filter (built by {@link #lookupValidAuthorization}) matches
	 * the authzDef's ownerId + tenantId + revoked=false + expiration&gt;NOW. An
	 * expired token is treated as absent — the script then mints a fresh one.
	 */
	@Expression(name = "findReusableAuthorization",
			description = "Looks up an existing valid (non-expired, non-revoked) authorization owned by the "
					+ "principal in the linked authorization domain when the authorization is storable. "
					+ "Returns the entity if found, or null when not storable / none reusable. Used by "
					+ "CREATE_AUTHORIZATION to skip create + sign + persist on the reuse path.")
	public static @Nullable Object findReusableAuthorization(@Nullable Object domainContextObj,
			@Nullable Object authResultObj) {
		if (domainContextObj == null || authResultObj == null) return null;
		if (!(authResultObj instanceof IAuthentication authResult)) return null;
		IDomain<?> authenticatorDomain = toDomain(domainContextObj);
		Object defObj = authorizationDefinition(authenticatorDomain);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || !authzDef.storable()) {
			return null;
		}
		String principalUuid = readPrincipalUuid(authResult, authenticatorDomain);
		if (principalUuid == null) return null;
		String tenantId = readPrincipalTenantId(authResult, authenticatorDomain);
		return lookupValidAuthorization(authzDef, authenticatorDomain, principalUuid, tenantId);
	}

	/**
	 * Catch-handler companion in CREATE_AUTHORIZATION.gs that fires on the reuse
	 * path (when {@link #findReusableAuthorization} returned a non-null entity
	 * and the {@code requirePresent(if(isNull(@output),1))} guard throws to
	 * short-circuit the fresh-create block). Encodes the reused authorization
	 * to its transport form (if an encode method is configured) and publishes
	 * it on the request as {@code encodedAuthorization}, so downstream stages
	 * see the same wire shape they get on a fresh token.
	 */
	@Expression(name = "publishReusedAuthorization",
			description = "Catch-handler companion in CREATE_AUTHORIZATION. Runs when findReusableAuthorization "
					+ "returned a non-null entity. Encodes the reused authorization to its transport form "
					+ "(if an encode method is configured) and publishes it on the request as "
					+ "'encodedAuthorization' so downstream stages see the same wire shape as a fresh token.")
	public static boolean publishReusedAuthorization(@Nullable Object authzEntity,
			@Nullable Object domainContextObj, @Nullable Object request) {
		if (authzEntity == null || domainContextObj == null) return false;
		Object encoded = encodeIfPossible(authzEntity, domainContextObj);
		if (request instanceof IOperationRequest opReq && encoded != null) {
			opReq.arg("encodedAuthorization", encoded);
		}
		return true;
	}

	@Expression(name = "encodeReusedIfPresent",
			description = "Reuse-path companion in CREATE_AUTHORIZATION. When a reusable authorization entity is "
					+ "present, encodes it to its transport form (if an encode method is configured), publishes that "
					+ "wire form on the request as 'encodedAuthorization', and RETURNS the encoded form (or the entity "
					+ "when no encode method) so it becomes the operation output. Returns null untouched when there is "
					+ "no reusable entity, so the fresh-create branch runs.")
	public static @Nullable Object encodeReusedIfPresent(@Nullable Object authzEntity,
			@Nullable Object domainContextObj, @Nullable Object request) {
		if (authzEntity == null || domainContextObj == null) {
			return null;
		}
		Object encoded = encodeIfPossible(authzEntity, domainContextObj);
		if (request instanceof IOperationRequest opReq && encoded != null) {
			opReq.arg("encodedAuthorization", encoded);
		}
		return encoded != null ? encoded : authzEntity;
	}

	private static @Nullable String readPrincipalUuid(IAuthentication authResult, IDomain<?> authenticatorDomain) {
		Object principal = authResult.principal();
		if (principal == null || authenticatorDomain.getEntityDefinition() == null) return null;
		ObjectAddress uuidAddr = authenticatorDomain.getEntityDefinition().uuid();
		if (uuidAddr == null) return null;
		try {
			Object val = DefaultMapper.reflection().getFieldValue(principal, uuidAddr.toString());
			return val != null ? val.toString() : null;
		} catch (Exception e) {
			return null;
		}
	}

	private static @Nullable String readPrincipalTenantId(IAuthentication authResult, IDomain<?> authenticatorDomain) {
		Object principal = authResult.principal();
		if (principal == null) return null;
		ObjectAddress tenantAddr = authenticatorDomain.getTenantIdFieldAddress();
		if (tenantAddr == null) return null;
		try {
			Object val = DefaultMapper.reflection().getFieldValue(principal, tenantAddr.toString());
			return val != null ? val.toString() : null;
		} catch (Exception e) {
			return null;
		}
	}


	@Expression(name = "authResultPrincipal", description = "Extracts the principal from an IAuthentication result")
	public static Object authResultPrincipal(@Nullable Object authResult) {
		if (authResult instanceof IAuthentication auth) {
			return auth.principal();
		}
		return null;
	}

	@Expression(name = "setRequestArg", description = "Sets a named argument on the operation request")
	public static boolean setRequestArg(@Nullable Object request, @Nullable Object key, @Nullable Object value) {
		if (request == null || key == null) return false;
		IOperationRequest opRequest = (IOperationRequest) request;
		opRequest.arg(key.toString(), value);
		return true;
	}

	/**
	 * Well-known request arg under which {@link #recordCaughtException} stashes
	 * the exception object. Domain.doInvoke reads it back to surface the exact
	 * type + message on OperationResponse failures rather than the generic
	 * fallback wording.
	 */
	public static final String LAST_EXCEPTION_ARG = "_lastException";

	@Expression(name = "recordCaughtException",
			description = "Catch-handler companion for the script's `! => recordCaughtException(@0, @exception) -> CODE` pattern. Stores the throwable bound to `@exception` on the operation request under the well-known '_lastException' key, so Domain.doInvoke can surface the exact exception type + message on the OperationResponse instead of falling back to a synthesised wording. Returns true on success; never throws (a broken catch handler must not turn a captured error into a SERVER_ERROR).")
	public static boolean recordCaughtException(@Nullable Object request, @Nullable Object exception) {
		if (!(request instanceof IOperationRequest opRequest)) return false;
		Object unwrapped = unwrapOptional(exception);
		if (!(unwrapped instanceof Throwable t)) return false;
		opRequest.arg(LAST_EXCEPTION_ARG, t);
		return true;
	}

	@Expression(name = "requireNotDirectAuthorizationCreate",
			description = "CREATE_ONE guard for authorization domains. A SIGNABLE authorization may only be minted by the "
					+ "framework's authenticate/refresh pipeline, which persists it ALREADY SIGNED (CREATE_AUTHORIZATION / "
					+ "REFRESH_AUTHORIZATION → persistIfStorable → invokeInternal). A direct client CRUD create is rejected: "
					+ "a caller cannot produce a valid signature, so it would store an unsigned/forgeable token. No-op for "
					+ "ordinary domains and non-signable authorizations; passes for framework-internal writes (recognised by "
					+ "the server-set FRAMEWORK_INTERNAL_WRITE marker, never read from the wire). Throws (→ 403) otherwise.")
	public static boolean requireNotDirectAuthorizationCreate(@Nullable Object entity, @Nullable Object domainContext,
			@Nullable Object request) {
		if (!isOwnAuthorizationSignable(domainContext)) {
			return true;
		}
		IOperationRequest req = (unwrapOptional(request) instanceof IOperationRequest r) ? r : null;
		boolean frameworkInternal = req != null
				&& Boolean.TRUE.equals(req.arg(FRAMEWORK_INTERNAL_WRITE_ARG).orElse(null));
		if (frameworkInternal) {
			return true;
		}
		throw new ApiException("A signable authorization cannot be created directly. It is issued (and signed) "
				+ "by the authentication pipeline — obtain it through authentication or token refresh.");
	}

	@Expression(name = "authorizationSignedPayload",
			description = "Returns the SIGNED payload of a signable authorization entity — the bytes its getDataToSign "
					+ "method produces, base64-encoded — or null when the domain is not a signable authorization (or no "
					+ "getDataToSign / entity). Used by UPDATE_ONE to capture the pre-update signed material so a mutation "
					+ "that would invalidate the signature can be detected without resolving the signing key.")
	public static @Nullable String authorizationSignedPayload(@Nullable Object entity, @Nullable Object domainContext) {
		Object defObj = ownAuthorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || !authzDef.signable()) {
			return null;
		}
		ObjectAddress dataMethod = authzDef.getDataToSignMethod();
		Object e = unwrapOptional(entity);
		if (dataMethod == null || e == null) {
			return null;
		}
		byte[] data = DefaultMapper.reflection().invokeMethod(e, dataMethod.toString(), IClass.getClass(byte[].class));
		return data == null ? null : java.util.Base64.getEncoder().encodeToString(data);
	}

	@Expression(name = "requireSignedPayloadUnchanged",
			description = "UPDATE_ONE guard for signable authorizations: a signed token's signed material is immutable. "
					+ "Compares the pre-update signed payload (captured via authorizationSignedPayload before the merge) "
					+ "with the merged entity's. Equal (e.g. only the revoked flag — which getDataToSign does not cover — "
					+ "changed) passes, so revocation stays allowed; different throws (→ 400), because the change would "
					+ "invalidate the stored signature. No-op when the pre-update payload is null (non-signable domain).")
	public static boolean requireSignedPayloadUnchanged(@Nullable Object beforePayload, @Nullable Object mergedEntity,
			@Nullable Object domainContext) {
		Object before = unwrapOptional(beforePayload);
		if (!(before instanceof String beforeStr)) {
			return true;
		}
		String after = authorizationSignedPayload(mergedEntity, domainContext);
		if (beforeStr.equals(after)) {
			return true;
		}
		throw new ApiException("A signed authorization is immutable: this update changes a field covered by the "
				+ "signature, which would invalidate it. Only non-signed fields (e.g. revocation) may be updated.");
	}

	@Expression(name = "isAuthorizationStorable", description = "Returns true if the authorization definition has storable=true")
	public static boolean isAuthorizationStorable(@Nullable Object authorizationDefObj) {
		if (authorizationDefObj instanceof IDomainAuthorizationDefinition def) {
			return def.storable();
		}
		return false;
	}

	@Expression(name = "persistIfStorable",
			description = "Persists the freshly-issued authorization entity to the linked authorization domain's repository when the resolved authorization definition has storable=true. No-op otherwise.")
	public static boolean persistIfStorable(@Nullable Object authzEntity, @Nullable Object authenticatorDomain) {
		if (authzEntity == null || authenticatorDomain == null) {
			throw new ApiException("persistIfStorable: entity and authenticatorDomain are required");
		}
		Object defObj = authorizationDefinition(authenticatorDomain);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || !authzDef.storable()) {
			return true;
		}
		IDomain<?> authzDomain = resolveAuthorizationDomain(toDomain(authenticatorDomain));
		if (authzDomain == null) {
			throw new ApiException("persistIfStorable: storable authorization but no authorization domain linked");
		}
		try {
			invokeCreate(authzDomain, authzEntity);
			return true;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("persistIfStorable: failed to save authorization: " + e.getMessage(), e);
		}
	}

	@Expression(name = "isAuthorizationSignable",
			description = "Returns true if the resolved authorization definition is configured as signable")
	public static boolean isAuthorizationSignable(@Nullable Object domainContext) {
		Object def = authorizationDefinition(domainContext);
		return def instanceof IDomainAuthorizationDefinition d && d.signable();
	}

	@Expression(name = "resolveKeyRealm",
			description = "Resolves an IKeyRealm for sign/verify. Two modes: (1) supplier — uses the user-provided ISupplierBuilder<IKeyRealm> declared via .key(supplier); (2) persisted — looks up or auto-creates a key entity on the domain declared via .key(domain), scoped by AuthenticatorKeyUsage (oneForAll / oneForTenant / oneForEach). The optional operationRequest argument carries the caller used to scope the realmName in persisted mode.")
	public static IKeyRealm resolveKeyRealm(@Nullable Object domainContext, @Nullable Object operationRequest) {
		return resolveKeyRealmAndSigner(domainContext, operationRequest).realm();
	}

	@Expression(name = "resolveSigningKey",
			description = "Resolves the KEY OBJECT that SIGNED a given authorization, by reading its signedBy field. "
					+ "Returns Object, not IKeyRealm: the framework does NOT impose its IKeyRealm shape — the key is "
					+ "whatever the user declared. Persisted-key mode (signedBy = ${keyDomain}:${uuid}): returns that "
					+ "EXACT @Key entity (robust to key rotation: the token verifies against the key that actually "
					+ "signed it), and REFUSES it when that key is revoked or expired. Supplier mode (signedBy = realm "
					+ "name, or no signedBy stamped): returns the object the configured .key(supplier) provides. Powers "
					+ "DomainKeySupplier; the user's verify method casts it to their own key type and extracts the "
					+ "verification material however they defined it.")
	public static Object resolveSigningKey(@Nullable Object authzEntity, @Nullable Object domainContext,
			@Nullable Object operationRequest) {
		Object token = unwrapOptional(authzEntity);
		IDomain<?> authzDomain = toDomain(domainContext);
		if (token == null || authzDomain == null) {
			throw new ApiException("resolveSigningKey: authorization entity and domain context are required");
		}
		IOperationRequest req = (unwrapOptional(operationRequest) instanceof IOperationRequest r) ? r : null;
		// Single source of truth: DomainKeySupplier (extends KeySupplier) resolves
		// the EXACT key from signedBy, falling back to KeySupplier's current-key rules.
		return new com.garganttua.api.core.security.key.DomainKeySupplier()
				.resolveKeyForToken(authzDomain, token, req);
	}

	/**
	 * A resolved key realm together with the qualified id of its signer, used to
	 * stamp an authorization's {@code signedBy} field. In persisted-key mode the
	 * signer id is {@code ${keyDomainName}:${keyUuid}} (the @Key entity backing
	 * the realm); in supplier mode there is no key entity, so the realm name is
	 * used instead.
	 */
	private record ResolvedKeyRealm(IKeyRealm realm, String signerId) {
	}

	// The key business rules (scope / autocreate / rotation) live in KeySupplier
	// — single source of truth. The sign path asks it for a materialized signing
	// realm + the qualified signer id to stamp on the token.
	private static ResolvedKeyRealm resolveKeyRealmAndSigner(@Nullable Object domainContext, @Nullable Object operationRequest) {
		return resolveKeyRealmAndSigner(domainContext, operationRequest, null);
	}

	private static ResolvedKeyRealm resolveKeyRealmAndSigner(@Nullable Object domainContext, @Nullable Object operationRequest,
			@Nullable ICaller signingCaller) {
		IDomain<?> authzDomain = toDomain(domainContext);
		IOperationRequest req = (unwrapOptional(operationRequest) instanceof IOperationRequest r) ? r : null;
		com.garganttua.api.core.security.key.KeySupplier.Signing s =
				new com.garganttua.api.core.security.key.KeySupplier().resolveSigning(authzDomain, req, signingCaller);
		return new ResolvedKeyRealm(s.realm(), s.signerId());
	}

	/**
	 * The identity a freshly-minted token's signing key is scoped to: the authenticated
	 * PRINCIPAL, read off the token itself (its {@code owned} + tenantId fields, set by
	 * {@link #createAuthorizationEntity}). This is what {@code oneForEach} keys on, so the
	 * realm is {@code …:caller:<tenant>:<principal-ownerId>} instead of the anonymous
	 * login request's {@code …:caller:anonymous:anonymous}. Returns null when neither
	 * field is populated (then the request caller is used, preserving legacy behaviour).
	 */
	private static @Nullable ICaller signingPrincipalCaller(@Nullable Object token, @Nullable IDomain<?> domainContext) {
		Object entity = unwrapOptional(token);
		if (entity == null || domainContext == null) {
			return null;
		}
		IDomain<?> tokenDomain = resolveAuthorizationDomain(domainContext);
		if (tokenDomain == null) {
			tokenDomain = domainContext;
		}
		ObjectAddress ownedAddr = tokenDomain.getDomainDefinition() != null
				? tokenDomain.getDomainDefinition().owned() : null;
		ObjectAddress tenantAddr = tokenDomain.getTenantIdFieldAddress();
		String ownerId = ownedAddr != null ? readField(entity, ownedAddr) : null;
		String tenantId = tenantAddr != null ? readField(entity, tenantAddr) : null;
		if (ownerId == null && tenantId == null) {
			return null;
		}
		// Preserve ownerId even when tenantId is null (non-tenant mode): the principal
		// is identified by its (qualified) ownerId.
		return new Caller(tenantId, tenantId, null, ownerId, false, false, null);
	}

	/**
	 * Builds the qualified signer id ({@code ${keyDomainName}:${keyUuid}}) for a
	 * persisted key entity, falling back to the bare key domain name when the
	 * entity carries no uuid.
	 */
	public static String keySignerId(IDomain<?> keyDomain, Object keyEntity, IReflection reflection) {
		ObjectAddress uuidAddr = keyDomain.getEntityDefinition() != null
				? keyDomain.getEntityDefinition().uuid() : null;
		if (uuidAddr != null) {
			Object v = reflection.getFieldValue(keyEntity, uuidAddr.toString());
			if (v != null) {
				return OwnerIds.qualify(keyDomain.getDomainName(), v.toString());
			}
		}
		return keyDomain.getDomainName();
	}

	// (resolvePersistedKeyRealm / buildRealmName / pickUsable moved to KeySupplier — single source of key business rules)

	public static IDomain<?> resolveKeyDomain(IDomain<?> authenticatorDomain,
			com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition keyConfig) {
		if (!(authenticatorDomain instanceof Domain<?> domCtx)) return null;
		IApi apiContext = domCtx.getApiContext();
		if (apiContext == null) return null;
		try {
			IClass<?> entityClass = keyConfig.keyDomain().getEntityClass();
			String domainName = Pluralizer.toPlural(entityClass.getSimpleName().toLowerCase());
			return apiContext.getDomain(domainName).orElse(null);
		} catch (Exception e) {
			return null;
		}
	}

	public static ICaller extractCaller(@Nullable Object operationRequest) {
		Object unwrapped = unwrapOptional(operationRequest);
		if (unwrapped instanceof IOperationRequest req) {
			ICaller caller = req.caller();
			if (caller != null) return caller;
		}
		return Caller.createAnonymousCaller();
	}

	private static boolean isExpired(Object value) {
		long now = System.currentTimeMillis();
		if (value instanceof java.util.Date d) return d.getTime() <= now;
		if (value instanceof java.time.Instant i) return i.toEpochMilli() <= now;
		if (value instanceof Long l) return l <= now;
		return false;
	}

	public static void stampIdentityAndTenancy(Object entity, IDomain<?> keyDomain,
			com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage usage,
			ICaller caller, IReflection reflection) {
		ObjectAddress uuidAddr = keyDomain.getEntityDefinition() != null
				? keyDomain.getEntityDefinition().uuid() : null;
		if (uuidAddr != null) {
			reflection.setFieldValue(entity, uuidAddr.toString(),
					com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch().toString());
		}

		ObjectAddress tenantAddr = keyDomain.getTenantIdFieldAddress();
		if (tenantAddr != null) {
			String stampedTenant = usage == com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage.oneForAll
					? null : caller.tenantId();
			if (stampedTenant != null) {
				reflection.setFieldValue(entity, tenantAddr, stampedTenant);
			}
		}

		ObjectAddress ownedAddr = keyDomain.getDomainDefinition() != null
				? keyDomain.getDomainDefinition().owned() : null;
		if (ownedAddr != null && usage == com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage.oneForEach
				&& caller.ownerId() != null) {
			reflection.setFieldValue(entity, ownedAddr, caller.ownerId());
		}
	}

	@Expression(name = "signAuthorization",
			description = "Signs an authorization entity by invoking its getDataToSign method, signing with keyRealm.getKeyForSigning(), and writing the signature back into the configured signature field.")
	public static boolean signAuthorization(@Nullable Object authzEntity, @Nullable Object domainContext, @Nullable Object keyRealmObj) {
		if (authzEntity == null || domainContext == null || keyRealmObj == null) {
			throw new ApiException("signAuthorization: entity, domainContext and keyRealm are required");
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || !authzDef.signable()) {
			throw new ApiException("signAuthorization: authorization is not signable on the resolved domain");
		}
		ObjectAddress dataMethod = authzDef.getDataToSignMethod();
		ObjectAddress sigField = authzDef.signatureField();
		if (dataMethod == null) {
			throw new ApiException("signAuthorization: signable authorization has no getDataToSign method configured");
		}
		if (sigField == null) {
			throw new ApiException("signAuthorization: signable authorization has no signature field configured");
		}
		IKeyRealm realm = (IKeyRealm) unwrapOptional(keyRealmObj);
		if (realm == null) {
			throw new ApiException("signAuthorization: keyRealm is null");
		}
		try {
			IReflection reflection = DefaultMapper.reflection();
			byte[] data = reflection.invokeMethod(
					authzEntity,
					dataMethod.toString(),
					IClass.getClass(byte[].class));
			if (data == null) {
				throw new ApiException("signAuthorization: getDataToSign returned null");
			}
			IKey key = realm.getKeyForSigning();
			byte[] signature = key.sign(data);
			reflection.setFieldValue(authzEntity, sigField, signature);
			return true;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("signAuthorization failed: " + e.getMessage(), e);
		}
	}

	@Expression(name = "signIfSignable",
			description = "If the resolved authorization is signable, resolves the key realm (supplier or persisted) and signs the entity. The operationRequest argument is forwarded to resolveKeyRealm to scope the persisted-mode realmName by caller. No-op when not signable. Throws when signable but no key is configured.")
	public static boolean signIfSignable(@Nullable Object authzEntity, @Nullable Object domainContext, @Nullable Object operationRequest) {
		if (authzEntity == null || domainContext == null) {
			throw new ApiException("signIfSignable: entity and domainContext are required");
		}
		if (!isAuthorizationSignable(domainContext)) {
			return true;
		}
		// Scope the signing key on the authenticated PRINCIPAL (read off the token),
		// not the anonymous login request — so oneForEach mints one key per principal.
		ICaller principalCaller = signingPrincipalCaller(authzEntity, toDomain(domainContext));
		ResolvedKeyRealm resolved = resolveKeyRealmAndSigner(domainContext, operationRequest, principalCaller);
		boolean signed = signAuthorization(authzEntity, domainContext, resolved.realm());
		stampSignedBy(authzEntity, domainContext, resolved.signerId());
		return signed;
	}

	/**
	 * Records who signed an authorization in its configured {@code signedBy}
	 * field (no-op when no such field is configured). The signer id follows the
	 * {@code ${domainName}:${id}} rule — see {@link #keySignerId}.
	 */
	private static void stampSignedBy(@Nullable Object authzEntity, @Nullable Object domainContext, @Nullable String signerId) {
		if (authzEntity == null || signerId == null) {
			return;
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || authzDef.signedBy() == null) {
			return;
		}
		DefaultMapper.reflection().setFieldValue(authzEntity, authzDef.signedBy(), signerId);
	}

	@Expression(name = "verifyIfSignable",
			description = "If the resolved authorization is signable, resolves the key realm (supplier or persisted) and verifies the entity's signature. The operationRequest argument is forwarded to resolveKeyRealm to scope the persisted-mode realmName by caller. Returns true when not signable or signature valid; false on signature mismatch. Throws when signable but no key is configured.")
	public static boolean verifyIfSignable(@Nullable Object authzEntity, @Nullable Object domainContext, @Nullable Object operationRequest) {
		if (authzEntity == null || domainContext == null) {
			throw new ApiException("verifyIfSignable: entity and domainContext are required");
		}
		if (!isAuthorizationSignable(domainContext)) {
			return true;
		}
		IKeyRealm realm = resolveKeyRealm(domainContext, operationRequest);
		return verifyAuthorizationSignature(authzEntity, domainContext, realm);
	}

	@Expression(name = "verifyTokenSignature",
			description = "Framework-owned signature verification for a SELF-VERIFYING signable authorization (a token "
					+ "whose own domain is an authenticator). Resolves the EXACT key the token was signed with — by its "
					+ "qualified signedBy, via DomainKeySupplier (rotation-robust; refuses a revoked/expired signing key) "
					+ "— and verifies the entity's signature. Unlike verifyIfSignable (which reads the key config on the "
					+ "given domain), this works on the TOKEN domain where no key config is declared. Returns true when not "
					+ "signable; false on signature mismatch; throws (→ 401) when the token cannot be verified (no qualified "
					+ "signedBy, missing/revoked/expired key).")
	public static boolean verifyTokenSignature(@Nullable Object authzEntity, @Nullable Object domainContext, @Nullable Object operationRequest) {
		if (authzEntity == null || domainContext == null) {
			throw new ApiException("verifyTokenSignature: entity and domainContext are required");
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || !authzDef.signable()) {
			return true; // not signable → nothing to verify
		}
		IDomain<?> authzDomain = toDomain(domainContext);

		// Resolve the EXACT @Key the token was signed with — by its qualified signedBy
		// (the supplier's job; refuses a revoked/expired signing key). Fail-closed when
		// there is no qualified signedBy.
		var signer = new com.garganttua.api.core.security.key.DomainKeySupplier()
				.resolveSignerKey(authzDomain, authzEntity);
		if (signer == null) {
			throw new ApiException("verifyTokenSignature: cannot verify the token signature — it carries no qualified "
					+ "signedBy (${keyDomain}:${uuid}). A self-verifying signable authorization must be signed by a "
					+ "persisted @Key whose reference is stamped on the token.");
		}

		// Read the verification IKey DIRECTLY off the resolved @Key entity — the entity
		// already exposes it, so there is no realm to materialise. Verification is
		// read-only: the private signing material is never touched.
		IDomainKeyDefinition keyDef = signer.keyDomain().getDomainDefinition() != null
				? signer.keyDomain().getDomainDefinition().keyDefinition() : null;
		if (keyDef == null || keyDef.keyForSignatureVerification() == null) {
			throw new ApiException("verifyTokenSignature: key domain '" + signer.keyDomain().getDomainName()
					+ "' declares no @KeyForSignatureVerification field to verify the token against");
		}
		Object keyVal = DefaultMapper.reflection().getFieldValue(
				signer.entity(), keyDef.keyForSignatureVerification().toString());
		if (!(keyVal instanceof IKey verificationKey)) {
			throw new ApiException("verifyTokenSignature: the resolved @Key's verification field is not an IKey — got "
					+ (keyVal == null ? "null" : keyVal.getClass().getName()));
		}
		return verifyEntitySignatureWithKey(authzEntity, authzDef, verificationKey);
	}

	@Expression(name = "verifyAuthorizationSignature",
			description = "Verifies the signature on an authorization entity by invoking getDataToSign, reading the signature field, and calling keyRealm.getKeyForSignatureVerification().verifySignature. Returns true on valid signature, false on mismatch; throws on misconfiguration.")
	public static boolean verifyAuthorizationSignature(@Nullable Object authzEntity, @Nullable Object domainContext, @Nullable Object keyRealmObj) {
		if (authzEntity == null || domainContext == null || keyRealmObj == null) {
			throw new ApiException("verifyAuthorizationSignature: entity, domainContext and keyRealm are required");
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || !authzDef.signable()) {
			throw new ApiException("verifyAuthorizationSignature: authorization is not signable on the resolved domain");
		}
		IKeyRealm realm = (IKeyRealm) unwrapOptional(keyRealmObj);
		if (realm == null) {
			throw new ApiException("verifyAuthorizationSignature: keyRealm is null");
		}
		IKey key;
		try {
			key = realm.getKeyForSignatureVerification();
		} catch (Exception e) {
			throw new ApiException("verifyAuthorizationSignature failed: " + e.getMessage(), e);
		}
		return verifyEntitySignatureWithKey(authzEntity, authzDef, key);
	}

	/**
	 * Verifies an authorization entity's signature against a given verification key: invokes
	 * getDataToSign, reads the signature field, and calls {@code key.verifySignature}. Returns
	 * false on a signature mismatch or any crypto error (a tampered token is a 401, not a 500);
	 * throws an ApiException only on misconfiguration (no getDataToSign / no signature field /
	 * signature field empty or not a byte[]).
	 */
	private static boolean verifyEntitySignatureWithKey(Object authzEntity, IDomainAuthorizationDefinition authzDef, IKey verificationKey) {
		ObjectAddress dataMethod = authzDef.getDataToSignMethod();
		ObjectAddress sigField = authzDef.signatureField();
		if (dataMethod == null) {
			throw new ApiException("verifyAuthorizationSignature: signable authorization has no getDataToSign method configured");
		}
		if (sigField == null) {
			throw new ApiException("verifyAuthorizationSignature: signable authorization has no signature field configured");
		}
		IReflection reflection = DefaultMapper.reflection();
		byte[] data;
		byte[] signature;
		try {
			data = reflection.invokeMethod(authzEntity, dataMethod.toString(), IClass.getClass(byte[].class));
			if (data == null) {
				throw new ApiException("verifyAuthorizationSignature: getDataToSign returned null");
			}
			Object sigVal = reflection.getFieldValue(authzEntity, sigField.toString());
			if (!(sigVal instanceof byte[] sig)) {
				throw new ApiException("verifyAuthorizationSignature: signature field on authorization entity is empty or not a byte[]");
			}
			signature = sig;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("verifyAuthorizationSignature failed: " + e.getMessage(), e);
		}
		// Crypto errors during verification (malformed signature bytes, decoding failure,
		// key/algorithm mismatch) map to "signature invalid" (false), not a 500.
		try {
			return verificationKey.verifySignature(signature, data);
		} catch (Exception e) {
			return false;
		}
	}

	// ----- Encode authorization to transport-friendly form (Phase 3) -----

	@Expression(name = "hasEncodeMethod",
			description = "Returns true when the resolved authorization definition declares an encode method (set via .refreshable().encode(method) on the DSL).")
	public static boolean hasEncodeMethod(@Nullable Object domainContext) {
		Object def = authorizationDefinition(domainContext);
		return def instanceof IDomainAuthorizationDefinition d && d.encodeMethod() != null;
	}

	@Expression(name = "encodeAuthorization",
			description = "Invokes the user-declared encode method on an authorization entity and returns its result (typically a String for HTTP transport, or a byte[] for binary protocols). Return type is whatever the entity's method returns.")
	public static Object encodeAuthorization(@Nullable Object authzEntity, @Nullable Object domainContext) {
		if (authzEntity == null || domainContext == null) {
			throw new ApiException("encodeAuthorization: entity and domainContext are required");
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || authzDef.encodeMethod() == null) {
			throw new ApiException("encodeAuthorization: no encode method configured on authorization definition");
		}
		String methodName = authzDef.encodeMethod().toString();
		try {
			IReflection reflection = DefaultMapper.reflection();
			IClass<?> entityClass = IClass.getClass(authzEntity.getClass());
			com.garganttua.core.reflection.IMethod method = reflection.resolveMethod(entityClass, methodName)
					.orElseThrow(() -> new ApiException("encodeAuthorization: method '" + methodName
							+ "' not found on " + authzEntity.getClass().getName()));
			return method.invoke(authzEntity);
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("encodeAuthorization failed: " + e.getMessage(), e);
		}
	}

	@Expression(name = "encodeIfPossible",
			description = "If the resolved authorization declares an encode method, invokes it and returns the encoded form. Returns null when no encode method is configured (no-op for setups that ship the entity directly).")
	public static @Nullable Object encodeIfPossible(@Nullable Object authzEntity, @Nullable Object domainContext) {
		if (authzEntity == null || domainContext == null) {
			throw new ApiException("encodeIfPossible: entity and domainContext are required");
		}
		if (!hasEncodeMethod(domainContext)) {
			return null;
		}
		return encodeAuthorization(authzEntity, domainContext);
	}

	@Expression(name = "hasDecodeMethod",
			description = "Returns true when the resolved authorization definition declares a decode method (.authorization().decode(method) or @AuthorizationDecode).")
	public static boolean hasDecodeMethod(@Nullable Object domainContext) {
		Object def = authorizationDefinition(domainContext);
		return def instanceof IDomainAuthorizationDefinition d && d.decodeMethod() != null;
	}

	@Expression(name = "decodeAuthorizationEntity",
			description = "Reconstructs an authorization entity from its transport form (e.g. a JWT String / byte[]) "
					+ "using the configured decode method. Returns the value unchanged when it is already a decoded "
					+ "entity (not a String/byte[]) or when no decode method is configured. The decode method is "
					+ "invoked on a fresh entity instance with the raw bytes; it may populate the instance (void/returns "
					+ "this) or be a factory returning a new entity. Used by refresh/verify to accept an encoded token "
					+ "where the entity is expected.")
	public static @Nullable Object decodeAuthorizationEntity(@Nullable Object raw, @Nullable Object domainContext) {
		if (raw == null || domainContext == null) {
			return raw;
		}
		// Already a decoded entity (not a wire form) → nothing to do (Mode B).
		if (!(raw instanceof String) && !(raw instanceof byte[])) {
			return raw;
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef) || authzDef.decodeMethod() == null) {
			return raw; // no decode configured — leave the raw form for the caller to handle
		}
		IDomain<?> dc = toDomain(domainContext);
		IDomain<?> authzDomain = resolveAuthorizationDomain(dc);
		IClass<?> entityClass = authzDomain != null ? authzDomain.getEntityClass()
				: (dc != null ? dc.getEntityClass() : null);
		if (entityClass == null) {
			throw new ApiException("decodeAuthorizationEntity: cannot resolve the authorization entity class");
		}
		byte[] rawBytes = raw instanceof String s
				? s.getBytes(java.nio.charset.StandardCharsets.UTF_8) : (byte[]) raw;
		String methodName = authzDef.decodeMethod().toString();
		try {
			IReflection reflection = DefaultMapper.reflection();
			Object entity = entityClass.getConstructor().newInstance();
			com.garganttua.core.reflection.IMethod method = reflection.resolveMethod(entityClass, methodName)
					.orElseThrow(() -> new ApiException("decodeAuthorizationEntity: method '" + methodName
							+ "' not found on " + entityClass.getName()));
			Object result = method.invoke(entity, rawBytes);
			// Factory-style decode returns a fresh entity; populate-style returns void/this.
			if (result != null && entityClass.getType() instanceof Class<?> raw2 && raw2.isInstance(result)) {
				return result;
			}
			return entity;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("decodeAuthorizationEntity failed: " + e.getMessage(), e);
		}
	}

	@Expression(name = "predecodeRawAuthorization",
			description = "VERIFY_AUTHORIZATION pre-step (Mode A). When a raw Authorization header is present, no "
					+ "authorization is pre-decoded yet, and the domain declares a decode method, reconstructs the "
					+ "authorization entity from the header value via that decode method and sets it as the decoded "
					+ "authorization (so the rest of verify runs Mode B — signature + validation). No-op when already "
					+ "pre-decoded, no raw header, or no decode method (the scheme/protocol path then handles it).")
	public static boolean predecodeRawAuthorization(@Nullable Object operationRequest, @Nullable Object domainContext) {
		IOperationRequest req = (IOperationRequest) unwrapOptional(operationRequest);
		if (req == null) {
			return false;
		}
		if (req.arg(IOperationRequest.AUTHORIZATION).orElse(null) != null) {
			return false; // Mode B — caller already decoded
		}
		Object rawArg = req.arg(IOperationRequest.RAW_AUTHORIZATION).orElse(null);
		if (rawArg == null) {
			return false; // no raw header
		}
		if (!hasDecodeMethod(domainContext)) {
			return false; // no decode method — leave for the scheme/protocol path
		}
		String raw = AuthorizationProtocolExpressions.rawAuthorizationAsString(rawArg);
		String value = stripAuthorizationScheme(raw);
		Object entity = decodeAuthorizationEntity(value, domainContext);
		req.arg(IOperationRequest.AUTHORIZATION, entity);
		return true;
	}

	/** Strips a leading {@code <scheme> } (e.g. {@code Bearer }) from a raw header, returning the value. */
	private static @Nullable String stripAuthorizationScheme(@Nullable String raw) {
		if (raw == null) {
			return null;
		}
		String s = raw.strip();
		int sp = s.indexOf(' ');
		return sp > 0 ? s.substring(sp + 1).strip() : s;
	}

	// ----- Refresh authorization (Phase 2) -----

	@Expression(name = "isAuthorizationRefreshable",
			description = "Returns true if the resolved authorization definition is configured as refreshable (i.e. .refreshable() was called on its DSL).")
	public static boolean isAuthorizationRefreshable(@Nullable Object domainContext) {
		Object def = authorizationDefinition(domainContext);
		return def instanceof IDomainAuthorizationDefinition d && d.refreshable();
	}

	@Expression(name = "refreshNotRevoked",
			description = "Reads the refresh-revoked field on an authorization entity (the field declared by .refreshable().revokable(field)). Returns true if not revoked or no refresh-revoked field is configured. Returns false if the field reads true.")
	public static boolean refreshNotRevoked(@Nullable Object authzEntity, @Nullable Object domainContext) {
		if (authzEntity == null || domainContext == null) {
			throw new ApiException("refreshNotRevoked: entity and domainContext are required");
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef)) {
			throw new ApiException("refreshNotRevoked: authorization definition not resolved");
		}
		ObjectAddress refreshRevoked = authzDef.refreshRevoked();
		if (refreshRevoked == null) {
			// No refresh-revoked field configured — treat as never revoked.
			return true;
		}
		try {
			Object value = DefaultMapper.reflection().getFieldValue(authzEntity, refreshRevoked.toString());
			return !Boolean.TRUE.equals(value);
		} catch (Exception e) {
			throw new ApiException("refreshNotRevoked: failed to read refresh-revoked field: " + e.getMessage(), e);
		}
	}

	@Expression(name = "refreshNotExpired",
			description = "Reads the refresh-expiration Instant on an authorization entity (the field declared by .refreshable().expirable(field)). Returns true when the expiration is in the future or no field is configured. Returns false when the refresh has expired.")
	public static boolean refreshNotExpired(@Nullable Object authzEntity, @Nullable Object domainContext) {
		if (authzEntity == null || domainContext == null) {
			throw new ApiException("refreshNotExpired: entity and domainContext are required");
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef)) {
			throw new ApiException("refreshNotExpired: authorization definition not resolved");
		}
		ObjectAddress refreshExpiration = authzDef.refreshExpiration();
		if (refreshExpiration == null) {
			// No refresh-expiration field configured — treat as no expiration.
			return true;
		}
		try {
			Object value = DefaultMapper.reflection().getFieldValue(authzEntity, refreshExpiration.toString());
			if (!(value instanceof java.time.Instant exp)) {
				// Field present but null or wrong type — treat as expired (refuse).
				return false;
			}
			return exp.isAfter(java.time.Instant.now());
		} catch (Exception e) {
			throw new ApiException("refreshNotExpired: failed to read refresh-expiration field: " + e.getMessage(), e);
		}
	}

	@Expression(name = "findPrincipalByOwnerUuid",
			description = "Looks up the principal entity in the authenticator domain's repository using the ownerId stored on an existing authorization. Returns the entity or throws if absent.")
	public static Object findPrincipalByOwnerUuid(@Nullable Object authzEntity, @Nullable Object authenticatorDomain, @Nullable Object repositoryObj) {
		if (authzEntity == null || authenticatorDomain == null || repositoryObj == null) {
			throw new ApiException("findPrincipalByOwnerUuid: entity, authenticatorDomain and repository are required");
		}
		IDomain<?> domain = toDomain(authenticatorDomain);
		IRepository repo = (IRepository) repositoryObj;

		IDomain<?> authzDomain = resolveAuthorizationDomain(domain);
		if (authzDomain == null) {
			throw new ApiException("findPrincipalByOwnerUuid: no authorization domain linked to '"
					+ (domain != null ? domain.getDomainName() : "<null>") + "'");
		}
		ObjectAddress ownedField = authzDomain.getDomainDefinition().owned();
		if (ownedField == null) {
			throw new ApiException("findPrincipalByOwnerUuid: authorization domain '"
					+ authzDomain.getDomainName() + "' is not owned");
		}
		Object ownerUuid;
		try {
			ownerUuid = DefaultMapper.reflection().getFieldValue(authzEntity, ownedField.toString());
		} catch (Exception e) {
			throw new ApiException("findPrincipalByOwnerUuid: failed to read ownerId from authorization: " + e.getMessage(), e);
		}
		if (ownerUuid == null) {
			throw new ApiException("findPrincipalByOwnerUuid: authorization has no ownerId set");
		}
		ObjectAddress uuidField = domain.getEntityDefinition() != null ? domain.getEntityDefinition().uuid() : null;
		if (uuidField == null) {
			throw new ApiException("findPrincipalByOwnerUuid: authenticator domain '"
					+ domain.getDomainName() + "' has no uuid field");
		}
		// The owned field stores a qualified id (${domainName}:${uuid}); strip the
		// domain prefix so the principal is looked up by its bare uuid.
		String principalUuid = OwnerIds.idOf(ownerUuid.toString());
		IFilter filter = Filter.eq(uuidField.toString(), principalUuid);
		// Direct repository read: the principal is an authenticator entity, not a
		// key/authorization — outside the "route key/authz ops through the pipeline"
		// scope (same as findByLogin on the login side).
		List<Object> results = repo.getEntities(Optional.empty(), Optional.of(filter), Optional.empty());
		if (results == null || results.isEmpty()) {
			throw new ApiException("Principal not found for ownerId: " + ownerUuid);
		}
		return results.get(0);
	}

	@Expression(name = "synthAuthFromPrincipal",
			description = "Builds a synthetic IAuthentication from a resolved principal and the authorities/type carried by an existing authorization entity, used to feed createAuthorizationEntity2 during a refresh operation.")
	public static IAuthentication synthAuthFromPrincipal(@Nullable Object principal, @Nullable Object existingAuthzEntity, @Nullable Object domainContext) {
		if (principal == null || existingAuthzEntity == null || domainContext == null) {
			throw new ApiException("synthAuthFromPrincipal: principal, existingAuthz and domainContext are required");
		}
		Object defObj = authorizationDefinition(domainContext);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef)) {
			throw new ApiException("synthAuthFromPrincipal: authorization definition not resolved");
		}
		IReflection reflection = DefaultMapper.reflection();
		Object tokenType = null;
		if (authzDef.type() != null) {
			try {
				tokenType = reflection.getFieldValue(existingAuthzEntity, authzDef.type().toString());
			} catch (Exception ignored) {
				// keep null — the new entity will simply have no type
			}
		}
		List<String> authorities = null;
		if (authzDef.authorities() != null) {
			try {
				Object raw = reflection.getFieldValue(existingAuthzEntity, authzDef.authorities().toString());
				if (raw instanceof List<?> list) {
					@SuppressWarnings("unchecked")
					List<String> typed = (List<String>) list;
					authorities = typed;
				}
			} catch (Exception ignored) {
				// keep null
			}
		}
		// Fill the full security context onto the Authentication: identity (tenant /
		// owner) read off the token, super status from the server registries. This is
		// the trusted identity the pipeline reconciles the protocol caller against.
		IDomain<?> authzDomain = toDomain(domainContext);
		String authTenantId = null;
		String authOwnerId = null;
		boolean superTenant = false;
		boolean superOwner = false;
		if (authzDomain != null) {
			ObjectAddress tenantAddr = authzDomain.getTenantIdFieldAddress();
			authTenantId = tenantAddr != null ? readField(existingAuthzEntity, tenantAddr) : null;
			ObjectAddress ownedAddr = authzDomain.getDomainDefinition() != null
					? authzDomain.getDomainDefinition().owned() : null;
			authOwnerId = ownedAddr != null ? readField(existingAuthzEntity, ownedAddr) : null;
			IApi api = apiOf(authzDomain);
			if (api != null) {
				superTenant = authTenantId != null && api.isSuperTenant(authTenantId);
				superOwner = authOwnerId != null && api.isSuperOwner(authOwnerId);
			}
		}
		return new com.garganttua.api.commons.security.authentication.Authentication(
				true,
				principal,
				null,
				tokenType,
				authorities,
				authTenantId,
				authOwnerId,
				superTenant,
				superOwner,
				true, true, true, true);
	}

	@Expression(name = "authenticationResponse",
			description = "Builds the IAuthentication returned to the client after a successful authenticate/login: "
					+ "the security context (tenantId/ownerId read off the minted token, super status from the "
					+ "registries, authorities from the token, the encoded token as authorization), SANITIZED of "
					+ "credentials and principal (never returned over the wire). Returns null when the token entity is "
					+ "null (lets the reuse branch fall through to fresh-create).")
	public static Object authenticationResponse(@Nullable Object authResult, @Nullable Object tokenEntity,
			@Nullable Object encodedToken, @Nullable Object domainContext) {
		Object token = unwrapOptional(tokenEntity);
		if (token == null) {
			return null;
		}
		IReflection reflection = DefaultMapper.reflection();
		// domainContext is the AUTHENTICATOR domain (e.g. users); the token's identity fields
		// live on the linked AUTHORIZATION domain, so resolve it to read tenant/owner correctly.
		IDomain<?> authzDomain = resolveAuthorizationDomain(toDomain(domainContext));
		String tenantId = null;
		String ownerId = null;
		boolean superTenant = false;
		boolean superOwner = false;
		List<String> authorities = null;
		if (authzDomain != null) {
			ObjectAddress tenantAddr = authzDomain.getTenantIdFieldAddress();
			tenantId = tenantAddr != null ? readField(token, tenantAddr) : null;
			ObjectAddress ownedAddr = authzDomain.getDomainDefinition() != null
					? authzDomain.getDomainDefinition().owned() : null;
			ownerId = ownedAddr != null ? readField(token, ownedAddr) : null;
			IApi api = apiOf(authzDomain);
			if (api != null) {
				superTenant = tenantId != null && api.isSuperTenant(tenantId);
				superOwner = ownerId != null && api.isSuperOwner(ownerId);
			}
			Object defObj = authorizationDefinition(domainContext);
			if (defObj instanceof IDomainAuthorizationDefinition authzDef && authzDef.authorities() != null) {
				try {
					Object raw = reflection.getFieldValue(token, authzDef.authorities().toString());
					if (raw instanceof List<?> list) {
						@SuppressWarnings("unchecked")
						List<String> typed = (List<String>) list;
						authorities = typed;
					}
				} catch (Exception ignored) {
					// keep null
				}
			}
		}
		Object authorization = unwrapOptional(encodedToken);
		return new com.garganttua.api.commons.security.authentication.Authentication(
				true,
				null, // principal — internal, never returned
				null, // credentials — internal, never returned
				authorization,
				authorities,
				tenantId,
				ownerId,
				superTenant,
				superOwner,
				true, true, true, true);
	}

	@Expression(name = "findByLogin", description = "Finds an entity by login field in the repository. Returns the entity or throws if not found.")
	public static Object findByLogin(@Nullable Object authContextObj, @Nullable Object repositoryObj, @Nullable Object loginValue) {
		if (authContextObj == null || repositoryObj == null || loginValue == null) {
			throw new ApiException("findByLogin: authContext, repository and login are required");
		}
		IAuthenticatorDefinition authDef = (IAuthenticatorDefinition) authContextObj;
		IRepository repo = (IRepository) repositoryObj;
		ObjectAddress loginField = authDef.login();
		if (loginField == null) {
			throw new ApiException("findByLogin: no login field configured on authenticator");
		}
		String loginFieldName = loginField.toString();
		IFilter filter = Filter.eq(loginFieldName, loginValue);
		List<Object> results = repo.getEntities(Optional.empty(), Optional.of(filter), Optional.empty());
		if (results == null || results.isEmpty()) {
			throw new ApiException("User not found for login: " + loginValue);
		}
		return results.get(0);
	}

	@Expression(name = "checkAccountStatus", description = "Checks enabled/locked/expired flags on an authenticator entity. Returns true if OK, throws if account is disabled/locked/expired.")
	public static boolean checkAccountStatus(@Nullable Object authContextObj, @Nullable Object entity) {
		if (authContextObj == null || entity == null) {
			throw new ApiException("checkAccountStatus: authContext and entity are required");
		}
		IAuthenticatorDefinition authDef = (IAuthenticatorDefinition) authContextObj;

		if (authDef.alwaysEnabled()) {
			return true;
		}

		IReflection reflection = DefaultMapper.reflection();

		if (authDef.enabled() != null) {
			Object value = reflection.getFieldValue(entity, authDef.enabled().toString());
			if (!Boolean.TRUE.equals(value)) {
				throw new ApiException("Account is disabled");
			}
		}

		if (authDef.accountNonLocked() != null) {
			Object value = reflection.getFieldValue(entity, authDef.accountNonLocked().toString());
			if (!Boolean.TRUE.equals(value)) {
				throw new ApiException("Account is locked");
			}
		}

		if (authDef.accountNonExpired() != null) {
			Object value = reflection.getFieldValue(entity, authDef.accountNonExpired().toString());
			if (!Boolean.TRUE.equals(value)) {
				throw new ApiException("Account is expired");
			}
		}

		if (authDef.credentialsNonExpired() != null) {
			Object value = reflection.getFieldValue(entity, authDef.credentialsNonExpired().toString());
			if (!Boolean.TRUE.equals(value)) {
				throw new ApiException("Credentials are expired");
			}
		}

		return true;
	}

	@Expression(name = "prepareAuthContext", description = "Prepares the runtime context with request and domainContext variables for authenticate method suppliers")
	public static boolean prepareAuthContext(@Nullable Object request, @Nullable Object domainContext) {
		IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
		if (runtimeCtx != null && request != null) {
			runtimeCtx.setVariable("request", request);
		}
		if (runtimeCtx != null && domainContext != null) {
			runtimeCtx.setVariable("domainContext", domainContext);
		}
		return true;
	}

	@Expression(name = "tryAuthenticate", description = "Attempts authentication using the IAuthenticatorDefinition, iterating over authentication methods until one succeeds")
	public static Object tryAuthenticate(@Nullable Object authenticatorDefinition) {
		if (authenticatorDefinition == null) {
			throw new ApiException("No authenticator definition available");
		}
		try {
			IAuthenticatorDefinition def = (IAuthenticatorDefinition) authenticatorDefinition;
			List<IAuthenticationDefinition> authDefs = def.authenticationDefinitions();
			if (authDefs == null || authDefs.isEmpty()) {
				throw new ApiException("No authentication methods configured");
			}

			return authDefs.stream()
					.map(authDef -> attemptAuthentication(authDef, def))
					.filter(Objects::nonNull)
					.findFirst()
					.orElseThrow(() -> new ApiException("All authentication methods failed"));
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Authentication failed: " + e.getMessage(), e);
		}
	}

	private static IAuthentication attemptAuthentication(IAuthenticationDefinition authDef, IAuthenticatorDefinition authenticatorDef) {
		try {
			IMethodBinder<?> binder = authDef.authenticateMethodBinder();
			if (binder == null) {
				return null;
			}

			Optional<? extends IMethodReturn<?>> result;
			if (binder instanceof IContextualMethodBinder<?, ?> contextualBinder) {
				IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
				result = ((IContextualMethodBinder<?, Object>) contextualBinder).execute(runtimeCtx);
			} else {
				result = binder.execute();
			}

			if (result.isEmpty()) {
				return null;
			}

			Object returned = result.get().single();
			if (returned instanceof IAuthentication auth) {
				if (auth.authenticated()) return auth;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	// ----- Authorization → authenticate pipeline bridge -----

	@Expression(name = "protocolTargetDomain",
			description = "Returns the entity class that an IAuthorizationProtocol declares as its target — the domain on which the authenticate pipeline should run for tokens decoded by this protocol.")
	public static IClass<?> protocolTargetDomain(@Nullable Object protocol) {
		IAuthorizationProtocol p = (IAuthorizationProtocol) unwrapOptional(protocol);
		if (p == null) {
			throw new ApiException("Authorization protocol is null");
		}
		IClass<?> target = p.targetDomain();
		if (target == null) {
			throw new ApiException(
					"IAuthorizationProtocol '" + p.getClass().getName()
					+ "' returned null from targetDomain()");
		}
		return target;
	}

	@Expression(name = "resolveDomainByEntityClass",
			description = "Iterates over IApi.getDomain and returns the first domain whose entity class matches. Throws 500 if none found.")
	public static IDomain<?> resolveDomainByEntityClass(@Nullable Object apiContext, @Nullable Object entityClass) {
		IApi api = (IApi) unwrapOptional(apiContext);
		IClass<?> target = (IClass<?>) unwrapOptional(entityClass);
		if (api == null) {
			throw new ApiException("API context is null");
		}
		if (target == null) {
			throw new ApiException("Target entity class is null");
		}
		if (api instanceof com.garganttua.api.core.api.Api concrete) {
			for (IDomain<?> domain : concrete.getDomains().values()) {
				IClass<?> domainEntity = domain.getEntityClass();
				if (domainEntity != null && domainEntity.equals(target)) {
					return domain;
				}
			}
		}
		throw new ApiException(
				"No domain registered for entity class: " + target.getName());
	}

	@Expression(name = "verifyAuthorization",
			description = "Single server-side verification step used by VERIFY_AUTHORIZATION.gs. The decoded authorization (token) verifies ITSELF: it must be a registered, authenticator-enabled domain. Resolves the token's own domain from the entity class, forges an AuthenticationRequest (login = token uuid, credentials = decoded token, tenantId = token's tenant) and runs that domain's authenticate pipeline — the user-declared @AuthenticationAuthenticate method enforces signature / expiration / revocation / custom rules. On success the framework resolves the OWNER from the token's qualified ownerId and returns it as the principal (carrying the token's type + authorities). Throws ApiException (→ 401) when the token is not a verifiable authenticator domain, fails its authenticate method, or its owner cannot be resolved.")
	public static IAuthentication verifyAuthorization(@Nullable Object apiContext,
			@Nullable Object authorization, @Nullable Object operationRequest) {
		IApi api = (IApi) unwrapOptional(apiContext);
		Object authz = unwrapOptional(authorization);
		if (api == null || authz == null) {
			throw new ApiException("verifyAuthorization: apiContext and authorization are required");
		}

		// 1. Resolve the token's OWN domain from the decoded entity's class.
		IDomain<?> authzDomain = domainOfEntity(api, authz);
		if (authzDomain == null) {
			// No registered domain for this token class. This is the trusted
			// in-process Mode-B path: the caller built and pre-decoded a token
			// the framework has no DSL to enforce against (no authenticator, no
			// owner link). Trust it as-is — an external protocol always decodes
			// to a REGISTERED token class (which takes the authenticate path
			// below); only an in-process caller can present an unregistered one.
			// Trusted in-process Mode-B token with no registered domain: the framework
			// cannot resolve its identity/authorities, so it carries NONE — null lets
			// reconcile fall back to the (trusted) protocol caller's tenant/owner/authorities.
			return new com.garganttua.api.commons.security.authentication.Authentication(
					true, authz, null, authz, null,
					null, null, false, false, // no resolved tenant/owner, non-super
					true, true, true, true);
		}

		// 2. Framework-owned intrinsic checks: expiration + revocation, read from
		//    the `expiration` / `revoked` field addresses declared on the
		//    authorization DSL. Run first so an expired/revoked token is rejected
		//    fast (→ 401), whichever verification path follows.
		validateAuthorizationFromDefinition(authz, authzDomain);

		// 3. Verify the token — custom OR default, symmetric to the mint side:
		//    - the token domain declares an authenticator (a user
		//      @AuthenticationAuthenticate method) → CUSTOM: forge an
		//      AuthenticationRequest (login = token uuid, credentials = decoded
		//      token, tenantId = token tenant) and run that authenticate pipeline,
		//      where the user enforces the signature + any custom rules;
		//    - otherwise → DEFAULT: the framework verifies the signature itself
		//      (no-op when the authorization is not signable).
		DomainDefinition<?> domDef = toDomainDefinition(authzDomain);
		boolean hasAuthenticator = domDef != null
				&& domDef.domainSecurityDefinition() != null
				&& domDef.domainSecurityDefinition().authenticatorDefinition() != null;

		if (hasAuthenticator) {
			// FRAMEWORK-OWNED signature verification — the decoded token is NEVER trusted
			// before its cryptographic signature is checked against the key that signed it
			// (resolved by the token's qualified signedBy). The user authenticate that
			// follows carries BUSINESS rules only, not the crypto. A tampered/empty
			// signature (false) or an unverifiable token (throws) → 401. No-op when the
			// authorization is not signable.
			if (isAuthorizationSignable(authzDomain)
					&& !verifyTokenSignature(authz, authzDomain, operationRequest)) {
				throw new ApiException("Authorization signature verification failed");
			}
			String login = readField(authz, authzDomain.getEntityDefinition().uuid());
			ObjectAddress tenantAddr = authzDomain.getEntityDefinition().tenantId();
			String tenantId = tenantAddr != null ? readField(authz, tenantAddr) : null;
			IAuthenticationRequest authRequest =
					new com.garganttua.api.core.security.authentication.AuthenticationRequest(login, authz);
			// The token's own tenant drives the lookup (verify flow); it is NOT carried
			// in the request body. Throws on rejection (→ mapped to 401 by the caller).
			invokeAuthenticate(api, authzDomain, authRequest, tenantId);
		} else {
			boolean sigOk = verifyIfSignable(authz, authzDomain, operationRequest);
			if (!sigOk) {
				throw new ApiException("Authorization signature verification failed");
			}
		}

		// 4. Server resolves the OWNER as the final principal, then wraps it with the
		//    authorities + type. SERVER-AUTHORITATIVE: for a storable token the persisted
		//    record wins over the decoded payload — the latter may carry forged fields the
		//    signature does not cover (getDataToSign rarely includes authorities), so trusting
		//    the decoded authorities would allow privilege escalation.
		Object owner = resolveOwnerPrincipal(api, authzDomain, authz);
		Object trusted = serverAuthoritativeAuthorization(authzDomain, authz);
		return synthAuthFromPrincipal(owner, trusted, authzDomain);
	}

	/**
	 * For a STORABLE authorization, returns the PERSISTED record (looked up by uuid) — the
	 * authoritative source for granted authorities/type. The decoded token is only trusted
	 * to identify itself (uuid) and carry a verifiable signature; any field NOT covered by
	 * getDataToSign (typically authorities) must come from the server. Falls back to the
	 * decoded token when not storable (stateless — no server record; its signature must then
	 * cover the authorities) or when the record cannot be found.
	 */
	private static Object serverAuthoritativeAuthorization(IDomain<?> authzDomain, Object authz) {
		Object defObj = authorizationDefinition(authzDomain);
		if (!(defObj instanceof IDomainAuthorizationDefinition d) || !d.storable()) {
			return authz;
		}
		ObjectAddress uuidAddr = authzDomain.getEntityDefinition() != null
				? authzDomain.getEntityDefinition().uuid() : null;
		if (uuidAddr == null) {
			return authz;
		}
		String uuid = readField(authz, uuidAddr);
		if (uuid == null) {
			return authz;
		}
		try {
			IFilter filter = Filter.eq(uuidAddr.toString(), uuid);
			List<Object> results = authzDomain.getRepository()
					.getEntities(Optional.empty(), Optional.of(filter), Optional.empty());
			if (results != null && !results.isEmpty()) {
				return results.get(0);
			}
		} catch (Exception ignored) {
			// The authenticate step already proved the token exists; on a lookup hiccup
			// fall back to the decoded (signature-verified) payload rather than failing open.
		}
		return authz;
	}

	/** Resolves the registered domain whose entity class matches the runtime class of {@code entity}. Null when none. */
	private static IDomain<?> domainOfEntity(IApi api, Object entity) {
		if (entity == null || !(api instanceof com.garganttua.api.core.api.Api concrete)) {
			return null;
		}
		IClass<?> cls = IClass.getClass(entity.getClass());
		for (IDomain<?> d : concrete.getDomains().values()) {
			if (cls.equals(d.getEntityClass())) {
				return d;
			}
		}
		return null;
	}

	/**
	 * Resolves the owner principal for a verified token. The token's {@code owned}
	 * field carries a qualified id {@code ${ownerDomain}:${uuid}}; we split it to
	 * find the owner domain and look the principal up by its bare uuid.
	 */
	private static Object resolveOwnerPrincipal(IApi api, IDomain<?> authzDomain, Object authz) {
		ObjectAddress ownedAddr = authzDomain.getDomainDefinition().owned();
		if (ownedAddr == null) {
			throw new ApiException("verifyAuthorization: authorization domain '" + authzDomain.getDomainName()
					+ "' is not owned — cannot resolve the owner principal. Declare .owned(field).");
		}
		String qualified = readField(authz, ownedAddr);
		if (qualified == null) {
			throw new ApiException("verifyAuthorization: authorization has no ownerId set");
		}
		String ownerDomainName = OwnerIds.domainOf(qualified);
		String ownerUuid = OwnerIds.idOf(qualified);
		IDomain<?> ownerDomain = ownerDomainName != null ? api.getDomain(ownerDomainName).orElse(null) : null;
		if (ownerDomain == null) {
			throw new ApiException("verifyAuthorization: owner domain '" + ownerDomainName
					+ "' (from ownerId '" + qualified + "') is not registered");
		}
		ObjectAddress uuidField = ownerDomain.getEntityDefinition() != null
				? ownerDomain.getEntityDefinition().uuid() : null;
		if (uuidField == null) {
			throw new ApiException("verifyAuthorization: owner domain '" + ownerDomain.getDomainName()
					+ "' has no uuid field");
		}
		IFilter filter = Filter.eq(uuidField.toString(), ownerUuid);
		List<Object> results = ownerDomain.getRepository()
				.getEntities(Optional.empty(), Optional.of(filter), Optional.empty());
		if (results == null || results.isEmpty()) {
			throw new ApiException("verifyAuthorization: owner not found for ownerId '" + qualified + "'");
		}
		return results.get(0);
	}

	/**
	 * DSL-driven intrinsic validation. Reads the {@code revoked} and
	 * {@code expiration} ObjectAddresses from
	 * {@link IDomainAuthorizationDefinition} and verifies the entity's fields
	 * against them. Either check raises a parlant {@link ApiException} so the
	 * `! => recordCaughtException(@0, @exception) -> 401` pattern surfaces the
	 * exact message on the OperationResponse.
	 */
	static void validateAuthorizationFromDefinition(Object authzEntity, IDomain<?> targetDomain) {
		Object defObj = authorizationDefinition(targetDomain);
		if (!(defObj instanceof IDomainAuthorizationDefinition authzDef)) {
			return;
		}
		IReflection reflection = DefaultMapper.reflection();

		ObjectAddress revokedAddr = authzDef.revoked();
		if (revokedAddr != null) {
			Object value = reflection.getFieldValue(authzEntity, revokedAddr.toString());
			if (value instanceof Boolean b && b) {
				throw new ApiException("Authorization revoked");
			}
		}

		ObjectAddress expirationAddr = authzDef.expiration();
		if (expirationAddr != null) {
			Object value = reflection.getFieldValue(authzEntity, expirationAddr.toString());
			if (value != null && isExpired(value)) {
				throw new ApiException("Authorization expired");
			}
		}
	}

	private static IDomain<?> resolveOptionalAuthenticatorDomain(IApi api, Object operationRequest, Object authz) {
		IClass<?> targetClass = AuthorizationProtocolExpressions
				.resolveAuthorizationTargetClass(operationRequest, authz);
		if (targetClass == null || api == null) return null;
		if (api instanceof com.garganttua.api.core.api.Api concrete) {
			for (IDomain<?> domain : concrete.getDomains().values()) {
				IClass<?> domainEntity = domain.getEntityClass();
				if (domainEntity != null && domainEntity.equals(targetClass)) {
					return domain;
				}
			}
		}
		return null;
	}

	@Expression(name = "invokeAuthenticate",
			description = "Synchronously invokes the 'authenticate' operation on the given target domain with the provided IAuthenticationRequest as body. Returns the resulting IAuthentication or throws ApiException on failure (mapped to 401 by the caller).")
	public static IAuthentication invokeAuthenticate(@Nullable Object apiContext, @Nullable Object targetDomain,
			@Nullable Object authRequest, @Nullable Object tenantId) {
		IApi api = (IApi) unwrapOptional(apiContext);
		IDomain<?> domain = (IDomain<?>) unwrapOptional(targetDomain);
		IAuthenticationRequest req = (IAuthenticationRequest) unwrapOptional(authRequest);
		if (api == null || domain == null || req == null) {
			throw new ApiException("invokeAuthenticate: apiContext, targetDomain and authRequest must all be non-null");
		}
		Object tenantUnwrapped = unwrapOptional(tenantId);
		String tenant = tenantUnwrapped == null ? null : tenantUnwrapped.toString();

		@SuppressWarnings({"unchecked", "rawtypes"})
		IClass<?> entityClass = ((IDomain) domain).getEntityClass();
		com.garganttua.api.core.service.OperationRequest invocation =
				new com.garganttua.api.core.service.OperationRequest(new java.util.HashMap<>());
		invocation.arg(IOperationRequest.OPERATION,
				OperationDefinition.authenticate(domain.getDomainName(), entityClass));
		invocation.arg("entity", req);
		// The tenant is set on the caller of the authenticate invocation (not in the
		// request body): for the verify flow it is the token's own tenant.
		if (tenant != null && !tenant.isBlank()) {
			invocation.arg(IOperationRequest.TENANT_ID, tenant);
			invocation.arg(IOperationRequest.REQUESTED_TENANT_ID, tenant);
		}

		IOperationResponse response = domain.invoke(invocation);
		OperationResponseCode code = response.getResponseCode();
		if (code != OperationResponseCode.OK && code != OperationResponseCode.CREATED) {
			throw new ApiException("Authenticate invocation on domain '" + domain.getDomainName()
					+ "' returned " + code + ": " + response.getResponse());
		}
		Object body = response.getResponse();
		if (body instanceof IAuthentication auth) {
			return auth;
		}
		throw new ApiException("Authenticate invocation on domain '" + domain.getDomainName()
				+ "' did not return an IAuthentication — got: "
				+ (body == null ? "null" : body.getClass().getName()));
	}

	// ─────────────────────────────────────────────────────────────
	// Persisted @Key entity ↔ IKeyRealm bridge.
	//
	// Two operations:
	//   - materializeKeyRealm: reads the 7 fields described by an
	//     IDomainKeyDefinition and rebuilds an IKeyRealm via core's
	//     KeyRealm.fromSignatureMaterial (which caches the JDK key).
	//   - generateAndStampKeyEntity: generates a fresh JDK KeyPair,
	//     instantiates the entity class, and stamps realmName /
	//     algorithm / signatureAlgorithm / publicMaterial /
	//     privateMaterial / expiration / revoked onto it.
	//
	// Both used exclusively by resolvePersistedKeyRealm. Tenancy
	// stamping (uuid / tenantId / ownerId) is a concern of the resolve
	// path and is performed separately by stampIdentityAndTenancy.
	// ─────────────────────────────────────────────────────────────

	public static IKeyRealm materializeKeyRealm(Object entity, IDomainKeyDefinition keyDef, IReflection reflection) {
		Objects.requireNonNull(entity, "entity");
		Objects.requireNonNull(keyDef, "keyDef");
		Objects.requireNonNull(reflection, "reflection");

		String name = readKeyString(entity, keyDef.name(), reflection, "name");
		String algorithmRaw = readKeyString(entity, keyDef.keyAlgorithm(), reflection, "keyAlgorithm");
		String signatureRaw = readKeyString(entity, keyDef.signatureAlgorithm(), reflection, "signatureAlgorithm");
		IKey signingKey = readKeyIKey(entity, keyDef.keyForSigning(), reflection, "keyForSigning");
		IKey verificationKey = readKeyIKey(entity, keyDef.keyForSignatureVerification(), reflection,
				"keyForSignatureVerification");
		Date expiration = readKeyExpiration(entity, keyDef.expiration(), reflection);
		boolean revoked = readKeyBoolean(entity, keyDef.revoked(), reflection);

		IKeyAlgorithm algorithm = parseKeyAlgorithm(algorithmRaw);
		SignatureAlgorithm sigAlgo = parseKeySignature(signatureRaw);

		// Extract JDK-encoded bytes from the IKey objects carried on the entity
		// and rebuild a fully-stitched IKeyRealm via core's factory. We do not
		// pass the IKey instances directly: KeyRealm.fromSignatureMaterial owns
		// the Key construction (handles caching, type checks, algorithm wiring)
		// so we feed it the bytes and let it reconstruct.
		byte[] privateBytes;
		byte[] publicBytes;
		try {
			privateBytes = signingKey.getKey().getEncoded();
			publicBytes = verificationKey.getKey().getEncoded();
		} catch (CryptoException e) {
			throw new ApiException("materializeKeyRealm: failed to extract JDK-encoded bytes from "
					+ "the entity's IKey fields: " + e.getMessage(), e);
		}

		return KeyRealm.fromSignatureMaterial(name, algorithm, sigAlgo,
				expiration, revoked, privateBytes, publicBytes);
	}

	public static Object generateAndStampKeyEntity(IClass<?> entityClass, IDomainKeyDefinition keyDef,
			IKeyAlgorithm algorithm, SignatureAlgorithm signatureAlgorithm,
			String realmName, int duration, TimeUnit unit, IReflection reflection) {
		Objects.requireNonNull(entityClass, "entityClass");
		Objects.requireNonNull(keyDef, "keyDef");
		Objects.requireNonNull(algorithm, "algorithm");
		Objects.requireNonNull(signatureAlgorithm, "signatureAlgorithm");
		Objects.requireNonNull(realmName, "realmName");
		Objects.requireNonNull(reflection, "reflection");

		if (!(algorithm instanceof KeyAlgorithm concreteAlgo)) {
			throw new ApiException("generateAndStampKeyEntity: algorithm must be a "
					+ KeyAlgorithm.class.getName() + " — got " + algorithm.getClass().getName());
		}

		KeyPair pair;
		try {
			pair = concreteAlgo.generateAsymmetricKey();
		} catch (Exception e) {
			throw new ApiException("generateAndStampKeyEntity: keypair generation failed for "
					+ concreteAlgo + ": " + e.getMessage(), e);
		}

		Object entity;
		try {
			entity = entityClass.getConstructor().newInstance();
		} catch (Exception e) {
			throw new ApiException("generateAndStampKeyEntity: cannot instantiate "
					+ entityClass.getName() + " — a no-arg constructor is required: " + e.getMessage(), e);
		}

		writeIfMapped(entity, keyDef.name(), realmName, reflection);
		// Store the algorithm in the canonical 'NAME-SIZE' form that
		// KeyAlgorithm.validateKeyAlgorithm consumes during materialize.
		// KeyAlgorithm.toString uses underscores ("EC_256"), which the
		// parser would reject — so we serialize explicitly.
		writeIfMapped(entity, keyDef.keyAlgorithm(),
				concreteAlgo.getName() + "-" + concreteAlgo.getKeySize(), reflection);
		writeIfMapped(entity, keyDef.signatureAlgorithm(), signatureAlgorithm.name(), reflection);

		// Build IKey objects up front: the entity's key-material fields are
		// typed IKey (so a @Key entity is a drop-in IKeyRealm shape), not
		// raw byte[]. We construct the IKeys via core's Key.fromSigningMaterial
		// factory and stamp the instances onto the entity. Persistence-side
		// translation to byte[] (for DB storage) is the DTO mapping's concern.
		IKey signingKey = Key.fromSigningMaterial(KeyType.PRIVATE, algorithm, signatureAlgorithm,
				pair.getPrivate().getEncoded());
		IKey verificationKey = Key.fromSigningMaterial(KeyType.PUBLIC, algorithm, signatureAlgorithm,
				pair.getPublic().getEncoded());
		writeIfMapped(entity, keyDef.keyForSigning(), signingKey, reflection);
		writeIfMapped(entity, keyDef.keyForSignatureVerification(), verificationKey, reflection);
		// For asymmetric algorithms, encryption uses the same private key as
		// signing and decryption uses the same public key as verification —
		// so the encryption-side IKey fields, when mapped, receive the same
		// instances. The framework leaves them empty when the user does not
		// map them (typical for signing-only setups).
		writeIfMapped(entity, keyDef.keyForEncryption(), signingKey, reflection);
		writeIfMapped(entity, keyDef.keyForDecryption(), verificationKey, reflection);

		ObjectAddress expirationAddr = keyDef.expiration();
		if (expirationAddr != null) {
			Instant exp = Instant.now().plusMillis(unit == null || duration <= 0 ? 0L : unit.toMillis(duration));
			Object value = adaptKeyExpiration(entityClass, expirationAddr, exp, reflection);
			reflection.setFieldValue(entity, expirationAddr, value);
		}

		writeIfMapped(entity, keyDef.revoked(), Boolean.FALSE, reflection);

		// Initial version is 1 — matches IKeyRealm's default. The framework
		// increments this on rotate(). When the field is not mapped the stamp
		// is a no-op.
		writeIfMapped(entity, keyDef.version(), Integer.valueOf(1), reflection);
		// rotate() field is the last-rotation timestamp; on a freshly minted
		// key there is no prior rotation — leave it null. The framework writes
		// it later when IKeyRealm.rotate() is invoked.

		return entity;
	}

	private static String readKeyString(Object entity, ObjectAddress addr, IReflection reflection, String label) {
		if (addr == null) {
			throw new ApiException("materializeKeyRealm: '" + label
					+ "' field is not configured on the key entity definition");
		}
		Object value = reflection.getFieldValue(entity, addr.toString());
		if (value == null) {
			throw new ApiException("materializeKeyRealm: '" + label + "' field at " + addr + " is null");
		}
		return value.toString();
	}

	private static IKey readKeyIKey(Object entity, ObjectAddress addr, IReflection reflection, String label) {
		if (addr == null) {
			throw new ApiException("materializeKeyRealm: '" + label
					+ "' field is not configured on the key entity definition");
		}
		Object value = reflection.getFieldValue(entity, addr.toString());
		if (!(value instanceof IKey key)) {
			throw new ApiException("materializeKeyRealm: '" + label + "' at " + addr
					+ " must be an IKey — got " + (value == null ? "null" : value.getClass().getName()));
		}
		return key;
	}

	private static Date readKeyExpiration(Object entity, ObjectAddress addr, IReflection reflection) {
		if (addr == null) return null;
		Object value = reflection.getFieldValue(entity, addr.toString());
		if (value == null) return null;
		if (value instanceof Date date) return (Date) date.clone();
		if (value instanceof Instant instant) return Date.from(instant);
		if (value instanceof Long millis) return new Date(millis);
		throw new ApiException("materializeKeyRealm: expiration at " + addr
				+ " must be Date / Instant / Long — got " + value.getClass().getName());
	}

	private static boolean readKeyBoolean(Object entity, ObjectAddress addr, IReflection reflection) {
		if (addr == null) return false;
		Object value = reflection.getFieldValue(entity, addr.toString());
		return Boolean.TRUE.equals(value);
	}

	private static void writeIfMapped(Object entity, ObjectAddress addr, Object value, IReflection reflection) {
		if (addr != null) {
			reflection.setFieldValue(entity, addr, value);
		}
	}

	private static Object adaptKeyExpiration(IClass<?> entityClass, ObjectAddress addr, Instant exp, IReflection reflection) {
		var fieldOpt = reflection.findField(entityClass, addr.toString());
		if (fieldOpt.isEmpty()) return exp;
		java.lang.reflect.Type rawType = fieldOpt.get().getType().getType();
		if (!(rawType instanceof Class<?> targetType)) return exp;
		if (Instant.class.isAssignableFrom(targetType)) return exp;
		if (Date.class.isAssignableFrom(targetType)) return Date.from(exp);
		if (Long.class.isAssignableFrom(targetType) || targetType == long.class) return exp.toEpochMilli();
		throw new ApiException("generateAndStampKeyEntity: cannot adapt expiration to "
				+ targetType.getName() + " — supported: Date, Instant, Long");
	}

	private static IKeyAlgorithm parseKeyAlgorithm(String raw) {
		try {
			return KeyAlgorithm.validateKeyAlgorithm(raw);
		} catch (IllegalArgumentException e) {
			throw new ApiException("materializeKeyRealm: invalid algorithm '" + raw
					+ "' — expected format 'NAME-SIZE' (e.g. RSA-2048, EC-256): " + e.getMessage(), e);
		}
	}

	private static SignatureAlgorithm parseKeySignature(String raw) {
		try {
			return SignatureAlgorithm.valueOf(raw);
		} catch (IllegalArgumentException e) {
			throw new ApiException("materializeKeyRealm: invalid signatureAlgorithm '" + raw
					+ "' — must be a SignatureAlgorithm enum name (e.g. SHA256, SHA512): " + e.getMessage(), e);
		}
	}
}
