# Garganttua API REST Interface

## Description

`garganttua-api-interface-rest` is the **REST transport binding** for the `garganttua-api` framework. Its role is to implement the `IProtocol<REQ, RES>` contract defined in `garganttua-api-commons`, wiring an HTTP request object (Servlet, Javalin `Context`, or any compatible HTTP abstraction) into the ten-stage domain pipeline and building the corresponding HTTP response on the way out.

The module sits at stage 1 (extract) and stage 10 (response) of the pipeline. When registered on an `IApi` context, it bridges the raw transport layer to the structured `IOperationRequest` fields the framework's CRUD, security, and serialization stages expect: path, method, headers, body bytes, caller identity, and query parameters. The reverse path takes the pipeline's final output (a serialized `byte[]` or a raw DTO) together with a numeric HTTP status code and assembles the transport response.

> **Dormant** — this module is currently commented out of the root reactor (`<module>garganttua-api-interface</module>` is inactive in the root `pom.xml`). The `IProtocol` abstraction it targets is fully operational in `garganttua-api-core` (see `ProtocolIntegrationTest` for working end-to-end coverage using a `FakeHttpProtocol`). The REST binding itself contains no Java sources yet; it is a placeholder for the 3.0 port.

**Key Features:**
- **`IProtocol` implementation** — provides the full protocol contract: `getCaller`, `getRawBody`, `getAuthorization`, `getContentType`, `getAccept`, `getPath`, `getMethod`, `getQueryParameters`, and `buildResponse`
- **Auto-discovery support** — intended to carry the `@Protocol` annotation so it registers itself automatically when `IApiBuilder.autoDetect(true)` is enabled and the containing package is in scope
- **Mode A / Mode B transparency** — when a `rawRequest` is present the pipeline routes through the protocol; when absent (programmatic / internal calls) the protocol stages are skipped entirely with zero overhead
- **Content negotiation bridge** — translates `Content-Type` and `Accept` headers into the serializer selection step, enabling the framework's `ISerializer` pool to pick the right codec per request
- **Status-code mapping** — converts the pipeline's `OperationResponseCode` into the appropriate HTTP numeric status, so transport clients receive standard 200 / 201 / 400 / 401 / 403 / 404 / 500 codes
- **Transport-agnostic design** — depends only on `garganttua-api-commons`; zero hard dependency on Servlet API, Javalin, or any HTTP container

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-interface-rest</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
 - `com.garganttua:garganttua-api-commons`
<!-- AUTO-GENERATED-END -->

> ⚠️ This module is currently commented out of the root reactor (dormant) and is not published. The block above documents its intended coordinates once reactivated.

## Core Concepts

### The `IProtocol` contract

`IProtocol<REQ, RES>` (package `com.garganttua.api.commons.protocol`) is the framework's transport-adapter interface. It decouples the pipeline from any specific HTTP library. An implementation provides two responsibilities:

**Extraction (stage 1)** — given a raw `REQ` object, decompose it into:
- `getCaller(REQ)` — parse the Authorization header or well-known identity headers into an `ICaller` (tenantId, callerId, authorities, super-tenant/owner flags)
- `getRawBody(REQ)` — return the request body as `byte[]`; `null` for bodyless verbs (GET, DELETE)
- `getAuthorization(REQ)` — the raw Authorization header value (e.g. `"Bearer <token>"`)
- `getContentType(REQ)` — the `Content-Type` header
- `getAccept(REQ)` — the `Accept` header
- `getPath(REQ)` — the request path (e.g. `"/users/42"`)
- `getMethod(REQ)` — the HTTP verb (`"GET"`, `"POST"`, `"PUT"`, `"DELETE"`, …)
- `getQueryParameters(REQ)` — query-string parameters as a flat `Map<String, String>`

**Response building (stage 10)** — given the original `REQ`, the pipeline output (`byte[]` or a raw object), and an HTTP-style status code, produce a `RES`:

```java
RES buildResponse(REQ request, Object output, int statusCode) throws ApiException;
```

The framework resolves which registered protocol handles a given raw request via `IApi.getProtocols()`, iterating in registration order and calling `IProtocol.requestType().isInstance(request)`. More specific request types must be registered before broader ones.

### Protocol discriminator

The `@Protocol` annotation (package `com.garganttua.api.commons.protocol`) marks an `IProtocol` implementation for auto-detection. Requirements for auto-detection:

- The class must implement `IProtocol`
- The class must expose a public no-arg constructor
- The containing package must be declared via `IApiBuilder.withPackage(...)` (or `IApiBuilder.packages(...)`) and `IApiBuilder.autoDetect(true)` must be set

Protocols registered manually via `IApiBuilder.protocol(IProtocol)` or `IApiBuilder.protocol(ISupplierBuilder)` are inserted at the front of the pool, before auto-detected ones.

### Pipeline modes

The framework supports two invocation modes that this protocol participates in:

**Mode A — full transport flow**

A `rawRequest` key is present in the `IOperationRequest` argument map. The pipeline calls `resolveProtocol` to find the matching `IProtocol`, extracts all fields, runs the CRUD and security stages, serializes the output if `Accept` was set, and finally calls `buildResponse` to produce the transport response. The `IOperationResponse.getResponse()` is the `RES` object produced by `buildResponse`.

**Mode B — bypass**

No `rawRequest` is present. The protocol stages (1 and 10) are skipped. The request is built programmatically (`IApi.request("users").createOne(body).execute()`), and the response carries the raw DTO or entity object directly. This is how internal service calls work — no HTTP involvement.

### Caller extraction and security handoff

`getCaller` is the protocol's critical contribution to security. It receives the raw request and returns an `ICaller` that carries:
- `tenantId` / `requestedTenantId` — resolved from a header or the Authorization token
- `callerId` — user/account identifier
- `ownerId` — optional owner identity for owned-entity filtering
- `authorities` — list of roles/grants the caller holds
- `superTenant` / `superOwner` flags — set when the caller bypasses tenant/owner filtering

The result of `getCaller` is handed to the `VERIFY_AUTHORIZATION` workflow stage unchanged. If the protocol returns a minimal anonymous caller, that caller will only be admitted to operations whose access level is `anonymous`.

### HTTP status code mapping

`buildResponse` receives the status code resolved by the pipeline's exit-code stage. The mapping from `OperationResponseCode` to HTTP codes follows the REST convention:

| `OperationResponseCode` | HTTP |
|---|---|
| `OK` | 200 |
| `CREATED` | 201 |
| `BAD_REQUEST` | 400 |
| `UNAUTHORIZED` | 401 |
| `FORBIDDEN` | 403 |
| `NOT_FOUND` | 404 |
| `CONFLICT` | 409 |
| `INTERNAL_SERVER_ERROR` | 500 |

## Usage

### Implementing a REST protocol

The pattern for a concrete REST implementation — e.g. over `jakarta.servlet.http.HttpServletRequest` — follows the contract directly:

```java
@Protocol
public class ServletHttpProtocol
        implements IProtocol<HttpServletRequest, HttpServletResponse> {

    @Override
    public IClass<HttpServletRequest> requestType() {
        return IClass.getClass(HttpServletRequest.class);
    }

    @Override
    public ICaller getCaller(HttpServletRequest req) throws ApiException {
        String tenantId = req.getHeader("X-Tenant-Id");
        String callerId = req.getHeader("X-Caller-Id");
        // parse Authorization for JWT-based identity, etc.
        return new Caller(tenantId, tenantId, callerId, null, false, false, List.of());
    }

    @Override
    public byte[] getRawBody(HttpServletRequest req) throws ApiException {
        try {
            return req.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new ApiException("Failed to read body", e);
        }
    }

    @Override
    public String getAuthorization(HttpServletRequest req) throws ApiException {
        return req.getHeader("Authorization");
    }

    @Override
    public String getContentType(HttpServletRequest req) throws ApiException {
        return req.getContentType();
    }

    @Override
    public String getAccept(HttpServletRequest req) throws ApiException {
        return req.getHeader("Accept");
    }

    @Override
    public String getPath(HttpServletRequest req) throws ApiException {
        return req.getRequestURI();
    }

    @Override
    public String getMethod(HttpServletRequest req) throws ApiException {
        return req.getMethod();
    }

    @Override
    public Map<String, String> getQueryParameters(HttpServletRequest req) throws ApiException {
        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        return params;
    }

    @Override
    public HttpServletResponse buildResponse(HttpServletRequest req, Object output,
            int statusCode) throws ApiException {
        // In practice, write to the paired HttpServletResponse obtained
        // from the dispatching layer and return it.
        throw new UnsupportedOperationException("wire to the paired HttpServletResponse");
    }
}
```

### Registering manually

```java
IApi api = ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .protocol(new ServletHttpProtocol())
    .domain(IClass.getClass(User.class))
        .entity().id("id").uuid("uuid").tenantId("tenantId").up()
        .dto(IClass.getClass(UserDto.class)).id("id").uuid("uuid").tenantId("tenantId")
            .db(dao).up()
        .creation(true).readAll(true).readOne(true).update(true).delete(true)
    .up()
    .build();
api.start();
```

### Registering via auto-detection

```java
IApi api = ApiBuilder.builder()
    .superTenantId("SUPER_TENANT")
    .packages("com.example.app")   // package containing @Protocol-annotated class
    .autoDetect(true)
    .domain(...)
    .up()
    .build();
```

Any class in the scanned packages annotated with `@Protocol` and implementing `IProtocol` with a public no-arg constructor is picked up automatically.

### Invoking with a raw request (Mode A)

Once the protocol is registered, dispatch from your HTTP layer to the framework by placing the raw request in the operation request's argument map under the `"rawRequest"` key:

```java
// Typically done inside a route handler or dispatcher
IOperationRequest req = new OperationRequest(new HashMap<>());
req.arg(IOperationRequest.OPERATION,
    OperationDefinition.readOneWithStandardSecurity("users", IClass.getClass(User.class)));
req.arg("rawRequest", httpServletRequest);

IOperationResponse resp = api.getDomain("users").orElseThrow().invoke(req);
Object transportResponse = resp.getResponse(); // the RES returned by buildResponse
```

The pipeline resolves the protocol automatically, extracts caller/body/headers, runs CRUD and security, serializes the output, and calls `buildResponse`. The `IOperationResponse.getResponse()` value is exactly what `buildResponse` returned — typically an HTTP response object ready for the transport layer to flush.

## Tips and best practices

- **Register specific protocols before broad ones** — protocol selection is first-match on `requestType().isInstance(rawRequest)`. If you register a generic base-class protocol alongside a specific subclass protocol, always add the subclass first.
- **Keep `getCaller` fast** — it runs on every request before any business logic. Avoid remote calls or heavy parsing inside it; extract only what the `ICaller` fields need. JWT parsing is fine if it is local; remote token introspection should be cached.
- **Null-safe `Accept` / `Content-Type`** — the framework's serialize stage is a no-op when `getAccept` returns `null`, and the deserialize stage is skipped when `getRawBody` returns `null`. Return `null` (not an empty string) for absent headers so pipeline guard conditions work correctly.
- **`buildResponse` owns the response lifecycle** — for Servlet-based implementations, write the body and set headers on the `HttpServletResponse` side-channel before returning. For framework-internal callers (Mode B), `buildResponse` is never called, so no defensive null-guard is needed there.
- **Test with `FakeHttpProtocol`** — `garganttua-api-core`'s `ProtocolIntegrationTest` (package `com.garganttua.api.core.integ.protocol`) ships an exemplary `FakeHttpProtocol` whose `buildResponse` captures payloads for assertion. Copy this pattern for unit tests of custom protocol implementations.
- **One protocol per transport type** — do not combine Servlet and Javalin `Context` logic in a single `IProtocol`. Keep them in separate classes and register both; the discriminator handles routing transparently.

## License
This module is distributed under the MIT License.
