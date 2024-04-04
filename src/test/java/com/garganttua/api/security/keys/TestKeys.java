package com.garganttua.api.security.keys;

import java.security.KeyPair;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

class TestKeys {

	@Test
	void test() {
		
//		 byte[] keyByte = Decoders.BASE64.decode(SECRET);
		 
		SecretKey key1 = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		System.out.println(Encoders.BASE64.encode(key1.getEncoded()));
		
		
		SecretKey key2 = Keys.secretKeyFor(SignatureAlgorithm.HS512);
		System.out.println(Encoders.BASE64.encode(key2.getEncoded()));
		
		byte[] key2__ = key2.getEncoded();
		SecretKey key3 = Keys.hmacShaKeyFor(key2__);
		System.out.println(Encoders.BASE64.encode(key3.getEncoded()));

		String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
		System.out.println(Encoders.BASE64.encode(Decoders.BASE64.decode(SECRET)));
		
		KeyPair keys1 = Keys.keyPairFor(SignatureAlgorithm.RS256);
		
		System.out.println(Encoders.BASE64.encode(keys1.getPrivate().getEncoded()));
		keys1.getPrivate().getEncoded();
		
	}

}
