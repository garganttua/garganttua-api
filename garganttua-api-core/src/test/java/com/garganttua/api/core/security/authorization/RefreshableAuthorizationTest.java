package com.garganttua.api.core.security.authorization;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.exceptions.SecurityException;
import com.garganttua.api.core.security.key.KeyRealm;
import com.garganttua.api.spec.CoreException;
import com.garganttua.api.spec.security.authorization.IRefreshableAuthorization;
import com.garganttua.api.spec.security.key.KeyAlgorithm;
import com.garganttua.api.spec.security.key.SignatureAlgorithm;
import com.garganttua.api.spec.security.key.IKeyRealm;

public class RefreshableAuthorizationTest {

    @Test
    public void testSHA224withRSA() throws CoreException {
        KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.RSA_4096, null,
                SignatureAlgorithm.SHA224);

        RefreshableAuthorization token = new RefreshableAuthorization("uuid", "id", "tenantId", "ownerUuid",
                null, new Date(), Date.from(Instant.now().plusSeconds(3600))) {

            @Override
            protected byte[] getSignatureFromRaw(byte[] raw) throws CoreException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getSignatureFromRaw'");
            }

            @Override
            protected byte[] getDataToSign() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
            }

            @Override
            protected void decodeFromRaw(byte[] raw) throws SecurityException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'decodeFromRaw'");
            }

            @Override
            public IRefreshableAuthorization refresh(Date newExpirationDate) throws CoreException {
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
    public void testHMAC_SHA256() throws CoreException {
        KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.HMAC_SHA256_4096, null,
                SignatureAlgorithm.HMAC_SHA256);

        RefreshableAuthorization token = new RefreshableAuthorization("uuid", "id", "tenantId", "ownerUuid",
                null, new Date(), Date.from(Instant.now().plusSeconds(3600))) {

            @Override
            protected byte[] getSignatureFromRaw(byte[] raw) throws CoreException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getSignatureFromRaw'");
            }

            @Override
            protected byte[] getDataToSign() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
            }

            @Override
            protected void decodeFromRaw(byte[] raw) throws SecurityException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'decodeFromRaw'");
            }

            @Override
            public IRefreshableAuthorization refresh(Date newExpirationDate) throws CoreException {
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
    public void testMD5withRSA() throws CoreException {
        KeyRealm realm = new KeyRealm("toto", KeyAlgorithm.RSA_3072, null, SignatureAlgorithm.MD5);

        RefreshableAuthorization token = new RefreshableAuthorization("uuid", "id", "tenantId", "ownerUuid",
                null, new Date(), Date.from(Instant.now().plusSeconds(3600))) {

            @Override
            protected byte[] getSignatureFromRaw(byte[] raw) throws CoreException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getSignatureFromRaw'");
            }

            @Override
            protected byte[] getDataToSign() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getDataToSign'");
            }

            @Override
            protected void decodeFromRaw(byte[] raw) throws SecurityException {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'decodeFromRaw'");
            }

            @Override
            public IRefreshableAuthorization refresh(Date newExpirationDate) throws CoreException {
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
