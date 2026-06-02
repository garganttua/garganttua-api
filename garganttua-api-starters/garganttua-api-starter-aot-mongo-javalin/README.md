# Garganttua API Starter — AOT (MongoDB + Javalin)

## Description

One-dependency, batteries-included starter for Garganttua API applications
targeting **GraalVM native-image** — or any plain-JVM deployment where
classpath scanning at startup is unacceptable. It bundles the same MongoDB
persistence and Javalin HTTP stack as
[`garganttua-api-starter-jvm-mongo-javalin`](../garganttua-api-starter-jvm-mongo-javalin/README.md)
but promotes `garganttua-aot-reflection` and
`garganttua-aot-annotation-scanner` ahead of the runtime fallbacks in the
SPI resolution order, so every `IClass.getClass(...)` lookup and every
`@Entity*` scan hits the AOT index built at compile time rather than the
JDK classpath walker.

**Key Features:**

- **AOT-first reflection** — `AOTReflectionProvider@20` serves all type lookups from a compile-time index; JDK reflection is never invoked on the hot path.
- **AOT annotation scanning** — `AOTAnnotationScanner@20` replaces the Reflections classpath walker for annotation discovery; no classpath scanning at startup.
- **Runtime fallbacks included** — `garganttua-runtime-reflection` and `garganttua-reflections` remain on the classpath as lower-priority SPI entries, so the starter is a safe drop-in replacement for the JVM variant even before you wire the AOT plugin.
- **MongoDB persistence** — `garganttua-api-dao-mongodb` provides the `IDao` implementation for document storage, AOT-processed and `@Reflected`-annotated.
- **Native-image ready** — all active framework artifacts carry `resource-config.json` entries and `@Reflected` metadata consumable by the GraalVM `native-image` agent.
- **Javalin HTTP + REST interface + security** — modules pending the 3.0 port; dependency blocks are present in the POM but commented out and will be activated in a future release.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-aot-mongo-javalin</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-core`
 - `com.garganttua.core:garganttua-aot-reflection`
 - `com.garganttua.core:garganttua-aot-annotation-scanner`
 - `com.garganttua.core:garganttua-runtime-reflection`
 - `com.garganttua.core:garganttua-reflections`
 - `com.garganttua:garganttua-api-dao-mongodb`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### AOT vs JVM reflection

Both this starter and the JVM variant put
`garganttua-runtime-reflection` / `garganttua-reflections` on the
classpath. The difference is **SPI priority**: this starter also
includes `garganttua-aot-reflection` and
`garganttua-aot-annotation-scanner`, which the `ReflectionBuilder`
selects first when both provider families are present. The result:

| Concern | JVM starter | AOT starter |
|---|---|---|
| Type lookup (`IClass.getClass`) | JDK reflection | AOT index (compile-time) |
| Annotation scanning | Reflections classpath walk | AOT index (compile-time) |
| Runtime fallback | primary | secondary (safety net only) |
| Cold-start cost | O(classpath size) | O(1) after index load |

### The AOT index

The index is populated by `garganttua-aot-maven-plugin` (a
compile-time Maven plugin, separate from this runtime starter). The
plugin scans your source tree for `@Reflected` classes and writes the
metadata. **Without the plugin wired in your application POM, the AOT
providers return nothing and the runtime fallbacks take over** — the
starter is therefore a safe, no-op upgrade over the JVM variant until
you explicitly enable the plugin.

To wire the plugin, add the following to your application's
`maven-compiler-plugin` configuration:

```xml
<plugin>
    <groupId>com.garganttua.core</groupId>
    <artifactId>garganttua-aot-maven-plugin</artifactId>
    <version>${garganttua.core.version}</version>
    <executions>
        <execution>
            <goals><goal>process</goal></goals>
        </execution>
    </executions>
</plugin>
```

### Native-image build expectations

When building with GraalVM `native-image`:

1. All active framework modules (`garganttua-api-core`,
   `garganttua-api-dao-mongodb`, the core reflection and injection
   modules) ship embedded `resource-config.json` files recognised by
   the `native-image` tool automatically via the
   `META-INF/native-image/` convention.
2. Framework builders and annotation-processed types are annotated
   `@Reflected`, which instructs the AOT annotation processor to
   register them in the index and, when native-image support is
   finalised, to emit the corresponding `reflect-config.json` entries.
3. The `garganttua-api-security`, `garganttua-api-javalin`, and
   `garganttua-api-interface-rest` modules are **not yet active** in
   this starter (see the commented-out blocks in the POM). Their
   native-image configuration will be included once the 3.0 port is
   complete.

Expected startup log line (AOT stack active):

```
SPI bootstrap: providers=[AOTReflectionProvider@20, RuntimeReflectionProvider],
               scanners=[AOTAnnotationScanner@20, ReflectionsAnnotationScanner]
```

If you see only `RuntimeReflectionProvider` in the log, the AOT index
was not generated — verify the `garganttua-aot-maven-plugin` is in
your application build.

## Usage

Add the starter as your single framework dependency:

```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-starter-aot-mongo-javalin</artifactId>
    <version>3.0.0-ALPHA01</version>
    <type>pom</type>
</dependency>
```

Wire the AOT plugin (see **Core Concepts** above), then build your API
exactly as you would with the JVM starter:

```java
IApi api = ApiBuilder.builder()
    .packages("com.myapp")
    .domain(IClass.getClass(User.class))
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(IClass.getClass(UserDto.class))
            .id("id").uuid("uuid").tenantId("tenantId")
            .db(new MongoDao(mongoClient, "myapp"))
        .up()
    .up()
    .build();
```

For a GraalVM native-image build, compile with the standard
`native-image` Maven plugin pointing at your main class. The AOT index
and the embedded `resource-config.json` files are picked up
automatically — no additional `reflect-config.json` hand-authoring is
required for framework types.

## Tips and best practices

- **Wire the AOT plugin early.** Adding it later is safe (the fallback keeps things working), but you will not see the cold-start improvement until it is active and has produced an index.
- **Annotate your own types with `@Reflected`.** The AOT processor only indexes classes it finds annotated; application-defined entities, DTOs, and authentication classes must carry `@Reflected` to be included in the index.
- **Keep the runtime fallbacks.** Do not exclude `garganttua-runtime-reflection` or `garganttua-reflections` from the dependency graph. They cover types registered dynamically at runtime or loaded by a classloader the AOT processor did not see.
- **Prefer this starter over the JVM variant for production deployments** even when not targeting native-image: deterministic type discovery and the elimination of classpath scanning reduces startup time and removes a class of environment-specific reflection failures.
- **Pending modules (Javalin, REST interface, security)** — the commented-out dependency blocks in the POM are intentional placeholders. Watch the 3.0 release notes; enabling them will require only uncommenting those blocks, no API changes.

## License

This module is distributed under the Apache License, Version 2.0.
