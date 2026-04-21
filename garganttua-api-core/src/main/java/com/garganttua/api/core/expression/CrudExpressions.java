package com.garganttua.api.core.expression;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.repository.RepositoryFilterTools;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IEntityDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.operation.OperationDefinition;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.service.IOperationRequest;
import com.garganttua.api.commons.service.Page;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.expression.annotations.Expression;
import com.garganttua.core.reflection.ObjectAddress;

import static com.garganttua.api.core.expression.ExpressionUtils.*;

/**
 * Expressions for CRUD operations: repository access, filter building, and data transformations.
 */
public class CrudExpressions {

	@Expression(name = "businessOperation", description = "Extracts the business operation label from an IOperationRequest")
	public static String businessOperation(Object request) {
		IOperationRequest opRequest = (IOperationRequest) request;
		OperationDefinition opDef = opRequest.operation();
		if (opDef == null) return null;
		return opDef.getBusinessOperation().getLabel();
	}

	@Expression(name = "buildFilter", description = "Builds access filter from caller permissions and domain definition")
	public static Optional<IFilter> buildFilter(Object caller, Object filter, Object context) {
		Optional<ICaller> castedCaller = (Optional<ICaller>) caller;
		IDomain<?> dc = toDomain(context);
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
		var apiContext = opRequest.arg(IOperationRequest.API_CONTEXT).orElse(null);
		if (apiContext == null) {
			throw new ApiException("No API context available in request");
		}
		return ((com.garganttua.api.commons.context.IApi) apiContext).getDomain(domainName)
				.orElseThrow(() -> new ApiException("Domain not found: " + domainName));
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

		IDomain<?> dc = toDomain(context);
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

		IDomain<?> dc = toDomain(context);
		IDomainDefinition<?> domainDef = dc.getDomainDefinition();

		IFilter accessFilter = RepositoryFilterTools.buildFilter(castedCaller.orElse(null), null, domainDef, dc.isMultiTenant());

		IEntityDefinition<?> entityDef = dc.getEntityDefinition();
		IFilter identifierFilter;
		if ("id".equals(typeStr)) {
			identifierFilter = RepositoryFilterTools.createIdFilter(entityDef.id(), identifierStr);
		} else {
			identifierFilter = RepositoryFilterTools.createUuidFilter(entityDef.uuid(), identifierStr);
		}

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
}
