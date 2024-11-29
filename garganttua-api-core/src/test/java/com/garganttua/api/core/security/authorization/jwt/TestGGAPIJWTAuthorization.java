package com.garganttua.api.core.security.authorization.jwt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.core.security.key.GGAPIKey;
import com.garganttua.api.core.security.key.GGAPIKeyRealm;
import com.garganttua.api.spec.GGAPIException;
import com.garganttua.api.spec.GGAPIExceptionCode;
import com.garganttua.api.spec.security.key.GGAPIKeyAlgorithm;
import com.garganttua.api.spec.security.key.GGAPIKeyType;
import com.garganttua.api.spec.security.key.IGGAPIKey;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public class TestGGAPIJWTAuthorization {
	
	private static String RSA4096_CIPH = "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";
	private static String RSA4096_UNCIPH = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAuQ7g8TSJOauB5QZk8XsW+5gp2xE+t5xV4Xqj6XLug8p/reH4QS1ww+/Pd5ojfh1bPO3mhWea4745Ga3rGNdess9dIxh1GkB5+0OcolSKLtN5irqd0gfZtjcT18z5kV2Kro8KPBmLl2fkd7PeiZoJYhKB/H/a2V10O/xSqAVqpabsRWIMV3opUVf9EXqfbIFrIwKTchOfACiOsJiuwO8do8v8YOZp488Kk9hlCFSclnOjdPlKCJtIau4cvAFv7/IxFtsH79DpukQVZDWfP9tiC4jY77m1F+Ga4pzJlMP1tO+PVGUjnH0YgSUyUlbimcicqB1tEdM6ndHul6jaNM9SjnI+wvkxrF9XmJc+Ue2vOqFX8DUqnfnWatbOlk4y6Qj2PxZIhSRQyHGhZ0q4qGAWN3SCs9xca2ODVyw4Yvty8U3OpnBS+/TRa89ReABsvwkAac09NgTtkBcKEvKj8OTVNGuYBJOeV8Asu3qsZXncZFzz0j+Hp1CQQ80NMCTvRPsHNX9cT24WWPoY47pNYD4ezAmTLe4jZFH08r6YCZcZnDnA2vHmBnaGwjiMBmI7PFO6616aQDyiiFiUHJX0uCCS5S6clvHUy6VMDVbakV7m01Nnzw8isba4Dc0gERwoarGUELgxsEKA84bI24VoyjYl8893Shn+v55w59asfEPenOkCAwEAAQ==";
	
	private static String JWT = "ZXlKaGJHY2lPaUpTVXpVeE1pSjkuZXlKMFpXNWhiblJKWkNJNkluUmxjM1FpTENKdmQyNWxja2xrSWpvaWRHVnpkQ0lzSW5WMWFXUWlPaUowWlhOMElpd2lZWFYwYUc5eWFYUnBaWE1pT2x0ZExDSnpkV0lpT2lKMFpYTjBJaXdpYVdGMElqb3dMQ0psZUhBaU9qQjkuVEVJUk9iWmdwQzJkYW84SmxTcFY1a2JQbmhsMzlNb0RyZXhSM2FQUTJ1aW8wUWk3dmN2Qk1CUWdPUjkyOFZXV2dZWEdkTFF4LVdIYXN4Tzl0UndMOGlaWU5IaGVOZjVZRnJjWHY1R3ZwdEgtdXlhOWg0bXBFaXVBSVpCeHo4RkQya0cwSmVoMVZPRC1SaVFuUVNzbEVELVVoUWNYclcyaFJvMG9vYU55OTJuQWF4V1ZVREhLa1dEMHFQUFhRVDNuR3UwU0p6b1hLXzN5amt6T3FKZHpScTUtLW1nLTQyR2s0MW9YZHdWVWtIakdDRXFqZG03empvSG5Sb2hZNjdTenFWTGtiRHhOejdPeVRkSkZGY1h3UTBWZGdTU1ZjLUFVMW5Sa1p5Tl9MbnVnNnpRSXN4eGRUN3JWU2dRMTJ4SUl1WFhSdE5tQjM5RFBHbnVzMHpoOHZqMHJveUxoTHhYS2J1QmJKMTFKNHlxY0dSRmRaZmRkNXF3ekpGb1dGMUNhNGZDMnNCQnpjUnlDQ25fVXl4RDhIVzR6NE1lQ3VLWWExWEZnUmh0ekpSRUFUNXFrMG1xa1NjNXF3ZFlDX3pIVUUtT0dUc1RxejJfTldPUDFTMnZkdW1pMGxkZ0dXa0dIbHhXUXZxR3pyRVFFWFd2WmtNbk1SR2tDaXl1d0FCc2psbzc0c0htTnNaaVpHX1FsYmxRUDhWelN3VEZxNEoyd0c4ZlZWN2NieUloSVFlcndqODZLUWtBVnRrN01EaDNhNWZDWE5COEN5M1FwSkh1VF9oVFh1X0d1S2VaN0FBYlRZRnRtdnJXQXdBRlJpVjNTYzVMVm1QYUtFSHc3dkJlZ0RXbEZiLVpnT0tIVGp5NVNlY2t3ck44YmdsMzVwV1VXRUFIaEV6a2RQYU0=";
	
	private static IGGAPIKeyRealm realm = new IGGAPIKeyRealm() {
		
		@Override
		public String getUuid() {
			return "test";
		}
		
		@Override
		public String getName() {
			return "test";
		}
		
		@Override
		public IGGAPIKey getKeyForUnciphering() throws GGAPIException {
			return new GGAPIKey(GGAPIKeyType.PUBLIC, "RSA", Base64.getDecoder().decode(RSA4096_UNCIPH));
		}
		
		@Override
		public IGGAPIKey getKeyForCiphering() throws GGAPIException {
			return new GGAPIKey(GGAPIKeyType.PRIVATE, "RSA", Base64.getDecoder().decode(RSA4096_CIPH));
		}
		
		@Override
		public GGAPIKeyAlgorithm getAlgorithm() {
			return GGAPIKeyAlgorithm.RSA_4096;
		}
		
		@Override
		public boolean equals(IGGAPIKeyRealm object) {
			return false;
		}
	};
	
	@Test
	public void testNewTokenFromCorruptedRaw() throws GGAPIException {
		GGAPIException exception = assertThrows(GGAPIException.class, () -> {
			GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("bjkfhdjklsfsd".getBytes(), realm);
		});
		assertEquals(GGAPIExceptionCode.BAD_REQUEST, exception.getCode());
		assertEquals("Unable to decrypt JWT token from raw", exception.getMessage());
		
	}
	
	@Test
	public void testJWTByteArrayGeneration() throws GGAPIException {
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(0L), realm);	
		assertEquals(JWT, new String(Base64.getEncoder().encode(auth.toByteArray())));
	}
	
	@Test
	public void testValidationAgainst() throws GGAPIException {
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+1200L), realm);
		GGAPIJWTAuthorization auth2 = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+1200L), realm);
		
		auth.validateAgainst(auth2);		
	}
	
	@Test
	public void testValidationAgainstNotTheSame() throws GGAPIException {
		Date expirationDate = new Date(new Date().getTime()+1200L);
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), expirationDate, realm);
		GGAPIJWTAuthorization auth2 = new GGAPIJWTAuthorization("test", "toto", "test", "test", List.of(), new Date(0L), expirationDate, realm);
		GGAPIException exception = assertThrows(GGAPIException.class, ()->{
			auth.validateAgainst(auth2);		
		});
		assertEquals(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, exception.getCode());
		assertEquals("Expected sub claim to be: test, but was: toto.", exception.getMessage());
	}
	
	@Test
	public void testValidationAgainstSignatureMismatch() throws GGAPIException {
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+120000L), realm);
		GGAPIJWTAuthorization auth2 = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+120000L), this.createRealm(GGAPIKeyAlgorithm.RSA_4096));
		GGAPIException exception = assertThrows(GGAPIException.class, ()->{
			auth.validateAgainst(auth2);		
		});
		assertEquals(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, exception.getCode());
		assertEquals("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.", exception.getMessage());
	}
	
	@Test
	public void testValidateExpiredToken() throws GGAPIException {
		GGAPIException exception = assertThrows(GGAPIException.class, ()->{
			new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()-1200L), realm).validate();			
		});
		
		assertEquals(GGAPIExceptionCode.TOKEN_EXPIRED, exception.getCode());
		assertEquals("Token expired", exception.getMessage());
	}
	
	@Test
	public void testValidateRevokedToken() throws GGAPIException {
		GGAPIException exception = assertThrows(GGAPIException.class, ()->{
			GGAPIJWTAuthorization jwt = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+1200L), realm);
			jwt.revoke();
			jwt.validate();
		});
		
		assertEquals(GGAPIExceptionCode.TOKEN_REVOKED, exception.getCode());
		assertEquals("Token revoked", exception.getMessage());
	}
	
	@Test
	public void testValidate() throws GGAPIException {
		assertDoesNotThrow(()-> {
			GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+120000L), realm);
			auth.validate();
			auth.validate();
			auth.validate();
			GGAPIJWTAuthorization jwt = new GGAPIJWTAuthorization(auth.toByteArray(), realm);
			jwt.validate();
			jwt.validate();
			jwt.validate();
		});
	}
	
	@Test
	public void testValidateSignatureCorruption() throws GGAPIException {
		GGAPIException exception = assertThrows(GGAPIException.class, ()-> {
			GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+120000L), realm);

			byte[] authAsbyte = auth.toByteArray();
			byte[] corrupted = Arrays.copyOf(authAsbyte, authAsbyte.length -2);
			
			GGAPIJWTAuthorization jwt = new GGAPIJWTAuthorization(corrupted, realm);
			jwt.validate();
		});
		
		assertEquals(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH, exception.getCode());
		assertEquals("Invalid signature", exception.getMessage());
	}

	@Test
	public void testJWTAlgos() throws GGAPIException {
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.HMAC_SHA512_256);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.HMAC_SHA512_384);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.HMAC_SHA512_512);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.RSA_2048);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.RSA_3072);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.RSA_4096);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.EC_256);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.EC_384);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.EC_512);
 		
 		GGAPISecurityException exception = assertThrows(GGAPISecurityException.class, ()-> {
 			this.createTokenWithNewRealm(GGAPIKeyAlgorithm.BLOWFISH_104);
 		});
 		
 		assertEquals("Unsupported Java algorithm Blowfish_104", exception.getMessage());
 		assertEquals(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, exception.getCode());
	}

	private GGAPIJWTAuthorization createTokenWithNewRealm(GGAPIKeyAlgorithm algorithm) throws GGAPISecurityException, GGAPIException {
		GGAPIKeyRealm realm = this.createRealm(algorithm);
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", "test", List.of(), new Date(), new Date(), realm);
		auth.toByteArray();
		return auth;
	}

	private GGAPIKeyRealm createRealm(GGAPIKeyAlgorithm algorithm) throws GGAPISecurityException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("test", algorithm, new Date(new Date().getTime()+15000));
		realm.createKeys(null, null);
		return realm;
	}
}
