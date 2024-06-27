package com.garganttua.api.spec.security.annotations;

import com.garganttua.api.spec.security.GGAPIServiceAccess;

public @interface GGEntitySecurity {
	
	GGAPIServiceAccess creation_access() default GGAPIServiceAccess.authenticated;

	GGAPIServiceAccess read_all_access() default GGAPIServiceAccess.authenticated;

	GGAPIServiceAccess read_one_access() default GGAPIServiceAccess.authenticated;

	GGAPIServiceAccess update_one_access() default GGAPIServiceAccess.authenticated;

	GGAPIServiceAccess delete_one_access() default GGAPIServiceAccess.authenticated;

	GGAPIServiceAccess delete_all_access() default GGAPIServiceAccess.authenticated;

	GGAPIServiceAccess count_access() default GGAPIServiceAccess.authenticated;
	
	boolean creation_authority() default false;

	boolean read_all_authority() default false;

	boolean read_one_authority() default false;

	boolean update_one_authority() default false;

	boolean delete_one_authority() default false;

	boolean delete_all_authority() default false;

	boolean count_authority() default false;

}
