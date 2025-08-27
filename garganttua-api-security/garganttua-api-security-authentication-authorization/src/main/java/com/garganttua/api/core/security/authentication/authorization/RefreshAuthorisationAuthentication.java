package com.garganttua.api.core.security.authentication.authorization;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.garganttua.api.core.caller.Caller;
import com.garganttua.api.core.entity.tools.EntityHelper;
import com.garganttua.api.core.filter.Literal;
import com.garganttua.api.core.security.authentication.AbstractAuthentication;
import com.garganttua.api.core.security.authorization.EntityAuthorizationHelper;
import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.CoreExceptionCode;
import com.garganttua.api.spec.caller.ICaller;
import com.garganttua.api.spec.domain.IDomain;
import com.garganttua.api.spec.engine.IEngine;
import com.garganttua.api.spec.security.annotations.Authentication;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.AuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.service.ReadOutputMode;
import com.garganttua.api.spec.service.ServiceResponseCode;
import com.garganttua.api.spec.service.IServiceResponse;
import com.garganttua.reflection.GGObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Authentication(findPrincipal = true)
public class RefreshAuthorisationAuthentication extends AbstractAuthentication {

    public RefreshAuthorisationAuthentication() {
        super(null);
    }

    public RefreshAuthorisationAuthentication(IDomain domain) {
        super(domain);
    }

    @Inject
    private IEngine engine;
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
        String ownerDomainName = EntityHelper.getDomainNameFromOwnerId(owner);
        String ownerUuid = EntityHelper.getUuidFromOwnerId(owner);
        return this.engine.getRepository(ownerDomainName).getOneByUuid(caller, ownerUuid);
    }

    private Optional<Object> findAuthorization(ICaller caller, byte[] refreshToken) throws SecurityException {

        GGObjectAddress refreshTokenFieldName = RefreshAuthorizationAuthenticatorChecker
                .checkEntityAuthenticatorClass(this.domain.getEntityClass()).refreshTokenFieldAddress();

        Literal filter = Literal.eq(refreshTokenFieldName.toString(), Base64.getDecoder().decode(refreshToken));
        IServiceResponse response = this.authenticatorService.getEntities(caller, ReadOutputMode.full, null,
                filter, null, new HashMap<String, String>());

        if( response.getResponseCode() == ServiceResponseCode.OK ){
            if( ((List) response.getResponse()).size() == 1 )
                return Optional.ofNullable(((List) response.getResponse()).get(0));
        }     
        return Optional.ofNullable(null);
    }

    @Override
    protected void doAuthentication() throws CoreException {
        if (this.authorizationToBeRevoked == null)
            throw new SecurityException(CoreExceptionCode.FAILED_AUTHENTICATION, "Principal not found");

        if (!EntityAuthorizationHelper.isRenewable(this.authorizationToBeRevoked.getClass())) {
            throw new SecurityException(CoreExceptionCode.FAILED_AUTHENTICATION, "Authorization not renewable");
        }

        if (EntityAuthorizationHelper
                .isRefreshTokenExpired(this.authorizationToBeRevoked)) {
            throw new SecurityException(CoreExceptionCode.FAILED_AUTHENTICATION, "Refresh authorization expired");
        }

        if (EntityAuthorizationHelper.isRevoked(this.authorizationToBeRevoked)) {
            throw new SecurityException(CoreExceptionCode.FAILED_AUTHENTICATION, "Authorization revoked");
        }

        EntityAuthorizationHelper.revoke(this.authorizationToBeRevoked);
        EntityHelper.save(this.authorizationToBeRevoked, Caller.createSuperCaller(), new HashMap<>());
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
