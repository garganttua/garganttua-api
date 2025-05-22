package com.garganttua.api.core.security.authorization.jwt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
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
import com.garganttua.api.spec.security.key.GGAPISignatureAlgorithm;
import com.garganttua.api.spec.security.key.IGGAPIKey;
import com.garganttua.api.spec.security.key.IGGAPIKeyRealm;

public class TestGGAPIJWTAuthorization {
	
	private static String RSA4096_CIPH = "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";
	private static String RSA4096_UNCIPH = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAuQ7g8TSJOauB5QZk8XsW+5gp2xE+t5xV4Xqj6XLug8p/reH4QS1ww+/Pd5ojfh1bPO3mhWea4745Ga3rGNdess9dIxh1GkB5+0OcolSKLtN5irqd0gfZtjcT18z5kV2Kro8KPBmLl2fkd7PeiZoJYhKB/H/a2V10O/xSqAVqpabsRWIMV3opUVf9EXqfbIFrIwKTchOfACiOsJiuwO8do8v8YOZp488Kk9hlCFSclnOjdPlKCJtIau4cvAFv7/IxFtsH79DpukQVZDWfP9tiC4jY77m1F+Ga4pzJlMP1tO+PVGUjnH0YgSUyUlbimcicqB1tEdM6ndHul6jaNM9SjnI+wvkxrF9XmJc+Ue2vOqFX8DUqnfnWatbOlk4y6Qj2PxZIhSRQyHGhZ0q4qGAWN3SCs9xca2ODVyw4Yvty8U3OpnBS+/TRa89ReABsvwkAac09NgTtkBcKEvKj8OTVNGuYBJOeV8Asu3qsZXncZFzz0j+Hp1CQQ80NMCTvRPsHNX9cT24WWPoY47pNYD4ezAmTLe4jZFH08r6YCZcZnDnA2vHmBnaGwjiMBmI7PFO6616aQDyiiFiUHJX0uCCS5S6clvHUy6VMDVbakV7m01Nnzw8isba4Dc0gERwoarGUELgxsEKA84bI24VoyjYl8893Shn+v55w59asfEPenOkCAwEAAQ==";
	
	private static String JWT = "ZXlKaGJHY2lPaUpTVXpVeE1pSjkuZXlKemRXSWlPaUowWlhOMElpd2lhblJwSWpvaWRHVnpkQ0lzSW5SbGJtRnVkRWxrSWpvaWRHVnpkQ0lzSW1saGRDSTZNQ3dpWlhod0lqb3dMQ0poZFhSb2IzSnBkR2xsY3lJNlcxMTkuYndwYXQ0UHVBOXRpQmdlRTZFYWVPYWtEUUoyaER6YUJTeVg2Q0hiQlhVaFJhR1M3enJHOWtic2EwMmhLYzB2QWhDSklQSjJtbVBTUlA5NFpuNGJWUXZlL1VIUlhIbnorNklGR0h3Qi8wYmZjMUx3NThGdVFFaWQzVTVNQ3BJdzRwVDYxaWdFZ1VRcFB6UnpXTjBsNUFDWmdYbWdIYks2aWtxeGpnWFpEdnV1QVVpbmpKcGhkVjNxL2gzcTE1Wlp1ZklIQ3REKzhPd0p3U1Nja2dJOXhzN0lvTmRNcGJiZFcrS04zTHpCOGNtcVhvSzYzV0NubXEvS2pmMjRsYUtnS3MzSWpoYWV2emc3TmhxWDU2WktzSkZNVktySmtjbWRPVmE4N2k1YnBIUUp3QTRHT3AxYU9iVWdreWJ0amZ1STRIdTVnY29UclBndmdOc1BMN1lYNDIzU1Y2dEE1MDRncnQySURYYjhwaTF4blZEYlRQdmFzRHh0NkhBZVBIcld5MXVwTkRlT05Pc0FDdHYvV3FRenF2cmJ3ME9mbFF0SUhIa09pNC8rRUlLZGRuL3VCOFFEOGpHNVpqVDJONjErTTl3ZEtGM1dRSDZ4WlVncGU0aHduT295Vm1qbGNGMC9ZT3MyNk8wZVVkU0VPNFVYNFJiZFkxcXA2cHBhMk9aQkM1cmo0L2J3R2dUMGIvYVhlTmpIaWlGcHVveWtJbXlWZU53YTM2WDJ3MjVrZEoza1pKWXVVUjlvRVlIZmZYdFNoMVY4WDJsVnFvMFNmRzJteGVSYit3NWVjbmFFa3E3WjNzaDAxOTlWdnNBSFBnM3M0TXRwQndIRHJyQm1rNHpEVWJnNVIvOExJa1RGR09OamNwcDhXZUVTMEcySWVBZ0N2bEVLV1ZPV25RMk09";
	
	private static IGGAPIKeyRealm realm = new IGGAPIKeyRealm() {
		
		GGAPIKey puk = new GGAPIKey(GGAPIKeyType.PUBLIC, GGAPIKeyAlgorithm.RSA_4096, Base64.getDecoder().decode(RSA4096_UNCIPH), null, null, null, GGAPISignatureAlgorithm.SHA512);
		GGAPIKey prk = new GGAPIKey(GGAPIKeyType.PRIVATE, GGAPIKeyAlgorithm.RSA_4096, Base64.getDecoder().decode(RSA4096_CIPH), null, null, null, GGAPISignatureAlgorithm.SHA512);
		
		@Override
		public String getUuid() {
			return "test";
		}
		
		@Override
		public String getName() {
			return "test";
		}
		
		@Override
		public boolean equals(IGGAPIKeyRealm object) {
			return false;
		}

		@Override
		public GGAPIKeyAlgorithm getKeyAlgorithm() {
			return GGAPIKeyAlgorithm.RSA_4096;
		}

		@Override
		public IGGAPIKey getKeyForDecryption() throws GGAPIException {
			return this.puk;
		}

		@Override
		public IGGAPIKey getKeyForEncryption() throws GGAPIException {
			return this.prk;
		}

		@Override
		public IGGAPIKey getKeyForSigning() throws GGAPIException {
			return this.prk;
		}

		@Override
		public IGGAPIKey getKeyForSignatureVerification() throws GGAPIException {
			return this.puk;
		}

		@Override
		public void revoke() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeKeyForEncryption() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public GGAPISignatureAlgorithm getSignatureAlgorithm() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'getSignatureAlgorithm'");
		}

		@Override
		public boolean isAbleToSign() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'isAbleToSign'");
		}
	};
	
	@Test
	public void test() throws InvalidKeySpecException, NoSuchAlgorithmException {
		String key = "TUlJRXZBSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS1l3Z2dTaUFnRUFBb0lCQVFDZXJadUNyZi9qdFhGUUJSVDBYSnE0VCsyOXFDVlFlb1A3eG9ZY28vSHdzRGN3M3Q0S0ZJRDdNNFNXREVZN1VjUVpqbk8yb1BtTW9WRWNOWGlBd0QzcWtjdzJiWkNlYi92ZlFsWTNQOWVWNWV3Slg1WUtSUXhTMnRCZkRHNHhIcWNqSHFicDlZM3NQSkpLemg4NWpFTXROT1pNaUw0ZkZaNzNkdjJLemJ0bEgyNk5VUGRMNHpvRHhJOHRmSGViYWp5ZVZyeWtVb0VlcU5rWVQrTnpYWWpiRUpxbE5iek9xbGw3T0lXN2JFdUFHMGxVN2M1T0JYSGw4UUgrVHdzbmFlTy9lNmcvUmFGT3BIc25sN3JVRVlyVElWSkJYOE5pVmNQc3RHdmFkdkhWdjJLbWNSelplelUrKzFHTmhFZW1qMTYvY2NpU2VnR0lhdTc0WE1XV1ZPM2ZBZ01CQUFFQ2dnRUFFYUpOengyQzBRTDMxMEtML0J0b2ViQXU4RmliTmxEa2thL3BIK05icVU4T3F4M2NHTXhZM2RIR3NSMThhKzQxSXFQeFJHdi9uNlhKSVBTVG5PSUFYNEpFRldPNERTdDVGeGJGQkdSOHZib0NCbDZsaXM3Mm1pU2F3SWdJbk9rcnFOeFRXZ1VYSXhoRGZSQmdGRXltcGRqNW82cS9UNFFKdzdXVHQvKzZETTJKekd2UUZRUFBLSTU5VzNFK1J3eHJhdFZKclhLT2IwMlpGYm1EcnpyMGRVK09HSFFEY250KytXZ0MweWx1VFNDZURGSU1mcnpPVHUxazhkTng2TU5pK3g2MG5aNTdqMEUxOE5iZElIZDQ2SmxqTFdVN3lGREFsTlJ5YTVBdzA5MFoyMVd0TWxhT2tTUC9PVWZxR1p5bG5hdzd1MzZRbDNEOWxhL2d5emFzWVFLQmdRREpEcDBQSUE4S0R1Z0owTGNUV0NGaFFzdzNmWWNGUGIrTWRkY3Arbm5wUVlnSUwwTkhNVTdTeUhraHdNWHFJT09xbVE1YUlabElTYjluQVRQd1ZYNHNKS3Z6R0FkSmFXNm9xb05WRS9rRGNZd1BMeDVubHZOeWgwRkF4dTA4ZWxCd0NialFZVy96TUFLRk5YTzV6Z29rRFpJak92Yjc1QU1hMGhkTzc2K2hvUUtCZ1FES0NraVc0ZGxISmUvS3NuOU5OTWpLYXhmcUpyN2hqYllSSlBqMWgwRXExZGdCN080ck9JYjE5N0dvRWNUek5oSmNlU3JXcHdsM2hFcHk3WlJiU0daRTdGb01SeEZuZTFBUGNxY3FZNFVvTXhaZVIyR1JIdGJ1SVk2NmV2R3BRNGpCM2kzTmN4MDNpVFZCSE55emN2SjNUNGxONlRzN1g3bnhaNzZaNkpsZmZ3S0JnQ00rVEltNk5PaXVkRDVFTjlOTVA0MnRCOTh5UWRqSldpcGsvYkorRmFDdHZBcXpQamZjak5TbUQ1VzdGUFpWalpMNkFXb0xPYk5TT2hyME93YUY5ZmdHOVoxSE9QL0ZXNEIxWTFBbWtCL3FaNExCMzdLSTQyUFFFY0FwSHF6YlEvNWZ0ZVhSTHE2U1c5NjkzK3RTRXJUL0svN0E5MHB3VFpzRitNLzljK3dCQW9HQVo0TTNSNTUwdk1BU0NBN0ZyQiswRkFra1dvSDVZQVJEMktyUnBySGlVSGo1VDVzOU53V2lGOWtNdTJoSE9MaG1WZHg2ZHpsalgwbDIvMFQ3c256NGNLMkxRU2tVSUttTURpNFd1Vi85cytmZ3VQTG5hUHVjOEtwZXZ1b0ljRWs2Z3VFN0pEeXJKbTMweStSbXFzWGt0ZjRaQWNMUjV3eVhicmhSYmZjK3hTOENnWUF2akxWaEx4eDE3QVlZUnV2YWowR2xrTHEvTmQySjh6cUVGMW83WS9QSTRJd2ZwZXhyNGkrK2J0NTduSDRnZmQ1NUhZeEpLVkhObWdXUFV2TWIva01PTXltM0NreVBPbExuZGZ2cDNod2tXQVhFalNYQkY1VWdXZGluK2FCc05sNHVTNkwxaktNWkUvUmpuTnZxaXdYVGVDU1M3a3hsWWZKNUIyWDBRYjgvQlE9PQ==";
		byte[] decodedKey = Base64.getDecoder().decode(key);
		System.out.println(new String(decodedKey));
		
		KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(decodedKey)));
	}
	
	@Test
	public void testNewTokenFromCorruptedRaw() throws GGAPIException {
		GGAPIException exception = assertThrows(GGAPIException.class, () -> {
			GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("bjkfhdjklsfsd".getBytes());
		});
		assertEquals(GGAPIExceptionCode.BAD_REQUEST, exception.getCode());
		assertEquals("Unable to decrypt JWT token from raw", exception.getMessage());
		
	}
	
	@Test
	public void testJWTByteArrayGeneration() throws GGAPIException {
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(0L));	
		auth.sign(TestGGAPIJWTAuthorization.realm);
		assertEquals(JWT, new String(Base64.getEncoder().encode(auth.toByteArray())));
	}
	
	@Test
	public void testValidationAgainst() throws GGAPIException {
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+1200L));
		GGAPIJWTAuthorization auth2 = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+1200L));
		auth.sign(TestGGAPIJWTAuthorization.realm);
		auth2.sign(TestGGAPIJWTAuthorization.realm);
		auth.validateAgainst(auth2);		
	}
	
	@Test
	public void testValidationAgainstNotTheSame() throws GGAPIException {
		Date expirationDate = new Date(new Date().getTime()+1200L);
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), expirationDate);
		GGAPIJWTAuthorization auth2 = new GGAPIJWTAuthorization("test", "toto", "test", List.of(), new Date(0L), expirationDate);
		auth.sign(TestGGAPIJWTAuthorization.realm);
		auth2.sign(TestGGAPIJWTAuthorization.realm);
		GGAPIException exception = assertThrows(GGAPIException.class, ()->{
			auth.validateAgainst(auth2, TestGGAPIJWTAuthorization.realm);		
		});
		assertEquals(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH, exception.getCode());
		assertEquals("Invalid signature", exception.getMessage());
	}
	
	@Test
	public void testValidationAgainstSignatureMismatch() throws GGAPIException {
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+120000L));
		GGAPIJWTAuthorization auth2 = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+120000L));
		auth.sign(TestGGAPIJWTAuthorization.realm);
		auth2.sign(this.createRealm(GGAPIKeyAlgorithm.RSA_4096, GGAPISignatureAlgorithm.SHA256));
		GGAPIException exception = assertThrows(GGAPIException.class, ()->{
			auth.validateAgainst(auth2, TestGGAPIJWTAuthorization.realm);		
		});
		assertEquals(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH, exception.getCode());
		assertEquals("Invalid signature", exception.getMessage());
	}
	
	@Test
	public void testValidateExpiredToken() throws GGAPIException {
		GGAPIException exception = assertThrows(GGAPIException.class, ()->{
			new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()-1200L)).validate();			
		});
		
		assertEquals(GGAPIExceptionCode.TOKEN_EXPIRED, exception.getCode());
		assertEquals("Token expired", exception.getMessage());
	}
	
	@Test
	public void testValidateRevokedToken() throws GGAPIException {
		GGAPIException exception = assertThrows(GGAPIException.class, ()->{
			GGAPIJWTAuthorization jwt = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+1200L));
			jwt.revoke();
			jwt.validate(TestGGAPIJWTAuthorization.realm);
		});
		
		assertEquals(GGAPIExceptionCode.TOKEN_REVOKED, exception.getCode());
		assertEquals("Token revoked", exception.getMessage());
	}
	
	@Test
	public void testValidate() throws GGAPIException {
		assertDoesNotThrow(()-> {
			GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+120000L));
			auth.sign(TestGGAPIJWTAuthorization.realm);
			auth.validate(TestGGAPIJWTAuthorization.realm);
			auth.validate(TestGGAPIJWTAuthorization.realm);
			auth.validate(TestGGAPIJWTAuthorization.realm);
			GGAPIJWTAuthorization jwt = new GGAPIJWTAuthorization(auth.toByteArray());
			jwt.validate(TestGGAPIJWTAuthorization.realm);
			jwt.validate(TestGGAPIJWTAuthorization.realm);
			jwt.validate(TestGGAPIJWTAuthorization.realm);
		});
	}
	
	@Test
	public void testValidateSignatureCorruption() throws GGAPIException {
		GGAPIException exception = assertThrows(GGAPIException.class, ()-> {
			GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(0L), new Date(new Date().getTime()+120000L));
			auth.sign(TestGGAPIJWTAuthorization.realm);
			byte[] authAsbyte = auth.toByteArray();
			byte[] corrupted = Arrays.copyOf(authAsbyte, authAsbyte.length -2);
			
			GGAPIJWTAuthorization jwt = new GGAPIJWTAuthorization(corrupted);
			jwt.validate(TestGGAPIJWTAuthorization.realm);
		});
		
		assertEquals(GGAPIExceptionCode.TOKEN_SIGNATURE_MISMATCH, exception.getCode());
		assertEquals("Signature verification error", exception.getMessage());
	}

	@Test
	public void testJWTAlgos() throws GGAPIException {
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.HMAC_SHA512_256, GGAPISignatureAlgorithm.HMAC_SHA512);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.HMAC_SHA512_384, GGAPISignatureAlgorithm.HMAC_SHA512);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.HMAC_SHA512_512, GGAPISignatureAlgorithm.HMAC_SHA512);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.RSA_4096, GGAPISignatureAlgorithm.SHA256);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.RSA_4096, GGAPISignatureAlgorithm.SHA384);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.RSA_4096, GGAPISignatureAlgorithm.SHA512);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.EC_256, GGAPISignatureAlgorithm.SHA256);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.EC_384, GGAPISignatureAlgorithm.SHA384);
 		this.createTokenWithNewRealm(GGAPIKeyAlgorithm.EC_512, GGAPISignatureAlgorithm.SHA512);
 		
 		GGAPISecurityException exception = assertThrows(GGAPISecurityException.class, ()-> {
 			this.createTokenWithNewRealm(GGAPIKeyAlgorithm.BLOWFISH_104, GGAPISignatureAlgorithm.SHA512);
 		});
 		
 		assertEquals("Signature error", exception.getMessage());
 		assertEquals(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, exception.getCode());
	}

	private GGAPIJWTAuthorization createTokenWithNewRealm(GGAPIKeyAlgorithm algorithm, GGAPISignatureAlgorithm signatureAlgo) throws GGAPISecurityException, GGAPIException {
		GGAPIKeyRealm realm = this.createRealm(algorithm, signatureAlgo);
		GGAPIJWTAuthorization auth = new GGAPIJWTAuthorization("test", "test", "test", List.of(), new Date(), new Date(new Date().getTime()+15000));
		auth.sign(realm);
		auth.toByteArray();
		return auth;
	}

	private GGAPIKeyRealm createRealm(GGAPIKeyAlgorithm algorithm, GGAPISignatureAlgorithm signatureAlgo) throws GGAPISecurityException {
		GGAPIKeyRealm realm = new GGAPIKeyRealm("test", algorithm, new Date(new Date().getTime()+15000), null, null, signatureAlgo);
		return realm;
	}
}
