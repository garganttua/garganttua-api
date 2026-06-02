# Garganttua API Interface

## Description

Garganttua API Interface is the **transport-layer aggregator** of the garganttua-api framework. Its responsibility is to group the modules that bind the domain model — built with the garganttua-api core engine — to concrete network protocols. Each sub-module translates the protocol-agnostic `IDomain.invoke(IServiceRequest)` contract into the wire format, routing, and HTTP/transport lifecycle of a given protocol.

The aggregator itself contains no code (packaging `pom`); it exists solely to collect sub-modules under a common parent and to enforce consistent versioning across them.

> ⚠️ **This module is currently commented out of the root reactor and is dormant.** It is not built or published as part of the standard `3.0.0-ALPHA01` release cycle. The blocks below document its intended coordinates and structure once it is reactivated.

**Key Features:**
- **Protocol isolation** — each sub-module handles exactly one transport, keeping protocol concerns out of the domain engine
- **Pluggable bindings** — integrators include only the protocol sub-modules they need; unused transports carry zero classpath weight
- **Uniform entry point** — every sub-module adapts to the same `IDomain` service contract, so swapping or combining transports requires no domain-layer changes
- **REST binding included** — `garganttua-api-interface-rest` maps the four CRUD operations to standard HTTP verbs and JSON payloads
- **Extension point for future protocols** — the aggregator structure anticipates additional sub-modules (gRPC, GraphQL, WebSocket, …) without touching the core engine

## Installation

<!-- AUTO-GENERATED-START -->
### Installation with Maven
```xml
<dependency>
    <groupId>com.garganttua</groupId>
    <artifactId>garganttua-api-interface</artifactId>
    <version>3.0.0-ALPHA01</version>
</dependency>
```

### Actual version
3.0.0-ALPHA01

### Dependencies
_None beyond the reactor parent._
<!-- AUTO-GENERATED-END -->

> ⚠️ This module is currently commented out of the root reactor (dormant) and is not published. The block above documents its intended coordinates once reactivated.

## Core Concepts

### Protocol-agnostic Domain Contract

Domains built with `garganttua-api-core` expose all operations through a single `IDomain.invoke(IServiceRequest)` method. Interface sub-modules receive a raw transport request (HTTP, gRPC, …), translate it into an `IServiceRequest`, call `invoke`, and serialize the `IServiceResponse` back to the wire. The domain layer has no knowledge of the transport.

### Interface Sub-module Responsibilities

Each sub-module in this aggregator is expected to handle:

1. **Endpoint registration** — declaring routes or listeners for the target protocol
2. **Request deserialization** — converting the protocol message into `IServiceRequest` (operation, caller context, body, pagination, filters)
3. **Authorization header propagation** — extracting tokens or credentials and passing them into the request context
4. **Response serialization** — converting `IServiceResponse` to the protocol's native response format (status codes, headers, body)
5. **Error mapping** — translating `ApiException` and framework error codes to idiomatic protocol errors

### Versioning

All sub-modules inherit the version from this aggregator, which in turn inherits it from the root `garganttua-api` parent. A single `./new-patch.sh` (or `new-minor.sh` / `new-major.sh`) at the project root bumps all interface sub-modules in one pass.

## Submodules

| Module | Description | README |
|--------|-------------|--------|
| `garganttua-api-interface-rest` | REST binding — maps domain CRUD operations to HTTP/REST endpoints over JSON | [README](./garganttua-api-interface-rest/README.md) |

## Tips and best practices

- **Depend on individual sub-modules, not the aggregator.** The aggregator POM has `packaging=pom` and does not produce a jar. Declare a dependency on `garganttua-api-interface-rest` (or whichever transport you need) directly.
- **One sub-module per protocol.** Resist the temptation to add HTTP-specific logic to a generic helper. Transport-specific serialization, routing, and error handling belong exclusively in the matching sub-module.
- **Reuse `garganttua-api-commons` for shared DTOs.** If a concept (e.g. pagination metadata, error envelope) must be shared across transports, define it in `garganttua-api-commons`, not inside an interface sub-module.
- **Await reactivation before integrating.** Until this module is re-enabled in the root reactor and published to GitHub Packages, depend on the `garganttua-api-spring` stack for REST exposure instead.

## License
This module is distributed under the MIT License.
