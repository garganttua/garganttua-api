package com.garganttua.api.core.builder.binder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.core.builder.resolver.MethodResolver;
import com.garganttua.api.core.builder.supplier.FixedObjectSupplierBuilder;
import com.garganttua.api.core.context.application.MethodBinder;
import com.garganttua.api.core.context.application.supplier.ContextualObjectSupplier;
import com.garganttua.api.core.context.application.supplier.SupplyException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.engine.IApplicationContext;
import com.garganttua.api.spec.engine.IExecutionContext;
import com.garganttua.api.spec.engine.IMethodBinder;
import com.garganttua.api.spec.engine.IMethodBinderBuilder;
import com.garganttua.api.spec.engine.IObjectSupplier;
import com.garganttua.api.spec.engine.IObjectSupplierBuilder;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MethodBinderBuilder<T extends IMethodBinderBuilder<T, V>, V>
        implements IMethodBinderBuilder<T, V> {

    private IObjectSupplierBuilder<?> supplier;
    private GGObjectAddress method = null;
    private List<IObjectSupplierBuilder<?>> parameters;
    private List<Boolean> parameterNullableAllowed;
    private Class<?>[] parameterTypes;

    protected abstract T getReturned();

    private final V up;
    private IGGObjectQuery objectQuery;
    private boolean collection = false;

    protected MethodBinderBuilder(V up, IObjectSupplierBuilder<?> supplier) throws BuilderException {
        this(up, supplier, false);
    }

    protected MethodBinderBuilder(V up, IObjectSupplierBuilder<?> supplier, boolean collection) throws BuilderException {
        log.atTrace().log("[MethodBinderBuilder] Creating with up={} and supplier={}", up, supplier);
        this.up = Objects.requireNonNull(up, "Up cannot be null");
        this.supplier = Objects.requireNonNull(supplier, "Supplier cannot be null");
        this.collection = collection;
        try {
            this.objectQuery = GGObjectQueryFactory.objectQuery(this.supplier.getObjectClass());
        } catch (GGReflectionException e) {
            log.atError().log("[MethodBinderBuilder] Error creating objectQuery for class {}",
                    this.supplier.getObjectClass(), e);
            throw new BuilderException(e.getMessage(), e);
        }
    }

    public String getMethodName() {
        return this.method != null ? this.method.getElement(this.method.length() - 1) : null;
    }

    @Override
    public T method() throws CoreException {
        if (this.method == null) {
            throw new BuilderException("Method must be set");
        }
        return this.getReturned();
    }

    @Override
    public T method(Method method) throws CoreException {
        log.atDebug().log("[MethodBinderBuilder] Resolving method {} in class {}", method.getName(),
                this.supplier.getObjectClass());

        this.method = MethodResolver.methodByMethod(method, this.supplier.getObjectClass());
        this.initParameters();
        return this.getReturned();
    }

    @Override
    public T method(GGObjectAddress methodAddress) throws CoreException {
        log.atDebug().log("[MethodBinderBuilder] Resolving method by address={} in class {}", methodAddress,
                this.supplier.getObjectClass());

        this.method = MethodResolver.methodByAddress(methodAddress, this.objectQuery, this.supplier.getObjectClass());
        this.initParameters();
        return this.getReturned();
    }

    @Override
    public T method(String methodName) throws CoreException {
        log.atDebug().log("[MethodBinderBuilder] Resolving method by name={} in class {}", methodName,
                this.supplier.getObjectClass());

        this.method = MethodResolver.methodByName(methodName, this.objectQuery, this.supplier.getObjectClass());
        this.initParameters();
        return this.getReturned();
    }

    @Override
    public T method(Method method, Class<?> returnType, Class<?>... parameterTypes) throws CoreException {
        log.atDebug().log("[MethodBinderBuilder] Resolving method {} with returnType={} in class {}",
                method.getName(), returnType, this.supplier.getObjectClass());

        this.method = MethodResolver.methodByMethod(method, this.supplier.getObjectClass(), returnType, parameterTypes);
        this.initParameters();
        return this.getReturned();
    }

    @Override
    public T method(GGObjectAddress methodAddress, Class<?> returnType, Class<?>... parameterTypes)
            throws CoreException {
        log.atDebug().log("[MethodBinderBuilder] Resolving method by address={} with returnType={} in class {}",
                methodAddress, returnType, this.supplier.getObjectClass());

        this.method = MethodResolver.methodByAddress(methodAddress, this.objectQuery, this.supplier.getObjectClass(),
                returnType, parameterTypes);
        this.initParameters();
        return this.getReturned();
    }

    @Override
    public T method(String methodName, Class<?> returnType, Class<?>... parameterTypes) throws CoreException {
        log.atDebug().log("[MethodBinderBuilder] Resolving method by name={} with returnType={} in class {}",
                methodName, returnType, this.supplier.getObjectClass());

        this.method = MethodResolver.methodByName(methodName, this.objectQuery, this.supplier.getObjectClass(),
                returnType, parameterTypes);
        this.initParameters();
        return this.getReturned();
    }

    private void initParameters() throws BuilderException {
        Objects.requireNonNull(this.method, "[MethodBinderBuilder] Method must be set before initializing parameters");
        Objects.requireNonNull(this.objectQuery, "[MethodBinderBuilder] Object query cannot be null");

        try {
            Method m = (Method) this.objectQuery.find(this.method).getLast();
            this.parameterTypes = m.getParameterTypes();
            this.parameters = new ArrayList<>(Collections.nCopies(this.parameterTypes.length, null));
            // default : parameters are NOT nullable unless specified
            this.parameterNullableAllowed = new ArrayList<>(
                    Collections.nCopies(this.parameterTypes.length, Boolean.FALSE));

            log.atInfo().log("[MethodBinderBuilder] Successfully bound method {} with {} parameters",
                    getMethodName(), this.parameterTypes.length);
        } catch (GGReflectionException e) {
            log.atError().log("[MethodBinderBuilder] Error initializing parameters for method {}", getMethodName(), e);
            throw new BuilderException(e.getMessage(), e);
        }
    }

    // -----------------------
    // withParam implementations
    // -----------------------

    @Override
    public T withParam(int i, Object object) throws CoreException {
        return withParam(i, object, false);
    }

    @Override
    public T withParam(int i, IObjectSupplierBuilder<?> object) throws CoreException {
        return withParam(i, object, false);
    }

    @Override
    public T withParam(int i, Object object, boolean acceptNullable) throws CoreException {
        log.atTrace().log("[MethodBinderBuilder] Binding parameter {} with value={} (acceptNullable={})", i, object,
                acceptNullable);
        Objects.requireNonNull(this.method, "[MethodBinderBuilder] Method must be set before setting parameters");

        if (!this.isValidParameterIndex(i)) {
            log.atWarn().log("[MethodBinderBuilder] Invalid parameter index {} for method {}", i, getMethodName());
            throw new BuilderException(
                    "Method " + getMethodName() + " has only " + this.parameterTypes.length + " parameters");
        }

        if (object == null) {
            if (!acceptNullable) {
                log.atWarn().log("[MethodBinderBuilder] Null value provided for parameter {} but acceptNullable=false",
                        i);
                throw new BuilderException(
                        "Parameter " + i + " of method " + getMethodName() + " cannot be null");
            }
            this.parameters.set(i, new FixedObjectSupplierBuilder<>(null));
            this.parameterNullableAllowed.set(i, Boolean.TRUE);
            log.atInfo().log("[MethodBinderBuilder] Parameter {} bound as nullable null", i);
            return this.getReturned();
        }

        if (!this.isValidParameterType(i, object)) {
            log.atWarn().log("[MethodBinderBuilder] Invalid parameter type {} for method {} expected {}",
                    object.getClass(), getMethodName(), this.parameterTypes[i]);
            throw new BuilderException(
                    "Parameter " + i + " of method " + getMethodName() + " is of type "
                            + this.parameterTypes[i].getName()
                            + " and cannot be assigned a value of type " + object.getClass().getName());
        }

        this.parameters.set(i, new FixedObjectSupplierBuilder<>(object));
        this.parameterNullableAllowed.set(i, acceptNullable);
        log.atInfo().log("[MethodBinderBuilder] Parameter {} bound successfully with type {} (acceptNullable={})", i,
                object.getClass(), acceptNullable);
        return this.getReturned();
    }

    @Override
    public T withParam(int i, IObjectSupplierBuilder<?> object, boolean acceptNullable) throws CoreException {
        log.atTrace().log("[MethodBinderBuilder] Binding parameter {} with supplier of type {} (acceptNullable={})", i,
                object == null ? "null" : object.getObjectClass(), acceptNullable);
        Objects.requireNonNull(this.method, "[MethodBinderBuilder] Method must be set before setting parameters");
        Objects.requireNonNull(object, "Supplier cannot be null");

        if (!this.isValidParameterIndex(i)) {
            log.atWarn().log("[MethodBinderBuilder] Invalid parameter index {} for method {}", i, getMethodName());
            throw new BuilderException(
                    "Method " + getMethodName() + " has only " + this.parameterTypes.length + " parameters");
        }
        // type check using supplier declared class
        Class<?> suppliedClass = object.getObjectClass();
        if (suppliedClass == null) {
            log.atWarn().log("[MethodBinderBuilder] Supplier.getObjectClass() returned null for parameter {}", i);
            throw new BuilderException(
                    "Supplier for parameter " + i + " does not declare object class");
        }
        if (!this.isValidParameterType(i, suppliedClass)) {
            log.atWarn().log("[MethodBinderBuilder] Invalid supplier type {} for parameter {} of method {} expected {}",
                    suppliedClass, i, getMethodName(), this.parameterTypes[i]);
            throw new BuilderException(
                    "Parameter " + i + " of method " + getMethodName() + " is of type "
                            + this.parameterTypes[i].getName()
                            + " and cannot be assigned a value of type " + suppliedClass.getName());
        }

        this.parameters.set(i, object);
        this.parameterNullableAllowed.set(i, acceptNullable);
        log.atInfo().log(
                "[MethodBinderBuilder] Parameter {} bound successfully with supplier type {} (acceptNullable={})", i,
                suppliedClass, acceptNullable);
        return this.getReturned();
    }

    @Override
    public T withParam(String paramName, Object parameter) throws CoreException {
        return withParam(paramName, parameter, false);
    }

    @Override
    public T withParam(String paramName, IObjectSupplierBuilder<?> supplier) throws CoreException {
        return withParam(paramName, supplier, false);
    }

    @Override
    public T withParam(String paramName, Object parameter, boolean acceptNullable) throws CoreException {
        Objects.requireNonNull(paramName, "paramName cannot be null");
        Objects.requireNonNull(this.method, "[MethodBinderBuilder] Method must be set before setting parameters");

        Method m;
        try {
            m = (Method) this.objectQuery.find(this.method).getLast();
        } catch (GGReflectionException e) {
            throw new BuilderException(e.getMessage(), e);
        }

        java.lang.reflect.Parameter[] params = m.getParameters();
        Integer foundIdx = null;
        for (int i = 0; i < params.length; i++) {
            if (paramName.equals(params[i].getName())) {
                foundIdx = i;
                break;
            }
        }
        if (foundIdx == null) {
            log.atWarn().log("[MethodBinderBuilder] Parameter name '{}' not found for method {}", paramName,
                    getMethodName());
            throw new BuilderException(
                    "Parameter name " + paramName + " not found for method " + getMethodName());
        }
        return withParam(foundIdx, parameter, acceptNullable);
    }

    @Override
    public T withParam(String paramName, IObjectSupplierBuilder<?> supplier, boolean acceptNullable)
            throws CoreException {
        Objects.requireNonNull(paramName, "paramName cannot be null");
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(this.method, "[MethodBinderBuilder] Method must be set before setting parameters");

        Method m;
        try {
            m = (Method) this.objectQuery.find(this.method).getLast();
        } catch (GGReflectionException e) {
            throw new BuilderException(e.getMessage(), e);
        }

        java.lang.reflect.Parameter[] params = m.getParameters();
        Integer foundIdx = null;
        for (int i = 0; i < params.length; i++) {
            if (paramName.equals(params[i].getName())) {
                foundIdx = i;
                break;
            }
        }
        if (foundIdx == null) {
            log.atWarn().log("[MethodBinderBuilder] Parameter name '{}' not found for method {}", paramName,
                    getMethodName());
            throw new BuilderException(
                    "Parameter name " + paramName + " not found for method " + getMethodName());
        }
        return withParam(foundIdx, supplier, acceptNullable);
    }

    @Override
    public T withParam(Object parameter) throws CoreException {
        return withParam(parameter, false);
    }

    @Override
    public T withParam(IObjectSupplierBuilder<?> supplier) throws CoreException {
        return withParam(supplier, false);
    }

    @Override
    public T withParam(Object parameter, boolean acceptNullable) throws CoreException {
        Objects.requireNonNull(this.method, "[MethodBinderBuilder] Method must be set before setting parameters");
        int idx = findNextFreeParameterIndex();
        if (idx < 0) {
            log.atWarn().log("[MethodBinderBuilder] No free parameter slot available for method {}", getMethodName());
            throw new BuilderException("No free parameter slot available");
        }
        return withParam(idx, parameter, acceptNullable);
    }

    @Override
    public T withParam(IObjectSupplierBuilder<?> supplier, boolean acceptNullable) throws CoreException {
        Objects.requireNonNull(this.method, "[MethodBinderBuilder] Method must be set before setting parameters");
        Objects.requireNonNull(supplier, "supplier cannot be null");
        int idx = findNextFreeParameterIndex();
        if (idx < 0) {
            log.atWarn().log("[MethodBinderBuilder] No free parameter slot available for method {}", getMethodName());
            throw new BuilderException("No free parameter slot available");
        }
        return withParam(idx, supplier, acceptNullable);
    }

    private int findNextFreeParameterIndex() {
        for (int i = 0; i < this.parameters.size(); i++) {
            if (this.parameters.get(i) == null) {
                return i;
            }
        }
        return -1;
    }

    private boolean isValidParameterIndex(int i) {
        boolean valid = i >= 0 && i < this.parameterTypes.length;
        log.atTrace().log("[MethodBinderBuilder] Parameter index {} validity: {}", i, valid);
        return valid;
    }

    private boolean isValidParameterType(int index, Object object) {
        return this.isValidParameterType(index, object.getClass());
    }

    private boolean isValidParameterType(int index, Class<?> object) {
        boolean valid = this.parameterTypes[index].isAssignableFrom(object);
        log.atTrace().log("[MethodBinderBuilder] Parameter type check for index {} expected {} got {} validity={}",
                index, this.parameterTypes[index], object, valid);
        return valid;
    }

    @Override
    public IMethodBinder build() throws CoreException {
        log.atTrace().log("[MethodBinderBuilder] Building MethodBinder");
        Objects.requireNonNull(this.method, "Method is not set");
        Objects.requireNonNull(this.parameters, "Parameters are not set");
        Objects.requireNonNull(this.parameterNullableAllowed, "Parameter nullability metadata not initialized");

        // build supplier instances and wrap them with nullable-aware wrapper
        List<IObjectSupplier<?>> builtParameterSuppliers = new ArrayList<>(this.parameters.size());
        for (int i = 0; i < this.parameters.size(); i++) {
            IObjectSupplierBuilder<?> builder = this.parameters.get(i);
            if (builder == null) {
                log.atWarn().log("[MethodBinderBuilder] Parameter {} has no supplier configured for method {}", i,
                        getMethodName());
                throw new BuilderException(
                        "Parameter " + i + " not configured for method " + getMethodName());
            }
            IObjectSupplier<?> supplierInstance;
            try {
                supplierInstance = builder.build();
            } catch (CoreException e) {
                log.atError().log("[MethodBinderBuilder] Error building supplier for parameter {} of method {}", i,
                        getMethodName(), e);
                throw e;
            }
            boolean allowNull = Boolean.TRUE.equals(this.parameterNullableAllowed.get(i));
            builtParameterSuppliers
                    .add(new NullableEnforcingSupplier<>(supplierInstance, allowNull, i, getMethodName()));
        }

        IObjectSupplier<?> ownerSupplier = this.supplier.build();
        // create MethodBinder with existing constructor (kept unchanged)
        return new MethodBinder(ownerSupplier, this.method, builtParameterSuppliers, this.collection);
    }

    @Override
    public V up() {
        log.atTrace().log("[MethodBinderBuilder] Returning up object {}", this.up);
        return this.up;
    }

    // -----------------------
    // Helper: nullable-enforcing wrapper
    // -----------------------
    public static class NullableEnforcingSupplier<T> implements IObjectSupplier<T> {
        private final IObjectSupplier<T> delegate;
        private final boolean allowNull;
        private final int index;
        private final String methodName;
        @Setter
        private IApplicationContext applicationContext;
        @Setter
        private IExecutionContext executionContext;

        NullableEnforcingSupplier(IObjectSupplier<T> delegate, boolean allowNull, int index, String methodName) {
            this.delegate = Objects.requireNonNull(delegate);
            this.allowNull = allowNull;
            this.index = index;
            this.methodName = methodName;
        }

        public Class<?> isContextNeeded() {
            if (ContextualObjectSupplier.class.isAssignableFrom(this.delegate.getClass())) {
                if (((ContextualObjectSupplier<?, ?>) this.delegate).getContextClass()
                        .isAssignableFrom(IApplicationContext.class)) {
                    return IApplicationContext.class;
                }
                if (((ContextualObjectSupplier<?, ?>) this.delegate).getContextClass()
                        .isAssignableFrom(IExecutionContext.class)) {
                    return IExecutionContext.class;
                }
            }
            return null;
        }

        @Override
        public Optional<T> getObject() throws CoreException {
            Optional<T> o = delegate.getObject();
            if (!allowNull && (o == null || !o.isPresent())) {
                String msg = String.format(
                        "Supplier for parameter %d of method %s returned null but parameter is not nullable", index,
                        methodName);
                log.atError().log("[MethodBinderBuilder] " + msg);
                throw new SupplyException(msg);
            }
            return o == null ? Optional.empty() : o;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<T> getObjectClass() {
            return delegate.getObjectClass();
        }

        @Override
        public Optional<T> getObject(IApplicationContext aContext, IExecutionContext eContext) throws CoreException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getObject'");
        }

        @Override
        public Optional<T> getObject(IExecutionContext context) throws CoreException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getObject'");
        }

        @Override
        public Optional<T> getObject(IApplicationContext context) throws CoreException {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getObject'");
        }
    }
}
