# Garganttua API Starter — AOT (MongoDB + Javalin)

## Description

The **AOT variant** of the batteries-included stack: identical to [`garganttua-api-starter-jvm-mongo-javalin`](../garganttua-api-starter-jvm-mongo-javalin) (bootstrap runner + MongoDB + Javalin), but it adds `garganttua-aot-reflection` and `garganttua-aot-annotation-scanner` ahead of the runtime reflection fallbacks. It targets **GraalVM native-image** — or any JVM deployment where classpath scanning at startup is too expensive.

The runner is unchanged: garganttua-core's `Bootstrap` ServiceLoader picks the AOT reflection providers automatically when they are on the classpath, so every `IClass.getClass(...)` lookup and `@Entity` scan hits the compile-time AOT index rather than walking the classpath.

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
 - `com.garganttua.core:garganttua-aot-reflection`
 - `com.garganttua.core:garganttua-aot-annotation-scanner`
 - `com.garganttua:garganttua-api-starter-bootstrap`
 - `com.garganttua:garganttua-api-starter-mongodb`
 - `com.garganttua:garganttua-api-starter-javalin`

<!-- AUTO-GENERATED-END -->

## Usage

The application code, annotations and `application.yaml` are **identical** to the [jvm-mongo-javalin starter](../garganttua-api-starter-jvm-mongo-javalin) — only the dependency changes:

```java
public final class MyApp {
    public static void main(String[] args) {
        GarganttuaApplication.runAndWait(MyApp.class, args);
    }
}
```

## AOT processing is opt-in

The AOT reflection providers consult an index produced at compile time. Add the `garganttua-aot-maven-plugin` to your application's build to populate it:

- **with the plugin** — `@Entity`/`@Dto`/security metadata is resolved from the AOT index; suitable for GraalVM native-image and fast cold starts.
- **without the plugin** — the AOT providers find nothing and the bundled runtime reflection fallback (`garganttua-runtime-reflection` + `garganttua-reflections`, brought transitively by the bootstrap starter) kicks in transparently. So this starter is a safe, no-op upgrade over the JVM variant until you enable the plugin.

## Configuration keys

Identical to the jvm-mongo-javalin starter: `api.*`, `mongodb.uri`/`mongodb.database`, `server.port`. All environment-overridable (`GARGANTTUA_…`).

## License

This module is distributed under the MIT License.
