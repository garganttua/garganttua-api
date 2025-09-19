package com.garganttua.api.core.engine;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MethodResolver {

    public static Method methodByName(String methodName, IGGObjectQuery objectQuery, Class<?> entityClass)
            throws BuilderException {
        log.atTrace().log("Entering methodByName with methodName={}, entityClass={}", methodName, entityClass);
        return MethodResolver.methodByName(methodName, objectQuery, entityClass, null);
    }

    public static Method methodByMethod(Method method, Class<?> entityClass) throws BuilderException {
        log.atTrace().log("Entering methodByMethod with method={}, entityClass={}", method, entityClass);
        return MethodResolver.methodByMethod(method, entityClass, null);
    }

    public static Method methodByAddress(GGObjectAddress methodAddress, IGGObjectQuery objectQuery, Class<?> entityClass)
            throws BuilderException {
        log.atTrace().log("Entering methodByAddress with methodAddress={}, entityClass={}", methodAddress, entityClass);
        return MethodResolver.methodByAddress(methodAddress, objectQuery, entityClass, null);
    }

    public static Method methodByName(String methodName, IGGObjectQuery objectQuery, Class<?> entityClass,
            Class<?> returnType, Class<?>... parameterTypes) throws BuilderException {
        log.atDebug().log("Resolving method by name: {}, returnType: {}, parameterTypes={} in entity: {}", 
                methodName, returnType, Arrays.toString(parameterTypes), entityClass);

        Objects.requireNonNull(methodName, "Method name cannot be null");
        Objects.requireNonNull(objectQuery, "Object query cannot be null");
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            GGObjectAddress address = objectQuery.address(methodName);
            log.atTrace().log("Resolved GGObjectAddress={} for methodName={}", address, methodName);

            if (address == null) {
                log.atWarn().log("Method {} not found in entity {}", methodName, entityClass.getName());
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Method " + methodName + " not found in entity " + entityClass.getName());
            }

            return MethodResolver.methodByAddress(address, objectQuery, entityClass, returnType, parameterTypes);
        } catch (GGReflectionException e) {
            log.atError().log("Reflection error resolving method {} in entity {}", methodName, entityClass.getName());
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Method " + methodName + " not found in entity " + entityClass.getName(), e);
        }
    }

    public static Method methodByMethod(Method method, Class<?> entityClass, Class<?> returnType, Class<?>... parameterTypes)
            throws BuilderException {
        log.atDebug().log("Resolving method by direct Method object: {}, returnType: {}, parameterTypes={} in entity {}", 
                method, returnType, Arrays.toString(parameterTypes), entityClass);

        Objects.requireNonNull(method, "Method cannot be null");
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            Method found = List.of(entityClass.getDeclaredMethods()).stream()
                    .peek(m -> log.atTrace().log("Checking declared method: {}", m))
                    .filter(m -> m.equals(method))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.atWarn().log("Method {} not found in entity {}", method.getName(), entityClass.getName());
                        return new BuilderException(CoreExceptionCode.BUILDER_CODE,
                                "Method " + method.getName() + " not found in entity " + entityClass.getName());
                    });

            validateSignature(found, returnType, parameterTypes, entityClass);
            log.atInfo().log("Successfully resolved method {} in entity {}", found.getName(), entityClass.getName());
            return found;

        } catch (BuilderException | SecurityException e) {
            log.atError().log("Error resolving method {} in entity {}", method.getName(), entityClass.getName());
            throw new BuilderException(CoreExceptionCode.CORE_GENERIC_CODE, e.getMessage(), e);
        }
    }

    public static Method methodByAddress(GGObjectAddress methodAddress, IGGObjectQuery objectQuery, Class<?> entityClass,
            Class<?> returnType, Class<?>... parameterTypes) throws BuilderException {
        log.atDebug().log("Resolving method by address: {}, returnType: {}, parameterTypes={} in entity {}", 
                methodAddress, returnType, Arrays.toString(parameterTypes), entityClass);

        Objects.requireNonNull(methodAddress, "Method address cannot be null");
        Objects.requireNonNull(objectQuery, "Object query cannot be null");
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            List<Object> struct = objectQuery.find(methodAddress);
            log.atTrace().log("Object query returned structure: {}", struct);

            Object leaf = struct.getLast();
            log.atTrace().log("Leaf object resolved: {}", leaf);

            if (!Method.class.isAssignableFrom(leaf.getClass())) {
                log.atWarn().log("Leaf object {} is not a Method", leaf);
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Method " + methodAddress.toString() + " not found in entity " + entityClass.getName());
            }

            Method method = (Method) leaf;
            validateSignature(method, returnType, parameterTypes, entityClass);

            log.atInfo().log("Successfully resolved method {} in entity {}", method.getName(), entityClass.getName());
            return method;

        } catch (GGReflectionException e) {
            log.atError().log("Reflection error resolving method {} in entity {}", methodAddress, entityClass.getName());
            throw new BuilderException(CoreExceptionCode.CORE_GENERIC_CODE, e.getMessage(), e);
        }
    }

    private static void validateSignature(Method method, Class<?> returnType, Class<?>[] parameterTypes, Class<?> entityClass)
            throws BuilderException {
        if (returnType != null && !returnType.isAssignableFrom(method.getReturnType())) {
            log.atWarn().log("Method {} in entity {} has return type {} but expected {}",
                    method.getName(), entityClass.getName(), method.getReturnType(), returnType);
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Method " + method.getName() + " in entity " + entityClass.getName()
                            + " does not return type " + returnType.getName());
        }

        if (parameterTypes != null && parameterTypes.length > 0) {
            Class<?>[] actualParams = method.getParameterTypes();
            if (actualParams.length != parameterTypes.length) {
                log.atWarn().log("Method {} in entity {} has {} parameters but expected {}",
                        method.getName(), entityClass.getName(), actualParams.length, parameterTypes.length);
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Method " + method.getName() + " in entity " + entityClass.getName()
                                + " has " + actualParams.length + " parameters but expected " + parameterTypes.length);
            }
            for (int i = 0; i < actualParams.length; i++) {
                if (!actualParams[i].isAssignableFrom(parameterTypes[i])) {
                    log.atWarn().log("Parameter {} of method {} in entity {} has type {} but expected {}",
                            i, method.getName(), entityClass.getName(), actualParams[i], parameterTypes[i]);
                    throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                            "Parameter " + i + " of method " + method.getName() + " in entity "
                                    + entityClass.getName() + " has type " + actualParams[i].getName()
                                    + " but expected " + parameterTypes[i].getName());
                }
            }
        }
    }
}
