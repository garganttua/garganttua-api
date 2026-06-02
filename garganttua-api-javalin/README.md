# Garganttua API Javalin Integration

## Description

`garganttua-api-javalin` is the **Javalin transport layer** for the Garganttua API framework. It wires a built `IApi` context onto a Javalin HTTP server, turning every registered domain into a live REST endpoint with no hand-written routing code. The module sits between the framework engine (`garganttua-api-core`) and the HTTP binding (`garganttua-api-binding-javalin`), and coordinates with `garganttua-api-interface-rest` for route shape and content-negotiation rules.

**Key Features:**
- **Zero-routing REST exposure** — a single adapter call registers one route per domain × operation (create, readAll, readOne, update, delete); no per-endpoint handler required
- **IApi-native** — accepts the `IApi` instance produced by `ApiBuilder.builder()...build()` directly; domain definitions, multi-tenancy rules, and security policies declared in the builder are honoured transparently by the HTTP layer
- **Authorities endpoint** — when `.exposeAuthorities()` is configured on the builder, a dedicated `/authorities` route is wired automatically
- **Javalin 6 / Loom-ready** — delegates to `garganttua-api-binding-javalin` (Javalin 6.6.0 over Jetty 12); virtual threads (`config.useVirtualThreads = true`) are available out of the box on Java 21
- **Jackson serialization** — JSON marshalling delegates to `garganttua-api-binding-jackson` so the same `ISerializer` used in-process is reused on the wire with no duplication
- **Pluggable port and configuration** — Javalin server configuration (port, CORS, static files, thread model) is passed at startup time; the adapter does not impose defaults beyond what Javalin provides

> **This module is currently commented out of the root reactor (dormant) and is not yet published.** It is pending a 3.0 port. See the [Installation](#installation) section for intended coordinates.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-javalin</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-commons`
 - `com.garganttua:garganttua-api-core`
 - `com.garganttua:garganttua-api-binding-javalin`
 - `com.garganttua:garganttua-api-binding-jackson`
<!-- AUTO-GENERATED-END -->

> ⚠️ This module is currently commented out of the root reactor (dormant) and is not published. The block above documents its intended coordinates once reactivated.

## Core Concepts

### Relationship with the framework layers

The module occupies a precise slot in the dependency stack:

```
garganttua-api-binding-javalin   ← io.javalin:javalin (version anchor)
        ↑
garganttua-api-javalin           ← this module (transport adapter)
        ↑
garganttua-api-interface-rest    ← REST route shape + content negotiation
        ↑
garganttua-api-core              ← IApi / IDomain / pipeline engine
```

`garganttua-api-javalin` consumes `IApi` (from `garganttua-api-commons`) and uses the routes described by `garganttua-api-interface-rest` to register handlers on a Javalin instance. It never duplicates domain or security logic — those live exclusively in `garganttua-api-core`.

### IApi as the source of truth

`IApi` exposes every domain via `getDomain(name)`, the complete list of serializers, authorization protocols, and the optional authorities endpoint descriptor. The Javalin adapter iterates these at startup to register routes. At request time it translates the incoming `Context` (path params, query params, body, headers) into an `IOperationRequest` and calls `IApi.invoke(...)`, letting the 8-stage pipeline (CONFIG → VERIFY_AUTH → BUSINESS → CRUD → RESPONSE …) execute as-is.

### Jackson binding

`garganttua-api-binding-jackson` provides an `ISerializer` backed by Jackson 2.17. The Javalin adapter reads the configured serializers from `IApi.getSerializers()` and uses the first one matching the `Content-Type` / `Accept` headers. For JSON — the only currently supported media type — Jackson is selected automatically with no extra wiring.

### Authorities endpoint

When `ApiBuilder.builder().exposeAuthorities()...` is used, `IApi.getAuthoritiesEndpoint()` returns a non-null descriptor that carries the configured HTTP path, access level, and optional authority gate. The Javalin adapter checks this at startup and registers the corresponding `GET` route only when the descriptor is present, forwarding the resolved caller to `IApi.getAuthoritiesForCaller(caller)`.

## Usage

> **Note:** the code patterns below describe the intended API once the 3.0 port is complete. The module currently ships no Java sources.

### Minimal startup

```java
import com.garganttua.api.core.builder.ApiBuilder;
import com.garganttua.api.javalin.GGAPIJavalinServer;
import com.garganttua.core.reflection.utils.IClass;
import io.javalin.config.JavalinConfig;

// 1. Build the API context (domain definitions, persistence, security)
IApi api = ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .domain(IClass.getClass(User.class))
        .entity()
            .id("id").uuid("uuid").tenantId("tenantId")
        .up()
        .dto(IClass.getClass(UserDto.class))
            .id("id").uuid("uuid").tenantId("tenantId")
            .db(userDao)
        .up()
        .creation(true).readAll(true).readOne(true).update(true).deletion(true)
    .up()
    .build();

// 2. Start the HTTP server — the adapter registers all routes automatically
GGAPIJavalinServer server = GGAPIJavalinServer.builder()
    .api(api)
    .port(8080)
    .configure((JavalinConfig cfg) -> cfg.useVirtualThreads = true)
    .start();
```

The adapter derives route paths from domain names: a domain named `users` exposes:

| Method | Path | Operation |
|--------|------|-----------|
| `POST` | `/users` | createOne |
| `GET` | `/users` | readAll |
| `GET` | `/users/{uuid}` | readOne |
| `PUT` | `/users/{uuid}` | updateOne |
| `DELETE` | `/users/{uuid}` | deleteOne |
| `DELETE` | `/users` | deleteAll |

Only operations enabled in the builder (`.creation(true)`, `.readAll(true)`, etc.) are registered.

### Pagination, filtering, and sorting

Standard query parameters are mapped to the `IFilter`, `IPageable`, and `ISort` interfaces and passed through to the pipeline unchanged:

```
GET /users?filter=name%3AJOHN&page=0&size=20&sort=name:ASC
```

The exact filter expression syntax is defined by `garganttua-api-commons` (`IFilter`). Invalid expressions surface as `400 Bad Request` responses.

### Security and caller resolution

The Javalin adapter extracts the `Authorization` header, resolves the caller via the configured `IAuthorizationProtocol` (JWT, etc.), and sets it as the `ICaller` for the request. Public domains (`@EntityPublic` / `.publik()`) accept anonymous requests; any other access level requires a valid token.

### Graceful shutdown

```java
server.stop(); // drains in-flight requests before closing the Javalin instance
```

## Tips and best practices

- **Use a starter instead of wiring this module directly** — `garganttua-api-starter-jvm-mongo-javalin` bundles this module with `garganttua-api-core`, `garganttua-api-dao-mongodb`, and the reflection stack. Declare a single dependency and you are done.
- **Virtual threads are free** — Javalin 6 on Java 21 supports Loom out of the box. Pass `cfg.useVirtualThreads = true` in the Javalin configuration consumer; the framework pipeline is thread-confinement-safe.
- **Test without the HTTP layer** — the fluent request builder (`IApi.request(domainName).readAll().execute()`) exercises the full 8-stage pipeline in-process. Write your business and security tests there; add HTTP-level integration tests only for header/status-code concerns.
- **Enable observability early** — attach a `ConsoleLogObserver` (from `garganttua-observability`) to your `IApi` or individual domain workflows during development. Per-stage timing and correlated `executionId` traces appear on stdout with zero configuration.
- **Do not redeclare `io.javalin:javalin` directly** — the transitive path through `garganttua-api-binding-javalin` is the single-version authority. Overriding it in your application pom risks split-brain Javalin/Jetty class hierarchies.
- **Watch the Javalin transitive closure on upgrade** — Javalin 6.x depends on Jetty 12 and Kotlin stdlib. Run `mvn dependency:tree -pl garganttua-api-binding-javalin` before bumping `javalin.version` in the root POM.

## License
This module is distributed under the MIT License.
