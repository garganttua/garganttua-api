package com.garganttua.api.commons.engine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.garganttua.core.reflection.annotations.Indexed;

/**
 * API-level configuration annotation. Place on any class within a scanned package
 * to configure the Garganttua API engine via auto-detection.
 *
 * <p>Mirrors the configuration available through {@code ApiBuilder}:
 * <pre>
 * ApiBuilder.builder()
 *     .multiTenant(true)
 *     .superTenantId("SUPER_TENANT")
 *     .superTenantAutoCreate(true)
 * </pre>
 *
 * <p>Equivalent annotation-based configuration:
 * <pre>
 * &#64;Api(multiTenancy = true, superTenantId = "SUPER_TENANT", superTenantAutoCreate = true)
 * public class MyApiConfig {}
 * </pre>
 */
@Indexed
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Api {

	/**
	 * Enables or disables multi-tenancy globally.
	 * When false, tenant-related features (superTenantId, tenantId filtering) are disabled.
	 */
	boolean multiTenancy() default true;

	/**
	 * The super-tenant identifier. Only applies when {@link #multiTenancy()} is true.
	 * Empty string means no super-tenant is configured.
	 */
	String superTenantId() default "";

	/**
	 * Whether to auto-create the super-tenant entity on startup.
	 * Only applies when {@link #multiTenancy()} is true and {@link #superTenantId()} is set.
	 */
	boolean superTenantAutoCreate() default false;

}
