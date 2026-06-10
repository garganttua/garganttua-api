# Repository Filter & Access Business Rules

How Garganttua API decides **who the caller is** and **which entities it may see**, under
multi-tenancy, ownership, and entity-characteristic rules.

> **Status legend** — 🟢 *in place* (implemented + tested) · 🟡 *target* (decided, not yet
> implemented) · ⚠️ *open question*.

Two layers cooperate:

1. **Caller identity resolution** — establishes the caller's `tenantId` / `ownerId` and
   `superTenant` / `superOwner` flags **from the verified authorization** (the token).
2. **Repository filtering** (`RepositoryFilterTools`) — turns that identity plus the entity's
   role/characteristic flags into a data filter.

---

## 1. Caller identity resolution

### 1.1 Current state 🟢

| Source | Where | Behaviour |
|---|---|---|
| Caller built | `JavalinProtocol.getCaller` | `tenantId` / `ownerId` / `callerId` read from the **headers** `X-Tenant-Id` / `X-Owner-Id` / `X-Caller-Id`; `superTenant`/`superOwner` = `false`. |
| Token verified | `SecurityExpressions.verifyAuthorization` | The token's own tenant is used **only** for the verification lookup — never written back onto the caller. |
| Super recomputed | `applyServerAuthoritativeSuperStatus` (`VERIFY_AUTHORIZATION.gs`) | Recomputes `superTenant`/`superOwner` from the **server registries** (`api.isSuperTenant`/`isSuperOwner`) — a server-side consult. **Preserves** the header `tenantId`/`ownerId`. |

> ⚠️ **Consequence (isolation gap when the transport is exposed)**: the operational identity
> (tenant/owner) comes from the **headers**, not the token. With no header an `authenticated`
> operation passes without a tenant; with a **wrong** header it is accepted (there is no
> `caller.tenantId == token.tenantId` guard).

### 1.2 The verified token is authoritative 🟢

"Verifying an authorization" ≡ "an authentication": `verifyAuthorization` produces an
`IAuthentication` carrying the principal's `tenantId`/`ownerId`/`isSuperTenant`/`isSuperOwner`.
`IAuthentication.reconcile(protocolCaller)` then **rebuilds the caller from the token** (the
proven identity), overriding the headers — wired into `VERIFY_AUTHORIZATION.gs` (replacing the
old header-trusted caller). `Access.tenant`/`Access.owner` and the `VERIFY_TENANT`/`VERIFY_OWNER`
gates have been removed.

| # | Rule | Detail |
|---|---|---|
| **R1** (tenant cross-tenant) | token is **super** AND `header.tenant ≠ token.tenant` | `requestedTenantId = header.tenant`; `tenantId = token.tenant`. |
| **R1-err** | `header.tenant ≠ token.tenant` AND token is **not super** | **Reject** ⚠️ (the authenticated user's tenant is not a super tenant) — *reject, not silent (to confirm)*. |
| **R2a** | token is **super** AND `caller.tenant == null` | data of **all tenants** (no tenant filter). |
| **R2b** | token is **not super** AND `caller.tenant == null` | `caller.tenantId = token.tenant` (scoped to the token's tenant). |
| **R3** | same as R1/R2 for **owner** (`superOwner`, `ownerId`). |

Consequence: **`Access.tenant` / `Access.owner` become redundant** (the token always carries the
scope) → access collapses to **`anonymous` vs `authenticated`**, and isolation becomes automatic.

---

## 2. Caller privileges (filtering)

| Privilege | Description |
|-----------|-------------|
| **Super Tenant** | Accesses entities across all tenants. With no tenant requested, **bypasses** tenant filtering entirely. |
| **Super Owner** | Accesses entities regardless of ownership. **Bypasses** owner filtering. |
| **Regular Caller** | Subject to tenant + owner isolation. |

### Super-owner visibility 🟢

| Case | Sees |
|---|---|
| **non-super** tenant + **super owner** | all data **of its tenant**, across all owners. |
| **super** tenant + **super owner** | all data of **all tenants**. |

*Implemented by composition: `buildOwnerFilter` → `null` when super owner (no owner filter) ∧
the tenant filter still applies (or is bypassed when super tenant).*

---

## 3. Entity configuration flags

| Flag | Description |
|------|-------------|
| **public** | Visible with no tenant restriction (cross-tenant). |
| **hiddenable** | `hidden` field: a non-super-owner does not see hidden entities. |
| **shared** | `shareWith` field: an **owned** entity's owner shares it with **another owner** (`shareWith` holds owner ids → `shareWith = callerOwnerId`). `shared` ⟹ `owned`. 🟡 *target — the code currently scopes per tenant.* |
| **owned** | Belongs to an owner via `ownerId`. |
| **tenant** | Belongs to a tenant via `tenantId`. |

---

## 4. Access filter matrix 🟢

`buildAccessFilter` (`RepositoryFilterTools`) by public / hiddenable / shared:

| Public | Hiddenable | Shared | Filter |
|:------:|:----------:|:------:|--------|
| ✓ | ✓ | - | `tenantId = callerTenant` **OR** `hidden = false` |
| ✓ | ✗ | - | no filter (all visible) |
| ✗ | ✓ | ✓ | (`hidden = false` **AND** `shareWith = callerTenant`) **OR** `tenantId = callerTenant` |
| ✗ | ✓ | ✗ | `tenantId = callerTenant` |
| ✗ | ✗ | ✓ | `shareWith = callerTenant` **OR** `tenantId = callerTenant` |
| ✗ | ✗ | ✗ | `tenantId = callerTenant` |

### Owner filter 🟢

| Condition | Filter |
|-----------|--------|
| **owned** AND caller is **not** super owner | `ownerId = callerOwnerId` |
| **not** owned **OR** super owner | no owner filter |

> 🟡 **Target — owner-scoped sharing.** Since `shared` ⟹ `owned` and shares are between
> owners, the `shared` characteristic extends the **owner** filter (not the tenant filter):
> for a `shared` + `owned` entity, a non-super-owner sees `ownerId = callerOwnerId` **OR**
> `shareWith = callerOwnerId`. `hidden = false` still gates a `hiddenable` entity. The matrix
> in §4 documents the **current** (tenant-scoped) behaviour, pending this change.

### Super-tenant bypass 🟢

| Condition | Behaviour |
|-----------|-----------|
| super tenant AND no tenant requested | all tenant/access filtering bypassed |
| super tenant AND a tenant requested | filters applied for the requested tenant |
| not super tenant | standard filtering |

### Combination

```
Final Filter = baseFilter AND accessFilter AND ownerFilter
```

---

## 5. Multi-tenancy toggle 🟢

`.multiTenant(false)`:
- `superTenantId()`, `superTenantAutoCreate()`, `domain().tenant(true)` throw `ApiException`.
- **tenant** and **share** filters disabled (`buildTenantFilter`/`buildShareFilter` → `null`).
- **owner** and **visibility** filters remain active.
- `@EntityUnicity(scope=TENANT)` behaves as `GLOBAL`.

---

## 6. Synthesis — rule → code → status

| Rule | Code | Status |
|---|---|---|
| Caller from headers (protocol) | `JavalinProtocol.getCaller:58` | 🟢 |
| Caller rebuilt from the token | `IAuthentication.reconcile` + `SecurityExpressions.reconcileCaller` (in `VERIFY_AUTHORIZATION.gs`) | 🟢 |
| Super recomputed (registry) on resolved home | `SecurityExpressions.applyServerAuthoritativeSuperStatus:277` | 🟢 |
| R1/R1-err — cross-tenant from token (reject non-super) | `IAuthentication.reconcile` | 🟢 |
| R2b — default tenant = token's tenant | `IAuthentication.reconcile` | 🟢 |
| R2a — super with no tenant → all tenants | `reconcile` (requested*=null) + `RepositoryFilterTools.isSuperTenantWithoutTenant` | 🟢 |
| R3 — same for owner (incl. cross-owner via requestedOwnerId) | `IAuthentication.reconcile` | 🟢 |
| Super-owner visibility (R4) | `buildOwnerFilter:149` | 🟢 |
| public / hiddenable | `buildAccessFilter:85`, `buildVisibleFilter` | 🟢 |
| shared — current (per tenant) | `buildShareFilter:264` (`shareWith = requestedTenantId`) | 🟢 |
| shared — target (per owner, ⟹ owned) | `buildShareFilter` / `buildOwnerFilter` *(to rework: `shareWith = callerOwnerId`)* | 🟡 |
| Owner filter | `buildOwnerIdFilter:281` | 🟢 |
| multiTenant toggle | `FilterContext` (`!multiTenant` → null) | 🟢 |
| `Access.tenant`/`Access.owner` gates | *removed — folded into `reconcile` + the filter* | 🟢 |

---

## 7. Decisions & open questions

1. ✅ **Resolved — `shared` is owner-scoped**: a `shared` entity is necessarily `owned`; an owner
   shares with **another owner** (`shareWith = callerOwnerId`). 🟡 *code change pending (currently
   tenant-scoped).*
2. ✅ **Resolved — R1-err rejects**: a header tenant/owner contradicting a non-super token →
   **403** (`IAuthentication.reconcile` throws), not silently ignored.
3. ✅ **Resolved — `Access.tenant` / `Access.owner` removed**: the token-authoritative caller
   resolution (R1-R3) shipped; the enum keeps only `anonymous` / `authenticated`.

---

## 8. Examples

**Private shared** (`public=false, hiddenable=true, shared=true`) — a caller from T1 sees:
`hidden=false AND shareWith=T1`, **or** `tenantId=T1`.

**Public hiddenable** (`public=true, hiddenable=true`) — a caller sees:
its own entities (`tenantId=callerTenant`) **or** any visible entity (`hidden=false`).

**Super tenant** with no tenant requested: full bypass → all entities (subject to the owner filter).
