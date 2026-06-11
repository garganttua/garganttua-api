package com.garganttua.api.core.unit.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.garganttua.core.reflection.IMethod;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.authentication.AuthenticationBuilder;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.definition.IAuthenticationDefinition;
import com.garganttua.api.commons.security.context.IAuthenticationContext;
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
    private AuthenticationBuilder<IApiSecurityBuilder> builder;

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {}


    @BeforeEach
    void setUp() {
        ReflectionBuilder.builder()
                .withProvider(new RuntimeReflectionProvider())
                .withScanner(new ReflectionsAnnotationScanner())
                .build();

        parentLink = mock(IApiSecurityBuilder.class);
        supplierBuilder = mock(ISupplierBuilder.class);
        when(supplierBuilder.getSuppliedClass()).thenReturn(IClass.getClass(TestAuthentication.class));
        when(supplierBuilder.build()).thenReturn(null);
        builder = new AuthenticationBuilder<IApiSecurityBuilder>(parentLink, supplierBuilder);
    }

    @Nested
    @DisplayName("Fluent API chaining")
    class FluentApi {

        @Test
        @DisplayName("authenticate(String) returns binder builder")
        void authenticateStringReturnsBinder() throws ApiException {
            assertNotNull(builder.authenticate("authenticate"), "should return binder builder");
        }

        @Test
        @DisplayName("authenticate(Method) returns binder builder")
        void authenticateMethodReturnsBinder() throws Exception {
            IMethod m = IClass.getClass(TestAuthentication.class).getMethod("authenticate");
            assertNotNull(builder.authenticate(m), "should return binder builder");
        }

        @Test
        @DisplayName("authenticate(ObjectAddress) returns binder builder")
        void authenticateObjectAddressReturnsBinder() throws ApiException {
            ObjectAddress addr = new ObjectAddress("authenticate");
            assertNotNull(builder.authenticate(addr), "should return binder builder");
        }

        @Test
        @DisplayName("applySecurityOnEntity(String) returns binder builder whose up() is the auth builder")
        void applySecurityStringReturnsBinder() throws ApiException {
            var binder = builder.applySecurityOnEntity("applySecurityOnEntity");
            assertNotNull(binder, "should return the binder builder");
            assertSame(builder, binder.up(), "binder.up() returns the authentication builder");
        }

        @Test
        @DisplayName("applySecurityOnEntity(Method) returns binder builder")
        void applySecurityMethodReturnsBinder() throws Exception {
            IMethod m = IClass.getClass(TestAuthentication.class).getMethod("applySecurityOnEntity");
            assertNotNull(builder.applySecurityOnEntity(m), "should return the binder builder");
        }

        @Test
        @DisplayName("applySecurityOnEntity(ObjectAddress) returns binder builder")
        void applySecurityObjectAddressReturnsBinder() throws ApiException {
            ObjectAddress addr = new ObjectAddress("applySecurityOnEntity");
            assertNotNull(builder.applySecurityOnEntity(addr), "should return the binder builder");
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
            assertThrows(NullPointerException.class, () -> builder.authenticate((IMethod) null));
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
            assertThrows(NullPointerException.class, () -> builder.applySecurityOnEntity((IMethod) null));
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
            assertThrows(NullPointerException.class, () -> builder.useCase((IMethod) null));
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
            IMethod m = IClass.getClass(TestAuthentication.class).getMethod("myUseCase");
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
        @DisplayName("authenticate method creates binder builder")
        void authenticateMethodCreatesBinder() throws Exception {
            var binderBuilder = builder.authenticate("authenticate");
            assertNotNull(binderBuilder, "authenticate should create a binder builder");
        }
    }
}
