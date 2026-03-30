package com.garganttua.api.core.unit.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.AuthenticationBuilder;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.spec.definition.IAuthenticationDefinition;
import com.garganttua.api.spec.security.context.IAuthenticationContext;
import com.garganttua.core.dsl.DslException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.dsl.ReflectionBuilder;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.core.reflections.ReflectionsAnnotationScanner;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

@DisplayName("AuthenticationBuilder Tests")
class AuthenticationBuilderTest {

    private IApiSecurityBuilder parentLink;
    @SuppressWarnings("rawtypes")
    private ISupplierBuilder supplierBuilder;
    private AuthenticationBuilder builder;

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {}

    public static class TestAuthentication {
        public void authenticate() {}
        public void applySecurityOnEntity() {}
        public void myUseCase() {}
    }

    @BeforeEach
    void setUp() {
        ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build();

        parentLink = mock(IApiSecurityBuilder.class);
        supplierBuilder = mock(ISupplierBuilder.class);
        builder = new AuthenticationBuilder(parentLink, supplierBuilder);
    }

    @Nested
    @DisplayName("Fluent API chaining")
    class FluentApi {

        @Test
        @DisplayName("authenticate(String) returns same builder")
        void authenticateStringReturnsSelf() throws ApiException {
            assertSame(builder, builder.authenticate("authenticate"));
        }

        @Test
        @DisplayName("authenticate(Method) returns same builder")
        void authenticateMethodReturnsSelf() throws Exception {
            Method m = TestAuthentication.class.getMethod("authenticate");
            assertSame(builder, builder.authenticate(m));
        }

        @Test
        @DisplayName("authenticate(ObjectAddress) returns same builder")
        void authenticateObjectAddressReturnsSelf() throws ApiException {
            ObjectAddress addr = new ObjectAddress("authenticate");
            assertSame(builder, builder.authenticate(addr));
        }

        @Test
        @DisplayName("applySecurityOnEntity(String) returns same builder")
        void applySecurityStringReturnsSelf() throws ApiException {
            assertSame(builder, builder.applySecurityOnEntity("applySecurityOnEntity"));
        }

        @Test
        @DisplayName("applySecurityOnEntity(Method) returns same builder")
        void applySecurityMethodReturnsSelf() throws Exception {
            Method m = TestAuthentication.class.getMethod("applySecurityOnEntity");
            assertSame(builder, builder.applySecurityOnEntity(m));
        }

        @Test
        @DisplayName("applySecurityOnEntity(ObjectAddress) returns same builder")
        void applySecurityObjectAddressReturnsSelf() throws ApiException {
            ObjectAddress addr = new ObjectAddress("applySecurityOnEntity");
            assertSame(builder, builder.applySecurityOnEntity(addr));
        }

        @Test
        @DisplayName("entityMustHaveFieldOfTypeAnnotatedWith returns same builder")
        void entityFieldAnnotationReturnsSelf() throws ApiException {
            assertSame(builder, builder.entityMustHaveFieldOfTypeAnnotatedWith(
                    IClass.getClass(TestAnnotation.class), IClass.getClass(String.class)));
        }

        @Test
        @DisplayName("up() returns parent link")
        void upReturnsParent() {
            assertSame(parentLink, builder.up());
        }
    }

    @Nested
    @DisplayName("Null checks")
    class NullChecks {

        @Test
        @DisplayName("authenticate(String) rejects null")
        void authenticateStringRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.authenticate((String) null));
        }

        @Test
        @DisplayName("authenticate(Method) rejects null")
        void authenticateMethodRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.authenticate((Method) null));
        }

        @Test
        @DisplayName("authenticate(ObjectAddress) rejects null")
        void authenticateObjectAddressRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.authenticate((ObjectAddress) null));
        }

        @Test
        @DisplayName("applySecurityOnEntity(String) rejects null")
        void applySecurityStringRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.applySecurityOnEntity((String) null));
        }

        @Test
        @DisplayName("applySecurityOnEntity(Method) rejects null")
        void applySecurityMethodRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.applySecurityOnEntity((Method) null));
        }

        @Test
        @DisplayName("applySecurityOnEntity(ObjectAddress) rejects null")
        void applySecurityObjectAddressRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.applySecurityOnEntity((ObjectAddress) null));
        }

        @Test
        @DisplayName("entityMustHaveFieldOfTypeAnnotatedWith rejects null annotation")
        void entityFieldAnnotationRejectsNullAnnotation() {
            assertThrows(NullPointerException.class,
                    () -> builder.entityMustHaveFieldOfTypeAnnotatedWith(null, IClass.getClass(String.class)));
        }

        @Test
        @DisplayName("entityMustHaveFieldOfTypeAnnotatedWith rejects null field type")
        void entityFieldAnnotationRejectsNullFieldType() {
            assertThrows(NullPointerException.class,
                    () -> builder.entityMustHaveFieldOfTypeAnnotatedWith(IClass.getClass(TestAnnotation.class), null));
        }

        @Test
        @DisplayName("useCase(String) rejects null")
        void useCaseStringRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.useCase((String) null));
        }

        @Test
        @DisplayName("useCase(Method) rejects null")
        void useCaseMethodRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.useCase((Method) null));
        }

        @Test
        @DisplayName("useCase(ObjectAddress) rejects null")
        void useCaseObjectAddressRejectsNull() {
            assertThrows(NullPointerException.class, () -> builder.useCase((ObjectAddress) null));
        }
    }

    @Nested
    @DisplayName("Use cases")
    class UseCases {

        // Note: useCase() currently passes null as IDomainBuilder link to UseCaseBuilder,
        // which causes NPE in AbstractAutomaticLinkedBuilder constructor.
        // These tests document the current (broken) behavior.

        @Test
        @DisplayName("useCase(String) throws NPE due to null domain builder link")
        void useCaseStringThrowsNpe() {
            assertThrows(NullPointerException.class, () -> builder.useCase("myUseCase"));
        }

        @Test
        @DisplayName("useCase(Method) throws NPE due to null domain builder link")
        void useCaseMethodThrowsNpe() throws Exception {
            Method m = TestAuthentication.class.getMethod("myUseCase");
            assertThrows(NullPointerException.class, () -> builder.useCase(m));
        }

        @Test
        @DisplayName("useCase(ObjectAddress) throws NPE due to null domain builder link")
        void useCaseObjectAddressThrowsNpe() {
            ObjectAddress addr = new ObjectAddress("myUseCase");
            assertThrows(NullPointerException.class, () -> builder.useCase(addr));
        }
    }

    @Nested
    @DisplayName("build()")
    class Build {

        @Test
        @DisplayName("build() produces an IAuthenticationContext")
        void buildProducesContext() throws DslException {
            IAuthenticationContext ctx = builder.build();
            assertNotNull(ctx);
        }

        @Test
        @DisplayName("built context has an authentication definition")
        void builtContextHasDefinition() throws DslException {
            IAuthenticationContext ctx = builder.build();
            IAuthenticationDefinition def = ctx.getAuthenticationDefinition();
            assertNotNull(def);
        }

        @Test
        @DisplayName("build() is idempotent (returns cached instance)")
        void buildIsIdempotent() throws DslException {
            IAuthenticationContext ctx1 = builder.build();
            IAuthenticationContext ctx2 = builder.build();
            assertSame(ctx1, ctx2);
        }

        @Test
        @DisplayName("full configuration builds successfully")
        void fullConfigurationBuilds() throws Exception {
            builder.authenticate("authenticate")
                    .applySecurityOnEntity("applySecurityOnEntity")
                    .entityMustHaveFieldOfTypeAnnotatedWith(
                            IClass.getClass(TestAnnotation.class), IClass.getClass(String.class));

            IAuthenticationContext ctx = builder.build();
            assertNotNull(ctx);
            assertNotNull(ctx.getAuthenticationDefinition());
        }
    }
}
