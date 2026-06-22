# Garganttua API Jackson Binding

## Description

`garganttua-api-binding-jackson` is a **pure dependency-bundle module** — it contains no source code of its own. Its sole purpose is to declare and version-pin the Jackson family of artifacts (`jackson-annotations`, `jackson-core`, `jackson-databind`) together with `geojson-jackson` so that every consuming module resolves exactly the same JAR versions without repeating coordinates or version literals.

Consumers (`garganttua-api-core`, `garganttua-api-security`, `garganttua-api-javalin`) declare a single dependency on this binding and get the full Jackson + GeoJSON surface transitively. The root POM controls the actual version strings via the properties `${jackson.version}` (currently **2.18.6**) and `${geojson-jackson.version}` (currently **1.14**) — bumping those properties is the only change required to upgrade Jackson across the entire framework.

**Key Features:**
- **Single upgrade point** — `jackson.version` and `geojson-jackson.version` in the root POM control the full Jackson surface for every module
- **Conflict-free transitive graph** — `geojson-jackson`'s own Jackson transitive dependencies are explicitly excluded so the three core Jackson JARs come from exactly one coordinate set
- **GeoJSON support** — `org.geojson.GeoJsonObject` and its subtypes are available for geolocation filter expressions (used by `Filter.geolocWithin` / `Filter.geolocWithinSphere` in `garganttua-api-core`)
- **No source, no surprise** — the module cannot introduce behaviour of its own; auditing its effect means reading its `pom.xml`

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-jackson</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.fasterxml.jackson.core:jackson-annotations:2.18.6`
 - `com.fasterxml.jackson.core:jackson-core:2.18.6`
 - `com.fasterxml.jackson.core:jackson-databind:2.18.6`
 - `de.grundid.opendatalab:geojson-jackson:1.14`
 - `com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.6`
 - `com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.6`
 - `com.garganttua:garganttua-api-commons`
 - `com.garganttua.core:garganttua-bootstrap:2.0.0-ALPHA04:test`
 - `org.junit.jupiter:junit-jupiter-engine:test`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### Bundle module pattern

`garganttua-api-bindings` follows the principle of **one external library per submodule**. Each binding submodule acts as a versioned façade: it re-exports a library's artifacts as a single Maven coordinate and centralises version management. Upgrading the underlying library requires only a property change in the root POM; no individual consumer POM needs to be touched.

### Dependency graph cleanup

`geojson-jackson` depends on Jackson internally but pins its own older version. This binding explicitly excludes those transitive Jackson JARs and instead relies on the three `com.fasterxml.jackson.core` artifacts declared at the top of the binding's own `<dependencies>` section. The result is a flat, unambiguous Jackson classpath regardless of how many modules pull in the binding.

### Jackson roles in garganttua-api

Jackson serves two distinct roles inside the framework:

| Role | Consumers | Detail |
|------|-----------|--------|
| **Annotation-driven serialization** | `garganttua-api-core` | `@JsonProperty` / `@JsonIgnore` on `Filter` control how filter expressions are serialized to JSON for repository adapters |
| **JWT payload serialization** | `garganttua-api-security` (JWT module) | `ObjectMapper` serializes and deserializes the JSON payload embedded in JWT tokens (`JWTAuthorization`, `JWTRefreshableAuthorization`) |

`geojson-jackson` is used specifically in `Filter` for geolocation predicates (`geolocWithin`, `geolocWithinSphere`) that accept `GeoJsonObject` arguments matching MongoDB `$geoWithin` / `$centerSphere` semantics.

## Usage

This module carries no sources and produces no classes of its own. You do not import anything from it directly. To gain access to Jackson and GeoJSON types, add a single dependency on this binding in your module's `pom.xml`:

```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-jackson</artifactId>
    <!-- version managed by garganttua-api root POM dependencyManagement -->
</dependency>
```

Once on the classpath, all three Jackson core artifacts and `geojson-jackson` resolve transitively:

```java
// Jackson annotations — control serialization shape
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

// Jackson databind — programmatic (de)serialization
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

// GeoJSON — geolocation filter arguments
import org.geojson.GeoJsonObject;
import org.geojson.Point;
import org.geojson.Polygon;
```

Modules that already pull in `garganttua-api-core` or `garganttua-api-security` as transitive dependencies receive Jackson automatically — there is no need to add an explicit dependency on this binding in application-level code.

## Tips and best practices

- **Never declare a direct Jackson dependency** alongside this binding — let the binding own all Jackson coordinates to avoid version conflicts. If a version upgrade is needed, change `jackson.version` in the root POM only.
- **Do not add `jackson-datatype-*` or `jackson-module-*` here** unless they are needed by every module. Extension modules that require, for example, `jackson-module-kotlin` or `jackson-datatype-jsr310` should declare those as direct dependencies in the module that needs them, not in this bundle.
- **Audit the exclusions** if you ever introduce another library that also depends on Jackson — check whether it re-declares Jackson transitive deps that would bypass the exclusion block.
- **Reuse `ObjectMapper` instances** — `ObjectMapper` is thread-safe after configuration and expensive to construct. Declare it as a static final field or inject a shared instance rather than constructing one per call.

## License

This module is distributed under the MIT License.
