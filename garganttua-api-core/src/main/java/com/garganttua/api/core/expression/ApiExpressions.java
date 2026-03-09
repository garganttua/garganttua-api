package com.garganttua.api.core.expression;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.core.context.Filter;
import com.garganttua.api.core.context.RepositoryFilterTools;
import com.garganttua.api.spec.service.Page;
import com.garganttua.api.core.context.application.DomainContext;
import com.garganttua.api.core.definition.EntityDefinition;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.definition.IEntityDefinition;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.service.IOperationRequest;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.injection.BeanDefinition;
import com.garganttua.core.injection.context.beans.BeanFactory;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;
import com.garganttua.core.reflection.binders.IMethodBinder;
import com.garganttua.core.reflection.runtime.RuntimeClass;

public class ApiExpressions {

	private static final IReflection REFLECTION = DefaultMapper.reflection();

	@Expression(name = "notNull", description = "Returns true if the value is not null and not an empty Optional")
	public static boolean notNull(Object value) {
		if (value == null) return false;
		if (value instanceof Optional<?> opt) return opt.isPresent();
		return true;
	}

	@Expression(name = "requirePresent", description = "Throws ApiException if value is null or an empty Optional, otherwise returns the unwrapped value")
	public static Object requirePresent(Object value) throws ApiException {
		if (value == null) throw new ApiException("Required value is null");
		if (value instanceof Optional<?> opt) {
			return opt.orElseThrow(() -> new ApiException("Required value is empty"));
		}
		return value;
	}

	@Expression(name = "equals", description = "Returns true when both arguments are equal")
	public static boolean equalsExpr(Object a, Object b) {
		return Objects.equals(unwrapOptional(a), unwrapOptional(b));
	}

	@Expression(name = "equals", description = "Returns true when both arguments are equal (boolean variant)")
	public static boolean equalsExprBool(Object a, boolean b) {
		return Objects.equals(unwrapOptional(a), b);
	}

	@Expression(name = "optionalGet", description = "Unwraps an Optional, throwing NoSuchElementException if empty")
	public static Object optionalGet(Object value) {
		if (value instanceof Optional<?> opt) {
			return opt.get();
		}
		return value;
	}

	@Expression(name = "buildFilter", description = "Builds access filter from caller permissions and domain definition")
	public static Optional<IFilter> buildFilter(Object caller, Object filter, Object context) {
		Optional<ICaller> castedCaller = (Optional<ICaller>) caller;
		IDomainContext<?> dc = context instanceof Optional<?> opt
				? (IDomainContext<?>) opt.get()
				: (IDomainContext<?>) context;
		IDomainDefinition<?> domainDef = dc.getDomainDefinition();
		Optional<IFilter> baseFilter = (Optional<IFilter>) filter;
		return Optional.ofNullable(
				RepositoryFilterTools.buildFilter(castedCaller.orElse(null), baseFilter.orElse(null), domainDef));
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
	public static IDomainContext<?> getContext(Object request, String domainName) {
		IOperationRequest opRequest = (IOperationRequest) request;
		IApiContext apiContext = opRequest.arg(IOperationRequest.API_CONTEXT).orElse(null);
		if (apiContext == null) {
			throw new ApiException("No API context available in request");
		}
		return apiContext.getDomainContext(domainName)
				.orElseThrow(() -> new ApiException("Domain not found: " + domainName));
	}

	@Expression(name = "doInjection", description = "Injects @Inject and @Property fields into entities using BeanFactory (no @PostConstruct)")
	public static List<Object> doInjection(Object request, Object entities) throws ApiException {
		if (entities == null) return List.of();
		List<Object> entityList = (List<Object>) entities;
		if (entityList.isEmpty()) return entityList;

		IOperationRequest opRequest = (IOperationRequest) request;
		DomainContext<?> dc = (DomainContext<?>) opRequest.arg(IOperationRequest.DOMAIN_CONTEXT).orElse(null);
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
			IDomainContext<?> dc = opRequest.arg(IOperationRequest.DOMAIN_CONTEXT).orElse(null);
			EntityDefinition<?> entityDef = (EntityDefinition<?>) dc.getEntityDefinition();
			List<IMethodBinder<Void>> afterGetBinders = entityDef.afterGetMethodBuilders();

			if (afterGetBinders == null || afterGetBinders.isEmpty()) return entityList;

			for (IMethodBinder<Void> binder : afterGetBinders) {
				ObjectAddress methodRef = new ObjectAddress(binder.getExecutableReference());
				for (Object entity : entityList) {
					REFLECTION.invokeDeep(entity, methodRef, RuntimeClass.of(Void.class));
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

		IDomainContext<?> dc = context instanceof Optional<?> opt
				? (IDomainContext<?>) opt.get()
				: (IDomainContext<?>) context;
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

		IDomainContext<?> dc = context instanceof Optional<?> opt
				? (IDomainContext<?>) opt.get()
				: (IDomainContext<?>) context;
		IDomainDefinition<?> domainDef = dc.getDomainDefinition();

		// Build access control filter
		IFilter accessFilter = RepositoryFilterTools.buildFilter(castedCaller.orElse(null), null, domainDef);

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

	private static Object unwrapOptional(Object value) {
		if (value instanceof Optional<?> opt) {
			return opt.orElse(null);
		}
		return value;
	}

	private static <T> Optional<T> unwrap(Object value, Class<T> type) {
		if (value instanceof Optional<?> opt) {
			return (Optional<T>) opt;
		}
		return Optional.ofNullable(type.isInstance(value) ? type.cast(value) : null);
	}

}
