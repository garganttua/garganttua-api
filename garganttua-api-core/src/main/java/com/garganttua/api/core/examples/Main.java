package com.garganttua.api.core.examples;

import com.garganttua.api.core.builder.ApiContextBuilder;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.context.dsl.IApiContextBuilder;
import com.garganttua.core.dsl.DslException;

import java.util.Optional;

/**
 * Main class demonstrating how to bootstrap the garganttua-api framework.
 *
 * The framework uses a DSL Builder pattern to configure the API context:
 * - Define domains (entities) with their operations
 * - Configure security (authentication, authorization)
 * - Set up multi-tenancy parameters
 * - Register startup handlers
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Garganttua API Framework Bootstrap ===");
        System.out.println();

        try {
            // Create the API context builder
            IApiContextBuilder builder = ApiContextBuilder.builder();

            // Configure super-tenant for multi-tenancy
            builder.superTenantId("SUPER_TENANT");
            builder.superTenantAutoCreate(true);

            System.out.println("[INFO] Configured super-tenant: SUPER_TENANT");

            // Register the User domain with DSL builder pattern
            builder.domain(User.class)
                // Configure entity fields
                .entity()
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                .up()
                // Configure DTO with DAO
                .dto(UserDto.class)
                    .id("id")
                    .uuid("uuid")
                    .tenantId("tenantId")
                    .db(new InMemoryDao())
                .up()
                // Configure CRUD operations
                .creation(true)
                .readAll(true)
                .readOne(true)
                .update(true)
                .deleteOne(true)
                .deleteAll(false)
            .up();

            System.out.println("[INFO] Registered domain: users (User.class)");

            // Access the injection context for custom beans
            System.out.println("[INFO] Injection context available: " + (builder.injection() != null));

            // Build the API context
            System.out.println("[INFO] Building API context...");

            IApiContext context = builder.build();
            System.out.println("[SUCCESS] API Context built successfully!");

            // Access domain contexts
            Optional<IDomainContext<?>> domainCtx = context.getDomainContext("users");
            if (domainCtx.isPresent()) {
                System.out.println("[INFO] Domain found: " + domainCtx.get().getDomain());
                System.out.println("[INFO] Entity class: " + domainCtx.get().getEntityClass().getName());
            } else {
                System.out.println("[WARN] Domain 'users' not found in context");
            }

            // Display framework capabilities
            displayFrameworkCapabilities();

        } catch (DslException e) {
            System.err.println("[ERROR] DSL configuration error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println();
        System.out.println("=== Bootstrap Complete ===");
    }

    private static void displayFrameworkCapabilities() {
        System.out.println();
        System.out.println("=== Garganttua API Framework Capabilities ===");
        System.out.println();
        System.out.println("1. ENTITY MANAGEMENT");
        System.out.println("   - @Entity: Define API domains with CRUD operations");
        System.out.println("   - @EntityId, @EntityUuid: Identity management");
        System.out.println("   - @EntityTenantId, @EntityOwnerId: Multi-tenancy support");
        System.out.println("   - @EntityHiddenable, @EntityShared: Visibility control");
        System.out.println();
        System.out.println("2. MULTI-TENANCY");
        System.out.println("   - Tenant isolation via tenantId/ownerId");
        System.out.println("   - Super-tenant for cross-tenant access");
        System.out.println("   - Magic-owner for special access patterns");
        System.out.println();
        System.out.println("3. SECURITY");
        System.out.println("   - Pluggable authentication (password, PIN, challenge)");
        System.out.println("   - Pluggable authorization (JWT)");
        System.out.println("   - Per-operation access rules");
        System.out.println("   - @Authentication, @Authorization annotations");
        System.out.println();
        System.out.println("4. DSL BUILDER PATTERN");
        System.out.println("   - Fluent API for context configuration");
        System.out.println("   - Domain -> Entity -> DTO hierarchy");
        System.out.println("   - Security builder for auth configuration");
        System.out.println("   - Startup handlers for initialization");
        System.out.println();
        System.out.println("5. SPRING BOOT INTEGRATION (separate modules)");
        System.out.println("   - REST controllers auto-generation");
        System.out.println("   - Spring Security adapter");
        System.out.println("   - MongoDB DAO support");
        System.out.println("   - Swagger/OpenAPI documentation");
        System.out.println();
    }
}
