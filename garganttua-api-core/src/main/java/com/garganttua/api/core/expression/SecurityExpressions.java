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
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IContextualMethodBinder;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.IMethodReturn;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.runtime.RuntimeExpressionContext;

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

			return entity;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to create authorization entity: " + e.getMessage(), e);
		}
	}

	@Expression(name = "lookupValidAuthorization", description = "Looks up a valid (non-expired, non-revoked) authorization owned by the principal via the authorization domain's readAll workflow.")
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

			ICaller superCaller = Caller.createSuperCaller();
			var response = authzDomain.readAll(combinedFilter, null, null, superCaller);
			if (response.getResponseCode() == com.garganttua.api.commons.service.OperationResponseCode.OK
					&& response.getResponse() instanceof java.util.List<?> results
					&& !results.isEmpty()) {
				return results.get(0);
			}
			return null;
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
