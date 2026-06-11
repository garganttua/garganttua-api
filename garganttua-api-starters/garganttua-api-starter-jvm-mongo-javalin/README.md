# Garganttua API Starter — JVM (MongoDB + Javalin)

## Description

The recommended **batteries-included** starter for a Garganttua API application on a standard JVM: MongoDB persistence and a Javalin HTTP layer, with runtime reflection. It is an aggregator that composes three code-bearing starters:

| Composed starter | Brings |
|---|---|
| [`garganttua-api-starter-bootstrap`](../garganttua-api-starter-bootstrap) | the `GarganttuaApplication` runner + runtime reflection + config loader |
| [`garganttua-api-starter-mongodb`](../garganttua-api-starter-mongodb) | MongoDB default-DAO auto-configuration |
| [`garganttua-api-starter-javalin`](../garganttua-api-starter-javalin) | Javalin transport auto-configuration + JSON |

Add this one dependency, annotate your `@Entity`/`@Dto`, drop an `application.yaml`, and `GarganttuaApplication.run(...)` serves CRUD over HTTP backed by MongoDB — with no `ApiBuilder` DSL.

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
 - `com.garganttua:garganttua-api-starter-bootstrap`
 - `com.garganttua:garganttua-api-starter-mongodb`
 - `com.garganttua:garganttua-api-starter-javalin`

<!-- AUTO-GENERATED-END -->

## Usage

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
        GarganttuaApplication.runAndWait(MyApp.class, args);
    }
}
```

`application.yaml`:

```yaml
api:
  multiTenant: false
  packages: com.myapp
mongodb:
  uri: mongodb://localhost:27017
  database: myapp
server:
  port: 7000
```

```bash
curl -X POST localhost:7000/users -H 'Content-Type: application/json' -d '{"name":"alice"}'
curl localhost:7000/users
```

The document is persisted in the `users` collection of the `myapp` database, and read back over HTTP.

## Configuration keys

Combines the keys of the composed starters: `api.*` (bootstrap), `mongodb.uri`/`mongodb.database` (MongoDB), `server.port` (Javalin). See each composed starter's README. All keys are environment-overridable (`GARGANTTUA_…`).

## Security note

CRUD defaults to `authenticated` access — anonymous HTTP calls get `401`. Configure anonymous access or wire authentication; see the [Javalin starter README](../garganttua-api-starter-javalin) for details.

## License

This module is distributed under the MIT License.
