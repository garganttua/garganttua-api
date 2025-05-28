package com.garganttua.api.core.security.authentication.authorization;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import com.garganttua.api.core.caller.GGAPICaller;
import com.garganttua.api.core.entity.tools.GGAPIEntityHelper;
import com.garganttua.api.core.filter.GGAPILiteral;
import com.garganttua.api.core.security.authentication.AbstractGGAPIAuthentication;
import com.garganttua.api.core.security.authorization.GGAPIEntityAuthorizationHelper;
import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.caller.IGGAPICaller;
import com.garganttua.api.spec.domain.IGGAPIDomain;
import com.garganttua.api.spec.engine.IGGAPIEngine;
import com.garganttua.api.spec.security.annotations.GGAPIAuthentication;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPostProcessing;
import com.garganttua.api.spec.security.annotations.GGAPIAuthenticatorSecurityPreProcessing;
import com.garganttua.api.spec.service.GGAPIReadOutputMode;
import com.garganttua.api.spec.service.GGAPIServiceResponseCode;
import com.garganttua.api.spec.service.IGGAPIServiceResponse;
import com.garganttua.reflection.GGObjectAddress;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@GGAPIAuthentication(findPrincipal = true)
public class GGAPIRefreshAuthorisationAuthentication extends AbstractGGAPIAuthentication {

    public GGAPIRefreshAuthorisationAuthentication() {
        super(null);
    }

    public GGAPIRefreshAuthorisationAuthentication(IGGAPIDomain domain) {
        super(domain);
    }

    @Inject
    private IGGAPIEngine engine;
    private Object authorizationToBeRevoked;

    @Override
    protected Object doFindPrincipal(IGGAPICaller caller) {

        
        byte[] refreshToken = ((String) this.credential).getBytes();
        
        // 1 récuperer le token access à partir du refresh token
        Optional<Object> authorization;
        try {
            
            authorization = this.findAuthorization(caller, refreshToken);
            // 2 récupérer le user à partir du ownerId du access token
            if (authorization.isPresent()) {
                this.authorizationToBeRevoked = authorization.get();
                if (GGAPIEntityAuthorizationHelper.isAuthorization(authorizationToBeRevoked)) {
                    Optional<Object> principal = this.findPrincipalFromAuthorization(caller, authorization.get());
                    if (principal.isPresent()) {
                        return principal.get();
                    }
                }
            }
        } catch (GGAPIException e) {
            log.atDebug().log("Authentication failed", e);
        }

        return null;
    }

    private Optional<Object> findPrincipalFromAuthorization(IGGAPICaller caller, Object authorization) throws GGAPIException {
        String owner = GGAPIEntityAuthorizationHelper.getOwnerId(authorization);
        String ownerDomainName = GGAPIEntityHelper.getDomainNameFromOwnerId(owner);
        String ownerUuid = GGAPIEntityHelper.getUuidFromOwnerId(owner);
        return this.engine.getRepository(ownerDomainName).getOneByUuid(caller, ownerUuid);
    }

    private Optional<Object> findAuthorization(IGGAPICaller caller, byte[] refreshToken) throws GGAPISecurityException {

        GGObjectAddress refreshTokenFieldName = GGAPIRefreshAuthorizationAuthenticatorChecker
                .checkEntityAuthenticatorClass(this.domain.getEntityClass()).refreshTokenFieldAddress();

        GGAPILiteral filter = GGAPILiteral.eq(refreshTokenFieldName.toString(), Base64.getDecoder().decode(refreshToken));
        IGGAPIServiceResponse response = this.authenticatorService.getEntities(caller, GGAPIReadOutputMode.full, null,
                filter, null, new HashMap<String, String>());

        if( response.getResponseCode() == GGAPIServiceResponseCode.OK ){
            if( ((List) response.getResponse()).size() == 1 )
                return Optional.ofNullable(((List) response.getResponse()).get(0));
        }     
        return Optional.ofNullable(null);
    }

    @Override
    protected void doAuthentication() throws GGAPIException {
        if (this.authorizationToBeRevoked == null)
            throw new GGAPISecurityException(GGAPIExceptionCode.FAILED_AUTHENTICATION, "Principal not found");

        if (!GGAPIEntityAuthorizationHelper.isRenewable(this.authorizationToBeRevoked.getClass())) {
            throw new GGAPISecurityException(GGAPIExceptionCode.FAILED_AUTHENTICATION, "Authorization not renewable");
        }

        if (GGAPIEntityAuthorizationHelper
                .isRefreshTokenExpired(this.authorizationToBeRevoked)) {
            throw new GGAPISecurityException(GGAPIExceptionCode.FAILED_AUTHENTICATION, "Refresh authorization expired");
        }

        if (GGAPIEntityAuthorizationHelper.isRevoked(this.authorizationToBeRevoked)) {
            throw new GGAPISecurityException(GGAPIExceptionCode.FAILED_AUTHENTICATION, "Authorization revoked");
        }

        GGAPIEntityAuthorizationHelper.revoke(this.authorizationToBeRevoked);
        GGAPIEntityHelper.save(this.authorizationToBeRevoked, GGAPICaller.createSuperCaller(), new HashMap<>());
        this.authenticated = true;
    }

    @GGAPIAuthenticatorSecurityPreProcessing
    public void applySecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
        // Nothing to do
    }

    @GGAPIAuthenticatorSecurityPostProcessing
    public void postProcessSecurityOnAuthenticator(IGGAPICaller caller, Object entity, Map<String, String> params) {
        // Nothing to do
    }

}
