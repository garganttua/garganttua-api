# Garganttua API Bindings

## Description

The **garganttua-api-bindings** module is the aggregator parent for all external third-party library integrations in the Garganttua API framework. Each submodule wraps exactly one external library (or one coherent library family) and isolates the framework from its compile/runtime contract. Consumers that need a particular library declare the binding artifact instead of the raw upstream coordinates, so swapping implementations or bumping a version remains a single edit in the root POM.

**Key Features:**
- **One lib per submodule** — each binding artifact has a single, clearly named external dependency (or family), making transitive graphs easy to audit
- **Centrally pinned versions** — all `${library.version}` properties live in the root POM `<properties>` block; no version literal ever appears inside a binding submodule
- **Swap-friendly isolation** — framework modules (`api-core`, `api-dao-mongodb`, etc.) import only `com.garganttua` binding coordinates; replacing an underlying library touches one binding POM and one root property
- **Exclusion management** — binding POMs own all `<exclusion>` rules needed to eliminate transitive duplicates, keeping consumer POMs clean
- **AOT/native-safe** — bindings carry no annotation processors of their own; each is reflected through the standard `garganttua-native` seed mechanism when native-image support is required

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-bindings</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies

<!-- AUTO-GENERATED-END -->

## Core Concepts

### The binding philosophy

Framework code that needs Jackson, SLF4J, JsonPath, MongoDB, or Javalin does **not** depend on those libraries directly. It depends on the corresponding `garganttua-api-binding-*` artifact. This creates a deliberate seam between the framework and any given external library:

- **Single-sourced versions** — the root POM declares `<jackson.version>`, `<mongodb-driver.version>`, etc. A version upgrade is one line; every consumer inherits it automatically.
- **Explicit coupling surface** — what the framework actually uses from a third-party library is visible by reading one binding POM, not by grepping transitive graphs across a dozen modules.
- **Implementation substitution** — a future `garganttua-api-binding-jackson-xml` or `garganttua-api-binding-mongodb-reactive` can be added as a sibling submodule without changing any framework source code.

### How versions are pinned

All external version properties are declared in the `<properties>` section of the root `garganttua-api` POM. Binding submodules reference them via `${property.name}` placeholders and never hard-code a version literal. This guarantees that a `grep` on the root POM gives a complete, accurate bill of third-party materials.

### The wrap-one-lib rule

A binding submodule must wrap **one** external library or one tightly coupled family (e.g. `jackson-core` + `jackson-databind` + `jackson-annotations` are one family; `geojson-jackson` extends it and is bundled for the same reason). If two unrelated libraries are needed in the same framework feature, they get two separate binding submodules.

## Submodules

| Module | Wraps | Version | Purpose |
|:--|:--|:--|:--|
| [garganttua-api-binding-jackson](./garganttua-api-binding-jackson/README.md) | `com.fasterxml.jackson` + `geojson-jackson` | Jackson `2.18.6`, GeoJSON-Jackson `1.14` | JSON serialisation and deserialisation (annotations, core, databind) plus GeoJSON type support used by the geolocalized entity characteristic |
| [garganttua-api-binding-javalin](./garganttua-api-binding-javalin/README.md) | `io.javalin:javalin` | `6.6.0` | Lightweight embedded HTTP server; pulled in exclusively by the `garganttua-api-javalin` transport module |
| [garganttua-api-binding-jsonpath](./garganttua-api-binding-jsonpath/README.md) | `com.jayway.jsonpath:json-path` | `2.9.0` | JSON path expression evaluation; used by the JWT security module for claims extraction, keeping `api-core` free of the dependency |
| [garganttua-api-binding-mongodb](./garganttua-api-binding-mongodb/README.md) | `org.mongodb:mongodb-driver-sync` | `5.1.1` | MongoDB synchronous driver; pulled in by `garganttua-api-dao-mongodb` so driver coordinates stay concentrated in one place |
| [garganttua-api-binding-slf4j](./garganttua-api-binding-slf4j/README.md) | `org.slf4j:slf4j-api` + `slf4j-simple` | `2.0.13` | SLF4J logging façade and simple implementation; opt-in for consumers who prefer the SLF4J API or need to bridge it into the garganttua-core Diagnostics provider |

## Tips and best practices

- **Never add a version literal inside a binding submodule.** Always reference a root-POM property. If no property exists yet, add it to the root POM first.
- **One external library per binding.** When in doubt, create a new sibling submodule rather than bundling an unrelated library into an existing one.
- **Declare the binding, not the raw artifact.** Framework modules and application code should depend on `garganttua-api-binding-*` coordinates, not on `com.fasterxml.jackson.*` directly. This is what makes version management single-sourced.
- **Put exclusions here, not upstream.** If a raw library pulls in a conflicting transitive dependency, add the `<exclusion>` in the binding POM — not in every consumer that includes the binding.
- **SLF4J is opt-in.** The framework itself logs through garganttua-core's Diagnostics façade. Only add `garganttua-api-binding-slf4j` to application POMs that explicitly want the SLF4J API on the classpath.
- **Check the root POM before upgrading.** All version properties are co-located in `<properties>` in the root `garganttua-api` POM. A single diff there is the complete record of a library upgrade.

## License
This module is distributed under the Apache License, Version 2.0.
