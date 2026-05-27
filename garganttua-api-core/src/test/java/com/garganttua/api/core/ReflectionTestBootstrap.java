package com.garganttua.api.core;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 auto-detected extension that installs an {@code IReflection} on the
 * global {@code IClass} holder once per Surefire JVM, by triggering
 * garganttua-core's {@code Bootstrap.builder()} cold-start (core commit
 * {@code c19c7d66}). The cold-start runs a {@code ServiceLoader} over any
 * {@code IReflectionProvider} / {@code IAnnotationScanner} found on the test
 * classpath (here: {@code garganttua-runtime-reflection} +
 * {@code garganttua-reflections}) and installs the resulting reflection
 * globally.
 *
 * <p>Without this hook, tests that depend on {@code IClass.getClass(...)}
 * during static initialization (e.g. {@code ExpressionUtils}, the
 * {@code @Bootstrap}-annotated builders) fail with
 * {@code "No IReflection available. Call IClass.setReflection()"} the first
 * time they are loaded — unless another test happened to install reflection
 * earlier in the JVM. The pre-3.0 setup relied on this happy accident via
 * {@code ApiBuilder.builder()} instantiating its own Bootstrap; with that
 * auto-bootstrap removed (intentional, per the "let the user choose"
 * principle), we surface the test-side bootstrap requirement explicitly.
 *
 * <p>Registered via
 * {@code META-INF/services/org.junit.jupiter.api.extension.Extension} +
 * {@code junit.jupiter.extensions.autodetection.enabled=true} in
 * {@code src/test/resources/junit-platform.properties}.
 */
public final class ReflectionTestBootstrap implements BeforeAllCallback {

    private static volatile boolean installed = false;

    @Override
    public void beforeAll(ExtensionContext context) {
        if (installed) {
            return;
        }
        synchronized (ReflectionTestBootstrap.class) {
            if (installed) {
                return;
            }
            // Bootstrap.builder() triggers garganttua-core's ServiceLoader cold-start,
            // which installs IReflection on the global holder. We never use the
            // returned Bootstrap — its sole purpose here is the side effect.
            com.garganttua.core.bootstrap.dsl.Bootstrap.builder();
            installed = true;
        }
    }
}
