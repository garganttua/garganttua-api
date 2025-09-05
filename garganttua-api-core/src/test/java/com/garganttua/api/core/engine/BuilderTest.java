package com.garganttua.api.core.engine;

import static com.garganttua.api.spec.engine.Beans.bean;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static com.garganttua.api.core.engine.Context.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.Action;
import com.garganttua.api.spec.engine.IBuilder;
import com.garganttua.api.spec.engine.IContext;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.security.annotations.AuthenticatorKeyUsage;
import com.garganttua.api.spec.security.authenticator.AuthenticatorScope;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.service.ServiceAccess;
import com.garganttua.reflection.GGObjectAddress;

public class BuilderTest {

    @Test
    public void test() throws CoreException{

        assertEquals(getClass(), getClass());

        Field field = null;
        Method method = null;
        GGObjectAddress fieldAddress = null;

        IBuilder b = new Builder();
        IContext context = b.autoDetect(true)
        .superTenantId("0")
        .supertenantAutoCreate(true)
        .startup(bean(Object.class)) // Startup methods are called once the garganttua context is initilized and started
            .method(method)
                .withParam(0, service("domainName"))
                .withParam(0, repository("domainName"))
                .withParam(0, factory("domainName"))
/*              .withParam(0, body())
                .withParam(0, pathParam("domainName"))
                .withParam(0, queryParam("domainName"))
                .withParam(0, header("domainName")) */
                .withParam(0, bean("domainName")).up()

        .domain("entities")
            .startup(bean(Object.class)) // Startup methods are called once the garganttua context is initilized and started
            .method(method)
                .withParam(0, service("domainName"))
                .withParam(0, repository("domainName"))
                .withParam(0, factory("domainName"))
/*              .withParam(0, body())
                .withParam(0, pathParam("domainName"))
                .withParam(0, queryParam("domainName"))
                .withParam(0, header("domainName")) */
                .withParam(0, bean("domainName")).up()
            .interfasse(bean(Object.class))
            .interfasse(bean("events"))
            .creation(true)
            .readAll(true)
            .readOne(true)
            .update(true)
            .deleteAll(true)
            .deleteOne(true)
            .events(bean(Object.class))
            .events(bean("events"))

            .tenant(true)

            .tenant("id")
            .tenant(field)
            .tenant(fieldAddress)

            .owner("id")
            .owner(field)
            .owner(fieldAddress)

            .owned("id")
            .owned(field)
            .owned(fieldAddress)

            .publik()

            .shared("id")
            .shared(field)
            .shared(fieldAddress)

            .hiddenable("id")
            .hiddenable(field)
            .hiddenable(fieldAddress)

            .authorization() //if authorization , must be owned
                .autoDetect(true)

                .type("id")
                .type(field)
                .type(fieldAddress)

                .authorities("id")
                .authorities(field)
                .authorities(fieldAddress)

                .creation("id")
                .creation(field)
                .creation(fieldAddress)

                .expiration("id")
                .expiration(field)
                .expiration(fieldAddress)

                .revoked("id")
                .revoked(field)
                .revoked(fieldAddress)

                .toByteArray("id")
                .toByteArray(method)
                .toByteArray(fieldAddress)

                .validate("id")
                .validate(method)
                .validate(fieldAddress)

                .validateAgainst("id")
                .validateAgainst(method)
                .validateAgainst(fieldAddress)

                .signable()

                    .sign("id")
                    .sign(method)
                    .sign(fieldAddress)

                    .up()

                .refreshable()
                    .expiration("id")
                    .expiration(field)
                    .expiration(fieldAddress)

                    .token("id")
                    .token(method)
                    .token(fieldAddress)

                    .create("id")
                    .create(method)
                    .create(fieldAddress)

                    .validate("id")
                    .validate(method)
                    .validate(fieldAddress)

                    .up()
                .up()

            .authenticator()
                .autoDetect(true)

                .login("id")
                .login(field)
                .login(fieldAddress)

                .authorities("id")
                .authorities(field)
                .authorities(fieldAddress)

                .alwaysEnabled(false) // if false, below is mandatroy

                .credentialsNonExpired("id")
                .credentialsNonExpired(field)
                .credentialsNonExpired(fieldAddress)

                .enabled("id")
                .enabled(field)
                .enabled(fieldAddress)

                .accountNonExpired("id")
                .accountNonExpired(field)
                .accountNonExpired(fieldAddress)

                .accountNonLocked("id")
                .accountNonLocked(field)
                .accountNonLocked(fieldAddress)

                .scope(AuthenticatorScope.system)

                .interfasse(bean(Object.class))
                .interfasse(bean("gg:events"))

                .authentication(Object.class)
                .authentication(Object.class)

                .authorization(Object.class)
                    .lifeTime(30, TimeUnit.DAYS)
                    .refreshLifeTime(60, TimeUnit.DAYS)
                    .key(Object.class)
                        .usage(AuthenticatorKeyUsage.oneForAll)
                        .algorithm(KeyAlgorithm.HMAC_SHA512_512)
                        .signatureAlgorithm(SignatureAlgorithm.HMAC_SHA512)
                        .lifeTime(30, TimeUnit.DAYS)
                        .autoCreate(true)
                        .up()
                    .up()
                .up()

            .entity(Object.class)
                .autoDetect(true)
                .id("id")
                .id(field)
                .id(fieldAddress)
                .uuid("id")
                .uuid(field)
                .uuid(fieldAddress)
                .tenantId("id")
                .tenantId(field)
                .tenantId(fieldAddress)
                .mandatory("id")
                .mandatory(field)
                .mandatory(fieldAddress)
                .unicity("id")
                .unicity(field)
                .unicity(fieldAddress)
                .unicity("id", UnicityScope.system)
                .unicity(field, UnicityScope.system)
                .unicity(fieldAddress, UnicityScope.system)
                .update("id")
                .update(field)
                .update(fieldAddress)
                .update("id", "authority")
                .update(field, "authority")
                .update(fieldAddress, "authority")
                .annotation("id", Object.class)
                .annotation(field, Object.class)
                .annotation(fieldAddress, Object.class)
                .afterGet("id")
                .afterGet(method)
                .afterGet(fieldAddress)
                .beforeCreate("id")
                .beforeCreate(method)
                .beforeCreate(fieldAddress)
                .beforeUpdate("id")
                .beforeUpdate(method)
                .beforeUpdate(fieldAddress)
                .beforeDelete("id")
                .beforeDelete(method)
                .beforeDelete(fieldAddress)
                .afterCreate("id")
                .afterCreate(method)
                .afterCreate(fieldAddress)
                .afterUpdate("id")
                .afterUpdate(method)
                .afterUpdate(fieldAddress)
                .afterDelete("id")
                .afterDelete(method)
                .afterDelete(fieldAddress)
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
                .authorization(Object.class)
                .authorization(Object.class)
                .authorizationProtocol(Object.class)
                .authorizationProtocol(Object.class)
                .up()
            .autoDetectDtos(true)
            .dto(Object.class)
                .autoDetect(true)
                .db("gg:SpringMongoDao")
                .tenantId("id")
                .tenantId(field)
                .tenantId(fieldAddress)
                .up()
            .autoDetectUseCases(true)
            .useCase("createNewTenant")
                .pathSuffix("/applicants")
                .completePath("/v1/applicants")
                .action(Action.allEntities)
                .action(Action.oneEntity)
                .action(Action.listOfEntities)
                .method(com.garganttua.api.spec.Method.create)
                .input(Object.class) //If not set, use domain entity
                .output(Object.class) //If not set, use domain entity
                .authority(false)
                .access(ServiceAccess.anonymous)
                .bind(bean(Object.class))
                    .method(method)
                        .withParam(0, service("domainName"))
                        .withParam(0, repository("domainName"))
                        .withParam(0, factory("domainName"))
    /*                   .withParam(0, body())
                        .withParam(0, pathParam("domainName"))
                        .withParam(0, queryParam("domainName"))
                        .withParam(0, header("domainName")) */
                        .withParam(0, bean("domainName"))
    /*                  .withParam(0, caller()) */
    //INJECT : service() factory() repository()
                    .up()
                .up()
        .build();

        context.start();
        context.stop();
        context.flush();
        context.init();
        context.reload();
    }

}
