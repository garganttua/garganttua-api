package com.garganttua.api.spec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.repository.dao.GGAPIDao;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GGAPIEntity {

	String dto();

	GGAPIDao db() default GGAPIDao.mongo;
	
	boolean allow_creation() default true;

	boolean allow_read_all() default true;

	boolean allow_read_one() default true;

	boolean allow_update_one() default true;

	boolean allow_delete_one() default true;

	boolean allow_delete_all() default true;

	boolean allow_count() default true;
	
	GGAPICrudAccess creation_access() default GGAPICrudAccess.authenticated;

	GGAPICrudAccess read_all_access() default GGAPICrudAccess.authenticated;

	GGAPICrudAccess read_one_access() default GGAPICrudAccess.authenticated;

	GGAPICrudAccess update_one_access() default GGAPICrudAccess.authenticated;

	GGAPICrudAccess delete_one_access() default GGAPICrudAccess.authenticated;

	GGAPICrudAccess delete_all_access() default GGAPICrudAccess.authenticated;

	GGAPICrudAccess count_access() default GGAPICrudAccess.authenticated;
	
	boolean creation_authority() default false;

	boolean read_all_authority() default false;

	boolean read_one_authority() default false;

	boolean update_one_authority() default false;

	boolean delete_one_authority() default false;

	boolean delete_all_authority() default false;

	boolean count_authority() default false;
	
	String controller() default "";
	
	String business() default "";
	
	String ws() default "";
	
	String connector() default "";

	String repository() default "";
	
	String dao() default "";

	String openApiSchemas() default "";
	
	String eventPublisher() default "";

	String domain();
	
	boolean publicEntity() default false;
	
	boolean hiddenAble() default false;
	
	String shared() default "";
	
	String[] unicity() default {};

	boolean tenantEntity() default false;
	
}
