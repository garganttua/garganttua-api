# Garganttua API Starters

## Description

The **Spring-Boot-style starter family**. A downstream application depends on a single starter coordinate, annotates its `@Entity`/`@Dto` classes, drops an `application.yaml`, and boots the whole stack with one call:

```java
GarganttuaApplication.run(MyApp.class, args);
```

The family is **composable**: a code-bearing socle (`bootstrap`) carries the runner and config loader, and add-on starters (`mongodb`, `javalin`) auto-wire persistence and transport simply by being on the classpath. Ready-to-use aggregators bundle these into named stacks.

**Key features:**
- **One-call boot** — `GarganttuaApplication.run(...)` replaces the manual assembly of six framework builders.
- **Auto-configuration** — add-on starters contribute via `IApiAutoConfiguration` discovered by `ServiceLoader`; adding a jar wires its behaviour.
- **Externalized config** — `application.yaml` / `application.properties` with environment-variable overrides.
- **No lock-in** — an explicit `.db(...)` or `.interfasse(...)` always overrides the starter's defaults.
- **JVM, AOT/native, and minimal variants** — pick the aggregator that matches your target.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starters</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies

<!-- AUTO-GENERATED-END -->

## Architecture

Two kinds of module:

**Code-bearing starters** (the building blocks):

| Starter | Role |
|---|---|
| [`garganttua-api-starter-bootstrap`](./garganttua-api-starter-bootstrap/README.md) | the `GarganttuaApplication` runner, `application.yaml` loader, auto-config ServiceLoader plumbing, runtime reflection. Transport- and persistence-agnostic. |
| [`garganttua-api-starter-mongodb`](./garganttua-api-starter-mongodb/README.md) | persistence add-on: a default MongoDB DAO per domain, from `mongodb.uri`/`mongodb.database`. |
| [`garganttua-api-starter-javalin`](./garganttua-api-starter-javalin/README.md) | transport add-on: a shared Javalin HTTP server for all domains, from `server.port`, JSON included. |

**Aggregators** (ready-to-use stacks):

| Aggregator | Composes | When to use |
|---|---|---|
| [`garganttua-api-starter-quickstart`](./garganttua-api-starter-quickstart/README.md) | bootstrap | Prototyping, tutorials, in-process tests — supply your own in-memory `IDao`. |
| [`garganttua-api-starter-jvm-mongo-javalin`](./garganttua-api-starter-jvm-mongo-javalin/README.md) | bootstrap + mongodb + javalin | The recommended production web stack on a standard JVM. |
| [`garganttua-api-starter-aot-mongo-javalin`](./garganttua-api-starter-aot-mongo-javalin/README.md) | + AOT reflection | GraalVM native-image or cold-start-sensitive deployments. |

## How a starter wires itself

An add-on starter ships an `IApiAutoConfiguration` registered under `META-INF/services/com.garganttua.api.commons.starter.IApiAutoConfiguration`. The runner discovers all of them, orders them (`order()` — persistence `0`, transport `100`), and applies each before the API is built. An auto-configuration can register a **default DAO** (consulted when a DTO has no `.db(...)`), a **default interface** (attached to every domain with no `.interfasse(...)`), and **resources** to close on shutdown.

## Tips and best practices

- Keep **exactly one aggregator** on the classpath (or one bootstrap + your chosen add-ons). Mixing the JVM and AOT aggregators places competing reflection providers on the classpath.
- A scanned domain's CRUD defaults to `authenticated` access — anonymous HTTP calls get `401`. Configure access (`.security().creationAccess(Access.anonymous)…`) or wire authentication; `@EntityPublic` only lifts tenant filtering.
- The AOT aggregator only delivers its cold-start benefit once `garganttua-aot-maven-plugin` runs during your build; without it the runtime fallback takes over transparently.
- Domain names are the plural lower-case of the entity class name (`User` → `users`) — these are also the HTTP route prefix and collection name.

## License

This module is distributed under the MIT License.
