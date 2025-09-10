package com.garganttua.api.core.engine;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.IGGObjectQuery;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FieldResolver {

    public static Field fieldByFieldName(String fieldName, IGGObjectQuery objectQuery, Class<?> entityClass)
            throws BuilderException {
        log.atTrace().log("Entering fieldByFieldName with fieldName={}, entityClass={}", fieldName, entityClass);
        return FieldResolver.fieldByFieldName(fieldName, objectQuery, entityClass, null);
    }

    public static Field fieldByField(Field field, Class<?> entityClass) throws BuilderException {
        log.atTrace().log("Entering fieldByField with field={}, entityClass={}", field, entityClass);
        return FieldResolver.fieldByField(field, entityClass, null);
    }

    public static Field fieldByAddress(GGObjectAddress fieldAddress, IGGObjectQuery objectQuery, Class<?> entityClass)
            throws BuilderException {
        log.atTrace().log("Entering fieldByAddress with fieldAddress={}, entityClass={}", fieldAddress, entityClass);
        return FieldResolver.fieldByAddress(fieldAddress, objectQuery, entityClass, null);
    }

    public static Field fieldByFieldName(String fieldName, IGGObjectQuery objectQuery, Class<?> entityClass,
            Class<?> fieldType) throws BuilderException {
        log.atDebug().log("Resolving field by name: {} an type: {} in entity: {}", fieldName, fieldType, entityClass);

        Objects.requireNonNull(fieldName, "Field name cannot be null");
        Objects.requireNonNull(objectQuery, "Object query cannot be null");
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            GGObjectAddress address = objectQuery.address(fieldName);
            log.atTrace().log("Resolved GGObjectAddress={} for fieldName={}", address, fieldName);

            if (address == null) {
                log.atWarn().log("Field {} not found in entity {}", fieldName, entityClass.getName());
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Field " + fieldName + " not found in entity " + entityClass.getName());
            }

            return FieldResolver.fieldByAddress(address, objectQuery, entityClass, fieldType);
        } catch (GGReflectionException e) {
            log.atError().log("Reflection error resolving field {} in entity {}", fieldName, entityClass.getName());
            throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                    "Field " + fieldName + " not found in entity " + entityClass.getName(), e);
        }
    }

    public static Field fieldByField(Field field, Class<?> entityClass, Class<?> fieldType) throws BuilderException {
        log.atDebug().log("Resolving field by direct Field object: {} an type: {} in entity {}", field, fieldType, entityClass);

        Objects.requireNonNull(field, "Field cannot be null");
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            Field found = List.of(entityClass.getDeclaredFields()).stream()
                    .peek(f -> log.atTrace().log("Checking declared field: {}", f))
                    .filter(f -> f.equals(field))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.atWarn().log("Field {} not found in entity {}", field.getName(), entityClass.getName());
                        return new BuilderException(CoreExceptionCode.BUILDER_CODE,
                                "Field " + field.getName() + " not found in entity " + entityClass.getName());
                    });

            if (fieldType != null && !fieldType.isAssignableFrom(found.getType())) {
                log.atWarn().log("Field {} in entity {} has type {} but expected {}",
                        field.getName(), entityClass.getName(), found.getType(), fieldType);
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Field " + field.getName() + " in entity " + entityClass.getName()
                                + " is not of type " + fieldType.getName());
            }

            log.atInfo().log("Successfully resolved field {} in entity {}", found.getName(), entityClass.getName());
            return found;

        } catch (BuilderException | SecurityException e) {
            log.atError().log("Error resolving field {} in entity {}", field.getName(), entityClass.getName());
            throw new BuilderException(CoreExceptionCode.CORE_GENERIC_CODE, e.getMessage(), e);
        }
    }

    public static Field fieldByAddress(GGObjectAddress fieldAddress, IGGObjectQuery objectQuery, Class<?> entityClass,
            Class<?> fieldType) throws BuilderException {
        log.atDebug().log("Resolving field by address: {} an type: {} in entity {}", fieldAddress, fieldType, entityClass);

        Objects.requireNonNull(fieldAddress, "Field address name cannot be null");
        Objects.requireNonNull(objectQuery, "Object query cannot be null");
        Objects.requireNonNull(entityClass, "Entity class cannot be null");

        try {
            List<Object> struct = objectQuery.find(fieldAddress);
            log.atTrace().log("Object query returned structure: {}", struct);

            Object leaf = struct.getLast();
            log.atTrace().log("Leaf object resolved: {}", leaf);

            if (!Field.class.isAssignableFrom(leaf.getClass())) {
                log.atWarn().log("Leaf object {} is not a Field", leaf);
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Field " + fieldAddress.toString() + " not found in entity " + entityClass.getName());
            }

            Field field = (Field) leaf;

            if (fieldType != null && !fieldType.isAssignableFrom(field.getType())) {
                log.atWarn().log("Field {} in entity {} has type {} but expected {}",
                        field.getName(), entityClass.getName(), field.getType(), fieldType);
                throw new BuilderException(CoreExceptionCode.BUILDER_CODE,
                        "Field " + field.getName() + " in entity " + entityClass.getName()
                                + " is not of type " + fieldType.getName());
            }

            log.atInfo().log("Successfully resolved field {} in entity {}", field.getName(), entityClass.getName());
            return field;
        } catch (GGReflectionException e) {
            log.atError().log("Reflection error resolving field {} in entity {}", fieldAddress, entityClass.getName());
            throw new BuilderException(CoreExceptionCode.CORE_GENERIC_CODE, e.getMessage(), e);
        }
    }
}
