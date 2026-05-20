package com.garganttua.api.core.expression;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.context.Domain;
import com.garganttua.api.core.definition.DomainDefinition;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IAuthenticationDefinition;
import com.garganttua.api.commons.definition.IAuthenticatorDefinition;
import com.garganttua.api.commons.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.Pluralizer;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.security.authentication.IAuthentication;
import com.garganttua.api.commons.security.authentication.IAuthenticationRequest;
import com.garganttua.api.commons.security.authorization.IAuthorization;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.crypto.IKey;
import com.garganttua.core.crypto.IKeyRealm;
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
public class SecurityExpressions {

	@Expression(name = "operationAccess", description = "Returns the Access level string from an OperationDefinition")
	public static String operationAccess(@Nullable Object operation) {
		if (operation == null) return "anonymous";
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return "anonymous";
		return opDef.access() != null ? opDef.access().name() : "anonymous";
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
					+ "Super-tenant and super-owner callers bypass the check.")
	public static boolean callerHasAuthority(@Nullable Object caller, @Nullable Object authorityName) {
		ICaller c = (ICaller) unwrapOptional(caller);
		if (c == null) return false;
		if (c.superTenant() || c.superOwner()) return true;
		Object name = unwrapOptional(authorityName);
		if (!(name instanceof String authority) || authority.isBlank()) return false;
		java.util.List<String> authorities = c.authorities();
		return authorities != null && authorities.contains(authority);
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
		Optional<IAuthorization> authorization = (Optional<IAuthorization>) opRequest.arg(IOperationRequest.AUTHORIZATION);
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

	@Expression(name = "authRequestHasTenantId", description = "Returns true if the IAuthenticationRequest has a non-null tenantId (safe, never throws)")
	public static boolean authRequestHasTenantId(@Nullable Object entity) {
		Object unwrapped = unwrapOptional(entity);
		if (unwrapped instanceof IAuthenticationRequest req) {
			return req.tenantId() != null;
		}
		return false;
	}

	@Expression(name = "isTenantIdMandatory", description = "Returns true if the operation requires a tenantId based on access level")
	public static boolean isTenantIdMandatory(Object operation, Object context) {
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return false;
		Access access = opDef.access();
		return access == Access.tenant || access == Access.owner;
	}

	@Expression(name = "isOwnerIdMandatory", description = "Returns true if the operation requires an ownerId based on access level")
	public static boolean isOwnerIdMandatory(Object operation, Object context) {
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return false;
		return opDef.access() == Access.owner;
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

			ObjectAddress ownedField = authzDomain.getDomainDefinition().owned();
			if (ownedField != null && principalUuid != null) {
				reflection.setFieldValue(entity, ownedField, principalUuid);
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

			ObjectAddress ownedField = authzDomain.getDomainDefinition().owned();
			if (ownedField != null) {
				filters.add(Filter.eq(ownedField.toString(), principalUuid));
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

			// Direct repository query — bypasses VERIFY_AUTHORIZATION (which would
			// reject an internal lookup that has no caller-supplied token) and
			// VERIFY_TENANT (irrelevant here: the principal/tenant scoping is
			// already encoded in the filters above). This is a framework-internal
			// read, not a user-triggered request.
			java.util.List<Object> results = authzDomain.getRepository().getEntities(
					Optional.empty(), Optional.ofNullable(combinedFilter), Optional.empty());
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

		String principalUuid = null;
		Object principal = authResult.principal();
		if (principal != null && authenticatorDomain.getEntityDefinition() != null) {
			ObjectAddress uuidAddr = authenticatorDomain.getEntityDefinition().uuid();
			if (uuidAddr != null) {
				try {
					Object val = DefaultMapper.reflection().getFieldValue(principal, uuidAddr.toString());
					principalUuid = val != null ? val.toString() : null;
				} catch (Exception e) {
					// ignore
				}
			}
		}

		String tenantId = null;
		if (principal != null) {
			ObjectAddress tenantAddr = authenticatorDomain.getTenantIdFieldAddress();
			if (tenantAddr != null) {
				try {
					Object val = DefaultMapper.reflection().getFieldValue(principal, tenantAddr.toString());
					tenantId = val != null ? val.toString() : null;
				} catch (Exception e) {
					// ignore
				}
			}
		}

		IDomainAuthorizationDefinition authzDef = (IDomainAuthorizationDefinition) authorizationDefinition(authenticatorDomain);

		return createAuthorizationEntity(authzDef, authResult, authenticatorDomain, principalUuid, tenantId);
	}

	@Expression(name = "authRequestTenantId", description = "Extracts the tenantId from an IAuthenticationRequest")
	public static @Nullable String authRequestTenantId(@Nullable Object request) {
		if (request instanceof IAuthenticationRequest authReq) {
			return authReq.tenantId();
		}
		return null;
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
			authzDomain.getRepository().save(authzEntity);
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
		IDomain<?> domain = toDomain(domainContext);
		DomainDefinition<?> domDef = toDomainDefinition(domain);
		if (domDef == null) {
			throw new ApiException("resolveKeyRealm: invalid domain context");
		}
		var secDef = domDef.domainSecurityDefinition();
		if (secDef == null || secDef.authenticatorDefinition() == null
				|| secDef.authenticatorDefinition().authorizationDefinition() == null) {
			throw new ApiException("resolveKeyRealm: no authenticator authorization configured on domain '"
					+ (domain != null ? domain.getDomainName() : "<null>") + "'");
		}
		var authzAuthDef = secDef.authenticatorDefinition().authorizationDefinition();

		// Mode A: supplier — takes priority when declared. The supplier owns its
		// materialization (Vault, HSM, in-memory test realm); the framework does
		// not look at usage().
		var supplierBuilder = authzAuthDef.keyRealm();
		if (supplierBuilder != null) {
			try {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				ISupplier<? extends IKeyRealm> supplier = (ISupplier) supplierBuilder.build();
				Optional<? extends IKeyRealm> realmOpt = supplier.supply();
				return realmOpt.orElseThrow(
						() -> new ApiException("resolveKeyRealm: key supplier returned empty for domain '"
								+ domain.getDomainName() + "'"));
			} catch (ApiException e) {
				throw e;
			} catch (Exception e) {
				throw new ApiException("resolveKeyRealm: failed to obtain IKeyRealm from supplier: " + e.getMessage(), e);
			}
		}

		// Mode B: persisted key entity domain — lookup-or-create, scoped by
		// AuthenticatorKeyUsage. The realmName encodes the scope ("global",
		// per-tenant, per-caller) so a single repository query by realmName
		// returns the right key for the caller.
		var keyConfig = authzAuthDef.keyDefinition();
		if (keyConfig != null && keyConfig.keyDomain() != null) {
			return resolvePersistedKeyRealm(domain, keyConfig, operationRequest);
		}

		throw new ApiException("resolveKeyRealm: domain '" + domain.getDomainName()
				+ "' declares a signable authorization but neither .key(supplier) nor .key(domain) "
				+ "was configured on its authenticator's authorization DSL");
	}

	private static IKeyRealm resolvePersistedKeyRealm(IDomain<?> authenticatorDomain,
			com.garganttua.api.commons.definition.IDomainAuthenticatorAuthorizationKeyDefinition keyConfig,
			@Nullable Object operationRequest) {
		IDomain<?> keyDomain = resolveKeyDomain(authenticatorDomain, keyConfig);
		if (keyDomain == null) {
			throw new ApiException("resolveKeyRealm: the configured .key(domain) entity class '"
					+ keyConfig.keyDomain().getClass().getName()
					+ "' did not resolve to a registered domain on the API");
		}
		com.garganttua.api.commons.definition.IDomainKeyDefinition keyEntDef =
				keyDomain.getDomainDefinition().keyDefinition();
		if (keyEntDef == null) {
			throw new ApiException("resolveKeyRealm: key domain '" + keyDomain.getDomainName()
					+ "' is not marked as a @Key domain — declare .key().realmName(...).publicMaterial(...)... on it");
		}

		ICaller caller = extractCaller(operationRequest);
		String realmName = buildRealmName(keyConfig.usage(), caller, keyDomain.getDomainName());

		IFilter filter = Filter.eq(keyEntDef.realmName().toString(), realmName);
		List<Object> existing;
		try {
			existing = keyDomain.getRepository().getEntities(Optional.empty(), Optional.of(filter), Optional.empty());
		} catch (Exception e) {
			throw new ApiException("resolveKeyRealm: failed to query key domain '" + keyDomain.getDomainName()
					+ "' for realmName '" + realmName + "': " + e.getMessage(), e);
		}
		IReflection reflection = DefaultMapper.reflection();
		boolean hadExistingButUnusable = false;
		if (existing != null && !existing.isEmpty()) {
			Object entity = pickUsable(existing, keyEntDef, reflection);
			if (entity != null) {
				return com.garganttua.api.core.security.key.KeyRealmFactory.materialize(entity, keyEntDef, reflection);
			}
			// All matching keys are expired or revoked — the caller's policy
			// flags decide whether the framework rotates silently or refuses.
			hadExistingButUnusable = true;
		}

		// No usable key. Two policy gates:
		//   - autoRotate=false + unusable key in storage → refuse; user owns rotation.
		//   - autoGenerate=false + nothing in storage    → refuse; key must be seeded out of band.
		if (hadExistingButUnusable && !keyConfig.autoRotate()) {
			throw new ApiException("resolveKeyRealm: the only key on domain '" + keyDomain.getDomainName()
					+ "' matching realmName '" + realmName + "' is expired or revoked, and "
					+ ".autoRotate(false) was configured. Rotate the key out of band, or enable "
					+ ".autoRotate(true) on the authenticator's .key(...) DSL.");
		}
		if (!hadExistingButUnusable && !keyConfig.autoGenerate()) {
			throw new ApiException("resolveKeyRealm: no key found on domain '" + keyDomain.getDomainName()
					+ "' for realmName '" + realmName + "', and .autoGenerate(false) was configured. "
					+ "Seed the key out of band, or enable .autoGenerate(true) on the authenticator's "
					+ ".key(...) DSL.");
		}

		Object newEntity = com.garganttua.api.core.security.key.KeyRealmFactory.generateAndStamp(
				keyDomain.getEntityClass(), keyEntDef,
				keyConfig.algorithm(), keyConfig.signatureAlgorithm(),
				realmName, keyConfig.duration(), keyConfig.unit(), reflection);
		stampIdentityAndTenancy(newEntity, keyDomain, keyConfig.usage(), caller, reflection);

		try {
			keyDomain.getRepository().save(newEntity);
		} catch (Exception e) {
			throw new ApiException("resolveKeyRealm: failed to persist freshly-generated key on domain '"
					+ keyDomain.getDomainName() + "' for realmName '" + realmName + "': " + e.getMessage(), e);
		}
		return com.garganttua.api.core.security.key.KeyRealmFactory.materialize(newEntity, keyEntDef, reflection);
	}

	private static IDomain<?> resolveKeyDomain(IDomain<?> authenticatorDomain,
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

	private static ICaller extractCaller(@Nullable Object operationRequest) {
		Object unwrapped = unwrapOptional(operationRequest);
		if (unwrapped instanceof IOperationRequest req) {
			ICaller caller = req.caller();
			if (caller != null) return caller;
		}
		return Caller.createAnonymousCaller();
	}

	private static String buildRealmName(com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage usage,
			ICaller caller, String keyDomainName) {
		String base = keyDomainName != null ? keyDomainName : "key";
		if (usage == null || usage == com.garganttua.api.commons.security.annotations.AuthenticatorKeyUsage.oneForAll) {
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

	private static Object pickUsable(List<Object> candidates,
			com.garganttua.api.commons.definition.IDomainKeyDefinition keyEntDef, IReflection reflection) {
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

	private static boolean isExpired(Object value) {
		long now = System.currentTimeMillis();
		if (value instanceof java.util.Date d) return d.getTime() <= now;
		if (value instanceof java.time.Instant i) return i.toEpochMilli() <= now;
		if (value instanceof Long l) return l <= now;
		return false;
	}

	private static void stampIdentityAndTenancy(Object entity, IDomain<?> keyDomain,
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
		IKeyRealm realm = resolveKeyRealm(domainContext, operationRequest);
		return signAuthorization(authzEntity, domainContext, realm);
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
		ObjectAddress dataMethod = authzDef.getDataToSignMethod();
		ObjectAddress sigField = authzDef.signatureField();
		if (dataMethod == null) {
			throw new ApiException("verifyAuthorizationSignature: signable authorization has no getDataToSign method configured");
		}
		if (sigField == null) {
			throw new ApiException("verifyAuthorizationSignature: signable authorization has no signature field configured");
		}
		IKeyRealm realm = (IKeyRealm) unwrapOptional(keyRealmObj);
		if (realm == null) {
			throw new ApiException("verifyAuthorizationSignature: keyRealm is null");
		}
		IReflection reflection = DefaultMapper.reflection();
		byte[] data;
		byte[] signature;
		IKey key;
		try {
			data = reflection.invokeMethod(
					authzEntity,
					dataMethod.toString(),
					IClass.getClass(byte[].class));
			if (data == null) {
				throw new ApiException("verifyAuthorizationSignature: getDataToSign returned null");
			}
			Object sigVal = reflection.getFieldValue(authzEntity, sigField.toString());
			if (!(sigVal instanceof byte[] sig)) {
				throw new ApiException("verifyAuthorizationSignature: signature field on authorization entity is empty or not a byte[]");
			}
			signature = sig;
			key = realm.getKeyForSignatureVerification();
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("verifyAuthorizationSignature failed: " + e.getMessage(), e);
		}
		// Crypto errors during verification (malformed signature bytes, decoding
		// failure, key/algorithm mismatch) map to "signature invalid" rather than
		// surfacing as a misconfiguration ApiException. A tampered token must
		// land as 401, not 500.
		try {
			return key.verifySignature(signature, data);
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
		IFilter filter = Filter.eq(uuidField.toString(), ownerUuid);
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
		return new com.garganttua.api.commons.security.authentication.Authentication(
				true,
				principal,
				null,
				tokenType,
				authorities,
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
		if (api instanceof com.garganttua.api.core.context.Api concrete) {
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

	@Expression(name = "buildAuthRequestFromAuthorization",
			description = "Wraps a decoded IAuthorization into an IAuthenticationRequest (credentials slot) so it can be forwarded to the authenticate pipeline.")
	public static IAuthenticationRequest buildAuthRequestFromAuthorization(@Nullable Object authorization, @Nullable Object tenantId) {
		IAuthorization authz = (IAuthorization) unwrapOptional(authorization);
		if (authz == null) {
			throw new ApiException("Authorization is null — cannot build authentication request");
		}
		String tenant = tenantId == null ? null : String.valueOf(unwrapOptional(tenantId));
		if (tenant != null && tenant.equals("null")) tenant = null;
		return new com.garganttua.api.core.security.authentication.AuthenticationRequest(null, authz, tenant);
	}

	@Expression(name = "verifyAuthorization",
			description = "Single server-side verification step used by VERIFY_AUTHORIZATION.gs. Resolves the authenticator domain (Mode A: from the protocol stashed on the request; Mode B: from the authz's runtime class). Verifies the signature when the resolved domain marks the authorization signable. Then either invokes the authenticate pipeline (when an authenticator is wired) or calls IAuthorization.validate() (intrinsic checks: expiration, revocation, custom rules). Mode B may not match any registered domain — that's allowed, and we fall through to authz.validate() without a target. Throws ApiException (→ 401) on signature mismatch or validation rejection.")
	public static IAuthentication verifyAuthorization(@Nullable Object apiContext,
			@Nullable Object authorization, @Nullable Object operationRequest) {
		IApi api = (IApi) unwrapOptional(apiContext);
		IAuthorization authz = (IAuthorization) unwrapOptional(authorization);
		if (api == null || authz == null) {
			throw new ApiException("verifyAuthorization: apiContext and authorization are required");
		}

		// Resolve the target authenticator domain. Null is tolerated: that's the
		// Mode B path where the caller's IAuthorization instance has no matching
		// registered domain (e.g. a stateless self-validating token).
		IDomain<?> targetDomain = resolveOptionalAuthenticatorDomain(api, operationRequest, authz);

		if (targetDomain != null) {
			boolean sigOk = verifyIfSignable(authz, targetDomain, operationRequest);
			if (!sigOk) {
				throw new ApiException("Authorization signature verification failed");
			}

			DomainDefinition<?> domDef = toDomainDefinition(targetDomain);
			boolean hasAuthenticator = domDef != null
					&& domDef.domainSecurityDefinition() != null
					&& domDef.domainSecurityDefinition().authenticatorDefinition() != null;
			if (hasAuthenticator) {
				Object tenantId = operationRequest == null ? null
						: ((IOperationRequest) unwrapOptional(operationRequest))
								.arg("tenantId").orElse(null);
				IAuthenticationRequest authRequest = buildAuthRequestFromAuthorization(authz, tenantId);
				return invokeAuthenticate(api, targetDomain, authRequest);
			}
		}

		// No target domain or target without authenticator — fall back to the
		// authorization's intrinsic validate(). Signed-but-untracked tokens still
		// have their signature checked above (when a target resolved); fully
		// untracked Mode B authorizations rely on validate() alone, which is what
		// the IAuthorization contract is for.
		try {
			authz.validate();
		} catch (ApiException ae) {
			throw ae;
		} catch (RuntimeException re) {
			throw new ApiException("Authorization validation failed: " + re.getMessage(), re);
		}
		return new com.garganttua.api.commons.security.authentication.Authentication(
				true, authz, null, authz, java.util.List.of(), true, true, true, true);
	}

	private static IDomain<?> resolveOptionalAuthenticatorDomain(IApi api, Object operationRequest, IAuthorization authz) {
		IClass<?> targetClass = AuthorizationProtocolExpressions
				.resolveAuthorizationTargetClass(operationRequest, authz);
		if (targetClass == null || api == null) return null;
		if (api instanceof com.garganttua.api.core.context.Api concrete) {
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
	public static IAuthentication invokeAuthenticate(@Nullable Object apiContext, @Nullable Object targetDomain, @Nullable Object authRequest) {
		IApi api = (IApi) unwrapOptional(apiContext);
		IDomain<?> domain = (IDomain<?>) unwrapOptional(targetDomain);
		IAuthenticationRequest req = (IAuthenticationRequest) unwrapOptional(authRequest);
		if (api == null || domain == null || req == null) {
			throw new ApiException("invokeAuthenticate: apiContext, targetDomain and authRequest must all be non-null");
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		IClass<?> entityClass = ((IDomain) domain).getEntityClass();
		com.garganttua.api.core.service.OperationRequest invocation =
				new com.garganttua.api.core.service.OperationRequest(new java.util.HashMap<>());
		invocation.arg(IOperationRequest.OPERATION,
				OperationDefinition.authenticate(domain.getDomainName(), entityClass));
		invocation.arg("entity", req);
		if (req.tenantId() != null) {
			invocation.arg(IOperationRequest.TENANT_ID, req.tenantId());
			invocation.arg(IOperationRequest.REQUESTED_TENANT_ID, req.tenantId());
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
}
