package com.garganttua.api.core.repository;

import com.garganttua.api.core.filter.Filter;

import java.util.ArrayList;
import java.util.List;

import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.core.reflection.ObjectAddress;

import com.garganttua.core.observability.Logger;

/**
 * Utility class for building repository filters based on caller permissions and domain definition.
 *
 * This class handles the complex logic of combining filters for:
 * - Tenant isolation
 * - Owner-based access control
 * - Shared entities
 * - Hidden/visible entities
 * - Public entities
 */
public class RepositoryFilterTools {
	private static final Logger log = Logger.getLogger(RepositoryFilterTools.class);


    private RepositoryFilterTools() {
        // Utility class - no instantiation
    }

    /**
     * Builds a composite filter based on caller permissions and domain definition.
     *
     * @param caller The caller making the request
     * @param domainDefinition The domain definition with entity configuration
     * @param baseFilter Optional base filter to include
     * @return The composed filter, or null if no filtering needed
     */
    public static IFilter buildFilter(ICaller caller, IFilter baseFilter, IDomainDefinition<?> domainDefinition) {
        return buildFilter(caller, baseFilter, domainDefinition, true);
    }

    public static IFilter buildFilter(ICaller caller, IFilter baseFilter, IDomainDefinition<?> domainDefinition, boolean multiTenant) {
        if (caller == null) {
            return baseFilter;
        }
        log.debug("Building filter for domain {} with caller tenantId={}, ownerId={}, multiTenant={}",
                    domainDefinition.domainName(), caller.requestedTenantId(), caller.ownerId(), multiTenant);

        FilterContext filterContext = new FilterContext(caller, domainDefinition, multiTenant);

        // Super tenant without specific tenant ID bypasses tenant filtering
        if (filterContext.isSuperTenantWithoutTenant()) {
            log.debug("Super tenant access without tenant restriction");
            return baseFilter;
        }

        List<Filter> filters = new ArrayList<>();

        // Add base filter if provided
        if (baseFilter instanceof Filter filter) {
            filters.add(filter);
        }

        // Build tenant/visibility filter
        Filter accessFilter = buildAccessFilter(filterContext);
        if (accessFilter != null) {
            filters.add(accessFilter);
        }

        // Build owner filter
        Filter ownerFilter = buildOwnerFilter(filterContext);
        if (ownerFilter != null) {
            filters.add(ownerFilter);
        }

        return combineFilters(filters);
    }

    /**
     * Builds the access filter based on tenant, visibility, and sharing rules.
     */
    private static Filter buildAccessFilter(FilterContext ctx) {
        if (ctx.isPublicEntity && ctx.isHiddenableEntity) {
            return buildPublicHiddenableFilter(ctx);
        } else if (!ctx.isPublicEntity && ctx.isHiddenableEntity) {
            return buildPrivateHiddenableFilter(ctx);
        } else if (!ctx.isPublicEntity && !ctx.isHiddenableEntity) {
            return buildPrivateVisibleFilter(ctx);
        }
        return null;
    }

    /**
     * Filter for public entities that can be hidden.
     * Shows: tenant's own entities OR visible entities
     */
    private static Filter buildPublicHiddenableFilter(FilterContext ctx) {
        Filter tenantFilter = ctx.buildTenantFilter();
        Filter visibleFilter = ctx.buildVisibleFilter();

        if (tenantFilter != null && visibleFilter != null) {
            return Filter.or(tenantFilter, visibleFilter);
        }
        return visibleFilter;
    }

    /**
     * Filter for private entities that can be hidden.
     * Shows: (visible AND shared) OR tenant's own entities
     */
    private static Filter buildPrivateHiddenableFilter(FilterContext ctx) {
        Filter tenantFilter = ctx.buildTenantFilter();
        Filter visibleFilter = ctx.buildVisibleFilter();
        Filter shareFilter = ctx.buildShareFilter();

        if (ctx.isSharedEntity && visibleFilter != null && shareFilter != null) {
            Filter visibleAndShared = visibleFilter.andOperator(shareFilter);
            if (tenantFilter != null) {
                return visibleAndShared.orOperator(tenantFilter);
            }
            return visibleAndShared;
        }
        return tenantFilter;
    }

    /**
     * Filter for private entities that are always visible.
     * Shows: shared entities OR tenant's own entities
     */
    private static Filter buildPrivateVisibleFilter(FilterContext ctx) {
        Filter tenantFilter = ctx.buildTenantFilter();
        Filter shareFilter = ctx.buildShareFilter();

        if (ctx.isSharedEntity && shareFilter != null) {
            if (tenantFilter != null) {
                return shareFilter.orOperator(tenantFilter);
            }
            return shareFilter;
        }
        return tenantFilter;
    }

    /**
     * Builds the owner filter for owned entities.
     */
    private static Filter buildOwnerFilter(FilterContext ctx) {
        if (!ctx.isOwnedEntity || ctx.isSuperOwner) {
            return null;
        }
        return ctx.buildOwnerIdFilter();
    }

    /**
     * Combines multiple filters into a single AND filter.
     */
    private static IFilter combineFilters(List<Filter> filters) {
        if (filters.isEmpty()) {
            return null;
        }
        if (filters.size() == 1) {
            return filters.get(0);
        }
        Filter combined = Filter.and(filters.toArray(new Filter[0]));
        return combined;
    }

    /**
     * Creates a filter for UUID lookup.
     */
    public static IFilter createUuidFilter(ObjectAddress uuidFieldAddress, String uuid) {
        if (uuidFieldAddress == null || uuid == null) {
            return null;
        }
        return Filter.eq(uuidFieldAddress.toString(), uuid);
    }

    /**
     * Creates a filter for ID lookup.
     */
    public static IFilter createIdFilter(ObjectAddress idFieldAddress, String id) {
        if (idFieldAddress == null || id == null) {
            return null;
        }
        return Filter.eq(idFieldAddress.toString(), id);
    }

    /**
     * Creates a filter for UUID lookup using field name.
     */
    public static IFilter createUuidFilter(String uuidFieldName, String uuid) {
        if (uuidFieldName == null || uuid == null) {
            return null;
        }
        return Filter.eq(uuidFieldName, uuid);
    }

    /**
     * Creates a filter for ID lookup using field name.
     */
    public static IFilter createIdFilter(String idFieldName, String id) {
        if (idFieldName == null || id == null) {
            return null;
        }
        return Filter.eq(idFieldName, id);
    }

    /**
     * Internal context class to hold filter building state.
     */
    private static class FilterContext {
        final String requestedTenantId;
        final String ownerId;
        final boolean isSuperOwner;
        final boolean isSuperTenant;
        final boolean isSharedEntity;
        final boolean isHiddenableEntity;
        final boolean isOwnedEntity;
        final boolean isPublicEntity;
        final boolean multiTenant;
        final IDomainDefinition<?> domainDefinition;

        FilterContext(ICaller caller, IDomainDefinition<?> domainDefinition, boolean multiTenant) {
            this.domainDefinition = domainDefinition;
            this.multiTenant = multiTenant;
            this.requestedTenantId = caller.requestedTenantId();
            this.ownerId = caller.ownerId();
            this.isSuperOwner = caller.superOwner();
            this.isSuperTenant = caller.superTenant();
            this.isSharedEntity = domainDefinition.shared() != null;
            this.isHiddenableEntity = domainDefinition.hiddenable() != null;
            this.isOwnedEntity = domainDefinition.owned() != null;
            this.isPublicEntity = Boolean.TRUE.equals(domainDefinition.publik());
        }

        boolean isSuperTenantWithoutTenant() {
            if (!multiTenant) return false;
            return isSuperTenant && (requestedTenantId == null || requestedTenantId.isEmpty());
        }

        Filter buildTenantFilter() {
            if (!multiTenant) return null;
            if (requestedTenantId == null || domainDefinition.entityDefinition() == null) {
                return null;
            }
            ObjectAddress filterField = domainDefinition.entityDefinition().tenantId();
            // For a tenant entity (domain marked .tenant(true)) that does NOT
            // carry a tenantId field, the entity's uuid plays the role of
            // tenantId — without this fallback a non-super caller would see
            // every tenant row (no filter → no isolation). When tenantId IS
            // configured we keep filtering on it for backwards compatibility
            // with domains that mark .tenant(true) + .tenantId(field) together.
            if (filterField == null && Boolean.TRUE.equals(domainDefinition.tenant())) {
                filterField = domainDefinition.entityDefinition().uuid();
            }
            if (filterField == null) {
                return null;
            }
            return Filter.eq(filterField.toString(), requestedTenantId);
        }

        Filter buildShareFilter() {
            if (!multiTenant) return null;
            ObjectAddress shareField = domainDefinition.shared();
            if (!isSharedEntity || requestedTenantId == null || shareField == null) {
                return null;
            }
            return Filter.eq(shareField.toString(), requestedTenantId);
        }

        Filter buildVisibleFilter() {
            ObjectAddress hiddenField = domainDefinition.hiddenable();
            if (!isHiddenableEntity || isSuperOwner || hiddenField == null) {
                return null;
            }
            return Filter.eq(hiddenField.toString(), false);
        }

        Filter buildOwnerIdFilter() {
            ObjectAddress ownedField = domainDefinition.owned();
            if (ownerId == null || ownedField == null) {
                return null;
            }
            return Filter.eq(ownedField.toString(), ownerId);
        }
    }
}
