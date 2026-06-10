package com.garganttua.api.core.integ.authorities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IAuthoritiesEndpoint;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.operation.Access;
import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

@DisplayName("Authorities endpoint — opt-in DSL + IApi method + caller-level security check")
class AuthoritiesEndpointIntegrationTest extends AbstractCrudIntegrationTest {

    /**
     * Two minimal domains used to populate the authorities pool. Both opt
     * into authority enforcement on a subset of their CRUD operations so the
     * enumeration has something to return; authority names follow the
     * default {@code <domain>:<operation>} pattern (no explicit
     * {@code .creationAuthority("custom-name")}).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private IApi buildApi(boolean expose, Access access, String authority) throws ApiException {
        IApiBuilder builder = newBuilder();

        var users = builder.domain(IClass.getClass(User.class))
                .tenant(true)
                .superTenant("superTenant")
                .owner("uuid")
                .superOwner("superOwner")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                    // Field-level update authority — guards mutation of 'name'
                    // independent of the operation-level update authority.
                    // Must surface in getAuthorities() alongside the
                    // operation-level names.
                    .update("name", "user-update-name")
                .up()
                .dto(IClass.getClass(UserDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up();
        users.security()
                .creationAuthority(true)
                .readAllAuthority(true)
                .readOneAuthority(true);
        users.up();

        // Second domain so we can assert that the result is deduped across
        // domains and sorted alphabetically.
        var projects = builder.domain(IClass.getClass(Project.class))
                .tenant(true)
                .superTenant("superTenant")
                .entity()
                    .id("id").uuid("uuid").tenantId("tenantId")
                .up()
                .dto(IClass.getClass(ProjectDto.class))
                    .id("id").uuid("uuid").tenantId("tenantId")
                    .db(new CapturingDao())
                .up();
        projects.security()
                .creationAuthority(true)
                .readAllAuthority(true);
        projects.up();

        if (expose) {
            var ep = builder.exposeAuthorities();
            if (access != null) ep.access(access);
            if (authority != null) ep.authority(authority);
            ep.up();
        }
        return buildAndStart(builder);
    }

    public static class Project {
        private String id;
        private String uuid;
        private String tenantId;
        private Boolean superTenant = false;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    public static class ProjectDto {
        private String id;
        private String uuid;
        private String tenantId;
        private Boolean superTenant;
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }
        public Boolean getSuperTenant() { return superTenant; }
        public void setSuperTenant(Boolean superTenant) { this.superTenant = superTenant; }
    }

    @Nested
    @DisplayName("getAuthorities() — pure enumeration, ignores the endpoint exposure flag")
    class Enumeration {

        @Test
        @DisplayName("returns a sorted, deduplicated list of effectiveAuthorityName across all domains")
        void enumerates() throws ApiException {
            IApi api = buildApi(false, null, null);
            List<String> names = api.getAuthorities();
            assertNotNull(names);
            assertFalse(names.isEmpty(),
                    "default CRUD operations must each contribute an effectiveAuthorityName");

            // Sorted alphabetically — assert the result is monotonic.
            for (int i = 1; i < names.size(); i++) {
                assertTrue(names.get(i - 1).compareTo(names.get(i)) < 0,
                        "list must be sorted, deduped — found violation at index " + i
                                + ": '" + names.get(i - 1) + "' vs '" + names.get(i) + "'");
            }

            // The default authority name pattern is "<technicalOperation>-<scope>-<entity>",
            // where <entity> is the singular form for oneEntity scope and plural for
            // allEntities scope. Both declared domains must contribute at least
            // one authority — we assert by suffix on the entity name (which lives
            // in the trailing segment of the name).
            assertTrue(names.stream().anyMatch(n -> n.endsWith("-user")),
                    "users domain must contribute at least one one-entity authority (e.g. create-one-user) — got: "
                            + names);
            assertTrue(names.stream().anyMatch(n -> n.endsWith("-users")),
                    "users domain must contribute at least one all-entities authority (e.g. read-all-users) — got: "
                            + names);
            assertTrue(names.stream().anyMatch(n -> n.endsWith("-project")),
                    "projects domain must contribute at least one one-entity authority (e.g. create-one-project) — got: "
                            + names);
            assertTrue(names.stream().anyMatch(n -> n.endsWith("-projects")),
                    "projects domain must contribute at least one all-entities authority (e.g. read-all-projects) — got: "
                            + names);

            // Also pin the exact wiring: each .creationAuthority(true) on the
            // setup must surface as create-one-<entity-singular>, each
            // .readAllAuthority(true) as read-all-<entity-plural>. This is the
            // contract callers will pattern-match against.
            assertTrue(names.contains("create-one-user"),
                    "users.creationAuthority(true) must produce 'create-one-user' — got: " + names);
            assertTrue(names.contains("create-one-project"),
                    "projects.creationAuthority(true) must produce 'create-one-project' — got: " + names);
            assertTrue(names.contains("read-all-users"),
                    "users.readAllAuthority(true) must produce 'read-all-users' — got: " + names);
            assertTrue(names.contains("read-all-projects"),
                    "projects.readAllAuthority(true) must produce 'read-all-projects' — got: " + names);

            // Field-level update authority (entity().update("name",
            // "user-update-name")) must also surface — distinct from
            // operation-level names, stored on EntityDefinition.updates().
            assertTrue(names.contains("user-update-name"),
                    "field-level .update(\"name\", \"user-update-name\") must surface in getAuthorities() — got: "
                            + names);
        }

        @Test
        @DisplayName("a field-level update authority alone (no operation-level authority on the domain) still surfaces")
        void fieldLevelAuthorityAloneSurfaces() throws ApiException {
            // Build a minimal API with NO operation-level authority anywhere —
            // only a field-level update authority. Proves getAuthorities()
            // does not depend on operation-level configuration to find
            // field-level entries.
            IApiBuilder builder = newBuilder();
            var users = builder.domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .update("email", "user-update-email-only")
                    .up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(new CapturingDao())
                    .up();
            users.up();
            IApi api = buildAndStart(builder);

            List<String> names = api.getAuthorities();
            assertTrue(names.contains("user-update-email-only"),
                    "field-level update authority must surface even when no operation-level "
                            + "authority is configured on the domain — got: " + names);
            // And no spurious operation-level names leaked in.
            assertFalse(names.stream().anyMatch(n -> n.startsWith("create-")
                            || n.startsWith("read-")
                            || n.startsWith("delete-")
                            || n.startsWith("update-")),
                    "no operation-level authority was configured — none must appear — got: " + names);
        }

        @Test
        @DisplayName("a field declared updatable WITHOUT an authority does NOT contribute to the list")
        void fieldWithoutAuthorityIsNotListed() throws ApiException {
            // Same setup but using the non-authority overload: update(field)
            // without the second arg. The pair has a null right-hand side —
            // it must be filtered out by getAuthorities().
            IApiBuilder builder = newBuilder();
            var users = builder.domain(IClass.getClass(User.class))
                    .tenant(true)
                    .superTenant("superTenant")
                    .entity()
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .update("email") // no authority — just marked updatable
                    .up()
                    .dto(IClass.getClass(UserDto.class))
                        .id("id").uuid("uuid").tenantId("tenantId")
                        .db(new CapturingDao())
                    .up();
            users.up();
            IApi api = buildAndStart(builder);

            List<String> names = api.getAuthorities();
            assertTrue(names.isEmpty(),
                    "no authority anywhere → list must be empty (updatable-without-authority is not an authority) — got: "
                            + names);
        }

        @Test
        @DisplayName("the same name is never returned twice (dedup is real, not just a sorted concat)")
        void dedup() throws ApiException {
            IApi api = buildApi(false, null, null);
            List<String> names = api.getAuthorities();
            assertEquals(names.size(), names.stream().distinct().count(),
                    "duplicates leaked into the result: " + names);
        }
    }

    @Nested
    @DisplayName("getAuthoritiesEndpoint() — descriptor visibility for transport modules")
    class Descriptor {

        @Test
        @DisplayName("returns null when .exposeAuthorities() was not called")
        void notExposedYieldsNullDescriptor() throws ApiException {
            IApi api = buildApi(false, null, null);
            assertNull(api.getAuthoritiesEndpoint(),
                    "endpoint must report null when not opted in — transports skip the route on null");
        }

        @Test
        @DisplayName("returns a descriptor with the chosen access and authority when opted in")
        void exposedYieldsDescriptor() throws ApiException {
            IApi api = buildApi(true, Access.authenticated, "ops:authorities:read");
            IAuthoritiesEndpoint endpoint = api.getAuthoritiesEndpoint();
            assertNotNull(endpoint);
            assertEquals(Access.authenticated, endpoint.access());
            assertEquals("ops:authorities:read", endpoint.authority());
        }

        @Test
        @DisplayName(".exposeAuthorities().up() with no setter yields access=authenticated and no authority")
        void defaultsAreAuthenticatedAndNoAuthority() throws ApiException {
            IApi api = buildApi(true, null, null);
            IAuthoritiesEndpoint endpoint = api.getAuthoritiesEndpoint();
            assertNotNull(endpoint);
            assertEquals(Access.authenticated, endpoint.access(),
                    "default access must be 'authenticated' (intentionally not anonymous — "
                            + "exposing the authority matrix to the public is the unsafe choice)");
            assertNull(endpoint.authority(),
                    "default authority gate must be null — no specific authority required");
        }
    }

    @Nested
    @DisplayName("getAuthoritiesForCaller — security enforcement")
    class SecurityCheck {

        @Test
        @DisplayName("endpoint not exposed → throws regardless of caller (super-callers included)")
        void notExposedAlwaysThrows() throws ApiException {
            IApi api = buildApi(false, null, null);
            ApiException ex = assertThrows(ApiException.class,
                    () -> api.getAuthoritiesForCaller(Caller.createSuperCaller("SUPER_TENANT")));
            assertTrue(ex.getMessage().contains("not exposed"),
                    "error must say the endpoint is not exposed — got: " + ex.getMessage());
        }

        @Test
        @DisplayName("access=anonymous → list returned for any caller, even null")
        void anonymousNeedsNoCaller() throws ApiException {
            IApi api = buildApi(true, Access.anonymous, null);
            List<String> names = api.getAuthoritiesForCaller(null);
            assertNotNull(names);
            assertFalse(names.isEmpty());

            // Same result for an anonymous caller record.
            List<String> sameAgain = api.getAuthoritiesForCaller(Caller.createAnonymousCaller());
            assertEquals(names, sameAgain);
        }

        @Test
        @DisplayName("access=authenticated + anonymous caller → throws with parlant message")
        void authenticatedRejectsAnonymous() throws ApiException {
            IApi api = buildApi(true, Access.authenticated, null);
            ApiException ex = assertThrows(ApiException.class,
                    () -> api.getAuthoritiesForCaller(Caller.createAnonymousCaller()));
            assertTrue(ex.getMessage().contains("authenticated")
                            || ex.getMessage().contains("tenantId"),
                    "error must explain why the anonymous caller was rejected — got: " + ex.getMessage());
        }

        @Test
        @DisplayName("access=authenticated + tenant-scoped caller → list returned")
        void authenticatedAcceptsTenantCaller() throws ApiException {
            IApi api = buildApi(true, Access.authenticated, null);
            List<String> names = api.getAuthoritiesForCaller(Caller.createTenantCaller("ACME"));
            assertNotNull(names);
            assertFalse(names.isEmpty());
        }

        // Removed: access=tenant / access=owner gates no longer exist (token-authoritative
        // redesign — only anonymous / authenticated remain). The authenticated case is
        // already covered by "access=authenticated + anonymous caller → throws".

        @Test
        @DisplayName("authority gate + caller with the right authority → list returned")
        void authorityGatePasses() throws ApiException {
            IApi api = buildApi(true, Access.authenticated, "ops:authorities:read");
            Caller caller = (Caller) Caller.createTenantCaller("ACME");
            caller = caller.withAuthorities(List.of("ops:authorities:read", "noise"));
            List<String> names = api.getAuthoritiesForCaller(caller);
            assertNotNull(names);
            assertFalse(names.isEmpty());
        }

        @Test
        @DisplayName("authority gate + caller without the required authority → throws")
        void authorityGateBlocks() throws ApiException {
            IApi api = buildApi(true, Access.authenticated, "ops:authorities:read");
            Caller caller = (Caller) Caller.createTenantCaller("ACME");
            caller = caller.withAuthorities(List.of("unrelated:authority"));
            final Caller finalCaller = caller;
            ApiException ex = assertThrows(ApiException.class,
                    () -> api.getAuthoritiesForCaller(finalCaller));
            assertTrue(ex.getMessage().contains("ops:authorities:read"),
                    "error must name the required authority — got: " + ex.getMessage());
        }

        @Test
        @DisplayName("authority gate + caller with NO authorities at all → throws")
        void authorityGateBlocksNullAuthorities() throws ApiException {
            IApi api = buildApi(true, Access.authenticated, "ops:authorities:read");
            Caller caller = (Caller) Caller.createTenantCaller("ACME");
            // No .withAuthorities — authorities() returns null
            assertNull(caller.authorities(), "test prerequisite: caller starts with no authorities");
            final Caller finalCaller = caller;
            ApiException ex = assertThrows(ApiException.class,
                    () -> api.getAuthoritiesForCaller(finalCaller));
            assertTrue(ex.getMessage().contains("ops:authorities:read"));
        }

        @Test
        @DisplayName("super-tenant bypasses the authority gate but still must meet the access level")
        void superTenantBypassesAuthorityGate() throws ApiException {
            IApi api = buildApi(true, Access.authenticated, "ops:authorities:read");
            // Super-tenant has tenantId set internally — meets 'authenticated' AND bypasses authority.
            List<String> names = api.getAuthoritiesForCaller(
                    Caller.createSuperCaller("SUPER_TENANT"));
            assertNotNull(names);
            assertFalse(names.isEmpty());
        }
    }

    @Nested
    @DisplayName("DSL guards")
    class DslGuards {

        @Test
        @DisplayName(".access(null) is rejected with a parlant message")
        void rejectsNullAccess() throws ApiException {
            IApiBuilder builder = newBuilder();
            // The reject happens at the setter call — no .build() needed.
            assertThrows(NullPointerException.class,
                    () -> builder.exposeAuthorities().access(null));
        }

        @Test
        @DisplayName(".authority(blank) is rejected with a parlant message")
        void rejectsBlankAuthority() throws ApiException {
            IApiBuilder builder = newBuilder();
            ApiException ex = assertThrows(ApiException.class,
                    () -> builder.exposeAuthorities().authority(" "));
            assertTrue(ex.getMessage().contains("null or blank"),
                    "error must explain the blank rejection — got: " + ex.getMessage());
        }

        @Test
        @DisplayName(".authority(null) is rejected")
        void rejectsNullAuthority() throws ApiException {
            IApiBuilder builder = newBuilder();
            assertThrows(ApiException.class,
                    () -> builder.exposeAuthorities().authority(null));
        }

        @Test
        @DisplayName("calling .exposeAuthorities() twice returns the same builder instance (idempotent)")
        void exposeIsIdempotent() throws ApiException {
            IApiBuilder builder = newBuilder();
            var first = builder.exposeAuthorities();
            var second = builder.exposeAuthorities();
            assertEquals(first, second,
                    "second .exposeAuthorities() must reuse the first builder so .access/.authority "
                            + "configured earlier are not lost when the user re-enters the sub-builder");
        }
    }
}
