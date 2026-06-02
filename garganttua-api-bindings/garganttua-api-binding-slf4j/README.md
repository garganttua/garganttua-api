# Garganttua API SLF4J Binding

## Description

`garganttua-api-binding-slf4j` is a **pure dependency bundle** — it contains no Java sources. Its sole purpose is to pin the SLF4J facade and its reference simple implementation at a version that is compatible with the rest of the Garganttua API dependency graph (`slf4j-api` + `slf4j-simple`, both at `2.0.13`).

The Garganttua API framework itself does **not** depend on SLF4J internally; its own logging goes through `com.garganttua.core.observability.Logger` (the garganttua-core Diagnostics facade). This binding is provided as an **opt-in** for two categories of consumers:

1. **Application code** that prefers to write log statements against the standard `org.slf4j.Logger` / `LoggerFactory` API.
2. **Third-party libraries** brought into the same classpath that ship SLF4J-based logging and need a concrete backend present at runtime — adding this binding satisfies their `slf4j-api` requirement and provides `slf4j-simple` as the fallback backend.

**Key Features:**
- **Version pinning** — `slf4j-api` and `slf4j-simple` are locked to `2.0.13` (property `${slf4j.version}` in the root POM), eliminating version-conflict noise in downstream projects.
- **Zero framework coupling** — this module introduces no Garganttua API classes; adding or removing it never affects framework behaviour.
- **slf4j-simple included** — provides a working out-of-the-box backend so the classpath is never left without an SLF4J provider (avoids the `No SLF4J providers were found` warning at startup).
- **Drop-in replacement** — swap `slf4j-simple` for Logback, Log4j 2, or any other SLF4J-2.x provider by excluding `slf4j-simple` from this binding and adding your preferred provider.

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-slf4j</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `org.slf4j:slf4j-api:2.0.13`
 - `org.slf4j:slf4j-simple:2.0.13`

<!-- AUTO-GENERATED-END -->

## Core Concepts

### SLF4J facade

SLF4J (`org.slf4j:slf4j-api`) is a logging facade: application and library code compiles against its `Logger` / `LoggerFactory` interfaces without binding to any specific backend. The actual log output is delegated at runtime to whichever provider is on the classpath (Logback, Log4j 2, `slf4j-simple`, etc.).

### slf4j-simple

`org.slf4j:slf4j-simple` is the official minimal backend shipped alongside the SLF4J API. It writes all messages at `INFO` level or above to `System.err`. It is intentionally lightweight — suitable for tests, command-line tools, and local development — but not recommended for production systems where structured logging, async appenders, or log rotation are required.

### Relationship to the Garganttua Diagnostics facade

`com.garganttua.core.observability.Logger` is the logging entry point used by all Garganttua framework internals. It is decoupled from SLF4J by design so that foundation layers (injection, reflection, workflow engine) carry no SLF4J dependency. This binding does not bridge the two: framework logs still flow through the Diagnostics facade, while application logs written to `org.slf4j.Logger` flow through whichever SLF4J backend is present.

If you need framework-internal events to appear in the same SLF4J log stream, wire an `IObserver<ObservableEvent>` (from `garganttua-observability`) that delegates to an `org.slf4j.Logger` — that is the correct integration point.

## Usage

Add the binding to your application or Spring Boot module:

```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-slf4j</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

Then use SLF4J as normal in your own classes:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyService {

    private static final Logger log = LoggerFactory.getLogger(MyService.class);

    public void process(String input) {
        log.debug("Processing input: {}", input);
        // ...
        log.info("Processing complete");
    }
}
```

### Replacing slf4j-simple with Logback (recommended for production)

Exclude `slf4j-simple` and add Logback:

```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-binding-slf4j</artifactId>
    <version>3.0.0-ALPHA01</version>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
</dependency>
```

### Bridging Garganttua observability events into SLF4J

To route garganttua-core engine events (workflow execution, mapper, bootstrap, etc.) into your SLF4J-backed logs, implement a thin observer:

```java
import com.garganttua.core.observability.IObserver;
import com.garganttua.core.observability.ObservableEvent;
import com.garganttua.core.observability.StartEvent;
import com.garganttua.core.observability.EndEvent;
import com.garganttua.core.observability.ErrorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jObserver implements IObserver<ObservableEvent> {

    private static final Logger log = LoggerFactory.getLogger(Slf4jObserver.class);

    @Override
    public void onEvent(ObservableEvent event) {
        switch (event) {
            case StartEvent s  -> log.debug("[{}] start  {}", s.executionId(), s.source());
            case EndEvent e    -> log.debug("[{}] end    {} – {}ms",
                                      e.executionId(), e.source(), e.duration().toMillis());
            case ErrorEvent er -> log.warn("[{}] error  {}: {}",
                                      er.executionId(), er.source(),
                                      er.failure().getMessage(), er.failure());
        }
    }
}
```

Wire the observer on each workflow or use `ObservabilityBuilder.create()` to attach it to multiple observables at once.

## Tips and best practices

- **Do not depend on this binding transitively inside other framework modules.** It is intended for application-layer and integration modules only. Core framework modules (`garganttua-api-spec`, `garganttua-api-core`, security, DAO) must not pull in SLF4J.
- **Prefer `slf4j-simple` for tests only.** Replace it with Logback or Log4j 2 in any module that ships to production so you get async appenders, rolling files, and structured output.
- **One SLF4J provider per classpath.** If your Spring Boot project already brings in `spring-boot-starter-logging` (which includes Logback), exclude `slf4j-simple` from this binding to avoid the `Class path contains multiple SLF4J providers` warning.
- **Log levels with slf4j-simple** are controlled via system properties (`-Dorg.slf4j.simpleLogger.defaultLogLevel=debug`) or a `simplelogger.properties` file on the classpath — there is no XML configuration file.
- **SLF4J 2.x uses the ServiceLoader provider mechanism** instead of the static binding of SLF4J 1.x. Ensure all SLF4J artifacts on the classpath are on the same `2.x` line to avoid conflicts with older libraries that still ship `slf4j-api:1.x`.

## License

This module is distributed under the MIT License.
