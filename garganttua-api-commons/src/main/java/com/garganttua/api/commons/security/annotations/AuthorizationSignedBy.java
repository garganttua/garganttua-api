package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Marks the field on a (signable) authorization entity that records who signed
 * it. The framework stamps it at signing time with the qualified key-realm id
 * following the {@code ${domainName}:${id}} naming rule
 * ({@code ${keyDomainName}:${keyUuid}} in persisted-key mode, the realm name in
 * supplier mode). Equivalent DSL:
 * {@code .security().authorization().signedBy(field)}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AuthorizationSignedBy {

}
