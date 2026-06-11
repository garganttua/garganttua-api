# Garganttua API Javalin Binding

## Description

`garganttua-api-binding-javalin` plays a **dual role** inside the `garganttua-api-bindings` group:

1. It is the **single-version authority** for **Javalin** (`io.javalin:javalin`) — the one place the library's coordinates and version are pinned, so every consumer inherits the same artifact.
2. It ships the **Javalin HTTP transport** for garganttua-api: a `JavalinInterface` (an `IInterface` — the transport entry point that owns a Javalin server and routes HTTP requests to a domain) and its companion `JavalinProtocol` (an `IProtocol<Context, Context>` that adapts a Javalin `Context` to/from the request pipeline).

> Unlike the other binding modules (which are pure dependency bundles with no Java sources), this one carries transport code. It therefore depends on `garganttua-api-commons` (the contracts) and `garganttua-api-core` (the canonical `Caller` implementation) in addition to Javalin.

The pinned version (`javalin.version` in the root POM) is currently **6.6.0**. Updating the entire framework to a new Javalin release requires changing exactly one property.

**Key Features:**
- **Javalin-backed `IInterface`** — `JavalinInterface` owns a Javalin server and, on `handle(domain)`, wires the standard CRUD route table (`POST`/`GET`/`PUT`/`DELETE` on `/{domain}` and `/{domain}/{uuid}`) onto it; idempotent `onStart`/`onStop` lifecycle
- **Companion `IProtocol`** — `JavalinProtocol` extracts the transport fields (body, caller, headers, path, method, query) from a `Context` and writes the pipeline's response back onto it; reuses the framework's canonical `Caller`
- **Single-version authority** — Javalin coordinates and version are defined once in the root POM `<properties>` block; every consumer inherits the same artifact without risk of split-brain versioning
- **Lightweight HTTP/WebSocket server** — Javalin 6.x is a Kotlin/Java-friendly layer over Jetty 12 that exposes a concise, lambda-based routing API, HTTP/2, WebSocket, and SSE out of the box

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-javalin</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `io.javalin:javalin:6.6.0`
 - `com.garganttua:garganttua-api-commons`
 - `com.garganttua:garganttua-api-core:${project.version}`
 - `org.junit.jupiter:junit-jupiter-engine:test`
 - `org.mockito:mockito-core:test`
 - `com.garganttua:garganttua-api-binding-jackson:${project.version}:test`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### What a binding module is

The `garganttua-api-bindings` group follows a pattern: **one external library, one submodule** acting as the canonical version anchor for its wrapped library. This avoids Maven dependency-management sprawl and makes security audits simpler — there is exactly one place to review per third-party library.

This Javalin binding is the deliberate **exception**: on top of pinning Javalin, it hosts the framework's Javalin transport (`JavalinInterface` + `JavalinProtocol`). The transport could not live purely on the contracts (`garganttua-api-commons`) because it reuses the engine's `Caller`, which lives in `garganttua-api-core`; co-locating it with the Javalin version anchor keeps the HTTP layer a one-module concern.

### How the transport works (Mode A)

`JavalinInterface` decides *which* operation each route maps to (CRUD routing, plus the `{uuid}` path parameter threaded as `ENTITY_UUID`) and hands the live Javalin `Context` to the pipeline as `rawRequest`. `JavalinProtocol` is then resolved by request type to adapt the *how*: it pulls the body, caller, headers, path, method and query parameters off the `Context` (request side), and writes status + body back onto it (response side). Register the protocol once on the API and attach the interface to a domain:

```java
ApiBuilder.builder()
    .protocol(new JavalinProtocol())
    .domain(User.class)
        .interfasse(new JavalinInterface(7000))
        .entity()...
    .up()
    .build();
```

The interface seeds the caller from the optional `X-Tenant-Id` / `X-Owner-Id` / `X-Caller-Id` headers (anonymous when absent); `superTenant`/`superOwner` are never asserted from the wire — they are recomputed server-side. The authenticated principal of a token-bearing request is resolved later by `VERIFY_AUTHORIZATION` from the `Authorization` header.

### Scope

Per the per-domain interface model, one `JavalinInterface` owns one Javalin server; two domains each carrying their own instance must use distinct ports. The lifecycle guards are idempotent, so a single instance shared across domains via a supplier starts/stops its server exactly once while registering every domain's routes.

### What Javalin provides

Javalin is a lightweight JVM web framework built on top of Jetty. It exposes HTTP routing via typed handler lambdas, supports virtual threads (Project Loom) natively from version 6, and integrates with Jackson for JSON serialization.

### Version pinning

The property `javalin.version` is declared in the root `pom.xml` of `garganttua-api`:

```xml
<properties>
    <javalin.version>6.6.0</javalin.version>
</properties>
```

This binding's `pom.xml` references it as `${javalin.version}`, so the effective version is always inherited from that single source of truth.

## Usage

Add this binding as a dependency, then wire the transport on your API:

```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-javalin</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

```java
import com.garganttua.api.binding.javalin.JavalinInterface;
import com.garganttua.api.binding.javalin.JavalinProtocol;

ApiBuilder.builder()
    .protocol(new JavalinProtocol())            // the Context ↔ pipeline adapter, registered once
    .domain(User.class)
        .interfasse(new JavalinInterface(7000)) // the HTTP entry point, attached per-domain
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(UserDto.class).id("id").uuid("uuid").tenantId("tenantId").db(dao).up()
    .up()
    .build()
    .onInit()
    .onStart();                                 // binds the port and serves the CRUD routes
```

The generated routes for a `users` domain are:

| Verb   | Path             | Operation   |
|--------|------------------|-------------|
| POST   | `/users`         | createOne   |
| GET    | `/users`         | readAll     |
| GET    | `/users/{uuid}`  | readOne     |
| PUT    | `/users/{uuid}`  | updateOne   |
| DELETE | `/users/{uuid}`  | deleteOne   |
| DELETE | `/users`         | deleteAll   |

You can still use this binding purely as the Javalin version anchor in any other module — it brings `io.javalin:javalin:6.6.0` (and its transitive closure) without you declaring the coordinates directly. To upgrade Javalin across the entire framework, change `javalin.version` in the root POM and rebuild.

## Tips and best practices

- **Never redeclare `io.javalin:javalin` directly** in another garganttua-api module. Always go through this binding — it is the single-version authority.
- **Check Javalin's transitive closure** before upgrading: Javalin 6.x depends on Jetty 12, Kotlin stdlib, and a specific Jackson range. Run `mvn dependency:tree -pl garganttua-api-binding-javalin` to inspect the full tree before bumping `javalin.version`.
- **Scope management** — if a module only needs Javalin at test time, declare the binding with `<scope>test</scope>`. The binding itself does not enforce a scope.
- **Virtual threads** — Javalin 6 supports Loom virtual threads via `config.useVirtualThreads = true`. This pairs naturally with garganttua-api's Java 21 baseline and improves throughput under high concurrency at near-zero thread-pool tuning cost.
- **Logging** — Javalin uses SLF4J. Make sure your application module includes an SLF4J binding (Logback, Log4j 2, etc.); otherwise Javalin emits a `SLF4J: No SLF4J providers were found` warning at startup.

## License
This module is distributed under the MIT License.
