# Field-Level Authority (create & update)

Guard, per field, who may **valorize** it at creation and who may **mutate** it on update — independent of the operation-level authority. The authority rules are shared (`EntityCreator` / `EntityUpdater`):

- No authority required (`create(field)` / `update(field)`, or an empty authority) → the field is allowed.
- `caller.authorities()` is `null` or empty → a *guarded* field is denied.
- Otherwise → `authorities.contains(required)` decides.

There is **no super bypass**: `superTenant` / `superOwner` status grants cross-tenant / cross-owner reach, not the authority to valorize or mutate a guarded field — a super caller must still carry the named authority.

## Update — guard a mutation (silent-skip)

`entity().update(field[, "auth-name"])` declares the updatable fields. Only declared fields are ever merged onto the stored entity; for a guarded one the caller must carry the authority. A denied (or undeclared) field is **silently skipped** — the operation continues and the other fields update normally; 403 stays an operation-level concern via `VERIFY_AUTHORITY`.

```java
entity()
    .update("email")                       // freely updatable
    .update("name", "user-update-name")    // mutable only with the authority
```

## Create — authorize valorization (whitelist, strip-on-deny)

`entity().create(field[, "auth-name"])` declares the fields a caller may valorize at creation. Declaring **any** `create(...)` turns creation into a **whitelist**: only the declared fields the caller is authorized for survive on the inbound entity; every other client-supplied field is **stripped** (set to null) before the framework stamps `uuid`/`tenantId`/`ownerId` and persists. With **no** `create(...)` declared, creation is unrestricted (the client body is kept as-is — backward compatible).

```java
entity()
    .create("name")                        // free to valorize
    .create("role", "admin-create-role")   // valorized only with the authority
```

```
POST {name, role, secret}
  caller WITHOUT 'admin-create-role'  -> {name}          (role guarded → stripped; secret undeclared → stripped)
  caller WITH    'admin-create-role'  -> {name, role}    (secret undeclared → stripped)

// no .create(...) declared at all      -> {name, role, secret}  (unrestricted)
```

The strip runs **before** framework stamping, so `uuid`/`tenantId`/`ownerId` are still set by the `ensure*` stages (a client cannot pin them unless explicitly whitelisted — a useful security default). A *mandatory* field that ends up stripped then fails `validateMandatories` (400). Primitive fields cannot hold null and are never stripped.

## Notes

- Both are persistence-layer concerns enforced inside the business stage (`createEntity` / `updateEntity` expressions in `CREATE_ONE.gs` / `UPDATE_ONE.gs`); the entity keeps its declared shape.
- The asymmetry — create defaults to *all allowed*, update defaults to *nothing updatable* — is intentional: creation must persist the body by default, whereas mutation is restrictive by default.
