package com.garganttua.api.core.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.garganttua.api.core.builder.BuilderException;
import com.garganttua.api.core.builder.resolver.MethodResolver;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.reflection.GGObjectAddress;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;

class MethodResolverTest {

    static class DummyEntity {
        public String greet(String name) {
            return "Hello " + name;
        }

        public void noArgs() {
        }
    }

    @Test
    @DisplayName("methodByName doit résoudre une méthode existante")
    void testMethodByNameResolvesSuccessfully() throws Exception {
        IGGObjectQuery query = GGObjectQueryFactory.objectQuery(DummyEntity.class);

        GGObjectAddress result = MethodResolver.methodByName("greet", query, DummyEntity.class, String.class,
                String.class);

        assertNotNull(result);
        assertEquals("greet", result.toString());
    }

    @Test
    @DisplayName("methodByName doit lancer une exception si la méthode est introuvable")
    void testMethodByNameThrowsWhenNotFound() throws Exception {
        IGGObjectQuery query = mock(IGGObjectQuery.class);
        when(query.address("unknown")).thenReturn(null);

        BuilderException ex = assertThrows(BuilderException.class,
                () -> MethodResolver.methodByName("unknown", query, DummyEntity.class, void.class));

        assertTrue(ex.getMessage().contains("not found"));
        assertEquals(CoreExceptionCode.BUILDER_CODE, ex.getCode());
    }

    @Test
    @DisplayName("methodByName doit relayer une GGReflectionException")
    void testMethodByNameThrowsOnReflectionError() throws Exception {
        IGGObjectQuery query = mock(IGGObjectQuery.class);
        when(query.address("greet")).thenThrow(new GGReflectionException("reflection failed"));

        BuilderException ex = assertThrows(BuilderException.class,
                () -> MethodResolver.methodByName("greet", query, DummyEntity.class));

        assertEquals(CoreExceptionCode.BUILDER_CODE, ex.getCode());
    }

    @Test
    @DisplayName("methodByMethod doit trouver une méthode par objet Method")
    void testMethodByMethodResolvesSuccessfully() throws Exception {
        Method expected = DummyEntity.class.getDeclaredMethod("noArgs");

        GGObjectAddress result = MethodResolver.methodByMethod(expected, DummyEntity.class, void.class);

        assertNotNull(result);
        assertEquals("noArgs", result.toString());
    }

    @Test
    @DisplayName("methodByMethod doit lancer une exception si la méthode n'appartient pas à la classe")
    void testMethodByMethodThrowsIfNotInEntity() throws Exception {
        Method foreignMethod = String.class.getDeclaredMethod("isEmpty");

        BuilderException ex = assertThrows(BuilderException.class,
                () -> MethodResolver.methodByMethod(foreignMethod, DummyEntity.class));

        assertEquals(CoreExceptionCode.BUILDER_CODE, ex.getCode());
    }

    @Test
    @DisplayName("methodByAddress doit résoudre une méthode valide")
    void testMethodByAddressResolvesSuccessfully() throws Exception {
        IGGObjectQuery query = GGObjectQueryFactory.objectQuery(DummyEntity.class);

        GGObjectAddress result = MethodResolver.methodByAddress(query.address("greet"), query, DummyEntity.class, String.class,
                String.class);

        assertEquals("greet", result.toString());
    }

    @Test
    @DisplayName("methodByAddress doit lancer une exception si le leaf n'est pas un Method")
    void testMethodByAddressThrowsIfLeafNotMethod() throws Exception {
        IGGObjectQuery query = mock(IGGObjectQuery.class);
        GGObjectAddress address = mock(GGObjectAddress.class);

        when(query.find(address)).thenReturn(List.of("not a method"));

        BuilderException ex = assertThrows(BuilderException.class,
                () -> MethodResolver.methodByAddress(address, query, DummyEntity.class));

        assertEquals(CoreExceptionCode.BUILDER_CODE, ex.getCode());
    }

    @Test
    @DisplayName("validateSignature doit échouer si le returnType est incorrect")
    void testValidateSignatureWrongReturnType() throws Exception {
        Method method = DummyEntity.class.getDeclaredMethod("noArgs");

        BuilderException ex = assertThrows(BuilderException.class,
                () -> MethodResolver.methodByMethod(method, DummyEntity.class, String.class));

        assertEquals(CoreExceptionCode.BUILDER_CODE, ex.getCode());
    }

    @Test
    @DisplayName("validateSignature doit échouer si les paramètres ne correspondent pas")
    void testValidateSignatureWrongParams() throws Exception {
        Method method = DummyEntity.class.getDeclaredMethod("greet", String.class);

        BuilderException ex = assertThrows(BuilderException.class,
                () -> MethodResolver.methodByMethod(method, DummyEntity.class, String.class, Integer.class));

        assertEquals(CoreExceptionCode.BUILDER_CODE, ex.getCode());
    }
}
