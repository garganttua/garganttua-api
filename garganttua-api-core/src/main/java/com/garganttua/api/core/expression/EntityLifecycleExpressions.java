package com.garganttua.api.core.expression;
import com.garganttua.core.reflection.annotations.Reflected;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.javatuples.Pair;

import com.garganttua.api.core.domain.Domain;
import com.garganttua.api.core.entity.EntityUpdater;
import com.garganttua.api.core.entity.EntityDefinition;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IDomain;
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

			if (afterGetBinders == null || afterGetBinders.isEmpty()) return entityList;

			for (IMethodBinder<Void> binder : afterGetBinders) {
				ObjectAddress methodRef = new ObjectAddress(binder.getExecutableReference());
				for (Object entity : entityList) {
					REFLECTION.invokeDeep(entity, methodRef, IClass.getClass(Void.class));
				}
			}
		} catch (Exception e) {
			throw new ApiException("Failed to execute afterGet lifecycle hooks", e);
		}
		return entityList;
	}

	@Expression(name = "ensureUuid", description = "Generates a UUID for the entity if the uuid field is null")
	public static Object ensureUuid(Object entity, Object context) {
		try {
			IDomain<?> dc = toDomain(context);
			ObjectAddress uuidAddress = dc.getEntityDefinition().uuid();
			String fieldName = uuidAddress.toString();
			Object currentUuid = REFLECTION.getFieldValue(entity, fieldName);
			if (currentUuid == null) {
				REFLECTION.setFieldValue(entity, fieldName, UuidCreator.getTimeOrderedEpoch().toString());
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
			if (tenantIdAddress == null) return entity;
			String fieldName = tenantIdAddress.toString();
			Object currentTenantId = REFLECTION.getFieldValue(entity, fieldName);
			if (currentTenantId == null) {
				REFLECTION.setFieldValue(entity, fieldName, c.requestedTenantId());
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

			if (binders == null || binders.isEmpty()) return entity;

			for (IMethodBinder<Void> binder : binders) {
				ObjectAddress methodRef = new ObjectAddress(binder.getExecutableReference());
				REFLECTION.invokeDeep(entity, methodRef, IClass.getClass(Void.class));
			}
			return entity;
		} catch (Exception e) {
			throw new ApiException("Failed to execute " + hookName + " lifecycle hooks", e);
		}
	}
}
