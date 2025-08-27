package com.garganttua.api.core.security.authentication.authorization;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.security.annotations.AuthenticatorRefreshToken;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

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
            throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
                    "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                            + " does not have a field annotated with @AuthenticatorKeyRealm");
        }

        IGGObjectQuery q;
        try {
            q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
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
            fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
                    AuthenticatorRefreshToken.class, byte[].class);
            if (fieldAddress == null) {
                throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
                        "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                                + " does not have any field annotated with @AuthenticatorRefreshToken");
            }
        } catch (GGReflectionException e) {
            throw new SecurityException(CoreExceptionCode.ENTITY_DEFINITION,
                    "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                            + " does not have any field annotated with @AuthenticatorRefreshToken",
                    e);
        }
        return fieldAddress;
    }
}
