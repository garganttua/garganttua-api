package com.garganttua.api.core.engine;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MethodBinderBuilder<T extends IMethodBinderBuilder<T, U, V>, U, V>
        implements IMethodBinderBuilder<T, U, V> {

    private IObjectSupplier<?> supplier;
    private Method method = null;
    private List<IObjectSupplier<?>> parameters;
    private Class<?>[] parameterTypes;

    protected abstract T getReturned();

    private V up;

    protected MethodBinderBuilder(V up, IObjectSupplier<?> supplier) {
        log.atTrace().log("Creating MethodBinderBuilder with up={} and supplier={}", up, supplier);
        this.up = Objects.requireNonNull(up, "Up cannot be null");
        this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null");
    }

    @Override
    public T method(Method method) throws CoreException {
        log.atDebug().log("Resolving method {} in class {}", method.getName(), this.supplier.getObjectClass());

        this.method = List.of(this.supplier.getObjectClass().getDeclaredMethods()).stream()
                .peek(m -> log.atTrace().log("Checking declared method: {}", m))
                .filter(m -> m.equals(method))
                .findFirst()
                .orElseThrow(() -> {
                    try {
                        log.atWarn().log("Method {} not found in class {}", method.getName(),
                                this.supplier.getObjectClass().getName());
                        return new BuilderException(CoreExceptionCode.BUILDER_CODE,
                                "Method " + method.getName() + " not found in class "
                                        + this.supplier.getObjectClass().getName()
                                        + " or does not match signature");
                    } catch (CoreException e) {
                        return e;
                    }
                });

        this.parameterTypes = this.method.getParameterTypes();
        this.parameters = new ArrayList<>(Collections.nCopies(this.parameterTypes.length, null));

        log.atInfo().log("Successfully bound method {} with {} parameters",
                this.method.getName(), this.method.getParameterCount());

        return this.getReturned();
    }

    @Override
    public T method(GGObjectAddress method) throws CoreException {
        log.atDebug().log("Resolving method by GGObjectAddress={} in class {}", method, this.supplier.getObjectClass());
        try {
            IGGObjectQuery query = GGObjectQueryFactory.objectQuery(this.supplier.getObjectClass());
            List<Object> found = query.find(method);

            Object last = found.getLast();
            log.atTrace().log("Last resolved object: {}", last);

            if (!Method.class.isAssignableFrom(last.getClass())) {
                log.atWarn().log("Object at {} is not a Method", method);
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Object at address " + method.toString() + " is not a method");
            }

            this.method = (Method) last;
            this.parameterTypes = this.method.getParameterTypes();
            this.parameters = new ArrayList<>(Collections.nCopies(this.parameterTypes.length, null));

            log.atInfo().log("Successfully bound method {} with {} parameters",
                    this.method.getName(), this.method.getParameterCount());

        } catch (GGReflectionException e) {
            log.atError().log("Reflection error resolving method {}: {}", method, e.getMessage());
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }

        return this.getReturned();
    }

    @Override
    public T method(String method) throws CoreException {
        log.atDebug().log("Resolving method by name={} in class {}", method, this.supplier.getObjectClass());
        try {
            IGGObjectQuery query = GGObjectQueryFactory.objectQuery(this.supplier.getObjectClass());
            this.method(query.address(method));
        } catch (GGReflectionException e) {
            log.atError().log("Reflection error resolving method by name {}: {}", method, e.getMessage());
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE, e.getMessage(), e);
        }

        return this.getReturned();
    }

    @Override
    public T withParam(int i, Object object) throws CoreException {
        log.atTrace().log("Binding parameter {} with value={}", i, object);
        Objects.requireNonNull(this.method, "Method must be set before setting parameters");

        if (!this.isValidParameterIndex(i)) {
            log.atWarn().log("Invalid parameter index {} for method {}", i, this.method.getName());
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Method " + this.method.getName() + " has only " + this.method.getParameterCount() + " parameters");
        }
        if (!this.isValidParameterType(i, object)) {
            log.atWarn().log("Invalid parameter type {} for method {} expected {}",
                    object.getClass(), this.method.getName(), this.parameterTypes[i]);
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Parameter " + i + " of method " + this.method.getName() + " is of type "
                            + this.parameterTypes[i].getName()
                            + " and cannot be assigned a value of type " + object.getClass().getName());
        }

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

        log.atInfo().log("Parameter {} bound successfully with type {}", i, object.getClass());
        return this.getReturned();
    }

    @Override
    public T withParam(int i, IObjectSupplier<?> object) throws CoreException {
        log.atTrace().log("Binding parameter {} with supplier of type {}", i, object.getObjectClass());
        Objects.requireNonNull(this.method, "Method must be set before setting parameters");

        if (!this.isValidParameterIndex(i)) {
            log.atWarn().log("Invalid parameter index {} for method {}", i, this.method.getName());
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Method " + this.method.getName() + " has only " + this.method.getParameterCount() + " parameters");
        }
        if (!this.isValidParameterType(i, object.getObjectClass())) {
            log.atWarn().log("Invalid supplier type {} for parameter {} of method {} expected {}",
                    object.getObjectClass(), i, this.method.getName(), this.parameterTypes[i]);
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Parameter " + i + " of method " + this.method.getName() + " is of type "
                            + this.parameterTypes[i].getName()
                            + " and cannot be assigned a value of type " + object.getObjectClass().getName());
        }

        this.parameters.set(i, object);
        log.atInfo().log("Parameter {} bound successfully with supplier type {}", i, object.getObjectClass());
        return this.getReturned();
    }

    private boolean isValidParameterIndex(int i) {
        boolean valid = !(i > this.method.getParameterCount() - 1 || i < 0);
        log.atTrace().log("Parameter index {} validity: {}", i, valid);
        return valid;
    }

    private boolean isValidParameterType(int index, Object object) {
        return this.isValidParameterType(index, object.getClass());
    }

    private boolean isValidParameterType(int index, Class<?> object) {
        boolean valid = this.parameterTypes[index].isAssignableFrom(object);
        log.atTrace().log("Parameter type check for index {} expected {} got {} validity={}",
                index, this.parameterTypes[index], object, valid);
        return valid;
    }

    @Override
    public U build() throws CoreException {
        log.atError().log("Build method not implemented");
        throw new UnsupportedOperationException("Unimplemented method 'build'");
    }

    @Override
    public V up() {
        log.atTrace().log("Returning up object {}", this.up);
        return this.up;
    }
}
