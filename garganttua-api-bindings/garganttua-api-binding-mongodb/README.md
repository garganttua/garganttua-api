# Garganttua API MongoDB Binding

## Description

`garganttua-api-binding-mongodb` is a **dependency-only binding module** — it carries no Java sources of its own. Its sole purpose is to declare the MongoDB synchronous driver (`org.mongodb:mongodb-driver-sync`) as a managed, versioned dependency so that every consumer in the framework obtains exactly the same driver artifact without repeating coordinates or version numbers.

The driver version is pinned once in the root POM (`mongodb-driver.version = 5.1.1`) and resolved transitively by the single consumer that currently depends on this binding: `garganttua-api-dao-mongodb`.

**Key Features:**
- **Single-source version pinning** — the driver version lives exclusively in the root POM property `mongodb-driver.version`; bumping it in one place propagates to every consumer automatically
- **Zero application logic** — the module contains no classes, resources, or configuration; it is a pure BOM-style wrapper that keeps driver coordinates out of consumer POMs
- **Transitive re-export** — any module that depends on `garganttua-api-binding-mongodb` receives `mongodb-driver-sync` on its compile classpath without additional declarations
- **Isolated upgrade surface** — upgrading the MongoDB driver only requires changing `mongodb-driver.version` in the root POM and rebuilding this binding; no consumer POM needs to be touched

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-mongodb</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `org.mongodb:mongodb-driver-sync:5.1.1`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### Pure binding pattern

The Garganttua API bindings group (`garganttua-api-bindings`) follows a deliberate convention: **one submodule per external library**. Each submodule wraps exactly one third-party artifact, pins its version through a root-POM property, and exposes it as a transitive dependency. This keeps external version sprawl confined to a single, auditable layer of the build.

`garganttua-api-binding-mongodb` is the canonical application of that pattern for the MongoDB Java driver:

```
garganttua-api-bindings/
  garganttua-api-binding-mongodb/
    pom.xml   ← declares org.mongodb:mongodb-driver-sync:${mongodb-driver.version}
               (no src/, no resources)
```

### Version property

```xml
<!-- root pom.xml -->
<properties>
    <mongodb-driver.version>5.1.1</mongodb-driver.version>
</properties>
```

Changing this single property is the only action required to upgrade (or downgrade) the MongoDB driver across the entire framework.

### Driver variant

The binding wraps `mongodb-driver-sync`, the blocking/synchronous MongoDB Java driver. This is the appropriate choice for the garganttua-api stack, which is thread-per-request and does not use reactive streams for its DAO layer.

## Usage

Because this module carries no code, direct application code never imports it. The intended usage is **transitive**: depend on `garganttua-api-dao-mongodb` and the driver arrives automatically on the classpath.

```xml
<!-- In your application POM — depend on the DAO module, not this binding directly -->
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-dao-mongodb</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

`garganttua-api-dao-mongodb` declares `garganttua-api-binding-mongodb` as a compile dependency, which in turn re-exports `mongodb-driver-sync`. Your application therefore receives the driver without any additional POM entry.

If you need to add a second consumer (for example, a migration tool or a custom DAO variant), add a dependency on this binding directly to keep the driver version consistent with the rest of the framework:

```xml
<!-- Only when building a new module that needs the driver independently -->
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-mongodb</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

## Tips and best practices

- **Never redeclare `mongodb-driver-sync` directly** in a consumer POM — always go through this binding so the version remains consistent across the framework.
- **Driver upgrades** should be validated against `garganttua-api-dao-mongodb` integration tests before merging; the DAO module is the authoritative integration point.
- **Do not add application-level code to this module** — if MongoDB-related helpers or utilities are needed, they belong in `garganttua-api-dao-mongodb` or a dedicated sub-module, not here.
- **Version conflicts** — if a downstream Spring Boot application brings its own MongoDB driver via a Spring BOM, use Maven's `<dependencyManagement>` in the application POM to enforce the version declared here, or align the Spring BOM version with the one pinned in `mongodb-driver.version`.

## License

This module is distributed under the MIT License.
