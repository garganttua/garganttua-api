# Authority Introspection — `.exposeAuthorities()`

An opt-in endpoint that lists every authority enforced anywhere on the API, aggregating operation-level and field-level authorities.

Opt-in endpoint that lists every authority enforced anywhere on the API.

```java
ApiBuilder.builder()
    .exposeAuthorities()
        .access(Access.authenticated)             // default
        .authority("ops:authorities:read")        // optional gate
        .up()
    .build();
```

At runtime:

```java
List<String> names = api.getAuthoritiesForCaller(caller);
// e.g. ["create-one-user", "delete-all-users", "user-update-name", ...]
```

The list aggregates two sources:

1. **Operation-level** — `OperationDefinition.effectiveAuthorityName()` for
   every operation. Either an explicit `.authority("name")` or the
   auto-generated `<technicalOp>-<scope>-<entity>` default.
2. **Field-level** — every non-null authority declared via
   `entity().update(field, "auth-name")`.

Defaults are conservative: `access=authenticated` (not anonymous —
exposing the matrix to the public would help an attacker map the
surface), no authority gate. Super-tenant / super-owner status does
**not** bypass the authority gate (nor the field-level one): being super
grants cross-tenant / cross-owner reach, not the authority to perform an
operation — a super caller must still carry the required authority.

Transport modules read `api.getAuthoritiesEndpoint()` to decide whether
to publish the route — `null` when not opted in, populated descriptor
otherwise.
