# Garganttua API Native Image

## Description

Parent aggregator module for GraalVM native-image support in the `garganttua-api` framework (v3.0.0-ALPHA01, `groupId: com.garganttua`). It groups the submodules responsible for scanning the API classpath at build time and emitting the `reflect-config.json` / `resource-config.json` files that GraalVM requires to compile an API application to a native binary.

> **This module is currently commented out of the root reactor and is dormant.** The framework's primary AOT/native readiness strategy has moved to the active modules: classes and builders are annotated with `@Reflected` / `@ReflectedBuilder`, and each active module ships an `IAOTInfrastructureSeed` implementation that pre-registers its public types in the `garganttua-core` AOT registry at cold start. The `garganttua-starter-native` starter (from `garganttua-core`) then activates the GraalVM Feature that feeds those descriptors into `RuntimeReflection` at native-image analysis time — no manual `reflect-config.json` required for framework types. This module is preserved for cases where a lower-level, explicit configuration file approach is preferred, or where the annotation-driven path is insufficient.

**Key Features:**

- **Annotation-driven classpath scan** — `NativeImageConfigBuilder.createConfiguration(outputPath, packages)` scans listed packages and collects all classes annotated with `@Entity`, `@Dto`, `@GGBean`, `@Authenticator`, `@Authentication`, and `@Authorization`.
- **Reflection entry generation** — for each scanned class, the builder emits a `ReflectConfigEntry` with precisely the constructors, fields, and methods that the framework actually calls reflectively (no over-broad `allPublicMethods` blanketing).
- **Resource entry generation** — companion `resource-config.json` entries are written alongside the reflection entries so annotated classes are also retained as resources.
- **Idempotent merging** — existing `reflect-config.json` entries are loaded first; the builder merges new findings rather than overwriting, so it can be invoked incrementally.
- **Pluggable per-type processors** — the `ClassProcessorInterface` functional interface lets callers contribute custom processing logic for additional annotation categories without modifying the core builder.
- **Complementary to the `@Reflected` / AOT-seed path** — this module and the annotation-driven path are not mutually exclusive. The config files produced here can augment the AOT registry-driven approach for legacy types or third-party classes that cannot be annotated.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-native-image</artifactId>
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

The central utility class. Call it once during a build-time tool, Maven plugin, or test harness:

```java
NativeImageConfigBuilder.createConfiguration(
    "src/main/resources/META-INF/native-image",   // output directory
    List.of("com.example.myapp", "com.example.myapp.security")
);
```

Internally it delegates to one processor per annotation category:

| Annotation | Processor | Reflection surface registered |
|---|---|---|
| `@Entity` | `processEntityClass` | No-arg constructor, all declared fields, lifecycle-hook methods (`@EntityBefore*`, `@EntityAfter*`), persistence-method annotations (`@EntitySaveMethod`, `@EntityDeleteMethod`) |
| `@Dto` | `processDtoClass` | Fields annotated with `@GGFieldMappingRule`, `queryAllDeclaredMethods`, `queryAllDeclaredConstructors` |
| `@Authorization` | `processAuthorizationClass` | Two specific constructors (raw-bytes and full-parameter) required by the JWT signing/validation workflow |
| `@GGBean` | `processGGBeanClass` | Declared no-arg constructor |
| `@Authenticator`, `@Authentication` | _(collected, not yet processed in this version)_ | — |

### ClassProcessorInterface

A `@FunctionalInterface` (`ReflectConfigEntry processClass(ReflectConfig, Class<?>)`) that encapsulates the per-class logic. Implement it to teach `NativeImageConfigBuilder` about additional annotation categories:

```java
ClassProcessorInterface myProcessor = (reflectConfig, cls) -> {
    IReflectConfigEntryBuilder builder = ReflectConfigEntryBuilder.builder(cls);
    builder.queryAllDeclaredMethods(true);
    return builder.build();
};
```

### Idempotent Config File Handling

Before writing, `NativeImageConfigBuilder` calls `ReflectConfig.loadFromFile(reflectConfigFile)`. If a `reflect-config.json` already exists (e.g. from a previous run or from `native-agent` tracing), its entries are loaded and the builder merges into that set. This makes the tool safe to run multiple times or to combine with agent-generated configs.

### Relationship to the `@Reflected` / AOT-seed Approach

The `garganttua-core` AOT pipeline (`garganttua-aot-*` modules) and this module address the same problem from different angles:

| Aspect | `@Reflected` + AOT seed | This module |
|---|---|---|
| Configuration medium | Java registry (`IAOTRegistry`), fed by annotation processor at compile time and by seeds at cold start | JSON files (`reflect-config.json`, `resource-config.json`) read by the GraalVM `native-image` tool |
| Activation | `garganttua-starter-native` pulls the GraalVM Feature automatically | Must be invoked explicitly (build plugin, test, standalone tool) |
| Coverage | Framework types + any user type annotated `@Reflected` | Any type reachable by classpath scanning in the configured packages |
| Best for | Greenfield API applications built entirely on the framework | Migrating existing configs, covering third-party types, or environments where the AOT processor cannot run |

For new applications, prefer `@Reflected` annotations and `IAOTInfrastructureSeed` seeds. Reactivate this module when explicit JSON config files are required in addition to, or in place of, the registry-driven path.

## Submodules

| Module | Description | README |
|---|---|---|
| `garganttua-api-native-image-config` | Generates `reflect-config.json` and `resource-config.json` for API-annotated classes by scanning user packages at build time | [README](./garganttua-api-native-image-config/README.md) |

## Tips and best practices

- **Prefer `@Reflected` for framework types.** For any type owned by your project or the framework, annotate it with `@Reflected` and let the AOT annotation processor generate the descriptor. Reserve this module for third-party or legacy types you cannot annotate.
- **Pin the output path to `META-INF/native-image/<groupId>/<artifactId>/`.** The GraalVM toolchain auto-discovers config files under that path on the classpath; placing them elsewhere requires explicit `--native-image-config` flags.
- **Run the builder before `native-image`, not at runtime.** `NativeImageConfigBuilder` uses classpath reflection to inspect classes — it must run on a standard JVM during build, not inside the native binary being compiled.
- **Combine with agent tracing for completeness.** The annotation-based scan covers types the framework knows about; runtime-agent tracing (`-agentlib:native-image-agent`) catches dynamic reflection not reachable statically. Merge both outputs using the idempotent load-merge-save flow.
- **Watch the `@Authorization` constructor binding.** `processAuthorizationClass` registers two specific constructors by exact signature. If the `@Authorization` class changes its constructors, update the processor accordingly — missing constructors produce a silent instantiation failure at native runtime.
- **Reactivate carefully.** Before re-adding this module to the root reactor, verify that `garganttua-reflection` and `garganttua-native-image-utils` are published at the expected version in GitHub Packages, and that the `garganttua-api-commons` annotation set has not drifted from the imports in `NativeImageConfigBuilder`.

## License

This module is distributed under the MIT License.
