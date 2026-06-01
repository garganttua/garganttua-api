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
import com.garganttua.api.commons.definition.IDomainKeyDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.commons.operation.OperationDefinition;
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
		String principalUuid = readPrincipalUuid(authResult, authenticatorDomain);
		String tenantId = readPrincipalTenantId(authResult, authenticatorDomain);

		IDomainAuthorizationDefinition authzDef = (IDomainAuthorizationDefinition) authorizationDefinition(authenticatorDomain);

		return createAuthorizationEntity(authzDef, authResult, authenticatorDomain, principalUuid, tenantId);
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
					+ "' is not marked as a @Key domain — declare .key().name(...).keyForSignatureVerification(...)... on it");
		}

		ICaller caller = extractCaller(operationRequest);
		String realmName = buildRealmName(keyConfig.usage(), caller, keyDomain.getDomainName());

		IFilter filter = Filter.eq(keyEntDef.name().toString(), realmName);
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
				return materializeKeyRealm(entity, keyEntDef, reflection);
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

		Object newEntity = generateAndStampKeyEntity(
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
		return materializeKeyRealm(newEntity, keyEntDef, reflection);
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
			description = "Wraps a decoded authorization entity into an IAuthenticationRequest (credentials slot) so it can be forwarded to the authenticate pipeline. The credentials slot is an Object — strategies pattern-match on runtime type to decide whether they handle this shape.")
	public static IAuthenticationRequest buildAuthRequestFromAuthorization(@Nullable Object authorization, @Nullable Object tenantId) {
		Object authz = unwrapOptional(authorization);
		if (authz == null) {
			throw new ApiException("Authorization is null — cannot build authentication request");
		}
		String tenant = tenantId == null ? null : String.valueOf(unwrapOptional(tenantId));
		if (tenant != null && tenant.equals("null")) tenant = null;
		return new com.garganttua.api.core.security.authentication.AuthenticationRequest(null, authz, tenant);
	}

	@Expression(name = "verifyAuthorization",
			description = "Single server-side verification step used by VERIFY_AUTHORIZATION.gs. Resolves the authenticator domain (Mode A: from the protocol stashed on the request; Mode B: from the authz entity's runtime class). When the resolved domain has an authorization definition: verifies the signature (when signable), then either invokes the authenticate pipeline (when an authenticator is wired) or runs field-based validation from the DSL (`revoked` + `expiration` ObjectAddresses on IDomainAuthorizationDefinition). When Mode B has no matching registered domain (untracked token from a trusted in-process caller) the framework has no DSL to enforce against and accepts the pre-decoded entity as-is. Throws ApiException (→ 401) on signature mismatch or validation rejection.")
	public static IAuthentication verifyAuthorization(@Nullable Object apiContext,
			@Nullable Object authorization, @Nullable Object operationRequest) {
		IApi api = (IApi) unwrapOptional(apiContext);
		Object authz = unwrapOptional(authorization);
		if (api == null || authz == null) {
			throw new ApiException("verifyAuthorization: apiContext and authorization are required");
		}

		// Resolve the target authenticator domain. Null is tolerated: that's the
		// Mode B path where the caller's authorization entity has no matching
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

			// Target domain resolved but no authenticator — run DSL-driven
			// intrinsic checks (revoked flag, expiration timestamp) derived
			// from the field declarations on IDomainAuthorizationDefinition.
			// Custom validation rules belong on a future lifecycle hook on
			// the authz domain.
			validateAuthorizationFromDefinition(authz, targetDomain);
		}

		// No target domain (Mode B untracked token): trust the in-process
		// caller. Signature was already checked above when a target resolved;
		// without one, there's no DSL to derive intrinsic checks from.
		return new com.garganttua.api.commons.security.authentication.Authentication(
				true, authz, null, authz, java.util.List.of(), true, true, true, true);
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

	private static IKeyRealm materializeKeyRealm(Object entity, IDomainKeyDefinition keyDef, IReflection reflection) {
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

	private static Object generateAndStampKeyEntity(IClass<?> entityClass, IDomainKeyDefinition keyDef,
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
