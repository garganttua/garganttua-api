# Garganttua API DAO

## Description

Garganttua API DAO is the data-access layer of the garganttua-api framework. It defines the persistence contract that every DTO storage backend must fulfil (`IDao`, `IRepository`) and groups the concrete implementations of those contracts as Maven submodules. The split between abstraction and implementation is intentional: core engine code in `garganttua-api-core` and `garganttua-api-commons` depends only on the interfaces; application code chooses a concrete backend by wiring a specific `IDao` instance into the DSL via `.db(...)`, without coupling the domain model to any particular database technology.

**Key Features:**
- **`IDao` contract** — five-method persistence interface (`registerDomain`, `find`, `save`, `delete`, `count`) that every backend implements; supports optional pagination (`IPageable`), filtering (`IFilter`), and sorting (`ISort`)
- **`IRepository` contract** — entity-oriented facade used by the service pipeline; bridges between domain entities and their underlying DTOs, handling mapping in both directions and forwarding to the appropriate `IDao`
- **DSL binding via `.db(...)`** — each DTO declared in the fluent builder is associated with an `IDao` instance through the `.db(IDao)` (or supplier-based) overload on `IDtoBuilder`; the builder enforces that exactly one `IDao` is present before context construction
- **Multi-DAO / multi-DTO support** — a single domain entity can be split across multiple DTOs, each backed by a different `IDao`, and `Repository` merges the results by UUID at read time
- **AOT / native-image readiness** — concrete implementations carry `@Reflected` and register an `IAOTInfrastructureSeed` via `ServiceLoader`; they resolve correctly under `AOTReflectionProvider` without the runtime reflection library

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-dao</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies

<!-- AUTO-GENERATED-END -->

## Core Concepts

### IDao — the low-level persistence contract

`IDao` (`com.garganttua.api.commons.dao.IDao`) is the interface every storage backend must implement:

```java
public interface IDao {
    void registerDomain(IDomainDefinition<?> domainDefinition);
    List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort) throws ApiException;
    Object save(Object object) throws ApiException;
    void delete(Object object) throws ApiException;
    long count(IFilter filter) throws ApiException;
}
```

`registerDomain` is called once at context-build time so the DAO can capture the DTO class and any schema metadata it needs. All subsequent calls operate on raw `Object` values; the repository layer handles entity-to-DTO mapping before calling `save`/`delete` and DTO-to-entity mapping after `find`.

### IRepository — the entity-oriented facade

`IRepository` (`com.garganttua.api.commons.repository.IRepository`) is the higher-level contract consumed by the service pipeline. Its implementation (`Repository` in `garganttua-api-core`) holds a list of `IDtoContext` (one per DTO), maps every entity write to all DTOs via the garganttua-core `Mapper`, and merges the per-DTO result sets back into entities on reads.

### Binding a DAO to a DTO via `.db(...)`

In the fluent DSL, each DTO sub-builder exposes `.db(IDao dao)` (or a supplier-based overload for DI-managed backends). The builder rejects a missing DAO at build time with a descriptive error message:

```java
ApiBuilder.builder()
    .superTenantId("SUPER")
    .domain(User.class)
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(UserDto.class)
            .id("id").uuid("uuid").tenantId("tenantId")
            .db(new MongoDao(database, "users"))   // bind the backend here
        .up()
        .creation(true).readAll(true).readOne(true)
    .up()
    .build();
```

Calling `.build()` without `.db(...)` throws `ApiException` with an example snippet showing exactly where the call is missing.

### In-memory vs. persistent backends

For unit testing or lightweight prototypes, `IDao` can be implemented as a simple `List`-backed in-memory store — the test suite in `garganttua-api-core` includes a canonical example (`InMemoryDao`). For production use, a submodule provides a concrete backend (see Submodules). There is no built-in in-memory `IDao` shipped as a library artifact; the pattern is simple enough to inline in tests.

## Submodules

| Module | Role |
|---|---|
| [garganttua-api-dao-mongodb](./garganttua-api-dao-mongodb/README.md) | MongoDB `IDao` implementation — native-ready repository backed by the synchronous MongoDB Java driver, with `@Reflected` AOT support and `MongoDaoInfrastructureSeed` for GraalVM native-image compatibility |

## Tips and best practices

- Always call `.db(...)` on every DTO builder — the framework throws at build time if a DAO is missing, not at request time.
- Keep `IDao` implementations stateless except for the database connection and collection name captured at construction; the `IDomainDefinition` passed to `registerDomain` provides the DTO class for reflection-based mapping.
- For multi-DAO scenarios, all DAOs backing the same entity must carry the same `uuid` field on their respective DTOs — `Repository` merges results by UUID and will silently drop entities whose UUIDs are inconsistent across stores.
- Implement `IAOTInfrastructureSeed` and annotate your `IDao` class with `@Reflected` if you plan to deploy under GraalVM native image.
- Use the `ISupplierBuilder` overload of `.db(...)` when the DAO instance must be sourced from a dependency-injection container; the `FixedSupplierBuilder` wrapping a pre-built instance is the simplest alternative for tests and standalone use.

## License
This module is distributed under the Apache License, Version 2.0.
