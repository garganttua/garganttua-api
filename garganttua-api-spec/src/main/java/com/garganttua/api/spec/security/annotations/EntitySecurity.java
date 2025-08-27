package com.garganttua.api.spec.security.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.spec.service.ServiceAccess;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EntitySecurity {
	
	ServiceAccess creation_access() default ServiceAccess.tenant;

	ServiceAccess read_all_access() default ServiceAccess.tenant;

	ServiceAccess read_one_access() default ServiceAccess.tenant;

	ServiceAccess update_one_access() default ServiceAccess.tenant;

	ServiceAccess delete_one_access() default ServiceAccess.tenant;

	ServiceAccess delete_all_access() default ServiceAccess.tenant;

	ServiceAccess count_access() default ServiceAccess.tenant;
	
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
