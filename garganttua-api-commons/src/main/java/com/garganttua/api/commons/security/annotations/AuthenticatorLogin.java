package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AuthenticatorLogin {

	Class<?>[] authentications() default {};

}
