package com.garganttua.api.core.repository;

import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.filter.FilterMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.javatuples.Pair;

import com.garganttua.api.core.repository.RepositoryException;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.context.IDtoContext;
import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.filter.IFilterMapper;
import com.garganttua.api.commons.pageable.IPageable;
import com.garganttua.api.commons.repository.IRepository;
import com.garganttua.api.commons.sort.ISort;
import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.mapper.MapperException;
import com.garganttua.core.reflection.IClass;
import com.garganttua.core.reflection.IReflection;
import com.garganttua.core.reflection.ObjectAddress;

import com.garganttua.core.observability.Logger;

public class Repository implements IRepository {
	private static final Logger log = Logger.getLogger(Repository.class);


    private final List<IDtoContext<?>> dtoContexts;
    private final IClass<?> entityClass;
    private static final IReflection REFLECTION = DefaultMapper.reflection();
    private final IMapper mapper = DefaultMapper.mapper();
    private final IFilterMapper filterMapper = new FilterMapper();

    private IDomain<?> domainContext;
    private final Object domainContextLock = new Object();

    public Repository(List<IDtoContext<?>> dtoContexts, IClass<?> entityClass) {
        this.dtoContexts = Collections.unmodifiableList(new ArrayList<>(
                Objects.requireNonNull(dtoContexts, "Dto contexts cannot be null")));
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
    }

    public void setDomain(IDomain<?> domainContext) {
        synchronized (domainContextLock) {
            this.domainContext = Objects.requireNonNull(domainContext, "Domain context cannot be null");
        }
    }

    private IDomain<?> getDomain() {
        synchronized (domainContextLock) {
            return this.domainContext;
        }
    }

    // --- Read operations ---
    
    @Override
    public List<Object> getEntities(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
            throws ApiException {
        return getEntities(pageable, filter, sort, Optional.empty());
    }

    @Override
    public List<Object> getEntities(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort,
            Optional<List<String>> projection) throws ApiException {
        log.debug("Fetching entities with filter={} projection={}", filter.orElse(null), projection.orElse(null));
        List<Map<String, Object>> dtoMaps = queryAllDtos(pageable, filter, sort, projection);
        return mergeAndMapToEntities(dtoMaps);
    }

    // --- Write operations ---

    @Override
    public void save(Object entity) throws ApiException {
        Objects.requireNonNull(entity, "Entity cannot be null");
        log.debug("Saving entity of type {}", entity.getClass().getSimpleName());

        for (IDtoContext<?> dtoContext : dtoContexts) {
            Object dto = mapEntityToDto(entity, dtoContext.getDtoDefinition());
            dtoContext.save(dto);
        }
    }

    @Override
    public void delete(Object entity) throws ApiException {
        Objects.requireNonNull(entity, "Entity cannot be null");
        log.debug("Deleting entity of type {}", entity.getClass().getSimpleName());

        for (IDtoContext<?> dtoContext : dtoContexts) {
            Object dto = mapEntityToDto(entity, dtoContext.getDtoDefinition());
            dtoContext.delete(dto);
        }
    }

    // --- Existence checks ---

    @Override
    public boolean doesExist(Object entity) throws ApiException {
        Objects.requireNonNull(entity, "Entity cannot be null");
        String uuid = extractUuidFromEntity(entity);
        return doesExist(uuid);
    }

    @Override
    public boolean doesExist(String uuid) throws ApiException {
        Objects.requireNonNull(uuid, "UUID cannot be null");
        return findOneByField(ctx -> ctx.getDtoDefinition().uuid(), uuid).isPresent();
    }

    // --- Count ---

    @Override
    public long getCount(IFilter filter) throws ApiException {
        if (dtoContexts.isEmpty()) {
            return 0;
        }
        return dtoContexts.get(0).count(filter);
    }

    // --- Internal: querying ---

    private List<Map<String, Object>> queryAllDtos(Optional<IPageable> pageable, Optional<IFilter> filter,
            Optional<ISort> sort, Optional<List<String>> projection) throws ApiException {
        IDomain<?> dc = getDomain();
        if (dc != null) {
            return queryWithFilterMapping(dc, pageable, filter, sort, projection);
        }
        return dtoContexts.stream()
                .map(ctx -> queryDtoContext(ctx, pageable, filter, sort, projection))
                .toList();
    }

    private List<Map<String, Object>> queryWithFilterMapping(IDomain<?> dc, Optional<IPageable> pageable,
            Optional<IFilter> filter, Optional<ISort> sort, Optional<List<String>> projection) throws ApiException {
        IDomainDefinition<?> definition = dc.getDomainDefinition();
        List<Pair<IClass<?>, IFilter>> mappedFilters = filterMapper.map(definition, filter.orElse(null));

        List<Map<String, Object>> dtoMaps = new ArrayList<>();
        for (Pair<IClass<?>, IFilter> mapped : mappedFilters) {
            IDtoContext<?> dtoContext = findDtoContextByClass(mapped.getValue0());
            if (dtoContext != null) {
                dtoMaps.add(queryDtoContext(dtoContext, pageable, Optional.ofNullable(mapped.getValue1()), sort, projection));
            }
        }
        return dtoMaps;
    }

    private Optional<Object> findOneByField(FieldAddressExtractor extractor, String value) throws ApiException {
        List<Map<String, Object>> dtoMaps = new ArrayList<>();
        for (IDtoContext<?> dtoContext : dtoContexts) {
            ObjectAddress fieldAddress = extractor.extract(dtoContext);
            if (fieldAddress == null) {
                continue;
            }
            IFilter filter = Filter.eq(fieldAddress.toString(), value);
            dtoMaps.add(queryDtoContext(dtoContext, Optional.empty(), Optional.of(filter), Optional.empty(), Optional.empty()));
        }

        Map<String, List<Object>> merged = mergeMaps(dtoMaps, false);
        if (merged.isEmpty()) {
            return Optional.empty();
        }
        List<Object> dtos = merged.values().iterator().next();
        return Optional.ofNullable(mapDtosToEntity(dtos));
    }

    private Map<String, Object> queryDtoContext(IDtoContext<?> dtoContext, Optional<IPageable> pageable,
            Optional<IFilter> filter, Optional<ISort> sort, Optional<List<String>> projection) throws ApiException {
        Map<String, Object> map = new HashMap<>();
        // Use the 3-arg find for the common no-projection path so DAOs/contexts that only implement
        // it are unaffected; the projecting 4-arg variant is reached only when fields were requested.
        boolean hasProjection = projection != null && projection.isPresent() && !projection.get().isEmpty();
        List<Object> dtos = hasProjection
                ? dtoContext.find(pageable, filter, sort, projection)
                : dtoContext.find(pageable, filter, sort);
        for (Object dto : dtos) {
            String uuid = dtoContext.getUuid(dto);
            map.put(uuid, dto);
        }
        return map;
    }

    // --- Internal: mapping ---

    private List<Object> mergeAndMapToEntities(List<Map<String, Object>> dtoMaps) throws ApiException {
        Map<String, List<Object>> merged = mergeMaps(dtoMaps, false);
        return merged.values().stream()
                .map(this::mapDtosToEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Object mapDtosToEntity(List<Object> dtoList) {
        Object entity = null;
        for (Object dto : dtoList) {
            try {
                entity = (entity == null)
                        ? mapper.map(dto, entityClass)
                        : mapper.map(dto, entity);
            } catch (MapperException e) {
                log.error("Mapping failed for DTO {}", dto.getClass().getSimpleName(), e);
                return null;
            }
        }
        return entity;
    }

    private Object mapEntityToDto(Object entity, IDtoDefinition<?> dtoDefinition) throws ApiException {
        try {
            return mapper.map(entity, dtoDefinition.dtoClass());
        } catch (MapperException e) {
            throw new RepositoryException(
                    "Failed to map entity to DTO " + dtoDefinition.dtoClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private String extractUuidFromEntity(Object entity) throws ApiException {
        IDomain<?> dc = getDomain();
        if (dc == null) {
            throw new RepositoryException("Domain context not set, cannot extract UUID from entity");
        }
        ObjectAddress uuidAddress = dc.getEntityDefinition().uuid();
        try {
            Object value = REFLECTION.getFieldValue(entity, uuidAddress.toString());
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            throw new RepositoryException("Failed to extract UUID from entity: " + e.getMessage());
        }
    }

    // --- Internal: lookup ---

    private IDtoContext<?> findDtoContextByClass(IClass<?> dtoClass) {
        for (IDtoContext<?> ctx : dtoContexts) {
            if (ctx.getDtoDefinition().dtoClass().equals(dtoClass)) {
                return ctx;
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface FieldAddressExtractor {
        ObjectAddress extract(IDtoContext<?> dtoContext);
    }

    // --- Internal: merge ---

    public static Map<String, List<Object>> mergeMaps(List<Map<String, Object>> maps, boolean strict) throws ApiException {
        Map<String, List<Object>> result = new HashMap<>();
        maps.forEach(map -> map.forEach((key, value) ->
                result.computeIfAbsent(key, k -> new ArrayList<>()).add(value)));

        if (strict && !result.isEmpty()) {
            int expectedSize = result.values().iterator().next().size();
            result.forEach((key, list) -> {
                if (list.size() != expectedSize) {
                    throw new RepositoryException(
                            String.format("Key '%s' has %d DTOs, expected %d", key, list.size(), expectedSize));
                }
            });
        }
        return result;
    }
}
