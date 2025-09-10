package com.garganttua.api.core.engine;

import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.beans.IGGBeanLoader;

public class NamedBeanSupplier implements IObjectSupplier<Object> {

    private IGGBeanLoader loader;
    private Optional<String> supplier;
    private String beanName;

    private Object bean;

    public NamedBeanSupplier(IGGBeanLoader loader, String beanName, Optional<String> supplier) {
        this.loader = Objects.requireNonNull(loader, "BeanLoader cannot be null");
        this.beanName = Objects.requireNonNull(beanName, "Bean name cannot be null");
        this.supplier = supplier;
    }

    public NamedBeanSupplier(IGGBeanLoader loader, String beanName) {
        this.loader = Objects.requireNonNull(loader, "BeanLoader cannot be null");
        this.beanName = Objects.requireNonNull(beanName, "Bean name cannot be null");
        this.supplier = Optional.empty();
    }

    @Override
    public Object getObject() throws CoreException {
        try {
            this.getBeanIfNull();
            return this.bean;
        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }
    }

    private void getBeanIfNull() throws GGReflectionException {
        if (this.bean == null) {
            if (this.supplier.isPresent()) {
                this.bean = this.loader.getBeanNamed(this.supplier.get(), this.beanName);
            } else {
                this.bean = this.loader.getBeanNamed(this.beanName);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<Object> getObjectClass() throws CoreException {
        try {
            this.getBeanIfNull();
            return (Class<Object>) this.bean.getClass();
        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }
    }

}