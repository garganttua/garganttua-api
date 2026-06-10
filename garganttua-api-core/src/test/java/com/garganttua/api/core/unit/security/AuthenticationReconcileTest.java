package com.garganttua.api.core.unit.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.security.authentication.Authentication;
import com.garganttua.api.core.caller.Caller;

/**
 * The default {@link com.garganttua.api.commons.security.authentication.IAuthentication#reconcile}
 * implements the R1-R3 rules: the verified token is authoritative for the caller's identity,
 * a header that contradicts it is a cross-target (super only) or a rejection.
 */
@DisplayName("IAuthentication.reconcile — caller resolution from the verified token (R1-R3)")
class AuthenticationReconcileTest {

    /** A verified authentication for a principal in tenant `tenantId`, owner `ownerId`, with the given super flags. */
    private static Authentication auth(String tenantId, String ownerId, boolean superTenant, boolean superOwner) {
        return new Authentication(true, "principal", null, "the-token", List.of("ROLE_USER"),
                tenantId, ownerId, superTenant, superOwner, true, true, true, true);
    }

    /** A protocol-layer caller carrying header tenant/owner (super flags always false from the wire). */
    private static ICaller protocol(String headerTenant, String headerOwner) {
        return new Caller(headerTenant, headerTenant, "caller-1", headerOwner, false, false, List.of());
    }

    @Test
    @DisplayName("no header → operate on the token's own tenant/owner; super flags & authorities come from the token")
    void noHeaderUsesTokenIdentity() {
        ICaller c = auth("acme", "users:alice", false, false).reconcile(protocol(null, null));

        assertEquals("acme", c.tenantId());
        assertEquals("acme", c.requestedTenantId(), "non-super → scoped to the token's tenant");
        assertEquals("users:alice", c.ownerId());
        assertEquals("users:alice", c.requestedOwnerId());
        assertFalse(c.superTenant());
        assertFalse(c.superOwner());
        assertEquals(List.of("ROLE_USER"), c.authorities(), "authorities are the token's, not the header's");
    }

    @Test
    @DisplayName("super principal with no header → requested tenant/owner are null (sees all)")
    void superNoHeaderSeesAll() {
        ICaller c = auth("ROOT", "users:root", true, true).reconcile(protocol(null, null));

        assertEquals("ROOT", c.tenantId());
        assertNull(c.requestedTenantId(), "super + no target → no tenant restriction");
        assertNull(c.requestedOwnerId(), "super owner + no target → no owner restriction");
        assertTrue(c.superTenant());
        assertTrue(c.superOwner());
    }

    @Test
    @DisplayName("header equal to the token's tenant → normal (no cross-target)")
    void matchingHeaderIsNormal() {
        ICaller c = auth("acme", "users:alice", false, false).reconcile(protocol("acme", "users:alice"));
        assertEquals("acme", c.tenantId());
        assertEquals("acme", c.requestedTenantId());
        assertEquals("users:alice", c.requestedOwnerId());
    }

    @Test
    @DisplayName("super tenant + a DIFFERENT header tenant → cross-target (home=token, requested=header)")
    void superCrossTenant() {
        ICaller c = auth("ROOT", "users:root", true, false).reconcile(protocol("acme", null));

        assertEquals("ROOT", c.tenantId(), "home tenant stays the token's");
        assertEquals("acme", c.requestedTenantId(), "the targeted tenant is the header's");
        assertTrue(c.superTenant());
    }

    @Test
    @DisplayName("super owner + a DIFFERENT header owner → cross-owner (home=token, requested=header)")
    void superCrossOwner() {
        ICaller c = auth("acme", "users:root", false, true).reconcile(protocol("acme", "users:bob"));

        assertEquals("users:root", c.ownerId(), "home owner stays the token's");
        assertEquals("users:bob", c.requestedOwnerId(), "the targeted owner is the header's");
        assertTrue(c.superOwner());
    }

    @Test
    @DisplayName("non-super + a DIFFERENT header tenant → REJECTED")
    void nonSuperTenantMismatchRejected() {
        Authentication a = auth("acme", "users:alice", false, false);
        ApiException ex = assertThrows(ApiException.class, () -> a.reconcile(protocol("other", null)));
        assertTrue(ex.getMessage().contains("tenant") && ex.getMessage().contains("other"),
                "rejection must name the dimension and the refused target; got: " + ex.getMessage());
    }

    @Test
    @DisplayName("non-super owner + a DIFFERENT header owner → REJECTED")
    void nonSuperOwnerMismatchRejected() {
        Authentication a = auth("acme", "users:alice", false, false);
        assertThrows(ApiException.class, () -> a.reconcile(protocol("acme", "users:bob")));
    }
}
