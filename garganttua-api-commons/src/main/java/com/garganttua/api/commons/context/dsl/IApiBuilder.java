package com.garganttua.api.commons.context.dsl;

import com.garganttua.api.commons.context.BuildingStage;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.dsl.security.IApiSecurityBuilder;
import com.garganttua.api.commons.protocol.IProtocol;
import com.garganttua.api.commons.security.authorization.IAuthorizationProtocol;
import com.garganttua.api.commons.serialization.ISerializer;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.bootstrap.dsl.IBoostrap;
import com.garganttua.core.dsl.dependency.IDependentBuilder;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.supply.ISupplier;
import com.garganttua.core.supply.dsl.ISupplierBuilder;

public interface IApiBuilder extends IDependentBuilder<IApiBuilder, IApi> {

	IApiBuilder superTenantId(String string);

	IApiStartupBinderBuilder startup(BuildingStage stage, ISupplierBuilder<?, ? extends ISupplier<?>> supplier) throws ApiException;

	IApiStartupBinderBuilder startup(BuildingStage stage, Object object) throws ApiException;

	<E> IDomainBuilder<E> domain(IClass<E> entityClass) throws ApiException;

	IApiBuilder superTenantAutoCreate(boolean b) throws ApiException;

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
	 *
	 * Packages registered here propagate to every builder registered in the
	 * same {@link IBoostrap} (reflection, injection, expression…).
	 */
	IApiBuilder packages(String... packageNames) throws ApiException;

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
	 * Returns the {@link IBoostrap} that orchestrates the build of this
	 * {@code ApiBuilder}. Two cases:
	 * <ul>
	 *   <li>If the caller used {@code ApiBuilder.builder()} (the default), the
	 *       returned bootstrap is internal — a private orchestrator created
	 *       to wire reflection / injection / expression with sensible defaults.
	 *       Use this accessor to register additional builders (e.g. an
	 *       observer source) in the same orchestration unit.</li>
	 *   <li>If {@link #intoBootstrap(IBoostrap)} was called, returns the
	 *       external bootstrap.</li>
	 * </ul>
	 */
	IBoostrap bootstrap();

	/**
	 * Detaches this builder from its internal bootstrap and registers it in
	 * the supplied external one. Use when a single application orchestrates
	 * multiple garganttua frameworks (api + events + …) under a shared
	 * bootstrap:
	 *
	 * <pre>{@code
	 * IBoostrap shared = Bootstrap.builder().autoDetect(true);
	 * IApiBuilder api = ApiBuilder.builder().intoBootstrap(shared);
	 * IEventsBuilder events = EventsBuilder.builder().intoBootstrap(shared);
	 * shared.build();   // one orchestrated build for both
	 * }</pre>
	 *
	 * After this call, {@link #bootstrap()} returns the external instance and
	 * {@code build()} no longer triggers a private bootstrap build — the
	 * caller is expected to drive {@code shared.build()} (or call
	 * {@code build()} on any registered builder, which transitively drives
	 * the shared bootstrap thanks to its built-result caching).
	 */
	IApiBuilder intoBootstrap(IBoostrap external) throws ApiException;

}
