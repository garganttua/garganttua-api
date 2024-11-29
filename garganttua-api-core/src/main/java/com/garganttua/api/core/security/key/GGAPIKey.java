package com.garganttua.api.core.security.key;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.garganttua.api.core.security.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.security.key.GGAPIKeyType;
import com.garganttua.api.spec.security.key.IGGAPIKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GGAPIKey implements IGGAPIKey {
	
	private GGAPIKeyType type;
	
	private String algorithm;
	
	private byte[] rawKey;

	@Override
	public boolean equals(Object obj) {
		return Arrays.equals(rawKey, ((GGAPIKey) obj).rawKey);
	}

	@Override
	public Key getKey() throws GGAPISecurityException {
		Key key_ = null;
		try {
			if( this.type == GGAPIKeyType.SECRET ) {
				key_ = new SecretKeySpec(this.rawKey, 0, this.rawKey.length, this.algorithm);
			}
			if( this.type == GGAPIKeyType.PRIVATE ) {
				key_ = KeyFactory.getInstance(this.algorithm).generatePrivate(new PKCS8EncodedKeySpec(this.rawKey));
			}
			if( this.type == GGAPIKeyType.PUBLIC ) {
				key_ = KeyFactory.getInstance(this.algorithm).generatePublic(new X509EncodedKeySpec(this.rawKey));
			}
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			throw new GGAPISecurityException(e);
		}
		return key_;
	}

	@Override
	public byte[] cipher(byte[] clear) throws GGAPISecurityException {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(this.algorithm);
			cipher.init(Cipher.ENCRYPT_MODE, this.getKey());
			return cipher.doFinal(clear);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | GGAPISecurityException e) {
			throw new GGAPISecurityException(e);
		}
	}

	@Override
	public byte[] uncipher(byte[] encoded) throws GGAPISecurityException {
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(this.algorithm);
			cipher.init(Cipher.DECRYPT_MODE, this.getKey());
			return cipher.doFinal(encoded);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | GGAPISecurityException e) {
			throw new GGAPISecurityException(e);
		}
	}
}
