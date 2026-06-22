# Garganttua API Starter — Quickstart

## Description

The **quickstart starter** is the smallest entry point into the framework: an aggregator that pulls only the [bootstrap starter](../garganttua-api-starter-bootstrap) — the `GarganttuaApplication` runner plus the runtime reflection stack. **No persistence, no HTTP transport.** It is meant for tutorials, scripted demos and in-process integration tests, where you supply your own in-memory `IDao`.

For a real web app, switch to [`garganttua-api-starter-jvm-mongo-javalin`](../garganttua-api-starter-jvm-mongo-javalin) (or its AOT variant).

**Key features:**
- **Single dependency** — one coordinate pulls the framework + runtime reflection + the `GarganttuaApplication` runner.
- **No DAO, no transport** — supply an in-memory `IDao`; exercise the API in-process.
- **No AOT step** — reflection resolves at runtime; a plain `mvn package` is enough.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-quickstart</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-starter-bootstrap`
 - `com.garganttua.core:garganttua-starter-runtime`

<!-- AUTO-GENERATED-END -->

## Usage

Because the quickstart bundles no persistence, register an in-memory default DAO through a tiny `IApiAutoConfiguration` (discovered via `ServiceLoader`), then boot:

```java
// META-INF/services/com.garganttua.api.commons.starter.IApiAutoConfiguration
//   → com.myapp.InMemoryDaoAutoConfiguration
public final class InMemoryDaoAutoConfiguration implements IApiAutoConfiguration {
    @Override public void apply(AutoConfigurationContext ctx) {
        ctx.registerDefaultDao((domainName, dtoClass) -> new InMemoryDao());
    }
}

public final class MyApp {
    public static void main(String[] args) {
        IApi api = GarganttuaApplication.run(MyApp.class, args);

        api.getDomain("users").orElseThrow()
           .createOne(newUser("alice"), Caller.createAnonymousCaller());
    }
}
```

`application.yaml`:

```yaml
api:
  multiTenant: false
  packages: com.myapp
```

No server is started; every call runs synchronously through the same pipeline as production.

## When to graduate

- **Need persistence** → `garganttua-api-starter-mongodb` (or the jvm-mongo-javalin aggregator).
- **Need HTTP** → `garganttua-api-starter-javalin`.
- **Need GraalVM native / fast startup** → `garganttua-api-starter-aot-mongo-javalin`.

The `GarganttuaApplication.run(...)` call and your annotated `@Entity`/`@Dto` classes stay identical across starters; only the `pom.xml` and the `application.yaml` change.

## License

This module is distributed under the MIT License.
