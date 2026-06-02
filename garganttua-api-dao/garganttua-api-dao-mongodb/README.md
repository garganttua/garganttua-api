# Garganttua API MongoDB DAO

## Description

`garganttua-api-dao-mongodb` provides the **MongoDB-backed `IDao` implementation** for the Garganttua API framework. It bridges the framework's abstract persistence contract (`IDao`) to MongoDB collections via the synchronous MongoDB Java driver, and is fully native-ready for GraalVM compilation.

**Key Features:**
- **`IDao` implementation** — `MongoDao` fulfills the full `IDao` contract: `find`, `save`, `delete`, `count`, and `registerDomain`
- **Filter translation** — `MongoFilterConverter` maps the framework's `IFilter` tree (logical operators `$and` / `$or` / `$nor`, field comparisons `$eq` / `$ne` / `$gt` / `$gte` / `$lt` / `$lte` / `$regex` / `$empty` / `$in` / `$nin` / `$text`) to native MongoDB `Bson` predicates via `com.mongodb.client.model.Filters`
- **Sorting and pagination** — `ISort` translates to `Sorts.ascending` / `Sorts.descending`; `IPageable` applies `skip` and `limit` on the `FindIterable`
- **Upsert-based save** — `save()` performs a `replaceOne` with `upsert(true)` when `_id` is present, and an `insertOne` otherwise
- **Reflection-based DTO mapping** — `MongoDao` uses `garganttua-core` `IClass` / `IField` abstractions to convert between DTO instances and `Document` objects at runtime, traversing the class hierarchy and skipping `static` and `transient` fields
- **AOT / native-ready** — `MongoDao` is annotated with `@Reflected`; the AOT annotation processor emits `AOTClass_MongoDao` at compile time; `MongoDaoInfrastructureSeed` registers it in `AOTRegistry` via `ServiceLoader` so the class resolves under `AOTReflectionProvider` without a classpath scanner

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-dao-mongodb</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-commons`
 - `com.garganttua:garganttua-api-binding-mongodb`
 - `com.garganttua.core:garganttua-aot-reflection`
 - `com.garganttua.core:garganttua-aot-commons`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### `MongoDao`

`MongoDao` implements `com.garganttua.api.commons.dao.IDao`. It is constructed with a `MongoDatabase` instance and a collection name:

```java
new MongoDao(mongoDatabase, "users")
```

After construction, the framework calls `registerDomain(IDomainDefinition)` to supply the DTO class used for document-to-object mapping. The first DTO definition's `dtoClass()` is captured and used throughout the lifetime of the DAO instance. DTO fields are enumerated via `IClass.getDeclaredFields()` on each operation; `static` and `transient` fields are excluded.

### `MongoFilterConverter`

A stateless utility class that recursively converts an `IFilter` tree into a `Bson` predicate consumed by the MongoDB driver. The filter tree uses a hierarchical node model where each node has a `getName()` (the operator), an optional `getValue()`, and optional child filters via `getFilters()`.

| `IFilter` operator | MongoDB equivalent |
|---|---|
| `$and` | `Filters.and(...)` |
| `$or` | `Filters.or(...)` |
| `$nor` | `Filters.nor(...)` |
| `$field` + `$eq` | `Filters.eq(field, value)` |
| `$field` + `$ne` | `Filters.ne(field, value)` |
| `$field` + `$gt` / `$gte` | `Filters.gt` / `Filters.gte` |
| `$field` + `$lt` / `$lte` | `Filters.lt` / `Filters.lte` |
| `$field` + `$regex` | `Filters.regex(field, pattern)` |
| `$field` + `$empty` | `Filters.exists(field, false)` |
| `$field` + `$in` / `$nin` | `Filters.in` / `Filters.nin` |
| `$field` + `$text` | `Filters.text(value)` |

A `$field` node carries the field name as its `value` and exactly one comparison child. Logical operators require at least two children.

### AOT and Native-Image Readiness

`MongoDao` bears the `@Reflected` annotation. At compile time, the Garganttua AOT annotation processor generates `AOTClass_MongoDao` — a static `AOTClass<MongoDao>` descriptor that self-registers into `AOTRegistry` via its `static` initializer block.

`MongoDaoInfrastructureSeed` implements `IAOTInfrastructureSeed` and is declared in:

```
META-INF/services/com.garganttua.core.aot.commons.IAOTInfrastructureSeed
```

On cold-start, the bootstrap phase discovers this seed via `ServiceLoader` and calls `seed(IAOTSeedContext)`, which calls `context.registerClass(MongoDao.class)`. This ensures `MongoDao` is resolvable by `AOTReflectionProvider` in environments where runtime classpath scanning is unavailable (GraalVM native image).

`MongoFilterConverter` is a static-only utility with no instantiation or reflective access and is intentionally not registered.

## Usage

Wire a `MongoDao` into a domain via the `.dto(...).db(...)` step of the `ApiBuilder` DSL:

```java
import com.garganttua.dao.mongodb.MongoDao;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

MongoDatabase db = MongoClients.create("mongodb://localhost:27017").getDatabase("myapp");

ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .domain(User.class)
        .entity()
            .id("id")
            .uuid("uuid")
            .tenantId("tenantId")
        .up()
        .dto(UserDto.class)
            .id("id")
            .uuid("uuid")
            .tenantId("tenantId")
            .db(new MongoDao(db, "users"))   // <-- wire the MongoDB DAO here
        .up()
        .creation(true)
        .readAll(true)
        .readOne(true)
        .update(true)
        .delete(true)
    .up()
    .build();
```

The framework calls `registerDomain` on the DAO instance at build time, injecting the `IDomainDefinition` that carries the DTO class. Subsequent CRUD pipeline executions invoke `find`, `save`, `delete`, and `count` directly on `MongoDao`.

A single `MongoDao` instance handles one collection. To back multiple domains, create one instance per domain / collection name — they can all share the same `MongoDatabase`.

## Tips and best practices

- Pass a `MongoDatabase` obtained from a shared `MongoClient` — never create a `MongoClient` per `MongoDao` instance, as each client maintains its own connection pool.
- Collection names are passed as plain strings at construction time; they are used as-is in `database.getCollection(collectionName)`. Use the same name as the domain's logical resource (e.g. `"users"`, `"products"`).
- The current DTO-to-document mapping excludes `null` field values. Ensure fields that must be stored as explicit `null` are handled upstream or via a custom DAO wrapper.
- Index creation is not managed by `MongoDao`. Create indexes (unique, TTL, text, geospatial) independently — via `MongoCollection.createIndex(...)`, a migration tool, or your Spring configuration.
- For native-image builds, confirm that `garganttua-aot-reflection` and `garganttua-aot-commons` are on the compile and runtime classpath. The `AOTClass_MongoDao` descriptor is emitted into `target/generated-sources/annotations` and must be compiled into the artifact.
- `MongoFilterConverter.convert()` throws `ApiException` on unsupported operators. Extend it by adding cases to the `switch` expression in `convertField` if your domain requires additional MongoDB operators.

## License
This module is distributed under the Apache License, Version 2.0.
