package com.garganttua.api.commons.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * Field-level marker for the name of a {@link Key}-marked entity —
 * the value returned by {@code IKeyRealm.getName()}. The name is the
 * logical identifier the framework uses to find the matching key when
 * signing or verifying (e.g. {@code "auth-key"}). Type: {@code String}.
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface KeyName {

}
