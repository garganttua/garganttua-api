package com.garganttua.api.core.expression;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.javatuples.Pair;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.context.Domain;
import com.garganttua.api.core.context.EntityUpdater;
import com.garganttua.api.core.definition.DomainDefinition;

import com.garganttua.api.core.definition.EntityDefinition;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.repository.RepositoryFilterTools;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IApi;
import com.garganttua.api.spec.context.IDomain;
import com.garganttua.api.spec.definition.IAuthenticationDefinition;
import com.garganttua.api.spec.definition.IAuthenticatorDefinition;
import com.garganttua.api.spec.definition.IDomainAuthorizationDefinition;
import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.entity.annotations.UnicityScope;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.operation.Access;
import com.garganttua.api.spec.operation.OperationDefinition;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.security.authentication.IAuthentication;
import com.garganttua.api.spec.security.authentication.IAuthenticationRequest;
import com.garganttua.api.spec.security.authorization.IAuthorization;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.service.Page;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.expression.context.ExpressionVariableContext;
import com.garganttua.core.expression.context.IExpressionVariableResolver;
import com.garganttua.core.injection.BeanDefinition;
import com.garganttua.core.injection.context.beans.BeanFactory;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IMethodReturn;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IContextualMethodBinder;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.runtime.IRuntimeContext;
import com.garganttua.core.runtime.RuntimeExpressionContext;
import com.garganttua.core.script.IScript;
import com.garganttua.core.script.context.ScriptContext;
import com.garganttua.core.script.context.ScriptExecutionContext;
import com.github.f4b6a3.uuid.UuidCreator;

import jakarta.annotation.Nullable;

public class ApiExpressions {

	private static final IReflection REFLECTION = DefaultMapper.reflection();

	@Expression(name = "businessOperation", description = "Extracts the business operation label from an IOperationRequest")
	public static String businessOperation(Object request) {
		IOperationRequest opRequest = (IOperationRequest) request;
		OperationDefinition opDef = opRequest.operation();
		if (opDef == null) return null;
		return opDef.getBusinessOperation().getLabel();
	}

	@Expression(name = "script_output", description = "Gets the output of an executed included script by name")
	public static Object scriptOutput(@Nullable Object scriptName) {
		var ctx = ScriptExecutionContext.get();
		if (ctx == null) {
			throw new ApiException("script_output: no script execution context available");
		}
		IScript script = ctx.getIncludedScript(scriptName.toString());
		if (script == null) {
			throw new ApiException("script_output: script not found: " + scriptName);
		}
		return script.getOutput().orElse(null);
	}

	/**
	 * Wraps execute_script to save and restore the ExpressionVariableContext
	 * which gets cleared by the sub-script's ScriptRuntimeStep finally block.
	 * Without this, any @variable reference after execute_script would fail
	 * with "No variable resolver available".
	 */
	private static int runScriptImpl(Object name, Object... args) {
		if (name == null) {
			throw new ApiException("run_script: script name cannot be null");
		}
		ScriptContext ctx = ScriptExecutionContext.get();
		if (ctx == null) {
			throw new ApiException("run_script: no script execution context available");
		}
		String scriptName = name.toString();
		IScript script = ctx.getIncludedScript(scriptName);
		if (script == null) {
			throw new ApiException("run_script: script not found: " + scriptName
					+ ". Did you call include() first?");
		}
		IExpressionVariableResolver savedResolver = ExpressionVariableContext.get();
		IRuntimeContext<?, ?> savedRuntimeCtx = RuntimeExpressionContext.get();
		try {
			return script.execute(args != null ? args : new Object[0]);
		} finally {
			if (savedResolver != null) {
				ExpressionVariableContext.set(savedResolver);
			}
			if (savedRuntimeCtx != null) {
				RuntimeExpressionContext.set(savedRuntimeCtx);
			}
		}
	}

	@Expression(name = "run_script", description = "Executes an included script preserving variable context (no args)")
	public static int runScript(@Nullable Object name) {
		return runScriptImpl(name);
	}

	@Expression(name = "run_script", description = "Executes an included script preserving variable context (1 arg)")
	public static int runScript(@Nullable Object name, @Nullable Object arg0) {
		return runScriptImpl(name, arg0);
	}

	@Expression(name = "run_script", description = "Executes an included script preserving variable context (2 args)")
	public static int runScript(@Nullable Object name, @Nullable Object arg0, @Nullable Object arg1) {
		return runScriptImpl(name, arg0, arg1);
	}

	@Expression(name = "run_script", description = "Executes an included script preserving variable context (3 args)")
	public static int runScript(@Nullable Object name, @Nullable Object arg0, @Nullable Object arg1, @Nullable Object arg2) {
		return runScriptImpl(name, arg0, arg1, arg2);
	}

	@Expression(name = "notNull", description = "Returns true if the value is not null and not an empty Optional")
	public static boolean notNull(@Nullable Object value) {
		if (value == null) return false;
		if (value instanceof Optional<?> opt) return opt.isPresent();
		return true;
	}

	@Expression(name = "requirePresent", description = "Throws ApiException if value is null or an empty Optional, otherwise returns the unwrapped value")
	public static Object requirePresent(@Nullable Object value) throws ApiException {
		if (value == null) throw new ApiException("Required value is null");
		if (value instanceof Optional<?> opt) {
			return opt.orElseThrow(() -> new ApiException("Required value is empty"));
		}
		return value;
	}

	@Expression(name = "and", description = "Logical AND of two boolean values")
	public static boolean andExpr(boolean a, boolean b) {
		return a && b;
	}

	@Expression(name = "equals", description = "Returns true when both arguments are equal")
	public static boolean equalsExpr(@Nullable Object a, @Nullable Object b) {
		return Objects.equals(unwrapOptional(a), unwrapOptional(b));
	}

	@Expression(name = "equals", description = "Returns true when both arguments are equal (boolean variant)")
	public static boolean equalsExprBool(@Nullable Object a, boolean b) {
		return Objects.equals(unwrapOptional(a), b);
	}

	@Expression(name = "optionalGet", description = "Unwraps an Optional, throwing NoSuchElementException if empty")
	public static Object optionalGet(@Nullable Object value) {
		if (value instanceof Optional<?> opt) {
			return opt.get();
		}
		return value;
	}

	@Expression(name = "buildFilter", description = "Builds access filter from caller permissions and domain definition")
	public static Optional<IFilter> buildFilter(Object caller, Object filter, Object context) {
		Optional<ICaller> castedCaller = (Optional<ICaller>) caller;
		IDomain<?> dc = context instanceof Optional<?> opt
				? (IDomain<?>) opt.get()
				: (IDomain<?>) context;
		IDomainDefinition<?> domainDef = dc.getDomainDefinition();
		Optional<IFilter> baseFilter = (Optional<IFilter>) filter;
		return Optional.ofNullable(
				RepositoryFilterTools.buildFilter(castedCaller.orElse(null), baseFilter.orElse(null), domainDef, dc.isMultiTenant()));
	}

	@Expression(name = "getEntities", description = "Retrieves entities from repository with pagination, filtering and sorting")
	public static List<Object> getEntities(Object repository, Object pageable, Object filter, Object sort)
			throws ApiException {
		IRepository repo = (IRepository) repository;
		return repo.getEntities(
				unwrap(pageable, IPageable.class),
				unwrap(filter, IFilter.class),
				unwrap(sort, ISort.class));
	}

	@Expression(name = "saveEntity", description = "Saves an entity to the repository")
	public static void saveEntity(Object repository, Object entity) throws ApiException {
		IRepository repo = (IRepository) repository;
		repo.save(entity);
	}

	@Expression(name = "deleteEntity", description = "Deletes an entity from the repository")
	public static void deleteEntity(Object repository, Object entity) throws ApiException {
		IRepository repo = (IRepository) repository;
		repo.delete(entity);
	}

	@Expression(name = "deleteEntities", description = "Deletes a list of entities from the repository")
	public static void deleteEntities(Object repository, Object entities) throws ApiException {
		IRepository repo = (IRepository) repository;
		List<Object> entityList = (List<Object>) entities;
		for (Object entity : entityList) {
			repo.delete(entity);
		}
	}

	@Expression(name = "doesExist", description = "Checks whether an entity exists in the repository")
	public static boolean doesExist(Object repository, Object entityOrUuid) throws ApiException {
		IRepository repo = (IRepository) repository;
		if (entityOrUuid instanceof String uuid) {
			return repo.doesExist(uuid);
		}
		return repo.doesExist(entityOrUuid);
	}

	@Expression(name = "getCount", description = "Returns the count of entities matching the given filter")
	public static long getCount(Object repository, Object filter) throws ApiException {
		IRepository repo = (IRepository) repository;
		IFilter f = filter instanceof Optional<?> opt ? (IFilter) opt.orElse(null) : (IFilter) filter;
		return repo.getCount(f);
	}

	@Expression(name = "getContext", description = "Retrieve a domain context by name from the API context")
	public static IDomain<?> getContext(Object request, String domainName) {
		IOperationRequest opRequest = (IOperationRequest) request;
		IApi apiContext = opRequest.arg(IOperationRequest.API_CONTEXT).orElse(null);
		if (apiContext == null) {
			throw new ApiException("No API context available in request");
		}
		return apiContext.getDomain(domainName)
				.orElseThrow(() -> new ApiException("Domain not found: " + domainName));
	}

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
		// Strip postConstruct — entities from repository are already created
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

	@Expression(name = "reduceToUuids", description = "Extracts uuid field from each entity, returning a list of uuid strings")
	public static List<Object> reduceToUuids(Object entities, Object context) {
		return reduceToField(entities, context, true);
	}

	@Expression(name = "reduceToIds", description = "Extracts id field from each entity, returning a list of id strings")
	public static List<Object> reduceToIds(Object entities, Object context) {
		return reduceToField(entities, context, false);
	}

	private static List<Object> reduceToField(Object entities, Object context, boolean uuid) {
		if (entities == null) return List.of();
		List<Object> entityList = (List<Object>) entities;
		if (entityList.isEmpty()) return entityList;

		IDomain<?> dc = context instanceof Optional<?> opt
				? (IDomain<?>) opt.get()
				: (IDomain<?>) context;
		ObjectAddress address = uuid
				? dc.getEntityDefinition().uuid()
				: dc.getEntityDefinition().id();

		return entityList.stream()
				.map(entity -> {
					try {
						return REFLECTION.getFieldValue(entity, address.toString());
					} catch (Exception e) {
						throw new ApiException("Failed to read field " + address + " from entity", e);
					}
				})
				.filter(Objects::nonNull)
				.toList();
	}

	@Expression(name = "encapsulateInPage", description = "Wraps a list of entities into a Page record with totalCount")
	public static Page encapsulateInPage(Object entities, Object totalCount) {
		List<Object> entityList = entities != null ? (List<Object>) entities : List.of();
		long count = totalCount instanceof Number n ? n.longValue() : 0L;
		return new Page(count, entityList);
	}

	@Expression(name = "first", description = "Returns the first element of a list, or throws ApiException if the list is empty")
	public static Object first(Object list) throws ApiException {
		if (list == null) throw new ApiException("Cannot get first element of null list");
		List<Object> entityList = (List<Object>) list;
		if (entityList.isEmpty()) throw new ApiException("List is empty, no element to return");
		return entityList.get(0);
	}

	@Expression(name = "asList", description = "Wraps a single object into a singleton list")
	public static List<Object> asList(Object value) {
		if (value instanceof Optional<?> opt) {
			Object unwrapped = opt.orElse(null);
			if (unwrapped == null) return List.of();
			return List.of(unwrapped);
		}
		if (value == null) return List.of();
		return List.of(value);
	}

	@Expression(name = "buildGetOneFilter", description = "Builds a filter for single entity lookup by uuid or id, combined with access control filter")
	public static Optional<IFilter> buildGetOneFilter(Object caller, Object type, Object identifier, Object context) {
		Optional<ICaller> castedCaller = (Optional<ICaller>) caller;
		String typeStr = unwrapOptional(type) != null ? unwrapOptional(type).toString() : "uuid";
		String identifierStr = unwrapOptional(identifier) != null ? unwrapOptional(identifier).toString() : null;

		IDomain<?> dc = context instanceof Optional<?> opt
				? (IDomain<?>) opt.get()
				: (IDomain<?>) context;
		IDomainDefinition<?> domainDef = dc.getDomainDefinition();

		// Build access control filter
		IFilter accessFilter = RepositoryFilterTools.buildFilter(castedCaller.orElse(null), null, domainDef, dc.isMultiTenant());

		// Build identifier filter
		IEntityDefinition<?> entityDef = dc.getEntityDefinition();
		IFilter identifierFilter;
		if ("id".equals(typeStr)) {
			identifierFilter = RepositoryFilterTools.createIdFilter(entityDef.id(), identifierStr);
		} else {
			identifierFilter = RepositoryFilterTools.createUuidFilter(entityDef.uuid(), identifierStr);
		}

		// Combine filters
		if (accessFilter != null && identifierFilter != null) {
			return Optional.of(Filter.and((Filter) accessFilter, (Filter) identifierFilter));
		}
		if (identifierFilter != null) {
			return Optional.of(identifierFilter);
		}
		if (accessFilter != null) {
			return Optional.of(accessFilter);
		}
		return Optional.empty();
	}

	@Expression(name = "ensureUuid", description = "Generates a UUID for the entity if the uuid field is null")
	public static Object ensureUuid(Object entity, Object context) {
		try {
			IDomain<?> dc = context instanceof Optional<?> opt
					? (IDomain<?>) opt.get()
					: (IDomain<?>) context;
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
			IDomain<?> dc = context instanceof Optional<?> opt
					? (IDomain<?>) opt.get()
					: (IDomain<?>) context;
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

	@Expression(name = "validateMandatories", description = "Validates that all @EntityMandatory fields are non-null")
	public static void validateMandatories(Object entity, Object context) {
		try {
			IDomain<?> dc = context instanceof Optional<?> opt
					? (IDomain<?>) opt.get()
					: (IDomain<?>) context;
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
			IDomain<?> dc = context instanceof Optional<?> opt
					? (IDomain<?>) opt.get()
					: (IDomain<?>) context;
			EntityDefinition<?> entityDef = (EntityDefinition<?>) dc.getEntityDefinition();
			List<Pair<ObjectAddress, UnicityScope>> unicities = entityDef.unicities();
			if (unicities == null || unicities.isEmpty()) return;

			IRepository repo = (IRepository) repository;
			ObjectAddress tenantIdAddress = entityDef.tenantId();
			ObjectAddress uuidAddress = entityDef.uuid();

			// Read current entity uuid to exclude self from results (needed for UPDATE)
			Object currentUuid = uuidAddress != null
					? REFLECTION.getFieldValue(entity, uuidAddress.toString())
					: null;

			for (Pair<ObjectAddress, UnicityScope> unicity : unicities) {
				ObjectAddress fieldAddress = unicity.getValue0();
				UnicityScope scope = unicity.getValue1();
				Object fieldValue = REFLECTION.getFieldValue(entity, fieldAddress.toString());
				if (fieldValue == null) continue;

				// Build filter: field = value
				Filter fieldFilter = Filter.eq(fieldAddress.toString(), fieldValue);

				// For tenant scope, also filter by tenantId
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

				// Exclude self by uuid (avoids false positive on UPDATE)
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
		IDomain<?> dc = context instanceof Optional<?> opt
				? (IDomain<?>) opt.get()
				: (IDomain<?>) context;
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

	// --- Security expressions ---

	@Expression(name = "operationAccess", description = "Returns the Access level string from an OperationDefinition")
	public static String operationAccess(@Nullable Object operation) {
		if (operation == null) return "anonymous";
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return "anonymous";
		return opDef.access() != null ? opDef.access().name() : "anonymous";
	}

	@Expression(name = "operationAuthority", description = "Returns whether the operation requires an authority check")
	public static boolean operationAuthority(Object operation) {
		if (operation == null) return false;
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return false;
		return opDef.authority();
	}

	@Expression(name = "isSecurityDisabled", description = "Returns true if the domain has security disabled")
	public static boolean isSecurityDisabled(Object context) {
		IDomain<?> dc = context instanceof Optional<?> opt
				? (IDomain<?>) opt.get()
				: (IDomain<?>) context;
		if (dc instanceof Domain<?> domCtx) {
			var domDef = (DomainDefinition<?>) domCtx.getDomainDefinition();
			return domDef.domainSecurityDefinition() == null || domDef.domainSecurityDefinition().disabled();
		}
		return true;
	}

	@Expression(name = "requireAuthentication", description = "Checks that the caller has been authenticated (authorization present in request)")
	public static boolean requireAuthentication(@Nullable Object request) {
		IOperationRequest opRequest = (IOperationRequest) request;
		Optional<IAuthorization> authorization = (Optional<IAuthorization>) opRequest.arg(IOperationRequest.AUTHORIZATION);
		if (authorization.isEmpty()) {
			throw new ApiException("Authentication required but no authorization token provided");
		}
		return true;
	}

	@Expression(name = "requireTenantId", description = "Checks that the caller has a tenantId set")
	public static boolean requireTenantId(@Nullable Object caller) {
		ICaller c = (ICaller) unwrapOptional(caller);
		if (c == null || c.requestedTenantId() == null) {
			throw new ApiException("Tenant ID is required for this operation");
		}
		return true;
	}

	@Expression(name = "requireOwnerId", description = "Checks that the caller has an ownerId set")
	public static boolean requireOwnerId(@Nullable Object caller) {
		ICaller c = (ICaller) unwrapOptional(caller);
		if (c == null || c.ownerId() == null) {
			throw new ApiException("Owner ID is required for this operation");
		}
		return true;
	}

	@Expression(name = "callerHasTenantId", description = "Returns true if the caller has a non-null requestedTenantId (safe, never throws)")
	public static boolean callerHasTenantId(@Nullable Object caller) {
		ICaller c = (ICaller) unwrapOptional(caller);
		return c != null && c.requestedTenantId() != null;
	}

	@Expression(name = "callerHasOwnerId", description = "Returns true if the caller has a non-null ownerId (safe, never throws)")
	public static boolean callerHasOwnerId(@Nullable Object caller) {
		ICaller c = (ICaller) unwrapOptional(caller);
		return c != null && c.ownerId() != null;
	}

	@Expression(name = "authRequestLogin", description = "Extracts the login from an IAuthenticationRequest (safe, never throws)")
	public static @Nullable Object authRequestLogin(@Nullable Object entity) {
		Object unwrapped = unwrapOptional(entity);
		if (unwrapped instanceof com.garganttua.api.spec.security.authentication.IAuthenticationRequest req) {
			return req.login();
		}
		return null;
	}

	@Expression(name = "authRequestHasTenantId", description = "Returns true if the IAuthenticationRequest has a non-null tenantId (safe, never throws)")
	public static boolean authRequestHasTenantId(@Nullable Object entity) {
		Object unwrapped = unwrapOptional(entity);
		if (unwrapped instanceof com.garganttua.api.spec.security.authentication.IAuthenticationRequest req) {
			return req.tenantId() != null;
		}
		return false;
	}

	@Expression(name = "isTenantIdMandatory", description = "Returns true if the operation requires a tenantId based on access level")
	public static boolean isTenantIdMandatory(Object operation, Object context) {
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return false;
		Access access = opDef.access();
		return access == Access.tenant || access == Access.owner;
	}

	@Expression(name = "isOwnerIdMandatory", description = "Returns true if the operation requires an ownerId based on access level")
	public static boolean isOwnerIdMandatory(Object operation, Object context) {
		OperationDefinition opDef = (OperationDefinition) unwrapOptional(operation);
		if (opDef == null) return false;
		return opDef.access() == Access.owner;
	}

	@Expression(name = "authenticatorContext", description = "Returns the IAuthenticatorDefinition from the domain's security definition")
	public static IAuthenticatorDefinition authenticatorContext(Object context) {
		IDomain<?> dc = context instanceof Optional<?> opt
				? (IDomain<?>) opt.get()
				: (IDomain<?>) context;
		if (dc instanceof Domain<?> domCtx) {
			var domDef = (DomainDefinition<?>) domCtx.getDomainDefinition();
			var secDef = domDef.domainSecurityDefinition();
			if (secDef != null) {
				return secDef.authenticatorDefinition();
			}
		}
		return null;
	}

	@Expression(name = "authenticatorScope", description = "Returns the authenticator scope string from IAuthenticatorDefinition")
	public static String authenticatorScope(Object authContext) {
		if (authContext instanceof IAuthenticatorDefinition def) {
			return def.scope() != null ? def.scope().name() : null;
		}
		return null;
	}

	@Expression(name = "hasAuthorizationConfig", description = "Returns true if the authenticator has an authorization definition configured")
	public static boolean hasAuthorizationConfig(@Nullable Object authContextObj) {
		if (authContextObj instanceof IAuthenticatorDefinition def) {
			return def.authorizationDefinition() != null;
		}
		return false;
	}

	@Expression(name = "authorizationDefinition", description = "Returns the IDomainAuthorizationDefinition from the domain's security definition, resolving from the linked authorization domain if needed")
	public static @Nullable Object authorizationDefinition(@Nullable Object context) {
		IDomain<?> dc = context instanceof Optional<?> opt
				? (IDomain<?>) opt.get()
				: (IDomain<?>) context;
		if (dc instanceof Domain<?> domCtx) {
			var domDef = (DomainDefinition<?>) domCtx.getDomainDefinition();
			var secDef = domDef.domainSecurityDefinition();
			if (secDef != null) {
				// Direct authorization definition on this domain
				if (secDef.authorizationDefinition() != null) {
					return secDef.authorizationDefinition();
				}
				// Follow authenticator chain via API context (already built domains)
				IDomain<?> authzDomain = resolveAuthorizationDomain(dc);
				if (authzDomain != null && authzDomain.getDomainDefinition() instanceof DomainDefinition<?> dd
						&& dd.domainSecurityDefinition() != null) {
					return dd.domainSecurityDefinition().authorizationDefinition();
				}
			}
		}
		return null;
	}

	@Expression(name = "createAuthorizationEntity", description = "Creates a new authorization entity with fields populated from authentication result, principal uuid and tenant id")
	public static Object createAuthorizationEntity(@Nullable Object authorizationDefObj,
			@Nullable Object authenticationResult, @Nullable Object domainContextObj,
			@Nullable Object principalUuid, @Nullable Object tenantId) {
		if (authorizationDefObj == null || authenticationResult == null) {
			throw new ApiException("createAuthorizationEntity: authorizationDef and authenticationResult are required");
		}

		IDomainAuthorizationDefinition authzDef = (IDomainAuthorizationDefinition) authorizationDefObj;
		IAuthentication authResult = (IAuthentication) authenticationResult;
		IDomain<?> authenticatorDomain = domainContextObj instanceof Optional<?> opt
				? (IDomain<?>) opt.get() : (IDomain<?>) domainContextObj;
		IAuthenticatorDefinition authDef = null;
		if (authenticatorDomain instanceof Domain<?> domCtx) {
			var domDef = (DomainDefinition<?>) domCtx.getDomainDefinition();
			var secDef = domDef.domainSecurityDefinition();
			if (secDef != null) {
				authDef = secDef.authenticatorDefinition();
			}
		}
		IReflection reflection = DefaultMapper.reflection();

		try {
			// Get the authorization domain from the API context via the pre-resolved domain name
			IDomain<?> authzDomain = resolveAuthorizationDomain(authenticatorDomain);
			if (authzDomain == null) {
				throw new ApiException("createAuthorizationEntity: authorization domain not configured");
			}
			if (!authzDomain.isOwnedEntity()) {
				throw new ApiException("Authorization domain '" + authzDomain.getDomainName()
						+ "' must be owned (use .owned(field) on the domain builder)");
			}

			// Instantiate the authorization entity
			Object entity = authzDomain.getEntityClass().getConstructor().newInstance();

			// Generate uuid
			ObjectAddress uuidAddress = authzDomain.getEntityDefinition().uuid();
			if (uuidAddress != null) {
				reflection.setFieldValue(entity, uuidAddress.toString(),
						UuidCreator.getTimeOrderedEpoch().toString());
			}

			// Set ownerId (uuid of the principal)
			ObjectAddress ownedField = authzDomain.getDomainDefinition().owned();
			if (ownedField != null && principalUuid != null) {
				reflection.setFieldValue(entity, ownedField, principalUuid);
			}

			// Set tenantId if multi-tenant
			ObjectAddress tenantField = authzDomain.getTenantIdFieldAddress();
			if (tenantField != null && tenantId != null) {
				reflection.setFieldValue(entity, tenantField, tenantId);
			}

			// Set authorization fields
			if (authzDef.type() != null && authResult.authorization() != null) {
				reflection.setFieldValue(entity, authzDef.type(), authResult.authorization());
			}
			if (authzDef.authorities() != null && authResult.authorities() != null) {
				reflection.setFieldValue(entity, authzDef.authorities(), authResult.authorities());
			}
			if (authzDef.creation() != null) {
				reflection.setFieldValue(entity, authzDef.creation(), java.time.Instant.now());
			}
			if (authzDef.expiration() != null && authDef.authorizationDefinition() != null) {
				var authzAuthDef = authDef.authorizationDefinition();
				if (authzAuthDef.unit() != null && authzAuthDef.duration() > 0) {
					long millis = authzAuthDef.unit().toMillis(authzAuthDef.duration());
					reflection.setFieldValue(entity, authzDef.expiration(), java.time.Instant.now().plusMillis(millis));
				}
			}
			if (authzDef.revoked() != null) {
				reflection.setFieldValue(entity, authzDef.revoked(), false);
			}

			return entity;
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Failed to create authorization entity: " + e.getMessage(), e);
		}
	}

	@Expression(name = "lookupValidAuthorization", description = "Looks up a valid (non-expired, non-revoked) authorization owned by the principal via the authorization domain's readAll workflow.")
	public static @Nullable Object lookupValidAuthorization(@Nullable Object authorizationDefObj,
			@Nullable Object domainContextObj, @Nullable Object principalUuid, @Nullable Object tenantId) {
		if (authorizationDefObj == null || domainContextObj == null || principalUuid == null) {
			return null;
		}
		try {
			IDomainAuthorizationDefinition authzDef = (IDomainAuthorizationDefinition) authorizationDefObj;
			IDomain<?> authenticatorDomain = domainContextObj instanceof Optional<?> opt
					? (IDomain<?>) opt.get() : (IDomain<?>) domainContextObj;

			// Get the authorization domain from the API context
			IDomain<?> authzDomain = resolveAuthorizationDomain(authenticatorDomain);
			if (authzDomain == null) return null;

			// Build filters: owned = principalUuid AND revoked = false
			java.util.List<IFilter> filters = new java.util.ArrayList<>();

			// Filter by owner (uuid of the principal)
			ObjectAddress ownedField = authzDomain.getDomainDefinition().owned();
			if (ownedField != null) {
				filters.add(Filter.eq(ownedField.toString(), principalUuid));
			}

			// Filter by tenant if multi-tenant
			if (tenantId != null) {
				ObjectAddress tenantField = authzDomain.getTenantIdFieldAddress();
				if (tenantField != null) {
					filters.add(Filter.eq(tenantField.toString(), tenantId));
				}
			}

			// Filter non-revoked
			if (authzDef.revoked() != null) {
				filters.add(Filter.eq(authzDef.revoked().toString(), false));
			}

			// Filter non-expired
			if (authzDef.expiration() != null) {
				filters.add(Filter.gt(authzDef.expiration().toString(), java.time.Instant.now()));
			}

			IFilter combinedFilter = filters.isEmpty() ? null
					: filters.size() == 1 ? filters.get(0)
					: Filter.and(filters.toArray(new Filter[0]));

			// Invoke readAll on the authorization domain with a super caller
			ICaller superCaller = Caller.createSuperCaller();
			var response = authzDomain.readAll(combinedFilter, null, null, superCaller);
			if (response.getResponseCode() == com.garganttua.api.spec.service.OperationResponseCode.OK
					&& response.getResponse() instanceof java.util.List<?> results
					&& !results.isEmpty()) {
				return results.get(0);
			}
			return null;
		} catch (Exception e) {
			// Lookup failed — return null, let the caller create a new authorization
			return null;
		}
	}

	/**
	 * Resolves the authorization domain from the authenticator domain's API context
	 * using the pre-resolved domain name stored at build time.
	 */
	private static IDomain<?> resolveAuthorizationDomain(IDomain<?> authenticatorDomain) {
		if (authenticatorDomain instanceof Domain<?> domCtx) {
			var domDef = (DomainDefinition<?>) domCtx.getDomainDefinition();
			var secDef = domDef.domainSecurityDefinition();
			if (secDef != null && secDef.authenticatorDefinition() != null
					&& secDef.authenticatorDefinition().authorizationDefinition() != null
					&& secDef.authenticatorDefinition().authorizationDefinition().authorizationDomainBuilder() != null) {
				IApi apiContext = domCtx.getApiContext();
				if (apiContext != null) {
					try {
						var builder = secDef.authenticatorDefinition().authorizationDefinition().authorizationDomainBuilder();
						String simpleName = builder.getEntityClass().getSimpleName();
						String domainName = com.garganttua.api.spec.Pluralizer.toPlural(simpleName.toLowerCase());
						return apiContext.getDomain(domainName).orElse(null);
					} catch (Exception e) {
						return null;
					}
				}
			}
		}
		return null;
	}

	@Expression(name = "createAuthorizationEntity2", description = "Creates an authorization entity from an authentication result and domain context")
	public static Object createAuthorizationEntity2(@Nullable Object authResultObj, @Nullable Object domainContextObj) {
		if (authResultObj == null || domainContextObj == null) {
			throw new ApiException("createAuthorizationEntity2: authResult and domainContext are required");
		}

		IAuthentication authResult = (IAuthentication) authResultObj;
		IDomain<?> authenticatorDomain = domainContextObj instanceof Optional<?> opt
				? (IDomain<?>) opt.get() : (IDomain<?>) domainContextObj;

		// Extract principal uuid from the authentication result
		String principalUuid = null;
		Object principal = authResult.principal();
		if (principal != null && authenticatorDomain.getEntityDefinition() != null) {
			ObjectAddress uuidAddr = authenticatorDomain.getEntityDefinition().uuid();
			if (uuidAddr != null) {
				try {
					Object val = DefaultMapper.reflection().getFieldValue(principal, uuidAddr.toString());
					principalUuid = val != null ? val.toString() : null;
				} catch (Exception e) {
					// ignore — principal may not have the uuid field
				}
			}
		}

		// Extract tenantId from the principal
		String tenantId = null;
		if (principal != null) {
			ObjectAddress tenantAddr = authenticatorDomain.getTenantIdFieldAddress();
			if (tenantAddr != null) {
				try {
					Object val = DefaultMapper.reflection().getFieldValue(principal, tenantAddr.toString());
					tenantId = val != null ? val.toString() : null;
				} catch (Exception e) {
					// ignore
				}
			}
		}

		// Get authorization definition from the domain chain
		IDomainAuthorizationDefinition authzDef = (IDomainAuthorizationDefinition) authorizationDefinition(authenticatorDomain);

		return createAuthorizationEntity(authzDef, authResult, authenticatorDomain, principalUuid, tenantId);
	}

	@Expression(name = "authRequestTenantId", description = "Extracts the tenantId from an IAuthenticationRequest")
	public static @Nullable String authRequestTenantId(@Nullable Object request) {
		if (request instanceof IAuthenticationRequest authReq) {
			return authReq.tenantId();
		}
		return null;
	}

	@Expression(name = "authResultPrincipal", description = "Extracts the principal from an IAuthentication result")
	public static Object authResultPrincipal(@Nullable Object authResult) {
		if (authResult instanceof IAuthentication auth) {
			return auth.principal();
		}
		return null;
	}

	@Expression(name = "setRequestArg", description = "Sets a named argument on the operation request")
	public static boolean setRequestArg(@Nullable Object request, @Nullable Object key, @Nullable Object value) {
		if (request == null || key == null) return false;
		IOperationRequest opRequest = (IOperationRequest) request;
		opRequest.arg(key.toString(), value);
		return true;
	}

	@Expression(name = "isAuthorizationStorable", description = "Returns true if the authorization definition has storable=true")
	public static boolean isAuthorizationStorable(@Nullable Object authorizationDefObj) {
		if (authorizationDefObj instanceof IDomainAuthorizationDefinition def) {
			return def.storable();
		}
		return false;
	}

	@Expression(name = "findByLogin", description = "Finds an entity by login field in the repository. Returns the entity or throws if not found.")
	public static Object findByLogin(@Nullable Object authContextObj, @Nullable Object repositoryObj, @Nullable Object loginValue) {
		if (authContextObj == null || repositoryObj == null || loginValue == null) {
			throw new ApiException("findByLogin: authContext, repository and login are required");
		}
		IAuthenticatorDefinition authDef = (IAuthenticatorDefinition) authContextObj;
		IRepository repo = (IRepository) repositoryObj;
		ObjectAddress loginField = authDef.login();
		if (loginField == null) {
			throw new ApiException("findByLogin: no login field configured on authenticator");
		}
		String loginFieldName = loginField.toString();
		IFilter filter = Filter.eq(loginFieldName, loginValue);
		List<Object> results = repo.getEntities(Optional.empty(), Optional.of(filter), Optional.empty());
		if (results == null || results.isEmpty()) {
			throw new ApiException("User not found for login: " + loginValue);
		}
		return results.get(0);
	}

	@Expression(name = "checkAccountStatus", description = "Checks enabled/locked/expired flags on an authenticator entity. Returns true if OK, throws if account is disabled/locked/expired.")
	public static boolean checkAccountStatus(@Nullable Object authContextObj, @Nullable Object entity) {
		if (authContextObj == null || entity == null) {
			throw new ApiException("checkAccountStatus: authContext and entity are required");
		}
		IAuthenticatorDefinition authDef = (IAuthenticatorDefinition) authContextObj;

		// If alwaysEnabled, skip all checks
		if (authDef.alwaysEnabled()) {
			return true;
		}

		IReflection reflection = DefaultMapper.reflection();

		// Check enabled
		if (authDef.enabled() != null) {
			Object value = reflection.getFieldValue(entity, authDef.enabled().toString());
			if (!Boolean.TRUE.equals(value)) {
				throw new ApiException("Account is disabled");
			}
		}

		// Check accountNonLocked
		if (authDef.accountNonLocked() != null) {
			Object value = reflection.getFieldValue(entity, authDef.accountNonLocked().toString());
			if (!Boolean.TRUE.equals(value)) {
				throw new ApiException("Account is locked");
			}
		}

		// Check accountNonExpired
		if (authDef.accountNonExpired() != null) {
			Object value = reflection.getFieldValue(entity, authDef.accountNonExpired().toString());
			if (!Boolean.TRUE.equals(value)) {
				throw new ApiException("Account is expired");
			}
		}

		// Check credentialsNonExpired
		if (authDef.credentialsNonExpired() != null) {
			Object value = reflection.getFieldValue(entity, authDef.credentialsNonExpired().toString());
			if (!Boolean.TRUE.equals(value)) {
				throw new ApiException("Credentials are expired");
			}
		}

		return true;
	}

	@Expression(name = "prepareAuthContext", description = "Prepares the runtime context with request and domainContext variables for authenticate method suppliers")
	public static boolean prepareAuthContext(@Nullable Object request, @Nullable Object domainContext) {
		IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
		if (runtimeCtx != null && request != null) {
			runtimeCtx.setVariable("request", request);
		}
		if (runtimeCtx != null && domainContext != null) {
			runtimeCtx.setVariable("domainContext", domainContext);
		}
		return true;
	}

	@Expression(name = "tryAuthenticate", description = "Attempts authentication using the IAuthenticatorDefinition, iterating over authentication methods until one succeeds")
	public static Object tryAuthenticate(@Nullable Object authenticatorDefinition) {
		if (authenticatorDefinition == null) {
			throw new ApiException("No authenticator definition available");
		}
		try {
			IAuthenticatorDefinition def = (IAuthenticatorDefinition) authenticatorDefinition;
			List<IAuthenticationDefinition> authDefs = def.authenticationDefinitions();
			if (authDefs == null || authDefs.isEmpty()) {
				throw new ApiException("No authentication methods configured");
			}

			for (IAuthenticationDefinition ad : authDefs) {
			}

			return authDefs.stream()
					.map(authDef -> attemptAuthentication(authDef, def))
					.filter(Objects::nonNull)
					.findFirst()
					.orElseThrow(() -> new ApiException("All authentication methods failed"));
		} catch (ApiException e) {
			throw e;
		} catch (Exception e) {
			throw new ApiException("Authentication failed: " + e.getMessage(), e);
		}
	}

	private static IAuthentication attemptAuthentication(IAuthenticationDefinition authDef, IAuthenticatorDefinition authenticatorDef) {
		try {
			IMethodBinder<?> binder = authDef.authenticateMethodBinder();
			if (binder == null) {
				return null;
			}

			// The binder is self-contained — all parameter suppliers are already configured
			// by the user via the DSL builder. Execute with runtime context for contextual suppliers.
			Optional<? extends IMethodReturn<?>> result;
			if (binder instanceof IContextualMethodBinder<?, ?> contextualBinder) {
				IRuntimeContext<?, ?> runtimeCtx = RuntimeExpressionContext.get();
				result = ((IContextualMethodBinder<?, Object>) contextualBinder).execute(runtimeCtx);
			} else {
				result = binder.execute();
			}

			if (result.isEmpty()) {
				return null;
			}

			Object returned = result.get().single();
			if (returned instanceof IAuthentication auth) {
				if (auth.authenticated()) return auth;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private static Object unwrapOptional(Object value) {
		if (value instanceof Optional<?> opt) {
			return opt.orElse(null);
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private static <T> Optional<T> unwrap(Object value, Class<T> type) {
		// Unwrap nested Optionals (can happen when values pass through execute_script)
		Object unwrapped = value;
		while (unwrapped instanceof Optional<?> opt) {
			if (opt.isEmpty()) return Optional.empty();
			Object inner = opt.get();
			if (type.isInstance(inner)) return Optional.of(type.cast(inner));
			unwrapped = inner;
		}
		return Optional.ofNullable(type.isInstance(unwrapped) ? type.cast(unwrapped) : null);
	}

}
