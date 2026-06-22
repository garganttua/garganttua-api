# Garganttua API Starter — MongoDB

## Description

The **MongoDB starter** is a persistence add-on. Drop it on the classpath alongside the [bootstrap starter](../garganttua-api-starter-bootstrap) and every annotation-scanned domain becomes persistable in MongoDB **with no DSL**: a `MongoAutoConfiguration` reads the connection settings from your `application.yaml`, opens a `MongoClient`, and registers a default DAO that yields a `MongoDao` per domain (the collection name is the plural domain name, e.g. `User` → `users`).

A domain whose DTO declares an explicit `.db(...)` keeps it — the default DAO is only a fallback.

**Key features:**
- **Zero-DSL persistence** — annotate `@Entity`/`@Dto`, set `mongodb.uri`/`mongodb.database`, and CRUD persists to MongoDB.
- **One client, many collections** — a single `MongoClient` serves every domain; each domain maps to its own collection.
- **Lifecycle-managed** — the `MongoClient` is registered as a closeable resource and shut down with the application.
- **Composable** — combine with `garganttua-api-starter-javalin` to expose the persisted domains over HTTP (see `garganttua-api-starter-jvm-mongo-javalin`).

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-mongodb</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-starter-bootstrap`
 - `com.garganttua:garganttua-api-dao-mongodb`
 - `com.garganttua.core:garganttua-starter-runtime:test`
 - `org.junit.jupiter:junit-jupiter-engine:test`
 - `org.mockito:mockito-core:test`
 - `org.mockito:mockito-junit-jupiter:test`

<!-- AUTO-GENERATED-END -->

## Usage

```java
public final class MyApp {
    public static void main(String[] args) {
        GarganttuaApplication.run(MyApp.class, args);
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
```

That is all: the auto-configuration wires a `MongoDao` onto every domain that did not declare its own DAO.

## Configuration keys

| Key | Required | Effect |
|---|---|---|
| `mongodb.uri` | yes | MongoDB connection string passed to `MongoClients.create(...)` |
| `mongodb.database` | yes | database name; each domain maps to a collection named after the domain |

Both keys are overridable by environment variables: `GARGANTTUA_MONGODB_URI`, `GARGANTTUA_MONGODB_DATABASE`. A missing key fails fast at startup with a pointed message.

## Notes

- The collection name is the **plural, lower-case domain name** (`Order` → `orders`).
- The DAO is created once per domain at build time; document read-back relies on the framework handing each DAO its domain definition at startup, so no extra wiring is needed.

## License

This module is distributed under the MIT License.
