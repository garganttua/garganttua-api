package com.garganttua.api.core.expression;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.javatuples.Pair;

import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.entity.EntityCreator;
import com.garganttua.api.core.entity.EntityUpdater;
import com.garganttua.api.core.entity.EntityDefinition;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.core.reflection.binders.IContextualMethodBinder;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.runtime.RuntimeExpressionContext;
import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.api.commons.entity.annotations.UnicityScope;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.injection.BeanDefinition;
import com.garganttua.core.injection.context.beans.BeanFactory;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.github.f4b6a3.uuid.UuidCreator;

import static com.garganttua.api.core.expression.ExpressionUtils.*;

/**
 * Expressions for entity lifecycle: field management, validation, lifecycle hooks, and DI injection.
 */
@Reflected(queryAllPublicMethods = true)
public class EntityLifecycleExpressions {

	@Expression(name = "doInjection", description = "Injects @Inject and @Property fields into entities using BeanFactory (no @PostConstruct)")
	public static List<Object> doInjection(Object request, Object entities) throws ApiException {
		if (entities == null) return List.of();
		List<Object> entityList = (List<Object>) entities;
		if (entityList.isEmpty()) return entityList;

		IOperationRequest opRequest = (IOperationRequest) request;
		Domain<?> dc = (Domain<?>) opRequest.arg(IOperationRequest.DOMAIN_CONTEXT).orElse(null);
		if (dc == null || !dc.isDoInjection()) return entityList;

		BeanDefinition<?> entityBeanDefinition = dc.getEntityBeanDefinition();
		if (entityBeanDefinition == null) return entityList;

		BeanDefinition<Object> beanDef = (BeanDefinition<Object>) entityBeanDefinition;
		BeanDefinition<Object> injectionOnlyDef = new BeanDefinition<>(
				beanDef.reference(), beanDef.constructorBinder(),
				Set.of(), beanDef.injectableFields());

		for (Object entity : entityList) {
			new BeanFactory<>(injectionOnlyDef, entity).supply();
		}
		return entityList;
	}

	@Expression(name = "runAfterGet", description = "Executes @EntityGotFromRepository lifecycle hooks on entities")
	public static List<Object> runAfterGet(Object entities, Object request) throws ApiException {
		if (entities == null) return List.of();
		List<Object> entityList = (List<Object>) entities;
		if (entityList.isEmpty()) return entityList;

		try {
			IOperationRequest opRequest = (IOperationRequest) request;
			IDomain<?> dc = opRequest.arg(IOperationRequest.DOMAIN_CONTEXT).orElse(null);
			EntityDefinition<?> entityDef = (EntityDefinition<?>) dc.getEntityDefinition();
			List<IMethodBinder<Void>> afterGetBinders = entityDef.afterGetMethodBuilders();

			if (afterGetBinders != null) {
				for (IMethodBinder<Void> binder : afterGetBinders) {
					ObjectAddress methodRef = new ObjectAddress(binder.getExecutableReference());
					for (Object entity : entityList) {
						REFLECTION.invokeDeep(entity, methodRef, IClass.getClass(Void.class));
					}
				}
			}
			// Free afterGet hooks (bound to an exact, possibly external method) — executed with the
			// current entity + injected context.
			runFreeHooks(entityDef, "afterGet", entityList, dc, opRequest);
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to execute afterGet lifecycle hooks", e);
		}
		return entityList;
	}

	@Expression(name = "ensureUuid", description = "Assigns the entity's uuid at creation: generates one (a time-ordered UUID v7 by default, or the domain's custom uuidGenerator) when the client sent none, OR always when the domain declares overwriteUuid(true) — discarding any client-supplied value.")
	public static Object ensureUuid(Object entity, Object context) {
		try {
			IDomain<?> dc = toDomain(context);
			com.garganttua.api.commons.definition.IEntityDefinition<?> entityDef = dc.getEntityDefinition();
			ObjectAddress uuidAddress = entityDef.uuid();
			String fieldName = uuidAddress.toString();
			Object currentUuid = REFLECTION.getFieldValue(entity, fieldName);
			if (currentUuid == null || entityDef.overwriteUuid()) {
				com.garganttua.api.commons.entity.IUuidGenerator generator = entityDef.uuidGenerator();
				String uuid = generator != null
						? generator.generate(entity)
						: UuidCreator.getTimeOrderedEpoch().toString();
				REFLECTION.setFieldValue(entity, fieldName, uuid);
			}
			return entity;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to ensure UUID on entity", e);
		}
	}

	@Expression(name = "ensureTenantId", description = "Sets the tenantId on the entity from the caller if not already set")
	public static Object ensureTenantId(Object entity, Object caller, Object context) {
		try {
			ICaller c = (ICaller) unwrapOptional(caller);
			IDomain<?> dc = toDomain(context);
			ObjectAddress tenantIdAddress = dc.getEntityDefinition().tenantId();
			if (tenantIdAddress == null || c == null) return entity;
			String fieldName = tenantIdAddress.toString();
			Object currentTenantId = REFLECTION.getFieldValue(entity, fieldName);
			if (currentTenantId == null) {
				// Stamp the caller's REQUESTED tenant — a super tenant may target another tenant via the
				// request (cross-tenant create). When none was requested, fall back to the caller's HOME
				// tenant: requestedTenantId is null for an unscoped super tenant (the read-side "all
				// tenants" bypass signal), and that null must NOT leak into the persisted tenantId and
				// orphan the entity. For a non-super caller requestedTenantId already equals tenantId,
				// so this fallback only changes the super-tenant-without-target case.
				String target = c.requestedTenantId() != null ? c.requestedTenantId() : c.tenantId();
				REFLECTION.setFieldValue(entity, fieldName, target);
			}
			return entity;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to ensure tenantId on entity", e);
		}
	}

	@Expression(name = "ensureOwnerId",
			description = "On an owned domain, sets the owned/ownerId field from the caller's ownerId when not already set. No-op for non-owned domains, super-owner/anonymous callers (no ownerId), or a field the caller already populated. The caller's ownerId already carries the qualified ${domainName}:${id} form, so the stored value stays consistent with the repository owner filter.")
	public static Object ensureOwnerId(Object entity, Object caller, Object context) {
		try {
			IDomain<?> dc = toDomain(context);
			ObjectAddress ownedAddress = dc.getDomainDefinition().owned();
			if (ownedAddress == null) {
				return entity;
			}
			ICaller c = (ICaller) unwrapOptional(caller);
			if (c == null || c.ownerId() == null) {
				return entity;
			}
			String fieldName = ownedAddress.toString();
			Object currentOwnerId = REFLECTION.getFieldValue(entity, fieldName);
			if (currentOwnerId == null) {
				REFLECTION.setFieldValue(entity, fieldName, c.ownerId());
			}
			return entity;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to ensure ownerId on entity", e);
		}
	}

	@Expression(name = "validateMandatories", description = "Validates that all @EntityMandatory fields are non-null")
	public static void validateMandatories(Object entity, Object context) {
		try {
			IDomain<?> dc = toDomain(context);
			EntityDefinition<?> entityDef = (EntityDefinition<?>) dc.getEntityDefinition();
			List<ObjectAddress> mandatories = entityDef.mandatories();
			if (mandatories == null || mandatories.isEmpty()) return;

			for (ObjectAddress address : mandatories) {
				Object value = REFLECTION.getFieldValue(entity, address.toString());
				if (value == null) {
					throw new ApiException("Mandatory field '" + address + "' is null");
				}
			}
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to validate mandatory fields", e);
		}
	}

	@Expression(name = "validateUnicity", description = "Checks unicity constraints against existing entities in repository")
	public static void validateUnicity(Object entity, Object repository, Object context) throws ApiException {
		try {
			IDomain<?> dc = toDomain(context);
			EntityDefinition<?> entityDef = (EntityDefinition<?>) dc.getEntityDefinition();
			List<Pair<ObjectAddress, UnicityScope>> unicities = entityDef.unicities();
			if (unicities == null || unicities.isEmpty()) return;

			IRepository repo = (IRepository) repository;
			ObjectAddress tenantIdAddress = entityDef.tenantId();
			ObjectAddress uuidAddress = entityDef.uuid();

			Object currentUuid = uuidAddress != null
					? REFLECTION.getFieldValue(entity, uuidAddress.toString())
					: null;

			for (Pair<ObjectAddress, UnicityScope> unicity : unicities) {
				ObjectAddress fieldAddress = unicity.getValue0();
				UnicityScope scope = unicity.getValue1();
				Object fieldValue = REFLECTION.getFieldValue(entity, fieldAddress.toString());
				if (fieldValue == null) continue;

				Filter fieldFilter = Filter.eq(fieldAddress.toString(), fieldValue);

				IFilter queryFilter;
				if (scope == UnicityScope.tenant && tenantIdAddress != null) {
					Object tenantId = REFLECTION.getFieldValue(entity, tenantIdAddress.toString());
					if (tenantId != null) {
						Filter tenantFilter = Filter.eq(tenantIdAddress.toString(), tenantId);
						queryFilter = Filter.and(fieldFilter, tenantFilter);
					} else {
						queryFilter = fieldFilter;
					}
				} else {
					queryFilter = fieldFilter;
				}

				if (currentUuid != null && uuidAddress != null) {
					Filter excludeSelf = Filter.ne(uuidAddress.toString(), currentUuid);
					queryFilter = Filter.and((Filter) queryFilter, excludeSelf);
				}

				List<Object> existing = repo.getEntities(Optional.empty(), Optional.of(queryFilter), Optional.empty());
				if (!existing.isEmpty()) {
					throw new ApiException("Unicity constraint violated for field '" + fieldAddress + "'");
				}
			}
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to validate unicity constraints", e);
		}
	}

	@Expression(name = "runBeforeCreate", description = "Executes @EntityBeforeCreate lifecycle hooks on entity")
	public static Object runBeforeCreate(Object entity, Object request) {
		return runLifecycleHooks(entity, request, "beforeCreate",
				ed -> ((EntityDefinition<?>) ed).beforeCreateMethodBuilders());
	}

	@Expression(name = "runAfterCreate", description = "Executes @EntityAfterCreate lifecycle hooks on entity")
	public static Object runAfterCreate(Object entity, Object request) {
		return runLifecycleHooks(entity, request, "afterCreate",
				ed -> ((EntityDefinition<?>) ed).afterCreateMethodBuilders());
	}

	@Expression(name = "createEntity", description = "Strips fields the caller is not authorized to valorize at creation (create-time field whitelist; no-op when no .create(...) is declared, or for framework-internal/bootstrap writes)")
	public static Object createEntity(Object caller, Object entity, Object context, Object request) {
		Object e = unwrapOptional(entity);
		// Framework-internal / bootstrap writes (seed, startup upsert, token/key persist) are
		// server-orchestrated and trusted: they bypass the create whitelist, exactly as
		// requireNotDirectAuthorizationCreate does. Otherwise a seeded entity with a fixed
		// uuid/tenantId, or a guarded field (e.g. a super-owner admin), would be stripped because
		// the bootstrap caller carries no authorities. The marker is server-set only, never read
		// from the wire (see SecurityExpressions.FRAMEWORK_INTERNAL_WRITE_ARG), so it is unforgeable.
		IOperationRequest req = (unwrapOptional(request) instanceof IOperationRequest r) ? r : null;
		if (req != null
				&& Boolean.TRUE.equals(req.arg(SecurityExpressions.FRAMEWORK_INTERNAL_WRITE_ARG).orElse(null))) {
			return e;
		}
		ICaller c = (ICaller) unwrapOptional(caller);
		IDomain<?> dc = toDomain(context);
		EntityDefinition<?> entityDef = (EntityDefinition<?>) dc.getEntityDefinition();
		return new EntityCreator().create(c, e, entityDef.creates());
	}

	@Expression(name = "updateEntity", description = "Applies authorized field updates from updatedEntity onto storedEntity")
	public static Object updateEntity(Object caller, Object storedEntity, Object updatedEntity, Object context) {
		ICaller c = (ICaller) unwrapOptional(caller);
		IDomain<?> dc = toDomain(context);
		EntityDefinition<?> entityDef = (EntityDefinition<?>) dc.getEntityDefinition();
		return new EntityUpdater().update(c, storedEntity, updatedEntity, entityDef.updates());
	}

	@Expression(name = "runBeforeUpdate", description = "Executes @EntityBeforeUpdate lifecycle hooks on entity")
	public static Object runBeforeUpdate(Object entity, Object request) {
		return runLifecycleHooks(entity, request, "beforeUpdate",
				ed -> ((EntityDefinition<?>) ed).beforeUpdateMethodBuilders());
	}

	@Expression(name = "runAfterUpdate", description = "Executes @EntityAfterUpdate lifecycle hooks on entity")
	public static Object runAfterUpdate(Object entity, Object request) {
		return runLifecycleHooks(entity, request, "afterUpdate",
				ed -> ((EntityDefinition<?>) ed).afterUpdateMethodBuilders());
	}

	@Expression(name = "runBeforeDelete", description = "Executes @EntityBeforeDelete lifecycle hooks on entities")
	public static List<Object> runBeforeDelete(Object entities, Object request) {
		return runListLifecycleHooks(entities, request, "beforeDelete",
				ed -> ((EntityDefinition<?>) ed).beforeDeleteMethodBuilders());
	}

	@Expression(name = "runAfterDelete", description = "Executes @EntityAfterDelete lifecycle hooks on entities")
	public static List<Object> runAfterDelete(Object entities, Object request) {
		return runListLifecycleHooks(entities, request, "afterDelete",
				ed -> ((EntityDefinition<?>) ed).afterDeleteMethodBuilders());
	}

	private static List<Object> runListLifecycleHooks(Object entities, Object request, String hookName,
			java.util.function.Function<IEntityDefinition<?>, List<IMethodBinder<Void>>> bindersExtractor) {
		if (entities == null) return List.of();
		List<Object> entityList = (List<Object>) entities;
		if (entityList.isEmpty()) return entityList;

		try {
			IOperationRequest opRequest = (IOperationRequest) request;
			IDomain<?> dc = opRequest.arg(IOperationRequest.DOMAIN_CONTEXT).orElse(null);
			EntityDefinition<?> entityDef = (EntityDefinition<?>) dc.getEntityDefinition();
			List<IMethodBinder<Void>> binders = bindersExtractor.apply(entityDef);

			if (binders == null || binders.isEmpty()) return entityList;

			for (IMethodBinder<Void> binder : binders) {
				ObjectAddress methodRef = new ObjectAddress(binder.getExecutableReference());
				for (Object entity : entityList) {
					REFLECTION.invokeDeep(entity, methodRef, IClass.getClass(Void.class));
				}
			}
		} catch (Exception e) {
			throw new ApiException("Failed to execute " + hookName + " lifecycle hooks", e);
		}
		return entityList;
	}

	private static Object runLifecycleHooks(Object entity, Object request, String hookName,
			java.util.function.Function<IEntityDefinition<?>, List<IMethodBinder<Void>>> bindersExtractor) {
		try {
			IOperationRequest opRequest = (IOperationRequest) request;
			IDomain<?> dc = opRequest.arg(IOperationRequest.DOMAIN_CONTEXT).orElse(null);
			IEntityDefinition<?> entityDef = dc.getEntityDefinition();
			List<IMethodBinder<Void>> binders = bindersExtractor.apply(entityDef);

			if (binders != null) {
				for (IMethodBinder<Void> binder : binders) {
					ObjectAddress methodRef = new ObjectAddress(binder.getExecutableReference());
					REFLECTION.invokeDeep(entity, methodRef, IClass.getClass(Void.class));
				}
			}
			// Free hooks (bound to an exact, possibly external method) — executed with the current
			// entity + injected context. A thrown ApiException (validation) propagates as-is.
			runFreeHooks(entityDef, hookName, entity != null ? List.of(entity) : List.of(), dc, opRequest);
			return entity;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to execute " + hookName + " lifecycle hooks", e);
		}
	}

	/**
	 * Executes the domain's free lifecycle-hook binders for {@code hookName}: each bound method
	 * (possibly external/static) receives the current entity + injected framework context. Mirrors
	 * {@code applySecurityOnEntity} — seeds the runtime variables, then runs the contextual binder.
	 */
	@SuppressWarnings("unchecked")
	private static void runFreeHooks(IEntityDefinition<?> entityDef, String hookName, List<Object> entities,
			IDomain<?> dc, IOperationRequest opRequest) {
		if (!(entityDef instanceof EntityDefinition<?> ed)) {
			return;
		}
		Map<String, List<IMethodBinder<?>>> freeBinders = ed.freeHookBinders();
		if (freeBinders == null) {
			return;
		}
		List<IMethodBinder<?>> binders = freeBinders.get(hookName);
		if (binders == null || binders.isEmpty()) {
			return;
		}
		IApi api = (dc != null) ? dc.getApiContext() : null;
		for (Object entity : entities) {
			for (IMethodBinder<?> binder : binders) {
				IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
				if (runtimeCtx != null) {
					runtimeCtx.setVariable("entity", entity);
					if (dc != null) {
						runtimeCtx.setVariable("domainContext", dc);
					}
					if (opRequest != null) {
						runtimeCtx.setVariable("request", opRequest);
					}
					if (api != null) {
						runtimeCtx.setVariable("apiContext", api);
					}
				}
				Optional<? extends com.garganttua.core.reflection.IMethodReturn<?>> result;
				if (binder instanceof IContextualMethodBinder<?, ?> contextualBinder) {
					result = ((IContextualMethodBinder<?, Object>) contextualBinder).execute(runtimeCtx);
				} else {
					result = binder.execute();
				}
				// Surface any exception the hook threw (e.g. a validation rejection): the binder CAPTURES
				// it in the result (invokeMethodSafely) rather than throwing it from execute(). The return
				// value itself is ignored (lifecycle hooks are void).
				if (result.isPresent() && result.get().hasException()) {
					Throwable hookError = result.get().getException();
					if (hookError instanceof RuntimeException re) {
						throw re; // ApiException et al. propagate as-is (parlant message kept)
					}
					if (hookError instanceof Error err) {
						throw err;
					}
					throw new ApiException("Hook '" + hookName + "' threw: " + hookError.getMessage(), hookError);
				}
			}
		}
	}
}
