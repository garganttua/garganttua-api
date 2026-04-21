package com.garganttua.api.core.security.authentication.authorization;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.filter.Filter;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.core.CoreException;
import com.garganttua.api.commons.CoreExceptionCode;
import com.garganttua.api.commons.caller.ICaller;
import com.garganttua.api.commons.context.IApi;
import com.garganttua.api.commons.context.IDomain;
import com.garganttua.api.commons.security.annotations.Authentication;
import com.garganttua.api.commons.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.commons.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.commons.service.IOperationResponse;
import com.garganttua.api.commons.service.OperationResponseCode;
import com.garganttua.core.reflection.ObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Authentication(findPrincipal = true)
public class RefreshAuthorisationAuthentication extends AbstractAuthentication {

    public RefreshAuthorisationAuthentication() {
        super(null);
    }

    public RefreshAuthorisationAuthentication(IDomain<?> domainContext) {
        super(domainContext);
    }

    @Inject
    private IApi apiContext;
    private Object authorizationToBeRevoked;

    @Override
    protected Object doFindPrincipal(ICaller caller) {


        byte[] refreshToken = ((String) this.credential).getBytes();

        // 1 récuperer le token access à partir du refresh token
        Optional<Object> authorization;
        try {

            authorization = this.findAuthorization(caller, refreshToken);
            // 2 récupérer le user à partir du ownerId du access token
            if (authorization.isPresent()) {
                this.authorizationToBeRevoked = authorization.get();
                if (EntityAuthorizationHelper.isAuthorization(authorizationToBeRevoked)) {
                    Optional<Object> principal = this.findPrincipalFromAuthorization(caller, authorization.get());
                    if (principal.isPresent()) {
                        return principal.get();
                    }
                }
            }
        } catch (CoreException e) {
            log.atDebug().log("Authentication failed", e);
        }

        return null;
    }

    private Optional<Object> findPrincipalFromAuthorization(ICaller caller, Object authorization) throws CoreException {
        String owner = EntityAuthorizationHelper.getOwnerId(authorization);
        String ownerDomainName = owner.split(":")[0];
        String ownerUuid = owner.split(":")[1];
        IDomain<?> ownerDomainCtx = this.apiContext.getDomain(ownerDomainName).orElse(null);
        if (ownerDomainCtx == null) return Optional.empty();
        IOperationResponse response = ownerDomainCtx.readOne(ownerUuid, caller);
        if (response.getResponseCode() == OperationResponseCode.OK) {
            return Optional.ofNullable(response.getResponse());
        }
        return Optional.empty();
    }

    private Optional<Object> findAuthorization(ICaller caller, byte[] refreshToken) throws SecurityException {

        ObjectAddress refreshTokenFieldName = RefreshAuthorizationAuthenticatorChecker
                .checkEntityAuthenticatorClass((Class<?>) this.domainContext.getEntityClass().getType()).refreshTokenFieldAddress();

        Filter filter = Filter.eq(refreshTokenFieldName.toString(), Base64.getDecoder().decode(refreshToken));
        IOperationResponse response = this.authenticatorDomain.readAll(filter, null, null, caller);

        if( response.getResponseCode() == OperationResponseCode.OK ){
            if( ((List) response.getResponse()).size() == 1 )
                return Optional.ofNullable(((List) response.getResponse()).get(0));
        }
        return Optional.ofNullable(null);
    }

    @Override
    protected void doAuthentication() throws CoreException {
        if (this.authorizationToBeRevoked == null)
            throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Principal not found");

        if (!EntityAuthorizationHelper.isRenewable(this.authorizationToBeRevoked.getClass())) {
            throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authorization not renewable");
        }

        if (EntityAuthorizationHelper
                .isRefreshTokenExpired(this.authorizationToBeRevoked)) {
            throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Refresh authorization expired");
        }

        if (EntityAuthorizationHelper.isRevoked(this.authorizationToBeRevoked)) {
            throw new SecurityException(CoreExceptionCode.GENERIC_SECURITY_ERROR, "Authorization revoked");
        }

        EntityAuthorizationHelper.revoke(this.authorizationToBeRevoked);
        String uuid = EntityAuthorizationHelper.getUuid(this.authorizationToBeRevoked);
        this.authenticatorDomain.updateOne(uuid, this.authorizationToBeRevoked, Caller.createSuperCaller());
        this.authenticated = true;
    }

    @AuthenticatorSecurityPreProcessing
    public void applySecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
        // Nothing to do
    }

    @AuthenticatorSecurityPostProcessing
    public void postProcessSecurityOnAuthenticator(ICaller caller, Object entity, Map<String, String> params) {
        // Nothing to do
    }

}
