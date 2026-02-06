[![Maven Package](https://github.com/garganttua/garganttua-api/actions/workflows/maven-publish.yml/badge.svg)](https://github.com/garganttua/garganttua-api/actions/workflows/maven-publish.yml)

# Garganttua API

## Repository Filter Business Rules

The `RepositoryFilterTools` class in `garganttua-api-core` implements complex filtering logic for multi-tenant data access. This section documents the business rules that determine which entities are visible to callers.

### Caller Privileges

| Privilege | Description |
|-----------|-------------|
| **Super Tenant** | Can access entities across all tenants. If no specific tenant is requested, bypasses all tenant filtering. |
| **Super Owner** | Can access entities regardless of ownership. Bypasses owner-based filtering. |
| **Regular Caller** | Subject to tenant isolation and ownership rules. |

### Entity Configuration Flags

| Flag | Description |
|------|-------------|
| **public** | Entity is publicly accessible (no tenant restriction for visibility) |
| **hiddenable** | Entity has a `hidden` field that can hide it from non-super-owners |
| **shared** | Entity can be shared with specific tenants via a `shareWith` field |
| **owned** | Entity belongs to a specific owner (user) via an `ownerId` field |
| **tenant** | Entity belongs to a specific tenant via a `tenantId` field |

### Access Filter Matrix

The access filter determines which entities are visible based on entity configuration:

| Public | Hiddenable | Shared | Filter Logic |
|:------:|:----------:|:------:|--------------|
| ✓ | ✓ | - | `tenantId = callerTenant` **OR** `hidden = false` |
| ✓ | ✗ | - | No filter (all entities visible) |
| ✗ | ✓ | ✓ | (`hidden = false` **AND** `shareWith = callerTenant`) **OR** `tenantId = callerTenant` |
| ✗ | ✓ | ✗ | `tenantId = callerTenant` |
| ✗ | ✗ | ✓ | `shareWith = callerTenant` **OR** `tenantId = callerTenant` |
| ✗ | ✗ | ✗ | `tenantId = callerTenant` |

### Owner Filter Rules

| Condition | Filter Applied |
|-----------|----------------|
| Entity is **owned** AND caller is **not super owner** | `ownerId = callerOwnerId` |
| Entity is **not owned** OR caller is **super owner** | No owner filter |

### Super Tenant Bypass

| Condition | Behavior |
|-----------|----------|
| Caller is **super tenant** AND no specific tenant requested | All tenant/access filtering bypassed |
| Caller is **super tenant** AND specific tenant requested | Filters applied for requested tenant |
| Caller is **not super tenant** | Standard filtering applied |

### Filter Combination

All applicable filters are combined using **AND** logic:

```
Final Filter = baseFilter AND accessFilter AND ownerFilter
```

### Examples

#### Example 1: Private Shared Entity
Configuration: `public=false`, `hiddenable=true`, `shared=true`

A caller from tenant "T1" will see:
- Entities where `hidden=false` AND `shareWith=T1`
- OR entities where `tenantId=T1`

#### Example 2: Public Hiddenable Entity
Configuration: `public=true`, `hiddenable=true`

A caller will see:
- Their own tenant's entities (`tenantId=callerTenant`)
- OR any visible entities (`hidden=false`)

#### Example 3: Super Tenant Access
A super tenant caller without a specific tenant request bypasses all tenant filtering and sees all entities (subject to owner filtering if applicable)
