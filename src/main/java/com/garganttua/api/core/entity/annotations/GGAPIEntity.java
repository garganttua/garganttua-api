package com.garganttua.api.core.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.garganttua.api.core.GGAPIServiceAccess;
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
	
	String ws() default "";

	String repository() default "";
	
	String dao() default "";

	String openApiSchemas() default "";
	
	String eventPublisher() default "";

	String domain();
	
//	boolean publicEntity() default false;
//	
//	boolean hiddenAble() default false;
//	
//	/**
//	 * Sharing field 
//	 * @return
//	 */
//	String shared() default "";
//	
//	/**
//	 * Refers to a GeoJson field
//	 * @return
//	 */
//	String geolocialized() default "";
//	
//	String[] unicity() default {};
//	
//	String[] mandatory() default {};
//	
//	boolean ownedEntity() default false;
//
//	boolean showTenantId() default false;
	
}