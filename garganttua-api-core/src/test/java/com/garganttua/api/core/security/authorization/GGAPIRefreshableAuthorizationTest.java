package com.garganttua.api.core.security.authorization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.security.key.GGAPIKeyRealm;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.security.authorization.IGGAPIRefreshableAuthorization;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public class GGAPIRefreshableAuthorizationTest {

    @Test
    public void testSHA224withRSA() throws GGAPIException {
        GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.RSA_4096, null,
                GGAPISignatureAlgorithm.SHA224);

        GGAPIRefreshableAuthorization token = new GGAPIRefreshableAuthorization("uuid", "id", "tenantId", "ownerUuid",
                null, new Date(), Date.from(Instant.now().plusSeconds(3600))) {

            @Override
            protected byte[] getSignatureFromRaw(byte[] raw) throws GGAPIException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getSignatureFromRaw'");
            }

            @Override
            protected byte[] getDataToSign() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
            }

            @Override
            protected void decodeFromRaw(byte[] raw) throws GGAPISecurityException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'decodeFromRaw'");
            }

            @Override
            public IGGAPIRefreshableAuthorization refresh(Date newExpirationDate) throws GGAPIException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'refresh'");
            }

        };

        token.createRefreshToken(realm, Date.from(Instant.now().plusSeconds(3600)));
        byte[] refreshToken = token.getRefreshToken();

        assertDoesNotThrow(() -> {
            token.validateRefreshToken(realm, refreshToken);
        });

    }

    @Test
    public void testHMAC_SHA256() throws GGAPIException {
        GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.HMAC_SHA256_4096, null,
                GGAPISignatureAlgorithm.HMAC_SHA256);

        GGAPIRefreshableAuthorization token = new GGAPIRefreshableAuthorization("uuid", "id", "tenantId", "ownerUuid",
                null, new Date(), Date.from(Instant.now().plusSeconds(3600))) {

            @Override
            protected byte[] getSignatureFromRaw(byte[] raw) throws GGAPIException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getSignatureFromRaw'");
            }

            @Override
            protected byte[] getDataToSign() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
            }

            @Override
            protected void decodeFromRaw(byte[] raw) throws GGAPISecurityException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'decodeFromRaw'");
            }

            @Override
            public IGGAPIRefreshableAuthorization refresh(Date newExpirationDate) throws GGAPIException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'refresh'");
            }

        };

        token.createRefreshToken(realm, Date.from(Instant.now().plusSeconds(3600)));
        byte[] refreshToken = token.getRefreshToken();

        assertDoesNotThrow(() -> {
            token.validateRefreshToken(realm, refreshToken);
        });

    }

    @Test
    public void testMD5withRSA() throws GGAPIException {
        GGAPIKeyRealm realm = new GGAPIKeyRealm("toto", GGAPIKeyAlgorithm.RSA_3072, null, GGAPISignatureAlgorithm.MD5);

        GGAPIRefreshableAuthorization token = new GGAPIRefreshableAuthorization("uuid", "id", "tenantId", "ownerUuid",
                null, new Date(), Date.from(Instant.now().plusSeconds(3600))) {

            @Override
            protected byte[] getSignatureFromRaw(byte[] raw) throws GGAPIException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getSignatureFromRaw'");
            }

            @Override
            protected byte[] getDataToSign() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
            }

            @Override
            protected void decodeFromRaw(byte[] raw) throws GGAPISecurityException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'decodeFromRaw'");
            }

            @Override
            public IGGAPIRefreshableAuthorization refresh(Date newExpirationDate) throws GGAPIException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'refresh'");
            }

        };

        token.createRefreshToken(realm, Date.from(Instant.now().plusSeconds(3600)));
        byte[] refreshToken = token.getRefreshToken();

        assertDoesNotThrow(() -> {
            token.validateRefreshToken(realm, refreshToken);
        });

    }

}
