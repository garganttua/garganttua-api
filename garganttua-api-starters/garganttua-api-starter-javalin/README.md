# Garganttua API Starter — Javalin

## Description

The **Javalin starter** is an HTTP transport add-on. Drop it on the classpath alongside the [bootstrap starter](../garganttua-api-starter-bootstrap) and every annotation-scanned domain is served over HTTP **with no DSL**: a `JavalinAutoConfiguration` reads `server.port`, stands up a single shared Javalin server, attaches it to all domains as the default interface, and registers the protocol adapter. JSON serialization works out of the box (the starter pulls `garganttua-api-binding-jackson`, whose `@Serializer` is auto-detected).

A domain that declares its own interface via `.interfasse(...)` keeps it — the shared server is only a default.

**Key features:**
- **Zero-DSL HTTP** — annotate `@Entity`/`@Dto`, set `server.port`, and CRUD is reachable over REST.
- **One server, many domains** — a single Javalin instance serves every domain; routes are `/{domain}` and `/{domain}/{uuid}`.
- **JSON included** — request/response bodies negotiate JSON via the bundled Jackson serializer.
- **Composable** — combine with `garganttua-api-starter-mongodb` for a full persisted HTTP stack (see `garganttua-api-starter-jvm-mongo-javalin`).

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-javalin</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-starter-bootstrap`
 - `com.garganttua:garganttua-api-binding-javalin`
 - `com.garganttua:garganttua-api-binding-jackson`
 - `com.garganttua.core:garganttua-starter-runtime:test`
 - `org.junit.jupiter:junit-jupiter-engine:test`
 - `org.mockito:mockito-core:test`
 - `org.mockito:mockito-junit-jupiter:test`

<!-- AUTO-GENERATED-END -->

## Usage

```java
public final class MyApp {
    public static void main(String[] args) {
        GarganttuaApplication.runAndWait(MyApp.class, args);   // blocks; serves HTTP
    }
}
```

`application.yaml`:

```yaml
api:
  multiTenant: false
  packages: com.myapp
server:
  port: 7000
```

The `users` domain (from an `@Entity User`) is then served at:

```
POST   /users          create
GET    /users          read all
GET    /users/{uuid}   read one
PUT    /users/{uuid}   update
DELETE /users/{uuid}   delete one
DELETE /users          delete all
```

```bash
curl -X POST localhost:7000/users -H 'Content-Type: application/json' -d '{"name":"alice"}'
curl localhost:7000/users
```

## Configuration keys

| Key | Default | Effect |
|---|---|---|
| `server.port` | `7000` | port the shared Javalin server binds to |

Overridable via `GARGANTTUA_SERVER_PORT`.

## Security note

A scanned `@Entity`/`@Dto` domain still carries the framework's security gate, whose **default access is `authenticated`** — anonymous HTTP calls get `401`. `@EntityPublic` only lifts tenant filtering, not authentication. To expose a domain anonymously, configure its access (`.security().creationAccess(Access.anonymous)…`) or disable the gate (`.security().disable(true)`) — typically through a small `IApiAutoConfiguration` that pre-declares the domain (the `@Entity` scan re-enters the same builder). A production API will wire real authentication via the security module instead.

## License

This module is distributed under the MIT License.
