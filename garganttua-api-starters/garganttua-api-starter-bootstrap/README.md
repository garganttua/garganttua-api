# Garganttua API Starter — Bootstrap

## Description

The **bootstrap starter** is the socle of the Spring-Boot-style starter family. It carries the Java that turns the framework into a one-call boot experience: a `GarganttuaApplication.run(...)` runner, an `application.yaml`/`.properties` configuration loader, and the ServiceLoader plumbing that lets add-on starters (persistence, transport, …) auto-wire themselves.

It is deliberately **transport- and persistence-agnostic** — it brings the framework plus the runtime reflection stack and nothing else. Add the MongoDB or Javalin starters on top to get a database or an HTTP server; the runner does not change.

**Key features:**
- **One-call boot** — `GarganttuaApplication.run(App.class, args)` assembles the whole framework (reflection, injection, expression, runtimes, scripts, workflows), scans your `@Entity`/`@Dto` classes, runs every auto-configuration on the classpath, and starts the API.
- **Externalized config** — `GarganttuaConfig` reads `application.yaml` then `application.properties`, with environment-variable overrides (`server.port` ← `GARGANTTUA_SERVER_PORT`).
- **Pluggable auto-configuration** — discovers `IApiAutoConfiguration` services via `ServiceLoader`, ordered, so simply putting a starter jar on the classpath wires its behaviour.
- **No boilerplate** — replaces the manual assembly of six builders (`Reflection → Injection → Expression → Runtimes → Scripts → Workflows`) with a single call.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-bootstrap</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-core`
 - `com.garganttua.core:garganttua-runtime-reflection`
 - `com.garganttua.core:garganttua-reflections`
 - `com.garganttua.core:garganttua-bootstrap`
 - `org.yaml:snakeyaml`
 - `org.junit.jupiter:junit-jupiter-engine:test`
 - `org.mockito:mockito-core:test`
 - `org.mockito:mockito-junit-jupiter:test`

<!-- AUTO-GENERATED-END -->

## Usage

Annotate your entity and DTO, drop an `application.yaml` on the classpath, and write a `main`:

```java
@Entity
public class User {
    @EntityId   private String id;
    @EntityUuid private String uuid;
    private String name;
    // getters / setters …
}

@Dto(entityClass = User.class)
public class UserDto {
    @DtoId   private String id;
    @DtoUuid private String uuid;
    private String name;
    // getters / setters …
}

public final class MyApp {
    public static void main(String[] args) {
        GarganttuaApplication.run(MyApp.class, args);     // returns a running IApi
        // or GarganttuaApplication.runAndWait(MyApp.class, args) to block until shutdown
    }
}
```

`application.yaml`:

```yaml
api:
  multiTenant: false
  packages: com.myapp            # defaults to the run(...) source class package
```

On its own, the bootstrap starter has **no DAO and no transport**: a domain whose DTO has no `.db(...)` fails the build unless a persistence starter (e.g. `garganttua-api-starter-mongodb`) registers a default DAO, and there is no HTTP server unless a transport starter (e.g. `garganttua-api-starter-javalin`) is present. For an in-process API, supply your own `IDao` via a small `IApiAutoConfiguration` (see below) or the `garganttua-api-starter-quickstart` aggregator.

## Configuration keys

| Key | Read by | Effect |
|---|---|---|
| `api.multiTenant` (bool) | runner | `ApiBuilder.multiTenant(...)` |
| `api.superTenantId` (string) | runner | `ApiBuilder.superTenantId(...)` |
| `api.superTenantAutoCreate` (bool) | runner | `ApiBuilder.superTenantAutoCreate(...)` |
| `api.packages` (list or CSV) | runner | packages to scan (defaults to the source class package) |

Add-on starters read their own keys (`mongodb.uri`, `mongodb.database`, `server.port`, …) — see their READMEs. Any key is overridable by an environment variable: uppercase it, replace `.` with `_`, prefix with `GARGANTTUA_` (e.g. `api.multiTenant` ← `GARGANTTUA_API_MULTITENANT`).

## Writing an auto-configuration

A starter contributes by shipping an `IApiAutoConfiguration` and registering it under `META-INF/services/com.garganttua.api.commons.starter.IApiAutoConfiguration`:

```java
public final class MyAutoConfiguration implements IApiAutoConfiguration {
    @Override public int order() { return 0; }          // 0 = persistence, 100 = transport
    @Override public void apply(AutoConfigurationContext ctx) {
        ctx.registerDefaultDao((domainName, dtoClass) -> new MyDao(domainName));
        // ctx.registerDefaultInterface(...);  ctx.registerResource(closeable);
    }
}
```

The runner discovers it, sorts by `order()`, and applies it before the API is built — so the default DAO/interface are in place when the `@Entity` scan materialises each domain.

## How it works

1. `Bootstrap.builder()` runs garganttua-core's ServiceLoader cold-start, installing the reflection stack globally.
2. `ApiBuilder.builder()` is created, auto-detection enabled, top-level config applied.
3. Every `IApiAutoConfiguration` is discovered and applied (registering default DAO/interface, opening resources).
4. `Bootstrap` discovers the injection/expression/runtimes/scripts/workflows builders via SPI, builds everything, and `onInit()`/`onStart()`s the API (HTTP server included, when a transport starter is present).

## License

This module is distributed under the MIT License.
