package com.garganttua.api.commons.context;

import java.util.List;
import java.util.Optional;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.IRequestBuilder;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.lifecycle.ILifecycle;
import com.garganttua.core.workflow.WorkflowExecutionOptions;

public interface IApi extends ILifecycle {

    Optional<IDomain<?>> getDomain(String domainName);

    String getSuperTenantId();

    boolean isMultiTenant();

    // --- Super-tenant / super-owner registries ---
    //
    // The framework keeps a server-side registry of which tenant ids are
    // super-tenants and which owner ids are super-owners. It is the single
    // source of truth for super-status: it is populated at startup by scanning
    // the tenant/owner domains for entities whose boolean superTenant/superOwner
    // field is true, maintained on create/update of those domains, and consulted
    // (never the protocol-provided flag) when the framework decides whether a
    // caller is a super-tenant / super-owner. See the @EntitySuperTenant /
    // @EntitySuperOwner fields and DomainBuilder.validateSuperFields.

    /** True when {@code tenantId} is a registered super-tenant. Null-safe (false). */
    boolean isSuperTenant(String tenantId);

    /** True when {@code ownerId} is a registered super-owner. Null-safe (false). */
    boolean isSuperOwner(String ownerId);

    /** An unmodifiable snapshot of the registered super-tenant ids. */
    java.util.Set<String> getSuperTenantIds();

    /** An unmodifiable snapshot of the registered super-owner ids. */
    java.util.Set<String> getSuperOwnerIds();

    /** Adds {@code id} to the super-tenant registry. No-op on null/blank. */
    void registerSuperTenant(String id);

    /** Removes {@code id} from the super-tenant registry. */
    void unregisterSuperTenant(String id);

    /** Adds {@code id} to the super-owner registry. No-op on null/blank. */
    void registerSuperOwner(String id);

    /** Removes {@code id} from the super-owner registry. */
    void unregisterSuperOwner(String id);

    /**
     * When true, promoting a tenant to super-tenant at runtime (create/update
     * with {@code superTenant=true} for an id not already registered) is
     * rejected. Locked by default; only the startup scan and the auto-created
     * master tenant may seed super-tenants. Demotion is always allowed.
     */
    boolean isSuperTenantCreationLocked();

    /** Owner-side counterpart of {@link #isSuperTenantCreationLocked()}. */
    boolean isSuperOwnerCreationLocked();

    List<ISerializer> getSerializers();

    List<IProtocol<?, ?>> getProtocols();

    List<IAuthorizationProtocol> getAuthorizationProtocols();

    // --- Authorities endpoint ---

    /**
     * Returns the descriptor for the framework-provided "list authorities"
     * endpoint, or {@code null} when the endpoint has not been exposed via
     * {@link com.garganttua.api.commons.context.dsl.IApiBuilder#exposeAuthorities()}.
     *
     * <p>Modules that publish the API over a transport (HTTP, RPC, …) check
     * this to decide whether to wire the corresponding route.
     */
    IAuthoritiesEndpoint getAuthoritiesEndpoint();

    /**
     * Returns the sorted, deduplicated list of every authority name enforced
     * anywhere on any domain. Two sources are merged:
     * <ul>
     *   <li><b>Operation-level</b> —
     *       {@code OperationDefinition.effectiveAuthorityName()} for every
     *       operation. Either an explicit {@code .authority("name")} or the
     *       auto-generated default.</li>
     *   <li><b>Field-level</b> — every non-null authority declared via
     *       {@code entity().update(field, "auth-name")} on the entity DSL.
     *       These guard a specific field of the update operation, independent
     *       of the operation-level authority.</li>
     * </ul>
     *
     * <p>This method is unprotected — the security check is done by
     * {@link #getAuthoritiesForCaller(com.garganttua.api.commons.caller.ICaller)}.
     * Direct calls bypass authentication and are reserved for framework
     * internals.
     */
    List<String> getAuthorities();

    /**
     * Returns {@link #getAuthorities()} after enforcing the access level and
     * (optional) authority configured via
     * {@link com.garganttua.api.commons.context.dsl.IApiBuilder#exposeAuthorities()}.
     *
     * <p>Throws {@link ApiException} when:
     * <ul>
     *   <li>the endpoint was not exposed at all;</li>
     *   <li>the caller does not meet the configured access level
     *       (anonymous-only callers on an authenticated endpoint, etc.);</li>
     *   <li>an authority gate was configured and the caller does not carry it.</li>
     * </ul>
     *
     * <p>Super-tenant and super-owner callers bypass the authority gate
     * (mirrors {@code callerHasAuthority}). They still must meet the access
     * level requirements.
     */
    List<String> getAuthoritiesForCaller(ICaller caller);

    // --- Request builder ---

    /**
     * Returns a fluent {@link IRequestBuilder} bound to the named domain.
     * Equivalent to {@code getDomain(domainName).orElseThrow().request()},
     * with a clearer error message when the domain is unknown.
     *
     * @param domainName the registered domain name (auto-generated as the
     *                   plural lowercase of the entity class name, e.g.
     *                   {@code "users"} for {@code User})
     * @return a fresh builder — chain {@code .caller(...)}, the CRUD shortcut
     *         ({@code readAll()}, {@code createOne(body)}, …), and
     *         {@code .execute()} or {@code .build().execute()}.
     * @throws com.garganttua.api.commons.ApiException if {@code domainName}
     *         does not resolve to a registered domain
     */
    IRequestBuilder request(String domainName);

    // --- Workflow invocation ---

    default IOperationResponse invoke(IOperationRequest request) {
        return getDomainOrThrow(request.domain()).invoke(request);
    }

    default IOperationResponse invoke(IOperationRequest request, WorkflowExecutionOptions options) {
        return getDomainOrThrow(request.domain()).invoke(request, options);
    }

    // --- CRUD convenience methods ---

    default IOperationResponse createOne(String domainName, Object body, ICaller caller) {
        return getDomainOrThrow(domainName).createOne(body, caller);
    }

    default IOperationResponse readOne(String domainName, String uuid, ICaller caller) {
        return getDomainOrThrow(domainName).readOne(uuid, caller);
    }

    default IOperationResponse readAll(String domainName, ICaller caller) {
        return getDomainOrThrow(domainName).readAll(caller);
    }

    default IOperationResponse readAll(String domainName, IFilter filter, IPageable page, ISort sort, ICaller caller) {
        return getDomainOrThrow(domainName).readAll(filter, page, sort, caller);
    }

    default IOperationResponse updateOne(String domainName, String uuid, Object body, ICaller caller) {
        return getDomainOrThrow(domainName).updateOne(uuid, body, caller);
    }

    default IOperationResponse deleteOne(String domainName, String uuid, ICaller caller) {
        return getDomainOrThrow(domainName).deleteOne(uuid, caller);
    }

    default IOperationResponse deleteAll(String domainName, ICaller caller) {
        return getDomainOrThrow(domainName).deleteAll(caller);
    }

    // --- Internal helper ---

    private IDomain<?> getDomainOrThrow(String domainName) {
        return getDomain(domainName)
                .orElseThrow(() -> new ApiException("Domain not found: " + domainName));
    }

}
