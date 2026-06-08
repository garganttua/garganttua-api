package com.garganttua.api.core.integ.securityscan;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.IApiBuilder;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticationAuthenticate;
import com.garganttua.api.core.security.SecurityAnnotationScanner;
import com.garganttua.api.core.integ.crud.AbstractCrudIntegrationTest;
import com.garganttua.core.reflection.IClass;

@DisplayName("@Authentication auto-detection registers the class on the API security builder")
class SecurityAnnotationScanTest extends AbstractCrudIntegrationTest {

    // ── Test fixture: a class marked @Authentication with the authenticate hook
    @Authentication
    public static class FixtureLoginPasswordAuthentication {
        @AuthenticationAuthenticate
        public Object authenticate(Object request) {
            return null;
        }
    }

    @Test
    @DisplayName("scan registers @Authentication classes via apiBuilder.security().authentication(class)")
    void scannerRegistersAuthentication() throws ApiException {
        IApiBuilder builder = newBuilder();

        // Before scan: no authentication for this class
        assertFalse(builder.security().isAuthenticationAvailable(IClass.getClass(FixtureLoginPasswordAuthentication.class)).isPresent(),
                "Pre-scan: authentication class should not be registered");

        new SecurityAnnotationScanner(builder, java.util.Set.of("com.garganttua.api.core.integ.securityscan")).scan();

        assertTrue(builder.security().isAuthenticationAvailable(IClass.getClass(FixtureLoginPasswordAuthentication.class)).isPresent(),
                "Post-scan: authentication class should be registered");
    }

    @Test
    @DisplayName("scan is a no-op without packages")
    void scanNoOpWithoutPackages() throws ApiException {
        IApiBuilder builder = newBuilder();
        new SecurityAnnotationScanner(builder, java.util.Set.of()).scan();
        assertFalse(builder.security().isAuthenticationAvailable(IClass.getClass(FixtureLoginPasswordAuthentication.class)).isPresent(),
                "No packages → no scan → no registration");
    }

    @Test
    @DisplayName("scan is idempotent: calling twice does not double-register")
    void scanIsIdempotent() throws ApiException {
        IApiBuilder builder = newBuilder();
        SecurityAnnotationScanner scanner = new SecurityAnnotationScanner(builder, java.util.Set.of("com.garganttua.api.core.integ.securityscan"));
        scanner.scan();
        scanner.scan(); // must not throw "already registered"
        assertTrue(builder.security().isAuthenticationAvailable(IClass.getClass(FixtureLoginPasswordAuthentication.class)).isPresent());
    }
}
