# Garganttua API Native Image Config

## Description

Garganttua API Native Image Config scans the API class graph at build time and generates GraalVM native-image reflection and resource configuration files (`reflect-config.json`, `resource-config.json`) for all classes that the framework accesses reflectively at runtime.

The central entry point is `NativeImageConfigBuilder.createConfiguration(pathToConfiguration, packages)`. It performs classpath scanning over the supplied packages, classifies every discovered class by its annotation category, and delegates to a per-category processor that records exactly the constructors, methods, and fields that GraalVM must preserve. The resulting configuration is merged with any pre-existing content — so incremental runs are safe — and written back to disk.

> **Note:** The annotation package path contains a typo — `com.garganttua.api.nativve.image.config` (double `v` in `nativve`). This is a known artefact inherited from the legacy source tree. The module still compiles and produces correct configuration files; the typo does not affect runtime behaviour. When this module is reactivated, renaming the package would be the right time to fix it.

**Key Features:**
- **Annotation-driven scanning** — discovers classes carrying `@Entity`, `@Dto`, `@GGBean`, `@Authenticator`, `@Authentication`, and `@Authorization` across any list of packages using `GGObjectReflectionHelper`
- **Category-specific processors** — each class category has its own processor that records the precise reflection surface: entity classes get all declared fields plus every lifecycle-hook and persistence-method; DTO classes get `@GGFieldMappingRule` fields plus all declared methods and constructors; authorization classes get their two canonical constructors; GGBean classes get their no-arg constructor
- **Idempotent merge** — `ReflectConfig.loadFromFile` / `saveToFile` round-trips preserve existing entries so running the builder multiple times or combining output from several scanning passes does not produce duplicates
- **Resource registration** — every processed class is also registered in `resource-config.json` via `ResourceConfig.addResource` so GraalVM includes the corresponding `.class` resource
- **Pluggable processor interface** — `ClassProcessorInterface` is a `@FunctionalInterface`; custom processors can be passed to `processClasses` for project-specific annotation categories
- **Dormant / not yet published** — this module is currently commented out of the root reactor and is not part of the active build; it targets a forthcoming reactivation alongside the `garganttua-api-native-image` parent

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-native-image-config</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-reflection`
 - `com.garganttua:garganttua-native-image-utils`
 - `com.garganttua:garganttua-api-commons`
 - `com.garganttua:garganttua-objects-mapper`
<!-- AUTO-GENERATED-END -->

> ⚠️ This module is currently commented out of the root reactor (dormant) and is not published. The block above documents its intended coordinates once reactivated.

## Core Concepts

### NativeImageConfigBuilder

`NativeImageConfigBuilder.createConfiguration(String pathToConfiguration, List<String> packages)` is the sole public API. It:

1. Resolves (or creates) `reflect-config.json` and `resource-config.json` under `pathToConfiguration` using `NativeImageConfig.getReflectConfigFile` / `getResourceConfigFile`.
2. Loads the existing `ReflectConfig` from disk (empty when the file is newly created).
3. Scans each package for classes annotated with the six supported markers.
4. Calls `processClasses` once per category, passing the appropriate `ClassProcessorInterface` lambda.
5. Writes the merged `ReflectConfig` back to `reflect-config.json`.

### Class Categories and Their Processors

| Annotation | Processor | What is registered |
|---|---|---|
| `@Entity` | `processEntityClass` | No-arg constructor; all declared fields; lifecycle-hook methods (`@EntityBefore*`, `@EntityAfter*`); persistence methods (`@EntitySaveMethod`, `@EntityDeleteMethod`); all entity characteristic fields (`@EntityUuid`, `@EntityId`, `@EntityTenantId`, `@EntityOwnerId`, `@EntityLocation`, `@EntityHidden`, `@EntityShare`, …) |
| `@Dto` | `processDtoClass` | Fields annotated `@GGFieldMappingRule`; all declared methods; all declared constructors |
| `@Authorization` | `processAuthorizationClass` | Two specific constructors: `(byte[], IKeyRealm)` and `(String, String, String, String, List, Date, Date, IKeyRealm)` |
| `@GGBean` | `processGGBeanClass` | No-arg constructor |
| `@Authenticator` | *(scanned, not yet processed)* | Collected but no dedicated processor is wired in the current source; extend if needed |
| `@Authentication` | *(scanned, not yet processed)* | Collected but no dedicated processor is wired in the current source; extend if needed |

### ClassProcessorInterface

```java
@FunctionalInterface
public interface ClassProcessorInterface {
    ReflectConfigEntry processClass(ReflectConfig reflectConfig, Class<?> entityClass)
        throws NoSuchMethodException, SecurityException;
}
```

Implement this interface to add support for custom annotation categories, then call the package-private `processClasses` helper directly or wrap your invocation around `NativeImageConfigBuilder`.

### ReflectConfigEntryBuilder merge logic

Before creating a new entry, `getReflectConfigEntryBuilder` looks up the class by name in the already-loaded `ReflectConfig`. When an entry exists it returns a builder seeded from that entry, so subsequent processors accumulate information rather than overwriting it. This is the mechanism that makes multi-pass scanning safe.

## Usage

### Generate configuration for a single application package

```java
import com.garganttua.api.nativve.image.config.NativeImageConfigBuilder;
import java.util.List;

// Typically called from a Maven exec:java goal or a GraalVM feature
NativeImageConfigBuilder.createConfiguration(
    "src/main/resources/META-INF/native-image",
    List.of("com.example.myapp")
);
```

The two output files are written (or updated) under the target directory:

```
src/main/resources/META-INF/native-image/
  reflect-config.json
  resource-config.json
```

### Invoke from a Maven build (exec-maven-plugin)

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>generate-native-config</id>
            <phase>process-classes</phase>
            <goals><goal>java</goal></goals>
            <configuration>
                <mainClass>com.example.myapp.NativeConfigMain</mainClass>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Where `NativeConfigMain` is a thin wrapper that calls `NativeImageConfigBuilder.createConfiguration(...)`.

### Scanning multiple packages

```java
NativeImageConfigBuilder.createConfiguration(
    "target/native-config",
    List.of(
        "com.example.myapp.domain",
        "com.example.myapp.security",
        "com.example.myapp.dto"
    )
);
```

Each package is scanned independently; results are merged into the same pair of output files.

## Tips and best practices

- **Run at `process-classes` phase** — the builder requires compiled classes on the classpath. Binding it to `process-classes` (after `compile`) ensures all annotation-processed sources are visible.
- **Commit generated files** — treat the two config files as generated-but-committed artefacts (similar to `reflect-config.json` produced by the GraalVM agent). Regenerate them on every build that changes the domain model.
- **Wire `@Authenticator` and `@Authentication` processors** — these annotation categories are scanned but have no processor hooked up in the current source. If your application uses these classes in native image, add a processor lambda to the `createConfiguration` body or open an evolution proposal against `garganttua-api-native-image-config`.
- **Cross-check with the GraalVM tracing agent** — for integration tests, run the application under the GraalVM agent (`-agentlib:native-image-agent=config-output-dir=…`) and diff against the builder output. The two approaches are complementary: the builder covers the static annotation surface; the agent catches dynamic reflection paths.
- **Package typo** — the source package is `com.garganttua.api.nativve.image.config` (double `v`). If you import these classes, use that spelling until the package is renamed.
- **Idempotency** — the merge strategy means it is safe to run the builder in CI on every commit. Entries are only added, never removed; if you rename or delete a class, remove its entry from `reflect-config.json` manually or regenerate from scratch by deleting the file first.

## License
This module is distributed under the MIT License.
