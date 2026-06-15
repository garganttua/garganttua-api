# Field-Level Update Authority

Guard mutation of a specific field on an update operation, independent of the operation-level authority, with a silent-skip-on-deny policy.

`entity().update(field, "auth-name")` guards mutation of a specific
field on an update operation, independent of the operation-level
authority. The rules in `EntityUpdater`:

- No authority required (`update(field)` or empty string) → field
  always updated.
- `caller.authorities()` is `null` or empty → field skipped.
- Otherwise → `authorities.contains(required)` decides.

There is **no super bypass**: `superTenant` / `superOwner` status grants
cross-tenant / cross-owner reach, not the authority to mutate a guarded
field — a super caller must still carry the named authority.

The unauthorized update is **silently skipped**, not failed with 403 —
the operation continues and other fields update normally. 403 stays a
workflow-level concern via `VERIFY_AUTHORITY`.
