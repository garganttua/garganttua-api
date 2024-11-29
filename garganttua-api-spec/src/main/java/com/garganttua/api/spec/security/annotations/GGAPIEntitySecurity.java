package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.service.GGAPIServiceAccess;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GGAPIEntitySecurity {
	
	GGAPIServiceAccess creation_access() default GGAPIServiceAccess.tenant;

	GGAPIServiceAccess read_all_access() default GGAPIServiceAccess.tenant;

	GGAPIServiceAccess read_one_access() default GGAPIServiceAccess.tenant;

	GGAPIServiceAccess update_one_access() default GGAPIServiceAccess.tenant;

	GGAPIServiceAccess delete_one_access() default GGAPIServiceAccess.tenant;

	GGAPIServiceAccess delete_all_access() default GGAPIServiceAccess.tenant;

	GGAPIServiceAccess count_access() default GGAPIServiceAccess.tenant;
	
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
