package com.garganttua.api.core.engine;

import static com.garganttua.api.core.engine.ApplicationContext.Suppliers.bean;
import static com.garganttua.api.core.engine.ApplicationContext.Suppliers.factory;
import static com.garganttua.api.core.engine.ApplicationContext.Suppliers.repository;
import static com.garganttua.api.core.engine.ApplicationContext.Suppliers.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.DummyDao;
import com.garganttua.api.core.DummyEntity;
import com.garganttua.api.core.DummyInterface;
import com.garganttua.api.core.DummyKey;
import com.garganttua.api.core.ReflectionsAnnotationScanner;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.Action;
import com.garganttua.api.spec.engine.ContextBuildingStage;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IApplicationContextBuilder;
import com.garganttua.api.spec.entity.annotations.Entity;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.event.IEventPublisher;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.beans.GGBeanLoaderFactory;
import com.garganttua.reflection.beans.GGBeanSupplier;
import com.garganttua.reflection.beans.IGGBeanLoader;
import com.garganttua.reflection.properties.IGGPropertyLoader;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class BuilderTest {

    @Test
    public void test() throws CoreException, NoSuchMethodException, SecurityException, NoSuchFieldException {

        GGObjectReflectionHelper.annotationScanner = new ReflectionsAnnotationScanner();

        Field field = null;
        Method method = null;
        GGObjectAddress fieldAddress = null;
        IGGPropertyLoader propLoader = new IGGPropertyLoader() {

            @Override
            public String getProperty(String propertyName) {
                return "dummy";
            }
        };

        IGGBeanLoader beanloader = GGBeanLoaderFactory.getLoader(propLoader, List.of("com.garganttua"),
                List.of(new GGBeanSupplier(List.of("com.garganttua"), propLoader)));

        IApplicationContextBuilder b = new ApplicationContextBuilder();
        IApplicationContext context = b
                .packages(List.of("com.garganttua"))
                .propertyLoader(propLoader)
                .beanLoader(beanloader)
                .autoDetect(true)
                .superTenantId("0")
                .superTenantAutoCreate(true)
                /*
                 * .security()
                 * .authentication(Object.class)
                 * .findPrincipal(true)
                 * .authenticate("authenticate")
                 * .up()
                 * .up()
                 * .authorizationProtocol(Object.class)
                 * .up()
                 * .up()
                 */
                .startup(ContextBuildingStage.initial, new Object()) // Startup methods are called once the garganttua
                                                                     // context is initilized and started
                .method(Object.class.getDeclaredMethod("equals", Object.class))
                .withParam(0, new String("1234"))
                /*
                 * .withParam(0, service("domainName"))
                 * .withParam(0, repository("domainName"))
                 * .withParam(0, factory("domainName"))
                 * .withParam(0, body())
                 * .withParam(0, pathParam("domainName"))
                 * .withParam(0, queryParam("domainName"))
                 * .withParam(0, header("domainName"))
                 * .withParam(0, bean("domainName"))
                 */
                .up()
                .domain("dummies")
                .create(new String()) // create a new entity at startup if does not exists. TenantId must exists
                                      // otherwise an error is thrown. No error thrown if already exists
                .upsert(new String()) // create or update a new entity at startup. TenantId must exists otherwise an
                                      // error is thrown
                .startup(ContextBuildingStage.initial, bean(Object.class)) // Startup methods are called once the
                                                                           // garganttua context is initilized and
                                                                           // started
                .method(Object.class.getDeclaredMethod("equals", Object.class))
                .withParam(0, new String("1234"))
                /*
                 * .withParam(0, service("domainName"))
                 * .withParam(0, repository("domainName"))
                 * .withParam(0, factory("domainName"))
                 * .withParam(0, body())
                 * .withParam(0, pathParam("domainName"))
                 * .withParam(0, queryParam("domainName"))
                 * .withParam(0, header("domainName"))
                 * .withParam(0, bean("domainName"))
                 */
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
                /*
                 * .id(field)
                 * .id(fieldAddress)
                 */
                .uuid("uuid")
                /*
                 * .uuid(field)
                 * .uuid(fieldAddress)
                 */
                .tenantId("tenantId")
                /*
                 * .tenantId(field)
                 * .tenantId(fieldAddress)
                 */
                .mandatory("id")
                /*
                 * .mandatory(field)
                 * .mandatory(fieldAddress)
                 */
                .unicity("id")
                /*
                 * .unicity(field)
                 * .unicity(fieldAddress)
                 */
                .unicity("id", UnicityScope.system)
                /*
                 * .unicity(field, UnicityScope.system)
                 * .unicity(fieldAddress, UnicityScope.system)
                 */
                .update("id")
                /*
                 * .update(field)
                 * .update(fieldAddress)
                 */
                .update("id", "authority")
                /*
                 * .update(field, "authority")
                 * .update(fieldAddress, "authority")
                 */
                .annotation("id", Entity.class)
                .annotation(DummyEntity.class.getDeclaredField("uuid"), Entity.class)
                .annotation(DummyEntity.class.getDeclaredMethod("testMethod"), Entity.class)
                /* .annotation(fieldAddress, Entity.class) */
                .afterGet("testMethod2").withParam(0, new String("1234")).up()
                /*
                 * .afterGet(method).withParam(0, new String("1234")).up()
                 * .afterGet(fieldAddress).withParam(0, new String("1234")).up()
                 */
                .beforeCreate("testMethod2").withParam(0, new String("1234")).up()
                /*
                 * .beforeCreate(method).withParam(0, new String("1234")).up()
                 * .beforeCreate(fieldAddress).withParam(0, new String("1234")).up()
                 */
                .beforeUpdate("testMethod2").withParam(0, new String("1234")).up()
                /*
                 * .beforeUpdate(method).withParam(0, new String("1234")).up()
                 * .beforeUpdate(fieldAddress).withParam(0, new String("1234")).up()
                 */
                .beforeDelete("testMethod2").withParam(0, new String("1234")).up()
                /*
                 * .beforeDelete(method).withParam(0, new String("1234")).up()
                 * .beforeDelete(fieldAddress).withParam(0, new String("1234")).up()
                 */
                .afterCreate("testMethod2").withParam(0, new String("1234")).up()
                /*
                 * .afterCreate(method).withParam(0, new String("1234")).up()
                 * .afterCreate(fieldAddress).withParam(0, new String("1234")).up()
                 */
                .afterUpdate("testMethod2").withParam(0, new String("1234")).up()
                /*
                 * .afterUpdate(method).withParam(0, new String("1234")).up()
                 * .afterUpdate(fieldAddress).withParam(0, new String("1234")).up()
                 */
                .afterDelete("testMethod2").withParam(0, new String("1234")).up()
                /*
                 * .afterDelete(method).withParam(0, new String("1234")).up()
                 * .afterDelete(fieldAddress).withParam(0, new String("1234")).up()
                 */
                .up()

                .owner("id")
                /*
                 * .owner(field)
                 * .owner(fieldAddress)
                 */
                .owned("id")
                /*
                 * .owned(field)
                 * .owned(fieldAddress)
                 */
                .publik()

                .shared("id")
                /*
                 * .shared(field)
                 * .shared(fieldAddress)
                 */

                .hiddenable("hidden")
                /*
                 * .hiddenable(field)
                 * .hiddenable(fieldAddress)
                 */

                .authorization() // if authorization , must be owned, and must be authenticator
                .autoDetect(true)

                .signable(b.domain(DummyKey.class))

                .sign("sign")
                /*
                 * .sign(method)
                 * .sign(fieldAddress)
                 */

                .up()

                .type("id")
                /*
                 * .type(field)
                 * .type(fieldAddress)
                 */

                .authorities("authorities")
                /*
                 * .authorities(field)
                 * .authorities(fieldAddress)
                 */
                .creation("creation")
                /*
                 * .creation(field)
                 * .creation(fieldAddress)
                 */

                .expiration("expiration")
                /*
                 * .expiration(field)
                 * .expiration(fieldAddress)
                 */

                .revoked("revoked")
                /*
                 * .revoked(field)
                 * .revoked(fieldAddress)
                 */

                .toByteArray("toByteArray")
                /*
                 * .toByteArray(method)
                 * .toByteArray(fieldAddress)
                 */

                .fromByteArray("fromByteArray")
                /*
                 * .toByteArray(method)
                 * .toByteArray(fieldAddress)
                 */

                .validate("validate")
                /*
                 * .validate(method)
                 * .validate(fieldAddress)
                 */

                .validateAgainst("validateAgainst")
                /*
                 * .validateAgainst(method)
                 * .validateAgainst(fieldAddress)
                 */

                .refreshable()

                .expiration("refreshExpiration")
                /*
                 * .expiration(field)
                 * .expiration(fieldAddress)
                 */

                .revoked("refreshRevoked")
                /*
                 * .revoked(field)
                 * .revoked(fieldAddress)
                 */

                .toByteArray("refreshToByteArray")
                /*
                 * .toByteArray(method)
                 * .toByteArray(fieldAddress)
                 */

                .validate("refreshValidate")
                /*
                 * .validate(method)
                 * .validate(fieldAddress)
                 */

                .validateAgainst("refreshValidateAgainst")
                /*
                 * .validateAgainst(method)
                 * .validateAgainst(fieldAddress)
                 */

                .up()
                .up()

                .authenticator()
                .autoDetect(true)

                .login("id")
                /*
                 * .login(field)
                 * .login(fieldAddress)
                 */

                .authorities("authorities")
                /*
                 * .authorities(field)
                 * .authorities(fieldAddress)
                 */

                .alwaysEnabled(false) // if false, below is mandatroy

                .credentialsNonExpired("active")
                /*
                 * .credentialsNonExpired(field)
                 * .credentialsNonExpired(fieldAddress)
                 */

                .enabled("active")
                /*
                 * .enabled(field)
                 * .enabled(fieldAddress)
                 */

                .accountNonExpired("active")
                /*
                 * .accountNonExpired(field)
                 * .accountNonExpired(fieldAddress)
                 */

                .accountNonLocked("active")
                /*
                 * .accountNonLocked(field)
                 * .accountNonLocked(fieldAddress)
                 */

                .scope(AuthenticatorScope.system)

                .authentication(Object.class)
                /* .authentication(Object.class) */

                .authorization(b.domain(DummyEntity.class))
                .lifeTime(30, TimeUnit.DAYS)
                .refreshLifeTime(60, TimeUnit.DAYS)
                .key(b.domain(DummyEntity.class))
                .usage(AuthenticatorKeyUsage.oneForAll)
                .algorithm(KeyAlgorithm.HMAC_SHA512_512)
                .signatureAlgorithm(SignatureAlgorithm.HMAC_SHA512)
                .lifeTime(30, TimeUnit.DAYS)
                .autoCreate(true)
                .up()
                .up()

                .security()
                .autoDetect(true)
                .creationAccess(ServiceAccess.tenant)
                .readAllAccess(ServiceAccess.tenant)
                .readOneAccess(ServiceAccess.tenant)
                .updateAccess(ServiceAccess.tenant)
                .deleteAllAccess(ServiceAccess.tenant)
                .deleteOneAccess(ServiceAccess.tenant)
                .creationAuthority(true)
                .readAllAuthority(true)
                .readOneAuthority(true)
                .updateAuthority(true)
                .deleteAllAuthority(true)
                .deleteOneAuthority(true)
                .authorization(b.domain(DummyEntity.class))
                .interfasse(DummyInterface.class)
                .protocol(b.security().authorizationProtocol(Object.class))
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
                .method(com.garganttua.api.spec.Method.create)
                .input(Object.class) // If not set, use domain entity
                .output(Object.class) // If not set, use domain entity
                .authority(false)
                .access(ServiceAccess.anonymous)
                .bind(bean(Object.class))
                .method(method)
                .withParam(0, service("domainName"))
                .withParam(0, repository("domainName"))
                .withParam(0, factory("domainName"))
                /*
                 * .withParam(0, body())
                 * .withParam(0, pathParam("domainName"))
                 * .withParam(0, queryParam("domainName"))
                 * .withParam(0, header("domainName"))
                 */
                .withParam(0, bean("gg", Object.class))
                /* .withParam(0, caller()) */
                // INJECT : service() factory() repository()
                .up()
                .up()
                .build(); // => process auto detection, validate, build

        context.start();
        context.stop();
        context.flush();
        context.init();
        context.reload();
    }

}
