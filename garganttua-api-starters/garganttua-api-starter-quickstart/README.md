# Garganttua API Starter — Quickstart

## Description

The quickstart starter is the **smallest possible entry point** into the Garganttua API framework. It bundles the core engine together with the runtime reflection stack — and nothing else — so a developer can stand up a fully-functional, in-process API in minutes, without a database, without an HTTP transport, and without any build-time AOT processing step.

The intended audience is: developers writing their first garganttua-api service, tutorial authors, and anyone who needs a deterministic in-memory environment for integration tests or scripted demos.

**Key Features:**
- **Single dependency** — one Maven coordinate pulls in everything needed to run `ApiBuilder` end-to-end
- **No DAO to configure** — supply any `Map`-backed `IDao` inline; no MongoDB, no persistence layer required
- **No transport** — the API is exercised entirely in-process via `IApi.request(...).execute()`; no HTTP port, no server lifecycle
- **No security setup** — authentication and authorization modules are absent; focus stays on domain modelling
- **No AOT build step** — reflection is resolved at runtime via `garganttua-runtime-reflection` + `garganttua-reflections`; the project compiles and runs with a plain `mvn package`
- **Multi-tenancy toggle** — call `.multiTenant(false)` to skip tenant wiring entirely for the simplest possible setup

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
 - `com.garganttua:garganttua-api-core`
 - `com.garganttua.core:garganttua-runtime-reflection`
 - `com.garganttua.core:garganttua-reflections`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### The minimal stack

The quickstart starter bundles exactly three artifacts:

| Artifact | Role |
|---|---|
| `com.garganttua:garganttua-api-core` | The framework engine: `ApiBuilder`, domain/entity/DTO wiring, the 8-stage request pipeline |
| `com.garganttua.core:garganttua-runtime-reflection` | `IReflectionProvider` resolved at JVM startup via classpath scanning |
| `com.garganttua.core:garganttua-reflections` | `IAnnotationScanner` backed by `org.reflections` for annotation discovery |

Nothing else is on the classpath. There is no DAO implementation, no HTTP adapter, no security module, and no AOT metadata processor.

### In-memory DAO

The framework's persistence contract is `IDao<T>`. For the quickstart, implement it yourself with a `HashMap` or supply a pre-built in-memory stub from your test utilities. The DTO builder accepts any `IDao` instance via `.db(dao)`, so wiring is a single line.

### When to graduate to the JVM or AOT starters

The quickstart starter is intentionally limited. Move to a production-grade starter when any of the following apply:

- **You need persistent storage** — use `garganttua-api-starter-jvm-mongo-javalin`, which adds `garganttua-api-dao-mongodb` for a MongoDB-backed `IDao`.
- **You need an HTTP interface** — the jvm-mongo-javalin starter will enable `garganttua-api-javalin` and `garganttua-api-interface-rest` once those modules complete their 3.0 port.
- **You need authentication and authorization** — `garganttua-api-security` is included in the jvm-mongo-javalin and aot-mongo-javalin starters.
- **You are building a GraalVM native image or you need fast startup** — use `garganttua-api-starter-aot-mongo-javalin`, which layers `garganttua-aot-reflection` and `garganttua-aot-annotation-scanner` on top of the runtime fallback pair. AOT processing is opt-in: add `garganttua-aot-maven-plugin` to your build to populate the index; without it the runtime stack is used transparently.

## Usage

Add the starter to your `pom.xml`:

```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-quickstart</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

Then wire your first API entirely in-process:

```java
import com.garganttua.api.core.builder.ApiBuilder;
import com.garganttua.api.spec.IApi;
import com.garganttua.reflection.utils.IClass;

// 1. Define your entity and DTO (plain Java classes with Lombok or manual getters/setters)
public class User {
    private String id;
    private String uuid;
    private String tenantId;
    private String name;
    // getters/setters …
}

public class UserDto {
    private String id;
    private String uuid;
    private String tenantId;
    private String name;
    // getters/setters …
}

// 2. Provide an in-memory IDao
import com.garganttua.api.spec.dao.IDao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InMemoryUserDao implements IDao<UserDto> {
    private final Map<String, UserDto> store = new HashMap<>();

    @Override public void save(UserDto entity) { store.put(entity.getUuid(), entity); }
    @Override public void delete(String uuid)  { store.remove(uuid); }
    @Override public UserDto findByUuid(String uuid) { return store.get(uuid); }
    @Override public List<UserDto> findAll()   { return new ArrayList<>(store.values()); }
    // implement remaining contract methods …
}

// 3. Build the API
IApi api = ApiBuilder.builder()
    .multiTenant(false)
    .domain(IClass.getClass(User.class))
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(IClass.getClass(UserDto.class))
            .id("id").uuid("uuid").tenantId("tenantId")
            .db(new InMemoryUserDao())
        .up()
    .up()
    .build();

// 4. Invoke operations in-process
api.request("users")
    .createOne(new UserDto(/* … */))
    .execute();

api.request("users")
    .readAll()
    .execute();
```

No server is started. Every call is synchronous and returns immediately. This is the full execution path — the same 8-stage pipeline that runs in production, exercised end-to-end without any infrastructure dependency.

## Tips and best practices

- **Keep the in-memory DAO simple** — a `HashMap` keyed on `uuid` is enough for tutorials and unit tests; resist the urge to make it clever. In production, swap it for a real `IDao` by switching starters.
- **Disable multi-tenancy explicitly** — call `.multiTenant(false)` unless your domain model actually requires tenant isolation. Enabling multi-tenancy and forgetting `.superTenantId()` or `.domain().tenant(true)` produces a `DslException` at build time; the quickstart is cleaner without it.
- **Re-use the built `IApi` instance** — `ApiBuilder.build()` runs the full configuration and pre-compilation phase. Create one instance per test suite (or per application lifetime) and share it, rather than rebuilding for every test.
- **Validate domain names** — domain names are auto-generated as the plural lowercase of the entity class name (`User` → `users`). Pass the same string to `api.request(...)` to avoid a `DomainNotFoundException` at runtime.
- **Graduate early** — the quickstart starter has no DAO persistence, no HTTP layer, and no security. As soon as any of those concerns enters scope, switch to `garganttua-api-starter-jvm-mongo-javalin`. The `ApiBuilder` DSL is identical; only the `pom.xml` dependency and the `IDao` implementation change.
- **Leverage `.build()` + `.execute()` for assertions** — the two-step `IRequestBuilder.build()` then `.execute()` pattern lets you inspect the request object before sending it, which is useful in test code.

## License

This module is distributed under the MIT License.
