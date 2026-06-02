# Garganttua API JSONPath Binding

## Description

`garganttua-api-binding-jsonpath` is a dependency-isolation wrapper that brings **Jayway JsonPath 2.9.0** (`com.jayway.jsonpath:json-path`) into the Garganttua API framework under a single, centrally-versioned artifact. It contains no framework classes of its own — its sole job is to be the one place where the external library version is pinned and managed.

**Key Features:**
- **Single version pin** — `json-path.version` is declared once in the root POM; every consumer depends on this binding artifact rather than on the raw Jayway coordinate
- **Clean dependency boundary** — `garganttua-api-commons` and `garganttua-api-core` stay free of any `com.jayway.jsonpath.*` import; only modules that explicitly need JsonPath take this dependency
- **Transitive re-export** — declaring `garganttua-api-binding-jsonpath` is sufficient; the Jayway jar is pulled in transitively, no second `<dependency>` block required in consuming modules
- **Implementation swap at pom level** — replacing the underlying JsonPath implementation requires editing a single artifact, with no changes to consumers

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-jsonpath</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-commons`
 - `com.jayway.jsonpath:json-path:2.9.0`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### Wrapped library

| Coordinate | Version |
|---|---|
| `com.jayway.jsonpath:json-path` | `2.9.0` |

Jayway JsonPath is a Java DSL for reading values out of JSON documents using XPath-style path expressions (e.g. `$['jti']`, `$['authorities'][*]`). The two entry points used by framework consumers are:

- `JsonPath.parse(String json)` — returns a `DocumentContext` bound to the JSON document
- `DocumentContext.read(String path)` — extracts a typed value at the given path

### Framework consumers

The only framework module that currently depends on this binding is `garganttua-api-security` (specifically the JWT sub-module). `JWTAuthorization` and `JWTRefreshableAuthorization` both use `JsonPath.parse` / `DocumentContext.read` to decode Base64-encoded JWT payload and header segments into typed fields (`uuid`, `tenantId`, `authorities`, `ownerId`, `creationDate`, `expirationDate`, `alg`).

## Usage

This module has no public API of its own. It is consumed **transitively** — any module that needs to call `com.jayway.jsonpath.*` declares a single Maven dependency on the binding artifact:

```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-jsonpath</artifactId>
</dependency>
```

No version is required in consuming modules; the version is inherited from the `garganttua-api-bindings` BOM / parent POM.

Direct Jayway usage in the consuming class follows the standard JsonPath API:

```java
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

DocumentContext ctx = JsonPath.parse(jsonString);
String jti      = ctx.read("$['jti']");
String tenantId = ctx.read("$['tenantId']");
List<String> authorities = ctx.read("$['authorities'][*]");
```

## Tips and best practices

- Do not add `com.jayway.jsonpath:json-path` as a direct dependency anywhere else in the project — always go through this binding so version upgrades remain a single-line change in the root POM.
- `JsonPath.parse` creates an in-memory parse tree; for repeated reads on the same document, reuse the `DocumentContext` rather than calling `parse` multiple times.
- JsonPath silently returns `null` for missing keys by default. Use `Option.THROW_ON_MISSING_PROPERTY` when absence should be an error.
- Type coercion is inferred by the generic return type; be explicit with casts (e.g. `ctx.<Integer>read("$['exp']")`) to avoid `ClassCastException` at runtime when the JSON value is a numeric type narrower than `Long`.

## License
This module is distributed under the Apache License, Version 2.0.
