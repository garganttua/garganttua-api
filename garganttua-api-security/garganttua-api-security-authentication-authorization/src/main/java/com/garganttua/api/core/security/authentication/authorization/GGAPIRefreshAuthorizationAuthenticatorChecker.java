package com.garganttua.api.core.security.authentication.authorization;

import java.util.HashMap;
import java.util.Map;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorRefreshToken;
import com.garganttua.reflection.GGReflectionException;
import com.garganttua.reflection.query.GGObjectQueryFactory;
import com.garganttua.reflection.query.IGGObjectQuery;
import com.garganttua.reflection.utils.GGObjectReflectionHelper;

public class GGAPIRefreshAuthorizationAuthenticatorChecker {

    private static Map<Class<?>, GGAPIRefreshAuthorizationAuthenticatorInfos> infos = new HashMap<Class<?>, GGAPIRefreshAuthorizationAuthenticatorInfos>();

    public static GGAPIRefreshAuthorizationAuthenticatorInfos checkEntityAuthenticatorClass(
            Class<? extends Object> entityAuthenticatorClass) throws GGAPISecurityException {
        if (GGAPIRefreshAuthorizationAuthenticatorChecker.infos.containsKey(entityAuthenticatorClass)) {
            return GGAPIRefreshAuthorizationAuthenticatorChecker.infos.get(entityAuthenticatorClass);
        }

        String refreshTokenFieldName = null;

        try {
            refreshTokenFieldName = GGAPIRefreshAuthorizationAuthenticatorChecker
                    .checkRefreshTokenAnnotationPresentAndFieldHasGoodType(entityAuthenticatorClass);
        } catch (GGAPIException e) {
            throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
                    "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                            + " does not have a field annotated with @GGAPIAuthenticatorKeyRealm");
        }

        IGGObjectQuery q;
        try {
            q = GGObjectQueryFactory.objectQuery(entityAuthenticatorClass);
            GGAPIRefreshAuthorizationAuthenticatorInfos authenticatorinfos = new GGAPIRefreshAuthorizationAuthenticatorInfos(
                    q.address(refreshTokenFieldName));

            GGAPIRefreshAuthorizationAuthenticatorChecker.infos.put(entityAuthenticatorClass, authenticatorinfos);
            return authenticatorinfos;
        } catch (Exception e) {
            throw new GGAPISecurityException(e);
        }
    }

    private static String checkRefreshTokenAnnotationPresentAndFieldHasGoodType(
            Class<? extends Object> entityAuthenticatorClass) throws GGAPISecurityException {
        String fieldAddress;
        try {
            fieldAddress = GGObjectReflectionHelper.getFieldAddressAnnotatedWithAndCheckType(entityAuthenticatorClass,
                    GGAPIAuthenticatorRefreshToken.class, byte[].class);
            if (fieldAddress == null) {
                throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
                        "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                                + " does not have any field annotated with @GGAPIAuthenticatorRefreshToken");
            }
        } catch (GGReflectionException e) {
            throw new GGAPISecurityException(GGAPIExceptionCode.ENTITY_DEFINITION,
                    "Entity Authenticator " + entityAuthenticatorClass.getSimpleName()
                            + " does not have any field annotated with @GGAPIAuthenticatorRefreshToken",
                    e);
        }
        return fieldAddress;
    }
}
