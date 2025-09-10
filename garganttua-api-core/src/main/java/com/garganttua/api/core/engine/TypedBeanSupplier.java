package com.garganttua.api.core.engine;

import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.IGGBeanLoader;

public class TypedBeanSupplier<T> implements IObjectSupplier<T>{

    private IGGBeanLoader loader;
    private Class<T> beanClass;
    private Optional<String> supplier;

    public TypedBeanSupplier(IGGBeanLoader loader, Class<T> beanClass, Optional<String> supplier) {
        this.loader = Objects.requireNonNull(loader, "BeanLoader cannot be null");
        this.beanClass = Objects.requireNonNull(beanClass, "Bean class cannot be null");
        this.supplier = supplier;
    }

    public TypedBeanSupplier(IGGBeanLoader loader, Class<T> beanClass) {
        this.loader = Objects.requireNonNull(loader, "BeanLoader cannot be null");
        this.beanClass = Objects.requireNonNull(beanClass, "Bean class cannot be null");
         this.supplier = Optional.empty();
    }

    @Override
    public T getObject() throws CoreException {
        try {
            if(this.supplier.isPresent()) {
                return this.loader.getBeanOfType(this.supplier.get(), this.beanClass);
            } else {
                return this.loader.getBeanOfType(this.beanClass);
            }
        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }
    }

    @Override
    public Class<T> getObjectClass() {
        return this.beanClass;
    }

}
