# Garganttua API Starters

## Description

Aggregator for the **consumption starters** that bundle a coherent, ready-to-run
API stack so a downstream application depends on **one artifact** instead of
assembling the same five `<dependency>` blocks by hand. Each starter is a
`pom`-packaged module — no classes, just a curated `<dependencies>` block.
Switching stack is a one-line change of the starter coordinate in your
application's `pom.xml`.

**Key Features:**
- **One dependency, batteries included** — reflection provider/scanner, persistence, transport, and security wired together for you
- **No lock-in** — the framework stays "user chooses"; the starter just removes the boilerplate
- **JVM, AOT/native, and minimal quickstart variants** — pick the mode that matches your deployment target
- **Incremental adoption** — starters share the same API surface; switching is a single coordinate change
- **AOT opt-in** — the AOT starter degrades gracefully to runtime reflection when the AOT processor has not run

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

## Core Concepts

A starter bundles the following layers for a given stack:

| Layer | What it provides |
|---|---|
| **Framework** | `garganttua-api-core` — the declarative engine, pipeline, builders |
| **Reflection** | `garganttua-runtime-reflection` + `garganttua-reflections` for JVM mode; `garganttua-aot-reflection` + `garganttua-aot-annotation-scanner` (AOT-first) + runtime fallbacks for AOT/native mode |
| **Persistence** | `garganttua-api-dao-mongodb` — MongoDB `IDao` implementation (JVM and AOT variants) |
| **Transport** | `garganttua-api-javalin` + `garganttua-api-interface-rest` — Javalin HTTP interface and REST wiring (pending 3.0 port) |
| **Security** | `garganttua-api-security` — authentication and authorisation modules (pending 3.0 port) |

### Choosing a starter

- **`garganttua-api-starter-quickstart`** — no persistence, no transport, no security. Use this to prototype in-process, write integration tests, or follow tutorials. Supply your own `Map`-backed `IDao` and drive the API via `IApi.request(...).execute()`.
- **`garganttua-api-starter-jvm-mongo-javalin`** — the recommended default for production web applications. Uses runtime reflection (no build-time step required), MongoDB as the persistence layer, and Javalin as the HTTP transport.
- **`garganttua-api-starter-aot-mongo-javalin`** — same stack as the JVM variant but adds AOT reflection/scanner providers (higher SPI priority) with the runtime stack as fallback. Targets GraalVM `native-image` builds or any environment where classpath scanning at startup is prohibitively expensive. Without the `garganttua-aot-maven-plugin` in your build, the AOT providers return nothing and the runtime fallback takes over — this starter is therefore a zero-risk upgrade path from the JVM variant.

## Submodules

| Starter | Stack | When to use |
|---|---|---|
| [`garganttua-api-starter-quickstart`](./garganttua-api-starter-quickstart/README.md) | `garganttua-api-core` + runtime reflection | Prototyping, tutorials, integration tests — no DAO, no transport, user-supplied in-memory `IDao` |
| [`garganttua-api-starter-jvm-mongo-javalin`](./garganttua-api-starter-jvm-mongo-javalin/README.md) | core + runtime reflection + MongoDB + Javalin + security | Production web applications running on the JVM, no AOT step required |
| [`garganttua-api-starter-aot-mongo-javalin`](./garganttua-api-starter-aot-mongo-javalin/README.md) | core + AOT reflection (runtime fallback) + MongoDB + Javalin + security | GraalVM native-image builds or cold-start-sensitive JVM deployments |

## Tips and best practices

- Keep **exactly one** starter on the classpath. Having multiple starters from the same group (e.g., both JVM and AOT variants) will place competing reflection providers on the classpath and may produce unexpected SPI ordering.
- The AOT starter only delivers its cold-start benefit once the `garganttua-aot-maven-plugin` runs during your build. Add it to a `<profile id="native">` so standard `mvn install` and `native-image` builds can share the same `pom.xml`.
- The quickstart starter is intentionally minimal. For anything beyond a demo or test, switch to `jvm-mongo-javalin`; the API wiring code does not change.
- Pending transport and security modules (`garganttua-api-javalin`, `garganttua-api-interface-rest`, `garganttua-api-security`) are commented out in each starter's `pom.xml` and will be activated once they are ported to the 3.0 API — no consumer-side change will be required at that point.
- Domain names are auto-generated as the plural lowercase of the entity class name (`User` → `users`). Confirm the names match your URL expectations before going to production.

## License
This module is distributed under the MIT License.
