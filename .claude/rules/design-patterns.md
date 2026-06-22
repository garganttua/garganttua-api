# Design Patterns
---
paths:
  - "**/*.java"
---

Imported from garganttua-core, adapted to garganttua-api's patterns.

## Hierarchical DSL Builder

Context construction is a fluent, navigable builder tree. Builder interfaces live in
`garganttua-api-commons/context/dsl/`, implementations in `garganttua-api-core/builder/`.
Navigate back to a parent with `up()`; terminal is `build()`.

```java
ApiBuilder.builder()
    .domain(User.class)
        .entity().id("id").uuid("uuid").up()
        .dto(UserDto.class).id("id").db(new InMemoryDao()).up()
    .up()
    .build();
```

## Supplier + declarative injection

`ISupplier<T>` provides lazy/deferred values throughout. **Every Supplier needs a param
`@annotation` + an `@Resolver IElementResolver` returning its `SupplierBuilder`** — this is how
parameters are auto-wired (no core change). New suppliers must follow this pair.

## Method Binder

Dynamic binding via garganttua-core reflection (`core/builder/binder/`): wires lifecycle hooks
(beforeCreate, afterGet…) and security methods (authenticate, sign, validate) at build time.

## Pipeline

Service execution flows `IPipeline` → `IPhase` → `IPhaseScript`; script definitions live under
`garganttua-api-core/src/main/resources/scripts/` (see `PIPELINE.md`). New operation types
**decalque the CRUD path** — mirror it, never build a parallel mechanism.

## Definition / Context separation

Definitions (immutable config: `EntityDefinition`, `DomainDefinition`) are built once; Contexts
(runtime: `EntityContext`, `Domain`) aggregate them and provide services (`invoke(IServiceRequest)`).

## Inter-stage data

Carry data between pipeline stages via **workflow-scoped variables / numbered argument injection** —
never ThreadLocal or other hacks.

## Collaboration with garganttua-core

Do not work around a core bug or gap — file a bug / evolution proposal to the core team instead.
