package com.garganttua.api.core.expression;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.garganttua.api.core.context.RepositoryFilterTools;
import com.garganttua.api.core.context.application.DomainContext;
import com.garganttua.api.core.definition.EntityDefinition;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.ApiException;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.context.IApiContext;
import com.garganttua.api.spec.context.IDomainContext;
import com.garganttua.api.spec.definition.IDomainDefinition;
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

	@Expression(name = "equals", description = "Returns true when both arguments are equal")
	public static boolean equalsExpr(Object a, Object b) {
		return Objects.equals(a, b);
	}

	@Expression(name = "equals", description = "Returns true when both arguments are equal (boolean variant)")
	public static boolean equalsExprBool(Object a, boolean b) {
		return Objects.equals(a, b);
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
		Optional<IDomainContext<?>> dc = (Optional<IDomainContext<?>>) context;
		IDomainDefinition<?> domainDef = dc.get().getDomainDefinition();
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

	private static <T> Optional<T> unwrap(Object value, Class<T> type) {
		if (value instanceof Optional<?> opt) {
			return (Optional<T>) opt;
		}
		return Optional.ofNullable(type.isInstance(value) ? type.cast(value) : null);
	}

}
