# CORE EVOLUTION — Public, `java.time`-aware single-value coercer for DAOs

**Status**: proposal
**Affects**: `garganttua-core` — module `garganttua-mapper` (`com.garganttua.core.mapper`)
**Filed by**: garganttua-api team
**Date**: 2026-06-11
**Related**: `garganttua-api-dao-mongodb` `MongoDao` now hand-rolls the same coercion (`coerce`/`fromDate`/`fromNumber`/`fromString`) because it cannot reach a core equivalent. See `MongoDaoTypeFidelityTest`.

## Context

A DAO that reads from a loosely-typed store (MongoDB's `Document`, a JDBC `ResultSet`,
a JSON map) gets values back whose runtime type does not match the declared DTO field
type. The store's codec is lossy across the JVM type system:

| Declared field type      | What the MongoDB `Document` codec hands back |
|--------------------------|----------------------------------------------|
| an `enum`                | `String` (the name)                          |
| `java.time.Instant`      | `java.util.Date`                             |
| `long` / `Long`          | `Integer` (when the value fits in 32 bits)   |
| `int` from a config row  | `String`                                     |

`MongoDao` previously did a brute-force `field.set(instance, doc.get(name))`, which throws
`IllegalArgumentException` for every row above. To fix the round trip we needed a
**single-value coercer**: `(Object value, target type) -> coerced value`.

## What core offers today, and why it does not fit

`com.garganttua.core.mapper.rules.ImplicitConversions` already has exactly this shape:

```java
public static Optional<Function<Object, Object>> findConversion(IClass<?> source, IClass<?> dest)
```

…but it is unusable from a DAO for three reasons:

1. **Not on the DAO's classpath.** `garganttua-api-dao-mongodb` depends on
   `garganttua-commons` + `garganttua-reflection`, not `garganttua-mapper`. Pulling the
   whole mapper in just for one helper is a heavy, layering-muddying dependency.
2. **Internal package.** `…mapper.rules.ImplicitConversions` lives in a `.rules.`
   sub-package that reads as mapper-internal, not public API — depending on it couples the
   DAO to an implementation detail.
3. **It does not cover the case we actually hit.** `java.time.Instant` ↔ `java.util.Date`
   is *not* an implicit conversion — `LeafTypes` lists both as leaves and the mapper passes
   them through untouched. The single most common MongoDB mismatch (datetime → `Date`) is
   precisely the one core does not handle. So even after taking the mapper dependency, the
   DAO would still hand-roll the temporal half.

The whole-object entry point (`IMapper.map(source, destClass)`) does not help either: the
source here is an `org.bson.Document` (a `Map`), not a bean whose declared fields mirror the
target — the mapper reads source *fields*, so `map(document, Dto.class)` finds nothing.

## Proposal

Expose a **public**, **`java.time`-aware** single-value coercer in `garganttua-mapper`'s
public API surface (not `.rules.`), e.g.:

```java
package com.garganttua.core.mapper;

public interface IValueCoercer {
    /** Adapt {@code value} to {@code targetType}; return it untouched when no rule applies. */
    Object coerce(Object value, IClass<?> targetType);

    /** The matching converter, if any — for callers that want to cache per (source,target). */
    Optional<Function<Object, Object>> conversion(IClass<?> sourceType, IClass<?> targetType);
}
```

with a zero-arg factory (`ValueCoercer.standard()`) so a DAO can use it without standing up
a full `Mapper`. The standard ruleset should fold in what `ImplicitConversions` already does
**plus**:

- `java.util.Date` → `Instant` / `LocalDateTime` / `LocalDate` / `ZonedDateTime` /
  `OffsetDateTime` (at a caller-or-UTC zone) and the reverse;
- `Number` → any boxed/primitive numeric (widen/narrow), not just `String` → numeric;
- `String` ↔ enum / numeric / boolean (already present).

`Mapper` itself would then delegate per-field coercion to the same `IValueCoercer`, so the
whole-object and single-value paths share one ruleset.

## Why this belongs in core, not the DAO

Every DAO backend (Mongo, SQL, a future Redis/Cassandra binding) faces the identical
lossy-codec problem and will otherwise each grow its own copy of `fromDate`/`fromNumber`.
A coercer is generic type-system plumbing — the same concern the mapper already owns — so a
single public home keeps the rule set consistent (one place decides how `Date` becomes
`LocalDate`) and lets every DAO collapse its hand-rolled block to one delegated call.

## Impact on garganttua-api once it lands

`MongoDao` drops its private `coerce`/`fromDate`/`fromNumber`/`fromString` (≈70 lines) and
calls the core coercer:

```java
private final IValueCoercer coercer = ValueCoercer.standard();
...
field.set(instance, coercer.coerce(value, field.getType()));
```

`garganttua-api-dao-mongodb` takes a (light) dependency on `garganttua-mapper` for the
coercer interface only. The hand-rolled enum-on-write (`toStorable`) can stay DAO-side (it
is a storage-shape choice, not a type conversion) or move behind the same coercer's reverse
direction.

## Test plan (core side)

1. `ValueCoercerTest`: `"ACTIVE"` → `Status.ACTIVE`; `Date` → the exact same `Instant`;
   `Integer 5` → `Long 5`; `"42"` → `int 42`; unknown pair → value returned untouched.
2. `Mapper` regression: whole-object mapping still passes after delegating per-field coercion
   to the shared coercer.

---

Until this ships, the coercion lives in `garganttua-api-dao-mongodb/MongoDao` with
`MongoDaoTypeFidelityTest` pinning the behavior, so the migration is a straight delete-and-delegate.
