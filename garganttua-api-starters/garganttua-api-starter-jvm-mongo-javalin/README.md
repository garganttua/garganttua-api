# Garganttua API Starter — JVM (MongoDB + Javalin)

## Description

The recommended batteries-included starter for deploying a Garganttua API application on a standard JVM with MongoDB persistence and a Javalin HTTP layer. A single Maven dependency replaces the five or six `<dependency>` declarations you would otherwise assemble by hand — the framework keeps the "let the user choose" property, the starter just spares you the boilerplate.

**Key Features:**
- **Zero classpath scanning configuration** — runtime reflection provider and `org.reflections` scanner are pre-wired and activated automatically at startup; no AOT annotation-processor step required
- **MongoDB persistence out of the box** — `garganttua-api-dao-mongodb` is on the classpath, ready to be handed to any DTO builder via `.db(new MongoDao(...))`
- **Full framework engine** — `garganttua-api-core` brings the complete pipeline (8-stage request workflow, multi-tenancy, entity role/characteristic matrix, DSL builder, fluent request builder)
- **Honest about what is pending** — the Javalin HTTP interface (`garganttua-api-javalin`), REST wiring (`garganttua-api-interface-rest`), and security modules (`garganttua-api-security`) are **not yet active**: their 3.0 ports are in progress and will be switched on in this starter once ready, with no change required from consumers

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-jvm-mongo-javalin</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-core`
 - `com.garganttua.core:garganttua-runtime-reflection`
 - `com.garganttua.core:garganttua-reflections`
 - `com.garganttua:garganttua-api-dao-mongodb`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### What this starter bundles

| Artifact | Group | Role |
|---|---|---|
| `garganttua-api-core` | `com.garganttua` | Framework engine — pipeline, DSL builder, multi-tenancy, entity processing |
| `garganttua-runtime-reflection` | `com.garganttua.core` | JDK-reflection-backed `IReflectionProvider` (no AOT index required) |
| `garganttua-reflections` | `com.garganttua.core` | `org.reflections`-backed `IAnnotationScanner` for classpath scanning |
| `garganttua-api-dao-mongodb` | `com.garganttua` | MongoDB `IDao` implementation for all DTO persistence |

The three modules listed below are **commented out** in the pom pending their 3.0 port. They will be activated transparently once the port lands — no version bump or consumer action required:

- `garganttua-api-javalin` — Javalin-based HTTP server adapter
- `garganttua-api-interface-rest` — REST interface wiring (route registration, content negotiation)
- `garganttua-api-security` — Authentication and authorization implementations

### When to choose this starter over the others

| Goal | Recommended starter |
|---|---|
| Spike / tutorial / integration test, no external DB | `garganttua-api-starter-quickstart` |
| **Standard JVM web app with MongoDB** (this starter) | `garganttua-api-starter-jvm-mongo-javalin` |
| Faster startup, GraalVM native-image, or CI classpath scan is too slow | `garganttua-api-starter-aot-mongo-javalin` |

Choose this starter when you want the simplest possible path to a running web service: no annotation processor, no AOT index regeneration on each `@Reflected` change, and a well-known classpath scanning approach that works out of the box with any IDE. The tradeoff is a slightly slower startup compared to the AOT variant (classpath scan + cold JIT), which is negligible for long-running server processes but visible in lambda-style deployments.

## Usage

Declare a single dependency in your application pom:

```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-jvm-mongo-javalin</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

Minimal bootstrap wiring (adapt field names to your entities):

```java
import com.garganttua.api.core.builder.ApiBuilder;
import com.garganttua.api.dao.mongodb.GGAPIMongoRepository;
import com.garganttua.core.reflection.utils.IClass;
import com.mongodb.client.MongoClients;

IApi api = ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .domain(IClass.getClass(User.class))
        .entity()
            .id("id").uuid("uuid").tenantId("tenantId")
        .up()
        .dto(IClass.getClass(UserDto.class))
            .id("id").uuid("uuid").tenantId("tenantId")
            .db(new GGAPIMongoRepository<>(
                    MongoClients.create(connectionString),
                    "myapp",
                    UserDto.class))
        .up()
        .creation(true).readAll(true).readOne(true).update(true).deletion(true)
    .up()
    .build();
```

Once the Javalin interface module is ported to 3.0, the same `IApi` instance will be handed to the Javalin adapter to expose every domain as a REST endpoint — no rebuild of your domain configuration needed.

In-process requests (available today, useful for tests and background jobs):

```java
// Create
api.request("users")
    .createOne(userDto)
    .caller(superCallerContext)
    .execute();

// Read all
api.request("users")
    .readAll()
    .caller(tenantCallerContext)
    .page(0, 20)
    .execute();
```

## Tips and best practices

- **Provide one `MongoClient` per application** — `MongoClients.create(...)` is expensive; create it once, inject it into all your `GGAPIMongoRepository` instances.
- **Name your packages explicitly** — call `.packages("com.myapp")` on the builder so the classpath scanner narrows its search and startup stays fast even as your application grows.
- **Upgrade path to AOT is one line** — when startup time becomes a concern, swap the artifact ID to `garganttua-api-starter-aot-mongo-javalin` and add the `garganttua-aot-maven-plugin` to your build. No domain configuration changes required.
- **Mirror entity and DTO field names** — the mapper and pipeline scripts rely on matching field names between entity and DTO by default; diverge only when you have an explicit `@Mapping` reason.
- **Use the fluent request builder in integration tests** — `api.request(domainName).readAll().execute()` exercises the full 8-stage pipeline without an HTTP server, giving fast, deterministic coverage of your business and security rules before the Javalin layer arrives.
- **Observer wiring is optional but recommended** — attach a `ConsoleLogObserver` (from `garganttua-observability`) during development to see per-stage timing and correlate failures by `executionId` without touching production logging configuration.

## License

This module is distributed under the Apache License, Version 2.0.
