package com.garganttua.api.core.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.garganttua.api.spec.dao.IDao;
import com.garganttua.api.spec.filter.IFilter;
import com.garganttua.api.spec.pageable.IPageable;
import com.garganttua.api.spec.sort.ISort;
import com.garganttua.core.CoreException;

/**
 * In-memory DAO implementation for demonstration purposes.
 */
public class InMemoryDao implements IDao {

    private final List<Object> storage = new ArrayList<>();
    private Class<?> dtoClass;

    @Override
    public void setDtoClass(Class<?> dtoClass) {
        this.dtoClass = dtoClass;
    }

    @Override
    public List<Object> find(Optional<IPageable> pageable, Optional<IFilter> filter, Optional<ISort> sort)
            throws CoreException {
        // Simple implementation - returns all stored objects
        // In a real implementation, filtering, sorting, and pagination would be applied
        return new ArrayList<>(storage);
    }

    @Override
    public Object save(Object object) throws CoreException {
        storage.add(object);
        return object;
    }

    @Override
    public void delete(Object object) throws CoreException {
        storage.remove(object);
    }

    @Override
    public long count(IFilter filter) throws CoreException {
        return storage.size();
    }
}
