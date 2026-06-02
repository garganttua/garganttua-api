# Fluent Request Builder

A fluent API for building and executing CRUD requests against any domain, with `.caller()`, `.filter()`, `.page()`, `.sort()` chaining and one-step or two-step execution.

The framework provides a fluent API for building and executing requests, available on both `IDomainContext` and `IApiContext`.

### CRUD Shortcuts

```java
IDomainContext<?> products = context.getDomainContext("products").orElseThrow();

// Create
products.request()
    .createOne(myProduct)
    .caller(caller)
    .execute();

// Read
products.request()
    .readOne("uuid-123")
    .caller(caller)
    .execute();

products.request()
    .readAll()
    .filter(myFilter).page(pageable).sort(sort)
    .caller(caller)
    .execute();

// Update
products.request()
    .updateOne("uuid-123", updatedProduct)
    .caller(caller)
    .execute();

// Delete
products.request()
    .deleteOne("uuid-123")
    .caller(caller)
    .execute();

products.request()
    .deleteAll()
    .caller(caller)
    .execute();
```

### Shortcut from IApiContext

```java
context.request("products")
    .createOne(myProduct)
    .caller(caller)
    .execute();
```

### Two-Step Build

```java
IRequest request = products.request()
    .createOne(myProduct)
    .caller(caller)
    .build();

// Inspect before executing
IOperationRequest opRequest = request.operationRequest();

// Execute later
IOperationResponse response = request.execute();
```
