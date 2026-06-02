package com.garganttua.api.core.filter;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

import com.garganttua.api.commons.definition.IDomainDefinition;
import com.garganttua.api.commons.definition.IDtoDefinition;
import com.garganttua.api.commons.filter.IFilter;
import com.garganttua.api.commons.filter.IFilterMapper;
import com.garganttua.api.commons.ApiException;
import com.garganttua.core.reflection.IClass;

import com.garganttua.core.observability.Logger;

/**
 * Maps filters from entity field names to DTO field names.
 *
 * This is a simplified implementation that passes filters through without
 * field name mapping. A full implementation would map entity field addresses
 * to corresponding DTO field addresses based on mapping configuration.
 */
public class FilterMapper implements IFilterMapper {
	private static final Logger log = Logger.getLogger(FilterMapper.class);


    @Override
    public List<Pair<IClass<?>, IFilter>> map(IDomainDefinition<?> domainDefinition, IFilter filter) throws ApiException {
        log.debug("Mapping Filter {} for domain {}", filter, domainDefinition.domainName());

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
