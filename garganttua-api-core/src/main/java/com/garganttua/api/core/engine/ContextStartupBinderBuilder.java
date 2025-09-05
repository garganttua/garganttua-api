package com.garganttua.api.core.engine;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IBuilder;
import com.garganttua.api.spec.engine.IContextStartupBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

public class ContextStartupBinderBuilder implements IContextStartupBinderBuilder {

    private IBuilder builder;
    private IObjectSupplier<?> supplier;
    private Method method = null;
    private List<IObjectSupplier<?>> parameters = new ArrayList<>();
    private Class<?>[] parameterTypes;

    public ContextStartupBinderBuilder(IBuilder builder, IObjectSupplier<?> supplier) {
        this.builder = Objects.requireNonNull(builder, "Builder cannot be null");
        this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null");
    }

    @Override
    public IContextStartupBinderBuilder method(Method method) throws CoreException {
        this.method = List.of(this.supplier.getObjectClass().getDeclaredMethods()).stream().filter(m -> {
            return m.equals(method);
        }).findFirst()
                .orElseThrow(() -> new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Method " + method.getName() + " not found in class " + this.supplier.getObjectClass().getName()
                                + " or does not match signature"));

        this.parameterTypes = this.method.getParameterTypes();
        return this;
    }

    @Override
    public IContextStartupBinderBuilder method(GGObjectAddress method) throws CoreException {
        try {
            IGGObjectQuery query = GGObjectQueryFactory.objectQuery(this.supplier.getObjectClass());
            List<Object> found = query.find(method);

            if (!Method.class.isAssignableFrom(found.getLast().getClass())) {
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Object at address " + method.toString() + " is not a method");
            }

            this.method = (Method) found.getLast();
            this.parameterTypes = this.method.getParameterTypes();

        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }

        return this;
    }

    @Override
    public IContextStartupBinderBuilder method(String method) throws CoreException {
        try {
            IGGObjectQuery query = GGObjectQueryFactory.objectQuery(this.supplier.getObjectClass());
            this.method(query.address(method));
        } catch (GGReflectionException e) {
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }

        return this;
    }

    @Override
    public IContextStartupBinderBuilder withParam(int i, Object object) throws CoreException {
        Objects.requireNonNull(this.method, "Method must be set before setting parameters");
        if (!this.acceptNewParameter())
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Method " + this.method.getName() + " has only " + this.method.getParameterCount() + " parameters");
        if (!this.isValidParameterType(i, object))
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Parameter " + i + " of method " + this.method.getName() + " is of type "
                            + this.parameterTypes[i].getName()
                            + " and cannot be assigned a value of type " + object.getClass().getName());

        this.parameters.set(i, new IObjectSupplier<Object>() {
            @Override
            public Object getObject() throws CoreException {
                return object;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Class<Object> getObjectClass() {
                return (Class<Object>) object.getClass();
            }
        });

        return this;
    }

    @Override
    public IContextStartupBinderBuilder withParam(Object object) throws CoreException {
        Objects.requireNonNull(this.method, "Method must be set before setting parameters");
        if (!this.acceptNewParameter())
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Method " + this.method.getName() + " has only " + this.method.getParameterCount() + " parameters");
        if (!this.isValidParameterType(object))
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Parameter " + this.parameters.size() + " of method " + this.method.getName() + " is of type "
                            + this.parameterTypes[this.parameters.size()].getName()
                            + " and cannot be assigned a value of type " + object.getClass().getName());

        this.parameters.add(new IObjectSupplier<Object>() {
            @Override
            public Object getObject() throws CoreException {
                return object;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Class<Object> getObjectClass() {
                return (Class<Object>) object.getClass();
            }
        });
        return this;
    }

    @Override
    public IContextStartupBinderBuilder withParam(IObjectSupplier<?> object) throws CoreException {
        Objects.requireNonNull(this.method, "Method must be set before setting parameters");
        if (!this.acceptNewParameter())
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Method " + this.method.getName() + " has only " + this.method.getParameterCount() + " parameters");
        if (!this.isValidParameterType(object.getObjectClass()))
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Parameter " + this.parameters.size() + " of method " + this.method.getName() + " is of type "
                            + this.parameterTypes[this.parameters.size()].getName()
                            + " and cannot be assigned a value of type " + object.getObjectClass().getName());

        this.parameters.add(object);
        return this;
    }

    @Override
    public IContextStartupBinderBuilder withParam(int i, IObjectSupplier<?> object) throws CoreException {
        Objects.requireNonNull(this.method, "Method must be set before setting parameters");
        if (!this.acceptNewParameter())
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Method " + this.method.getName() + " has only " + this.method.getParameterCount() + " parameters");
        if( !this.isValidParameterType(i, object.getObjectClass()))
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Parameter " + i + " of method " + this.method.getName() + " is of type "
                            + this.parameterTypes[i].getName()
                            + " and cannot be assigned a value of type " + object.getObjectClass().getName());

        this.parameters.set(i, object);
        return this;
    }

    private boolean acceptNewParameter() {
        return this.method.getParameterCount() > this.parameters.size();
    }

    private boolean isValidParameterType(Object object) {
        return this.isValidParameterType(object.getClass());
    }

    private boolean isValidParameterType(Class<?> object) {
        return this.isValidParameterType(this.parameters.size(), object);
    }

    private boolean isValidParameterType(int index, Object object) {
        return this.isValidParameterType(index, object.getClass());
    }

    private boolean isValidParameterType(int index, Class<?> object) {
        return this.parameterTypes[index].isAssignableFrom(object);
    }

    @Override
    public IBuilder up() {
        return this.builder;
    }

}
