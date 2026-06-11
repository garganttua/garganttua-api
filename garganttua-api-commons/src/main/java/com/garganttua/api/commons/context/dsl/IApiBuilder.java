package com.garganttua.api.commons.context.dsl;

import com.garganttua.api.commons.context.BuildingStage;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.dao.IDaoFactory;
import com.garganttua.api.commons.endpoint.IInterface;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;
import com.garganttua.core.workflow.WorkflowTimingConfig;

public interface IApiBuilder extends IDependentBuilder<IApiBuilder, IApi> {

	IApiBuilder superTenantId(String string);

	IApiStartupBinderBuilder startup(BuildingStage stage, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException;

	IApiStartupBinderBuilder startup(BuildingStage stage, Object object) throws ApiException;

	<E> IDomainBuilder<E> domain(IClass<E> entityClass) throws ApiException;

	IApiBuilder superTenantAutoCreate(boolean b) throws ApiException;

	/**
	 * Locks (default) or unlocks runtime promotion of a tenant to super-tenant.
	 * When locked, a create/update that sets {@code superTenant=true} for a
	 * tenant id not already registered is rejected; the super-tenant registry
	 * may then only be seeded by the startup scan and the auto-created master
	 * tenant. Demotion (super→normal) is always allowed regardless of this lock.
	 */
	IApiBuilder lockSuperTenantCreation(boolean lock);

	/** Owner-side counterpart of {@link #lockSuperTenantCreation(boolean)}. Locked by default. */
	IApiBuilder lockSuperOwnerCreation(boolean lock);

	IApiBuilder multiTenant(boolean enabled) throws ApiException;

	IApiSecurityBuilder security();

	IApiBuilder serializer(ISerializer serializer) throws ApiException;

	IApiBuilder serializer(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException;

	IApiBuilder protocol(IProtocol<?, ?> protocol) throws ApiException;

	IApiBuilder protocol(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException;

	IApiBuilder authorizationProtocol(IAuthorizationProtocol protocol) throws ApiException;

	IApiBuilder authorizationProtocol(ISupplierBuilder<?, ? extends ISupplier<?>> bean) throws ApiException;

	/**
	 * Opt-in: exposes the framework-provided endpoint that lists every
	 * authority enforced anywhere on the API (one entry per distinct
	 * {@link com.garganttua.api.commons.operation.OperationDefinition#effectiveAuthorityName()}
	 * across all domains).
	 *
	 * <p>Returns a sub-builder so the caller picks the access level and
	 * (optionally) the authority required to call the endpoint. The
	 * default — when {@code .exposeAuthorities().up()} is called with no
	 * further setter — is {@code Access.authenticated} with no
	 * authority gate.
	 *
	 * <p>When this method is never called, the endpoint is not exposed:
	 * {@link com.garganttua.api.commons.context.IApi#getAuthoritiesEndpoint()}
	 * returns {@code null} and
	 * {@link com.garganttua.api.commons.context.IApi#getAuthoritiesForCaller(com.garganttua.api.commons.caller.ICaller)}
	 * refuses every call with an {@link ApiException}.
	 */
	IAuthoritiesEndpointBuilder exposeAuthorities() throws ApiException;

	/**
	 * Convenience shortcut for declaring the packages to scan for entities,
	 * security annotations, and any other classpath-scanned configuration.
	 * Equivalent to calling the inherited {@code withPackage(String)} once per
	 * package, but reads cleaner at the call site:
	 *
	 * <pre>{@code
	 * ApiBuilder.builder().packages("com.myapp.entities", "com.myapp.security")
	 * }</pre>
	 */
	IApiBuilder packages(String... packageNames) throws ApiException;

	/**
	 * Registers the default {@link IDaoFactory} consulted at build time for any
	 * dto that did not configure a DAO via {@code .db(...)}. An explicit
	 * {@code .db(...)} always wins; the factory is only asked when none is set.
	 *
	 * <p>This is the hook a persistence starter (e.g. the MongoDB starter) uses
	 * to make every annotation-scanned domain persistable with no DSL — see
	 * {@link com.garganttua.api.commons.starter.IApiAutoConfiguration}.
	 */
	IApiBuilder defaultDao(IDaoFactory factory) throws ApiException;

	/**
	 * Registers the default HTTP interface attached to every domain that did not
	 * declare one via {@code .interfasse(...)}. A single shared supplier can
	 * serve all domains (the interface's {@code handle(domain)} is per-domain
	 * and its {@code onStart()} is idempotent).
	 *
	 * <p>This is the hook a transport starter (e.g. the Javalin starter) uses to
	 * expose every scanned domain over HTTP with no DSL — see
	 * {@link com.garganttua.api.commons.starter.IApiAutoConfiguration}.
	 */
	IApiBuilder defaultInterface(
			ISupplierBuilder<? extends IInterface, ? extends ISupplier<? extends IInterface>> iface)
			throws ApiException;

	/**
	 * Toggles the auto-inclusion of the framework's own packages
	 * ({@code com.garganttua.api}, {@code com.garganttua.core}) in the scan
	 * surface. Default: {@code true}.
	 *
	 * <p>Auto-inclusion makes any annotation-driven asset shipped by the
	 * framework itself (built-in protocols, serializers, security primitives,
	 * etc.) discoverable without the user repeating those package names in
	 * every {@code ApiBuilder.builder()} call.
	 *
	 * <p>Set to {@code false} for hermetic setups — tests that assert "no
	 * built-in asset leaked in", or apps that ship a strict override of a
	 * framework-provided implementation and want the scanner to ignore the
	 * original. The framework packages are injected lazily by
	 * {@code doAutoDetection()}, so calling this with {@code false} any time
	 * before {@code build()} still excludes them.
	 */
	IApiBuilder includeFrameworkPackages(boolean include) throws ApiException;

	/**
	 * Enables workflow execution timing for every domain workflow assembled by
	 * the API. The supplied {@link WorkflowTimingConfig} is forwarded to each
	 * per-domain {@code IWorkflowBuilder.timing(...)} at assembly time, so the
	 * generated workflow scripts emit {@code observe("start"|"end", ...)}
	 * markers for the stages and/or scripts the config enables.
	 *
	 * <p>Those markers fire {@code StartEvent}/{@code EndEvent} on the workflow's
	 * own observable registry with {@code source="stage:<name>"} (when
	 * {@link WorkflowTimingConfig#stages(boolean) stages(true)}) and
	 * {@code source="script:<stage>.<name>"} (when
	 * {@link WorkflowTimingConfig#scripts(boolean) scripts(true)}). For a scanned
	 * {@code @Observer} to receive them, the API also attaches every domain's
	 * workflow as an observability source at {@code build()} time.
	 *
	 * <p>Default: {@link WorkflowTimingConfig#disabled()} — no markers are
	 * generated and the produced script is byte-identical to a build that never
	 * calls this method, so timing has zero overhead until opted into.
	 *
	 * <pre>{@code
	 * ApiBuilder.builder()
	 *     .workflowTiming(WorkflowTimingConfig.of().stages(true).scripts(true))
	 *     ...
	 * }</pre>
	 *
	 * @param config the timing configuration to forward to every domain
	 *               workflow; never {@code null}
	 */
	IApiBuilder workflowTiming(WorkflowTimingConfig config) throws ApiException;

}
