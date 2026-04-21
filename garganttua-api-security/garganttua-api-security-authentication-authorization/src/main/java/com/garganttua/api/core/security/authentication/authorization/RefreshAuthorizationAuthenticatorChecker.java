package com.garganttua.api.core.security.authentication.authorization;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.mapper.DefaultMapper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.commons.CoreExceptionCode;
import com.garganttua.core.CoreException;
import com.garganttua.core.reflection.IObjectQuery;
import com.garganttua.core.reflection.ReflectionException;
import com.garganttua.core.reflection.query.ObjectQueryFactory;
import com.garganttua.core.reflection.runtime.RuntimeReflectionProvider;
import com.garganttua.api.commons.security.annotations.AuthenticatorRefreshToken;

public class RefreshAuthorizationAuthenticatorChecker {

    private static Map<Class<?>, RefreshAuthorizationAuthenticatorInfos> infos = new HashMap<Class<?>, RefreshAuthorizationAuthenticatorInfos>();

    public static RefreshAuthorizationAuthenticatorInfos checkEntityAuthenticatorClass(
            Class<? extends Object> entityAuthenticatorClass) throws SecurityException {
        if (RefreshAuthorizationAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass)) {
            return RefreshAuthorizationAuthenticatorChecker.infos.get(entityAuthenticatorClass);
        }

        String refreshTokenFieldName = null;

        try {
            refreshTokenFieldName = RefreshAuthorizationAuthenticatorChecker
                    .checkRefreshTokenAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
        } catch (CoreException e) {
            throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
                    "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                            + " does not have a field annotated with @AuthenticatorKeyRealm");
        }

        IObjectQuery<?> q;
        try {
            q = ObjectQueryFactory.objectQuery(DefaultMapper.reflection().extractClass(entityAuthenticatorClass), new RuntimeReflectionProvider());
            RefreshAuthorizationAuthenticatorInfos authenticatorinfos = new RefreshAuthorizationAuthenticatorInfos(
                    q.address(refreshTokenFieldName));

            RefreshAuthorizationAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
            return authenticatorinfos;
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    private static String checkRefreshTokenAnnotationPresentAndFieldHasGoodType(
            Class<? extends Object> entityAuthenticatorClass) throws SecurityException {
        String fieldAddress;
        try {
            fieldAddress = findFieldAnnotatedWith(entityAuthenticatorClass, AuthenticatorRefreshToken.class);
            if (fieldAddress == null) {
                throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
                        "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                                + " does not have any field annotated with @AuthenticatorRefreshToken");
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR,
                    "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                            + " does not have any field annotated with @AuthenticatorRefreshToken",
                    e);
        }
        return fieldAddress;
    }

    private static String findFieldAnnotatedWith(Class<?> clazz, Class<? extends java.lang.annotation.Annotation> annotation) {
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(annotation) != null) return field.getName();
        }
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            return findFieldAnnotatedWith(clazz.getSuperclass(), annotation);
        }
        return null;
    }
}
