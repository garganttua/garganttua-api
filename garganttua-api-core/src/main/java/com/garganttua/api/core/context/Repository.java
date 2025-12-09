package com.garganttua.api.core.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.garganttua.api.core.context.application.RepositoryException;
import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.spec.context.IDtoContext;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.repository.IRepository;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.CoreException;
import com.garganttua.core.mapper.IMapper;
import com.garganttua.core.mapper.MapperException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Repository implements IRepository {

    private List<IDtoContext<?>> dtoContexts;
    private final IMapper mapper = DefaultMapper.mapper();
    private final @Nonnull Class<?> entityClass;

    public Repository(List<IDtoContext<?>> dtoContexts, Class<?> entityClass) {
        this.dtoContexts = Objects.requireNonNull(dtoContexts, "Dto contexts cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        log.atInfo().log("Repository initialized with {} DTO contexts and entity class {}", dtoContexts.size(),
                entityClass.getSimpleName());
    }

    @Override
    public boolean doesExist(Object entity) throws CoreException {
        log.atDebug().log("Checking existence of entity {}", entity);
        throw new UnsupportedOperationException("Unimplemented method 'doesExist'");
    }

    @Override
    public List<Object> getEntities(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
            throws CoreException {

        log.atInfo().log("Fetching entities for pageable={}, filter={}, sort={}", pageable, filter,
                sort);

        log.atDebug().log("Building DTO maps for {} contexts...", dtoContexts.size());
        List<Map<String, Object>> dtoMaps = dtoContexts.stream()
                .map(context -> buildDtoMap(context, pageable, filter, sort))
                .collect(Collectors.toList());

        log.atDebug().log("Merging {} DTO maps into a unified structure...", dtoMaps.size());
        Map<String, List<Object>> mergedDtos = Repository.mergeMaps(dtoMaps, false);

        log.atDebug().log("Mapping merged DTOs ({}) to entities of type {}", mergedDtos.size(),
                entityClass.getSimpleName());
        List<Object> entities = mergedDtos.values().stream()
                .map(this::mapDtosToEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        log.atInfo().log("Successfully built {} entities", entities.size());
        return entities;
    }

    private Map<String, Object> buildDtoMap(IDtoContext<?> context, Optional<IPageable> pageable, Optional<IFilter> filter,
            Optional<ISort> sort) {
        log.atTrace().log("Building DTO map for context {}", context.getClass().getSimpleName());
        Map<String, Object> map = new HashMap<>();

        try {
            List<Object> dtos = context.getDao().find(pageable, filter, sort);
            log.atDebug().log("Context {} returned {} DTOs", context.getClass().getSimpleName(), dtos.size());

            for (Object dto : dtos) {
                String uuid = context.getUuid(dto);
                map.put(uuid, dto);
            }
            log.atTrace().log("Built map with {} UUID entries", map.size());

        } catch (CoreException e) {
            log.atError().setCause(e).log("Error building DTO map for context {}", context.getClass().getSimpleName());
        }

        return map;
    }

    private Object mapDtosToEntity(List<Object> dtoList) {
        log.atTrace().log("Mapping {} DTOs into a single entity", dtoList.size());
        Object entity = null;

        for (Object dto : dtoList) {
            try {
                entity = (entity == null)
                        ? mapper.map(dto, this.entityClass)
                        : mapper.map(dto, entity);
                log.atTrace().log("Mapped DTO {} into entity {}", dto.getClass().getSimpleName(),
                        entity.getClass().getSimpleName());
            } catch (MapperException e) {
                log.atError().setCause(e).log("Mapping failed for DTO {}", dto);
                return null;
            }
        }

        log.atDebug().log("Successfully merged {} DTOs into an entity {}", dtoList.size(), entityClass.getSimpleName());
        return entity;
    }

    public static Map<String, List<Object>> mergeMaps(List<Map<String, Object>> maps, boolean strict)
            throws CoreException {

        log.atTrace().log("Merging {} maps (strict={})", maps.size(), strict);
        Map<String, List<Object>> result = new HashMap<>();

        maps.forEach(
                map -> map.forEach((key, value) -> result.computeIfAbsent(key, k -> new ArrayList<>()).add(value)));

        if (strict && !result.isEmpty()) {
            int expectedSize = result.values().iterator().next().size();
            log.atDebug().log("Strict mode enabled, expecting {} elements per key", expectedSize);

            result.forEach((key, list) -> {
                if (list.size() != expectedSize) {
                    String message = String.format("Key '%s' has %d elements, expected %d", key, list.size(),
                            expectedSize);
                    log.atError().log(message);
                    throw new RepositoryException(message);
                }
            });
        }

        log.atTrace().log("Merged maps into {} keys", result.size());
        return result;
    }

    @Override
    public void save(Object entity) throws CoreException {
        log.atWarn().log("save() called but not implemented (entity={})", entity);
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    @Override
    public Optional<Object> getOneById(String id) throws CoreException {
        log.atWarn().log("getOneById() called but not implemented (id={})", id);
        throw new UnsupportedOperationException("Unimplemented method 'getOneById'");
    }

    @Override
    public void delete(Object entity) throws CoreException {
        log.atWarn().log("delete() called but not implemented (entity={})", entity);
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public boolean doesExist(String uuid) throws CoreException {
        log.atWarn().log("doesExist() called but not implemented (uuid={})", uuid);
        throw new UnsupportedOperationException("Unimplemented method 'doesExist'");
    }

    @Override
    public Optional<Object> getOneByUuid(String uuid) throws CoreException {
        log.atWarn().log("getOneByUuid() called but not implemented (uuid={})", uuid);
        throw new UnsupportedOperationException("Unimplemented method 'getOneByUuid'");
    }

    @Override
    public long getCount(IFilter filter) throws CoreException {
        log.atWarn().log("getCount() called but not implemented (filter={})", filter);
        throw new UnsupportedOperationException("Unimplemented method 'getCount'");
    }

}