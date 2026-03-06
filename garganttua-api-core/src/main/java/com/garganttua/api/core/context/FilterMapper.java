package com.garganttua.api.core.context;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.spec.definition.IDomainDefinition;
import com.garganttua.api.spec.definition.IDtoDefinition;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.filter.IFilterMapper;
import com.garganttua.api.spec.ApiException;
import com.garganttua.core.reflection.IClass;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps filters from entity field names to DTO field names.
 *
 * This is a simplified implementation that passes filters through without
 * field name mapping. A full implementation would map entity field addresses
 * to corresponding DTO field addresses based on mapping configuration.
 */
@Slf4j
public class FilterMapper implements IFilterMapper {

    @Override
    public List<Pair<IClass<?>, IFilter>> map(IDomainDefinition<?> domainDefinition, IFilter filter) throws ApiException {
        if (log.isDebugEnabled()) {
            log.debug("Mapping Filter {} for domain {}", filter, domainDefinition.domainName());
        }

        List<Pair<IClass<?>, IFilter>> filters = new ArrayList<>();

        for (IDtoDefinition<?> dtoDefinition : domainDefinition.dtoDefinitions()) {
            if (filter == null) {
                filters.add(new Pair<>(dtoDefinition.dtoClass(), null));
                continue;
            }

            // For now, pass through the filter as-is without field name mapping
            // A full implementation would map entity field addresses to DTO field addresses
            IFilter clonedFilter = filter.clone();
            filters.add(new Pair<>(dtoDefinition.dtoClass(), clonedFilter));
        }

        return filters;
    }
}
