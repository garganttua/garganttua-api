package com.garganttua.api.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.core.builder.resolver.FieldResolver;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

public class FieldResolverTest {

    private IGGObjectQuery mockQuery;
    private Class<?> entityClass;

    static class TestEntity {
        String name;
        Integer age;
    }

    @BeforeEach
    void setup() {
        mockQuery = Mockito.mock(IGGObjectQuery.class);
        entityClass = TestEntity.class;
    }

    // -----------------------------
    // fieldByFieldName
    // -----------------------------

    @Test
    void fieldByFieldName_success() throws Exception {
        IGGObjectQuery query = GGObjectQueryFactory.objectQuery(entityClass);

        GGObjectAddress result = FieldResolver.fieldByFieldName("age", query, entityClass);
        assertEquals("age", result.toString());
    }

    @Test
    void fieldByFieldName_addressNotFound_throws() throws Exception {
        when(mockQuery.address("invalid")).thenReturn(null);

        BuilderException ex = assertThrows(BuilderException.class,
                () -> FieldResolver.fieldByFieldName("invalid", mockQuery, entityClass));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void fieldByFieldName_reflectionException_throws() throws Exception {
        when(mockQuery.address("name")).thenThrow(new GGReflectionException("boom"));

        BuilderException ex = assertThrows(BuilderException.class,
                () -> FieldResolver.fieldByFieldName("name", mockQuery, entityClass));
        assertTrue(ex.getMessage().contains("not found"));
    }

    // -----------------------------
    // fieldByField
    // -----------------------------

    @Test
    void fieldByField_success() throws Exception {
        Field field = entityClass.getDeclaredField("age");

        GGObjectAddress result = FieldResolver.fieldByField(field, entityClass);
        assertEquals("age", result.toString());
    }

    @Test
    void fieldByField_wrongType_throws() throws Exception {
        Field field = entityClass.getDeclaredField("age");

        BuilderException ex = assertThrows(BuilderException.class,
                () -> FieldResolver.fieldByField(field, entityClass, String.class));
        assertTrue(ex.getMessage().contains("not of type"));
    }

    @Test
    void fieldByField_notFound_throws() throws Exception {
        Field fakeField = String.class.getDeclaredField("value"); // pas dans TestEntity

        BuilderException ex = assertThrows(BuilderException.class,
                () -> FieldResolver.fieldByField(fakeField, entityClass));
        assertTrue(ex.getMessage().contains("not found"));
    }

    // -----------------------------
    // fieldByAddress
    // -----------------------------

    @Test
    void fieldByAddress_success() throws Exception {
        IGGObjectQuery query = GGObjectQueryFactory.objectQuery(entityClass);
        GGObjectAddress address = query.address("name");

        GGObjectAddress result = FieldResolver.fieldByAddress(address, query, entityClass, String.class);
        assertEquals("name", result.toString());
    }

    @Test
    void fieldByAddress_leafNotField_throws() throws Exception {
        GGObjectAddress address = mock(GGObjectAddress.class);

        when(mockQuery.find(address)).thenReturn(List.of(entityClass, "notAField"));

        BuilderException ex = assertThrows(BuilderException.class,
                () -> FieldResolver.fieldByAddress(address, mockQuery, entityClass, null));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void fieldByAddress_wrongType_throws() throws Exception {
        GGObjectAddress address = mock(GGObjectAddress.class);
        Field field = entityClass.getDeclaredField("age");

        when(mockQuery.find(address)).thenReturn(List.of(entityClass, field));

        BuilderException ex = assertThrows(BuilderException.class,
                () -> FieldResolver.fieldByAddress(address, mockQuery, entityClass, String.class));
        assertTrue(ex.getMessage().contains("not of type"));
    }

    @Test
    void fieldByAddress_reflectionException_throws() throws Exception {
        GGObjectAddress address = mock(GGObjectAddress.class);

        when(mockQuery.find(address)).thenThrow(new GGReflectionException("boom"));

        BuilderException ex = assertThrows(BuilderException.class,
                () -> FieldResolver.fieldByAddress(address, mockQuery, entityClass, null));
        assertTrue(ex.getMessage().contains("boom"));
    }
}