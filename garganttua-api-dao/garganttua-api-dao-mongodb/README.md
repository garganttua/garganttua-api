# Garganttua API MongoDB DAO

## Description

`garganttua-api-dao-mongodb` provides the **MongoDB-backed `IDao` implementation** for the Garganttua API framework. It bridges the framework's abstract persistence contract (`IDao`) to MongoDB collections via the synchronous MongoDB Java driver, and is fully native-ready for GraalVM compilation.

**Key Features:**
- **`IDao` implementation** — `MongoDao` fulfills the full `IDao` contract: `find`, `save`, `delete`, `count`, and `registerDomain`
- **Filter translation** — `MongoFilterConverter` maps the framework's `IFilter` tree (logical operators `$and` / `$or` / `$nor`, field comparisons `$eq` / `$ne` / `$gt` / `$gte` / `$lt` / `$lte` / `$regex` / `$empty` / `$in` / `$nin` / `$text`, and geospatial `$geoWithin` / `$geoWithinSphere`) to native MongoDB `Bson` predicates via `com.mongodb.client.model.Filters`
- **Sorting and pagination** — `ISort` translates to `Sorts.ascending` / `Sorts.descending`; `IPageable` applies `skip` and `limit` on the `FindIterable`
- **Upsert-based save** — `save()` performs a `replaceOne` with `upsert(true)` when `_id` is present, and an `insertOne` otherwise. The domain uuid is projected onto `_id` on write (kept under its own field name too, so uuid filters still match), so saves upsert by uuid instead of inserting duplicates, and `delete()` always has a key
- **Reflection-based DTO mapping** — `MongoDao` uses `garganttua-core` `IClass` / `IField` abstractions to convert between DTO instances and `Document` objects at runtime, traversing the class hierarchy and skipping `static` and `transient` fields
- **DTO composition (`@DBRef`-style)** — a field declared via `@Composed` / `.composed(fieldName, collection)` is persisted as a MongoDB `DBRef` reference (1-1) or a `List<DBRef>` (1-N) instead of an embedded document, and eagerly resolved back into the composed DTO(s) on read
- **Round-trip type fidelity** — on read, each stored value is adapted back to the field's declared Java type (BSON `String` → `enum`, BSON datetime / `java.util.Date` → `java.time.*`, 32-bit `Integer` → `Long`, `String` → scalar); on write an `enum` is stored by its `name()`
- **`IKey` key-material persistence** — a DTO field of type `com.garganttua.core.crypto.IKey` (the key-store entity that backs token signing) is stored as a self-describing BSON sub-document and reconstructed on read; the field stays `IKey`-typed at runtime
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

After construction, the framework calls `registerDomain(IDomainDefinition)` to supply the DTO class used for document-to-object mapping. The first DTO definition's `dtoClass()` (and its `uuid()` field address and `compositions()`) is captured and used throughout the lifetime of the DAO instance. DTO fields are enumerated via `IClass.getDeclaredFields()` on each operation; `static` and `transient` fields are excluded.

**uuid ↔ `_id`.** The domain uuid is projected onto MongoDB's `_id` on write and recovered from it on read. This makes `save()` upsert by uuid (`replaceOne(..., upsert(true))`) instead of inserting a duplicate row each time, and gives `delete()` a key. The uuid is also kept under its own field name, so filters that query it (every `readOne` / `readAll`-by-uuid, built as `Filter.eq(uuidField, …)`) keep matching without the filter converter needing to know about `_id`.

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
| `$field` + `$geoWithin` / `$geoWithinSphere` | `Filters.geoWithin(field, {$geometry: …})` |

A `$field` node carries the field name as its `value` and exactly one comparison child. Logical operators require at least two children.

### Geospatial queries (`$geoWithin` / `$geoWithinSphere`)

A geolocalized domain (`@EntityGeolocalized` / `.geolocalized(field)`, whose location field is forced to `org.geojson.Point`) can be queried with the framework's geo filters `Filter.geolocWithin(field, geometry)` and `Filter.geolocWithinSphere(field, geometry)`, where `geometry` is any `org.geojson` GeoJSON shape (`Point`, `Polygon`, `MultiPolygon`, …).

`MongoFilterConverter` translates both to a MongoDB GeoJSON predicate:

```json
{ "location": { "$geoWithin": { "$geometry": { "type": "Polygon", "coordinates": [ … ] } } } }
```

The `org.geojson` geometry is serialised to its `{type, coordinates}` GeoJSON form via Jackson (the geojson library's own (de)serialisation), so every shape is handled uniformly. **`$geoWithin` and `$geoWithinSphere` map to the same `$geometry` query** — a `2dsphere` index already evaluates containment on the sphere, so `Sphere` is an alias. This requires a **`2dsphere` index** on the location field (create it independently — see the index note below).

### DTO Composition (`@Composed` / `.composed(...)`)

A DTO field can **reference** DTOs stored in another collection rather than embedding them — the MongoDB analogue of Spring Data's `@DBRef`. Declare a composition either with the field-level annotation or the DSL:

```java
public class OrderDto {
    private String uuid;

    @Composed(collection = "customers")
    private CustomerDto customer;        // 1-1 — one DBRef

    @Composed(collection = "lines")
    private List<OrderLineDto> lines;    // 1-N — a List<DBRef>
}

// …or, equivalently, via the DSL (no annotation on the field):
.dto(OrderDto.class)
    .id("id").uuid("uuid").tenantId("tenantId")
    .composed("customer", "customers")
    .composed("lines", "lines")
    .db(new MongoDao(db, "orders"))
```

Both paths land in `IDtoDefinition.compositions()`, which `MongoDao` reads at `registerDomain`. Then:

- **On write** (`dtoToDocument`) — a composition field is stored **only as a reference**, never the embedded DTO: a single `DBRef{$ref: collection, $id: uuid}` for a 1-1 field, or a `List<DBRef>` for a 1-N (`Collection`) field. The `$id` is the composed DTO's `uuid`. The composed targets are persisted independently by their own domains — there is **no cascade save**.
- **On read** (`documentToDto`) — each reference is **eagerly resolved one level deep**: the referenced document is fetched from its collection (`getCollection($ref).find(uuid == $id)`) and mapped onto the field's (element) type. Resolution does **not** recurse into a referenced DTO's own compositions, so a reference graph can never loop. A dangling reference resolves to `null`.

The `uuid` stays under its own field name (no `_id` remapping), so existing `MongoFilterConverter` filters are unaffected; references resolve by querying the target's `uuid` field.

### Type fidelity on the round trip

MongoDB's `Document` codec is lossy across the JVM type system — an `enum` decodes back as a `String`, a `java.time.Instant` as a `java.util.Date`, a 32-bit field as an `Integer`. `MongoDao` adapts each stored value back to its declared field type on read:

| Stored (BSON-decoded) | Declared field type | Result |
|---|---|---|
| `String` | an `enum` | `Enum.valueOf(type, name)` |
| `java.util.Date` | `Instant` / `LocalDateTime` / `LocalDate` / `ZonedDateTime` / `OffsetDateTime` | converted at UTC |
| `Number` | any boxed/primitive numeric | widened/narrowed |
| `String` | `int` / `long` / `double` / `float` / `boolean` | parsed |
| `org.bson.types.Binary` | `byte[]` | unwrapped via `getData()` |
| already-assignable | — | passed through |

(The `Binary` → `byte[]` case matters in practice: the driver decodes every BSON binary as `org.bson.types.Binary`, so without it a `byte[]` field — a token signature, raw key material — would be unreadable and break token verification / reuse / `readAll`.)

On write, an `enum` is stored by its `name()` so the wire shape is codec-independent and human-readable. A value the coercer cannot adapt is left for `field.set`, which surfaces a mismatch as a parlant `ApiException` naming the field and the stored vs declared types.

> The single-value coercion is generic plumbing every DAO backend needs; `docs/CORE_EVOLUTION_value_coercion_for_daos.md` proposes hosting it in `garganttua-core`'s mapper so this block can later delegate.

### `IKey` key-material persistence

A key-store entity (the `@Key` domain that holds token signing/verification material) carries fields of type `com.garganttua.core.crypto.IKey`. There is **no BSON codec for `IKey`** — it is an interface wrapping a lazy `java.security.Key` behind a package-private constructor — so handing one to the driver fails with `Can't find a codec for class …crypto.Key`. (`byte[]` fields are fine; the driver encodes them natively. The problem is specifically the rich `IKey` type.)

`MongoDao` bridges `IKey` ↔ a self-describing BSON sub-document at the persistence boundary (`IKeyBsonBridge`). The DTO field **stays `IKey`-typed** at runtime — the entity signs/verifies with it; only the stored shape changes:

```json
{ "__ikey": true, "type": "PRIVATE", "algorithm": "EC-256",
  "signatureAlgorithm": "SHA256", "rawKey": "<base64 material>" }
```

The raw material round-trips through core's `KeySerializer.exportRawKey` / `importRawKey`; the metadata needed to rebuild the key (`KeyType`, the `NAME-SIZE` algorithm rebuilt via `KeyAlgorithm.validateKeyAlgorithm`, the `SignatureAlgorithm` / encryption mode + padding) is stored alongside. On read, a sub-document carrying the `__ikey` marker is reconstructed back into an `IKey`, byte-identical to the original.

> **Limitation:** `IKey` does not expose its IV size, so encryption keys round-trip with `ivSize = 0`. Signing keys — the key-store mint path — do not use it, so they are unaffected.

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
- **Composition is not cascade persistence.** Writing a DTO with a `@Composed` field stores only the reference — the composed targets must be saved through their own domains/DAOs. Reading resolves them, but writing does not create them.
- **Composition resolution costs one query per reference.** Reading an entity with N `@Composed` references issues N additional `find` calls (one per `DBRef`), one level deep. For wide 1-N fan-out, prefer fetching the references and batching lookups in application code, or model the data as embedded documents instead.
- **A composed target is expected to expose its `uuid` under the same field name** as the owning DTO's uuid (the `$id` of every emitted `DBRef`). Resolution queries the target collection by that field name.

## License
This module is distributed under the MIT License.
