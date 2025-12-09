package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.context.Access;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EntitySecurity {
	
	Access creation_access() default Access.tenant;

	Access read_all_access() default Access.tenant;

	Access read_one_access() default Access.tenant;

	Access update_one_access() default Access.tenant;

	Access delete_one_access() default Access.tenant;

	Access delete_all_access() default Access.tenant;

	Access count_access() default Access.tenant;
	
	boolean creation_authority() default true;

	boolean read_all_authority() default true;

	boolean read_one_authority() default true;

	boolean update_one_authority() default true;

	boolean delete_one_authority() default true;

	boolean delete_all_authority() default true;

	boolean count_authority() default true;
	
	Class<?>[] authorizations() default {};

	Class<?>[] authorizationProtocols() default {};

}
