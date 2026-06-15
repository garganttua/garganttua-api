package com.garganttua.api.core.unit.context;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.javatuples.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.entity.EntityUpdater;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.core.reflection.ObjectAddress;

@DisplayName("EntityUpdater Tests")
class EntityUpdaterTest {

    // --- Test POJO ---

    public static class Product {
        private String name;
        private double price;
        private String category;
        private String secret;

        public Product() {}

        public Product(String name, double price, String category, String secret) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.secret = secret;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
    }

    public static class OtherEntity {
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // --- Test Caller ---

    private static ICaller callerWith(List<String> authorities) {
        return callerWith(authorities, false, false);
    }

    private static ICaller callerWith(List<String> authorities, boolean superTenant, boolean superOwner) {
        return new ICaller() {
            @Override public String tenantId() { return "T1"; }
            @Override public String requestedTenantId() { return "T1"; }
            @Override public String ownerId() { return "O1"; }
            @Override public String callerId() { return "C1"; }
            @Override public boolean superTenant() { return superTenant; }
            @Override public boolean superOwner() { return superOwner; }
            @Override public List<String> authorities() { return authorities; }
        };
    }

    private EntityUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new EntityUpdater();
    }

    @Nested
    @DisplayName("Basic updates")
    class BasicUpdates {

        @Test
        @DisplayName("updates authorized fields from updated entity to stored entity")
        void updatesAuthorizedFields() {
            Product stored = new Product("OldName", 10.0, "OldCat", "keep-me");
            Product updated = new Product("NewName", 20.0, "NewCat", "hacked");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), ""),
                    Pair.with(new ObjectAddress("price"), "")
            );

            Object result = updater.update(callerWith(null), stored, updated, authorizations);

            assertSame(stored, result);
            assertEquals("NewName", stored.getName());
            assertEquals(20.0, stored.getPrice());
            assertEquals("OldCat", stored.getCategory(), "Non-authorized field should not change");
            assertEquals("keep-me", stored.getSecret(), "Non-authorized field should not change");
        }

        @Test
        @DisplayName("does not overwrite with null values from updated entity")
        void doesNotOverwriteWithNull() {
            Product stored = new Product("OldName", 10.0, "OldCat", null);
            Product updated = new Product(null, 20.0, null, null);

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), ""),
                    Pair.with(new ObjectAddress("price"), ""),
                    Pair.with(new ObjectAddress("category"), "")
            );

            updater.update(callerWith(null), stored, updated, authorizations);

            assertEquals("OldName", stored.getName(), "Should not be overwritten by null");
            assertEquals(20.0, stored.getPrice());
            assertEquals("OldCat", stored.getCategory(), "Should not be overwritten by null");
        }

        @Test
        @DisplayName("returns stored entity unchanged when authorizations list is empty")
        void returnsStoredWhenEmptyAuthorizations() {
            Product stored = new Product("OldName", 10.0, "OldCat", "secret");
            Product updated = new Product("NewName", 99.0, "NewCat", "hacked");

            Object result = updater.update(callerWith(null), stored, updated, List.of());

            assertSame(stored, result);
            assertEquals("OldName", stored.getName());
        }

        @Test
        @DisplayName("returns stored entity unchanged when authorizations list is null")
        void returnsStoredWhenNullAuthorizations() {
            Product stored = new Product("OldName", 10.0, "OldCat", "secret");
            Product updated = new Product("NewName", 99.0, "NewCat", "hacked");

            Object result = updater.update(callerWith(null), stored, updated, null);

            assertSame(stored, result);
            assertEquals("OldName", stored.getName());
        }
    }

    @Nested
    @DisplayName("Authority checks")
    class AuthorityChecks {

        @Test
        @DisplayName("updates field when caller has the required authority")
        void updatesWhenCallerHasAuthority() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 10.0, "Cat", "hacked");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "ROLE_ADMIN")
            );

            updater.update(callerWith(List.of("ROLE_ADMIN", "ROLE_USER")), stored, updated, authorizations);

            assertEquals("New", stored.getName());
        }

        @Test
        @DisplayName("skips field when caller lacks the required authority")
        void skipsWhenCallerLacksAuthority() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 10.0, "Cat", "hacked");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "ROLE_ADMIN")
            );

            updater.update(callerWith(List.of("ROLE_USER")), stored, updated, authorizations);

            assertEquals("Old", stored.getName(), "Field should not be updated without authority");
        }

        @Test
        @DisplayName("updates field when no authority is required (empty string)")
        void updatesWhenNoAuthorityRequired() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 10.0, "Cat", "secret");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "")
            );

            updater.update(callerWith(List.of()), stored, updated, authorizations);

            assertEquals("New", stored.getName());
        }

        @Test
        @DisplayName("updates field when no authority is required (null)")
        void updatesWhenAuthorityIsNull() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 10.0, "Cat", "secret");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), (String) null)
            );

            updater.update(callerWith(List.of()), stored, updated, authorizations);

            assertEquals("New", stored.getName());
        }

        @Test
        @DisplayName("caller with null authorities is NOT authorized — null is not a bypass")
        void rejectsWhenCallerAuthoritiesNull() {
            // Regression: a caller with no authorities at all (null list) used
            // to bypass every field-level gate — a freshly-built tenant caller
            // has null authorities, which let unprivileged callers update
            // authority-gated fields silently. The new contract: null/empty
            // authorities + required authority => skip the field.
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 10.0, "Cat", "secret");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "ROLE_ADMIN")
            );

            updater.update(callerWith(null), stored, updated, authorizations);

            assertEquals("Old", stored.getName(),
                    "caller has no authorities — the field-level gate must skip the update");
        }

        @Test
        @DisplayName("caller with empty authorities list is NOT authorized")
        void rejectsWhenCallerAuthoritiesEmpty() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 10.0, "Cat", "secret");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "ROLE_ADMIN")
            );

            updater.update(callerWith(List.of()), stored, updated, authorizations);

            assertEquals("Old", stored.getName(),
                    "empty authorities + required authority => no bypass");
        }

        @Test
        @DisplayName("super-tenant caller does NOT bypass field-level gates — it must carry the authority")
        void superTenantDoesNotBypassGate() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 99.0, "NewCat", "hacked");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "ROLE_ADMIN"),
                    Pair.with(new ObjectAddress("price"), "ROLE_PRICING"),
                    Pair.with(new ObjectAddress("category"), "ROLE_CATEGORY_EDITOR")
            );

            // Super-tenant WITHOUT the authorities: being super grants cross-tenant
            // reach, not the authority to mutate guarded fields — all are skipped.
            updater.update(callerWith(null, /*superTenant*/ true, /*superOwner*/ false),
                    stored, updated, authorizations);

            assertEquals("Old", stored.getName(), "guarded field must NOT change for a super-tenant lacking the authority");
            assertEquals(10.0, stored.getPrice());
            assertEquals("Cat", stored.getCategory());

            // The same super-tenant, now carrying the authorities, may mutate them.
            updater.update(callerWith(List.of("ROLE_ADMIN", "ROLE_PRICING", "ROLE_CATEGORY_EDITOR"),
                    /*superTenant*/ true, /*superOwner*/ false), stored, updated, authorizations);
            assertEquals("New", stored.getName());
            assertEquals(99.0, stored.getPrice());
            assertEquals("NewCat", stored.getCategory());
        }

        @Test
        @DisplayName("super-owner caller does NOT bypass field-level gates either")
        void superOwnerDoesNotBypassGate() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 99.0, "NewCat", "hacked");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "ROLE_ADMIN")
            );

            updater.update(callerWith(null, /*superTenant*/ false, /*superOwner*/ true),
                    stored, updated, authorizations);

            assertEquals("Old", stored.getName(),
                    "a super-owner caller must NOT bypass the field-level authority gate");
        }

        @Test
        @DisplayName("mixed authorities: updates only authorized fields")
        void mixedAuthorities() {
            Product stored = new Product("Old", 10.0, "OldCat", "secret");
            Product updated = new Product("New", 99.0, "NewCat", "hacked");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), ""),
                    Pair.with(new ObjectAddress("price"), "ROLE_ADMIN"),
                    Pair.with(new ObjectAddress("category"), "ROLE_MANAGER")
            );

            updater.update(callerWith(List.of("ROLE_ADMIN")), stored, updated, authorizations);

            assertEquals("New", stored.getName(), "No authority required — updated");
            assertEquals(99.0, stored.getPrice(), "Caller has ROLE_ADMIN — updated");
            assertEquals("OldCat", stored.getCategory(), "Caller lacks ROLE_MANAGER — skipped");
            assertEquals("secret", stored.getSecret(), "Not in authorizations — untouched");
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("throws ApiException when caller is null")
        void throwsWhenCallerNull() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            Product updated = new Product("New", 10.0, "Cat", "secret");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "")
            );

            ApiException ex = assertThrows(ApiException.class,
                    () -> updater.update(null, stored, updated, authorizations));
            assertTrue(ex.getMessage().contains("Caller is null"));
        }

        @Test
        @DisplayName("throws ApiException when entity types mismatch")
        void throwsWhenTypeMismatch() {
            Product stored = new Product("Old", 10.0, "Cat", "secret");
            OtherEntity updated = new OtherEntity();
            updated.setName("New");

            List<Pair<ObjectAddress, String>> authorizations = List.of(
                    Pair.with(new ObjectAddress("name"), "")
            );

            ApiException ex = assertThrows(ApiException.class,
                    () -> updater.update(callerWith(null), stored, updated, authorizations));
            assertTrue(ex.getMessage().contains("mismatch"));
        }
    }
}
