package com.garganttua.api.core.builder;

import static com.garganttua.api.core.context.application.ApplicationContext.Suppliers.bean;
import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.credentials;
import static com.garganttua.api.core.context.execution.ExecutionContext.Suppliers.principal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.DummyAuthentication;
import com.garganttua.api.core.DummyAuthorizationProtocol;
import com.garganttua.api.core.DummyDao;
import com.garganttua.api.core.DummyEntity;
import com.garganttua.api.core.DummyInterface;
import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.Action;
import com.garganttua.api.spec.engine.ContextBuildingStage;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.engine.TechnicalOperation;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.entity.annotations.EntityUuid;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.service.Access;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.GGBeanSupplier;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.injection.GGInjector;
import com.garganttua.reflection.injection.IGGInjector;
import com.garganttua.reflection.properties.IGGPropertyLoader;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class BuilderTest {

    @Test
    public void shouldThrowAnErrorIfBuildWithAutoDetectWithoutPackages(){
        IApplicationContextBuilder b = new ApplicationContextBuilder();
        BuilderException exception = assertThrows(BuilderException.class, () -> b.autoDetect(true).build());

        assertEquals(CoreExceptionCode.BUILDER_CODE, exception.getCode());
        assertEquals("Packages must be set before setting autoDetect", exception.getMessage());

    }

    @Test
    public void generalTest() throws CoreException, NoSuchMethodException, SecurityException, NoSuchFieldException {

		GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();

        IGGPropertyLoader propLoader = new IGGPropertyLoader() {

            @Override
            public String getProperty(String propertyName) {
                return "dummy";
            }
        };

        IGGBeanLoader beanloader = GGBeanLoaderFactory.getLoader(propLoader, List.of("com.garganttua"), List.of(new GGBeanSupplier(List.of("com.garganttua"), propLoader)));
        IGGInjector injector = GGInjector.injector(beanloader);

        IApplicationContextBuilder b = new ApplicationContextBuilder();
        IApplicationContext context = b
        .packages(List.of("com.garganttua"))
        .injector(injector)
        .propertyLoader(propLoader)
        .beanLoader(beanloader)
        .autoDetect(true)
        .superTenantId("0")
        .superTenantAutoCreate(true)
        .security()
            .disable(true)
            .authorizationProtocol(bean(DummyAuthorizationProtocol.class))
                .getAuthorization("getAuthorization")
                .setAuthorization("setAuthorization")
                .up()
            .authentication(new FixedObjectSupplierBuilder<DummyAuthentication>(new DummyAuthentication()))
                .findPrincipal(true)
                .authenticate("authenticate")
                .applySecurityOnEntity("applySecurityOnEntity")
                .entityMustHaveFieldOfTypeAnnotatedWith(EntityUuid.class, String.class)
                .useCase(DummyAuthentication.class.getDeclaredMethod("authenticate", Byte[].class, Object.class))
                    .pathSuffix("/applicants")
                    .completePath("/v1/applicants")
                    .action(Action.allEntities)
                    .action(Action.oneEntity)
                    .action(Action.listOfEntities)
                    .operation(TechnicalOperation.create)
                    .input(Object.class) //If not set, use domain entity
                    .output(Object.class) //If not set, use domain entity
                    .bind()
                        .method()
                            .withParam(0, credentials())
                            .withParam(1, principal())
                            .up()
                        .up()
                .up()
            .up()
        .startup(ContextBuildingStage.initial, new Object()) // Startup methods are called once the garganttua context is initilized and started
            .method(Object.class.getDeclaredMethod("equals", Object.class))
                .withParam(0, new String("1234"))
                .up()
        .domain("dummies")
            .create(new String()) // create a new entity at startup if does not exists. TenantId must exists otherwise an error is thrown. No error thrown if already exists
            .upsert(new String()) // create or update a new entity at startup. TenantId must exists otherwise an error is thrown
            .startup(ContextBuildingStage.initial, bean(Object.class)) // Startup methods are called once the garganttua context is initilized and started
                .method(Object.class.getDeclaredMethod("equals", Object.class))
                    .withParam(0, new String("1234"))
                    .up()
            .interfasse(new DummyInterface())
            .interfasse(bean("gg", DummyInterface.class))
            .creation(true)
            .readAll(true)
            .readOne(true)
            .update(true)
            .deleteAll(true)
            .deleteOne(true)
            .events(bean(IEventPublisher.class))
            .tenant(true)
            .entity(DummyEntity.class)
                .autoDetect(true)
                .id("id")
                .uuid("uuid")
                .tenantId("tenantId")
                .mandatory("id")
                .unicity("id")
                .unicity("id", UnicityScope.system)
                .update("id")
                .update("id", "authority")
                .annotation("id", Entity.class)
                .annotation(DummyEntity.class.getDeclaredField("uuid"), Entity.class)
                .annotation(DummyEntity.class.getDeclaredMethod("testMethod"), Entity.class)
                .afterGet("testMethod2").withParam(0, new String("1234")).up()
                .beforeCreate("testMethod2").withParam(0, new String("1234")).up()
                .beforeUpdate("testMethod2").withParam(0, new String("1234")).up()
                .beforeDelete("testMethod2").withParam(0, new String("1234")).up()
                .afterCreate("testMethod2").withParam(0, new String("1234")).up()
                .afterUpdate("testMethod2").withParam(0, new String("1234")).up()
                .afterDelete("testMethod2").withParam(0, new String("1234")).up()
                .up()

            .owner("id")
            .owned("id")
            .publik()
            .shared("id")
            .hiddenable("hidden")

            .security()
                .disable(false)
                .autoDetect(true)
                .creationAccess(Access.tenant)
                .readAllAccess(Access.tenant)
                .readOneAccess(Access.tenant)
                .updateAccess(Access.tenant)
                .deleteAllAccess(Access.tenant)
                .deleteOneAccess(Access.tenant)
                .creationAuthority(true)
                .readAllAuthority(true)
                .readOneAuthority(true)
                .updateAuthority(true)
                .deleteAllAuthority(true)
                .deleteOneAuthority(true)
                
                .useCase(b.domain("dummies").useCase("createNewTenant"), false, Access.anonymous)
                .useCase(b.security().authentication(DummyAuthentication.class).useCase("authenticate"), false, Access.anonymous)
                
                .authorizationProtocol(DummyInterface.class, b.security().authorizationProtocol(DummyAuthorizationProtocol.class))

                .authorization() //Indicates that entity is an authorization. if authorization , must be owned, and must be authenticator
                    .autoDetect(true)
                    .signable().up()
                    .storable(true)
                    .type("id")
                    .authorities("authorities")
                    .expirable("expiration")
                    .revokable("revoked")
                    .encode("toByteArray")
                    .decode("fromByteArray")
                    .refreshable()
                        .expirable("refreshExpiration")
                        .revokable("refreshRevoked")
                        .encode("refreshToByteArray")
                        .up()
                    .up()

                .key()
                    .autoDetect(true)
                    .up()

                .authenticator() //Indicates that entity is an authenticator.  if authenticator, must be owner
                    .autoDetect(true)
                    .login("id")
                    .authorities("authorities")
                    .alwaysEnabled(false) // if false, below is mandatroy
                    .credentialsNonExpired("active")
                    .enabled("active")
                    .accountNonExpired("active")
                    .accountNonLocked("active")
                    .scope(AuthenticatorScope.system)
                    .authentication(b.security().authentication(DummyAuthentication.class))
                    .authentication(b.security().authentication(DummyAuthentication.class))
                    .authorization(b.domain(DummyEntity.class))
                        .lifeTime(30, TimeUnit.DAYS)
                        .refreshLifeTime(60, TimeUnit.DAYS)//Only if refreshable
                        .key(b.domain(DummyEntity.class)) // Only if signable
                            .usage(AuthenticatorKeyUsage.oneForAll)
                            .algorithm(KeyAlgorithm.HMAC_SHA512_512)
                            .signatureAlgorithm(SignatureAlgorithm.HMAC_SHA512)
                            .lifeTime(30, TimeUnit.DAYS)
                            .autoDetect(true)
                            .autoCreate(true)
                            .up()
                        .up()
                    .up()
                .up()
            .dto(Object.class)
                .autoDetect(true)
                .db(bean("gg", DummyDao.class))
                .up()
            .useCase("createNewTenant")
                .pathSuffix("/applicants")
                .completePath("/v1/applicants")
                .action(Action.allEntities)
                .action(Action.oneEntity)
                .action(Action.listOfEntities)
                .operation(TechnicalOperation.create)
                .input(Object.class) //If not set, use domain entity
                .output(Object.class) //If not set, use domain entity
                .bind(bean(DummyEntity.class))
                    .method("testMethod2")
                        .withParam(0, new String("1234"))
                        .up()
                    .up()
                .up()

        .build(); // => process auto detection, validate, build

       /*  context.start();
        context.stop();
        context.flush();
        context.init();
        context.reload(); */
    }

}
