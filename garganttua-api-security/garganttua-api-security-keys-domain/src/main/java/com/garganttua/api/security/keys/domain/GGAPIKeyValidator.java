package com.garganttua.api.security.keys.domain;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECGenParameterSpec;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.garganttua.api.security.core.exceptions.GGAPISecurityException;
import com.garganttua.api.spec.GGAPIExceptionCode;

public class GGAPIKeyValidator {
	
    static final Set<String> supportedAlgorithms = new HashSet<>();

    static {
        add("DSA", 512);
        add("DSA", 1024);
        add("DSA", 2048);
        add("RSA", 512);
        add("RSA", 1024);
        add("RSA", 2048);
        add("RSA", 4096);
//        add("RSASSA_PSS", 512);
//        add("RSASSA_PSS", 1024);
//        add("RSASSA_PSS", 2048);
//        add("RSASSA_PSS", 4096);
//        add("EC", 128);
        add("EC", 256);
        add("EC", 384);
        add("EC", 512);
        add("DH", 512);
        add("DH", 576);
        add("DH", 640);
        add("DH", 704);
        add("DH", 768);
        add("DH", 832);
        add("DH", 896);
        add("DH", 960);
        add("DH", 1024);
        add("DH", 2048);
        add("DH", 3072);
        add("DH", 4096);
        add("DH", 8192);
        add("AES", 128);
        add("AES", 192);
        add("AES", 256);
        add("HmacSHA1", 128);
        add("HmacSHA1", 256);
        add("HmacSHA1", 384);
        add("HmacSHA1", 512);
        add("HmacSHA1", 1024);
        add("HmacSHA1", 2048);
        add("HmacSHA1", 4096);
        add("HmacSHA224", 128);
        add("HmacSHA224", 256);
        add("HmacSHA224", 384);
        add("HmacSHA224", 512);
        add("HmacSHA224", 1024);
        add("HmacSHA224", 2048);
        add("HmacSHA224", 4096);
        add("HmacSHA256", 128);
        add("HmacSHA256", 256);
        add("HmacSHA256", 384);
        add("HmacSHA256", 512);
        add("HmacSHA256", 1024);
        add("HmacSHA256", 2048);
        add("HmacSHA256", 4096);
        add("HmacSHA384", 128);
        add("HmacSHA384", 256);
        add("HmacSHA384", 384);
        add("HmacSHA384", 512);
        add("HmacSHA384", 1024);
        add("HmacSHA384", 2048);
        add("HmacSHA384", 4096);
        add("HmacSHA512", 128);
        add("HmacSHA512", 256);
        add("HmacSHA512", 384);
        add("HmacSHA512", 512);
        add("HmacSHA512", 1024);
        add("HmacSHA512", 2048);
        add("HmacSHA512", 4096);
        add("ARCFOUR", 128);
        add("ARCFOUR", 256);
        add("ARCFOUR", 384);
        add("ARCFOUR", 512);
        add("ARCFOUR", 1024);
        add("Blowfish", 32);
        add("Blowfish", 40);
        add("Blowfish", 48);
        add("Blowfish", 56);
        add("Blowfish", 64);
        add("Blowfish", 72);
        add("Blowfish", 80);
        add("Blowfish", 88);
        add("Blowfish", 96);
        add("Blowfish", 104);
        add("Blowfish", 112);
        add("Blowfish", 120);
        add("Blowfish", 128);
        add("Blowfish", 136);
        add("Blowfish", 144);
        add("Blowfish", 152);
        add("Blowfish", 160);
        add("Blowfish", 168);
        add("Blowfish", 176);
        add("Blowfish", 184);
        add("Blowfish", 192);
        add("Blowfish", 200);
        add("Blowfish", 208);
        add("Blowfish", 216);
        add("Blowfish", 224);
        add("Blowfish", 232);
        add("Blowfish", 240);
        add("Blowfish", 248);
        add("Blowfish", 256);
        add("Blowfish", 264);
        add("Blowfish", 272);
        add("Blowfish", 280);
        add("Blowfish", 288);
        add("Blowfish", 296);
        add("Blowfish", 304);
        add("Blowfish", 312);
        add("Blowfish", 320);
        add("Blowfish", 328);
        add("Blowfish", 336);
        add("Blowfish", 344);
        add("Blowfish", 352);
        add("Blowfish", 360);
        add("Blowfish", 368);
        add("Blowfish", 376);
        add("Blowfish", 384);
        add("Blowfish", 392);
        add("Blowfish", 400);
        add("Blowfish", 408);
        add("Blowfish", 416);
        add("Blowfish", 424);
        add("Blowfish", 432);
        add("Blowfish", 440);
        add("Blowfish", 448);
        add("DES", 56);
        add("DESede", 112);
        add("DESede", 168);
        add("RC2", 128);
        add("RC2", 256);
        add("RC2", 384);
        add("RC2", 512);
        add("RC2", 1024);
    }

    public static String[] validateAlgorithm(String input) throws GGAPISecurityException {
        String[] parts = input.split("-");
        if (parts.length != 2) {
            throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Invalid format of "+input+", must be algo-size");
        }

        String algo = parts[0];
        int size = 0;
        try {
            size = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
        	throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Invalid size "+size);
        }

        String key = algo + "-" + size;
        if (supportedAlgorithms.contains(key)) {
            return new String[] {algo, Integer.toString(size)};
        } else {
        	throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Unsupported size or algorithm "+input);
        }
    }

    private static void add(String algo, int size) {
        supportedAlgorithms.add(algo + "-" + size);
    }
    
    public static GGAPIKeyRealmType determineAlgorithmType(String input) throws GGAPISecurityException {
        String[] infos = validateAlgorithm(input);
        
        if (isSymetricAlgorithm(infos[0])) {
            return GGAPIKeyRealmType.SYMETRIC;
        } else if (isAsymetricAlgorithm(infos[0])) {
            return GGAPIKeyRealmType.ASYMETRIC;
        } else {
        	throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, "Unsupported algorithm "+input);
        }
    }

    private static boolean isSymetricAlgorithm(String algo) {
        switch (algo) {
            case "AES":
            case "HmacSHA1":
            case "HmacSHA224":
            case "HmacSHA256":
            case "HmacSHA384":
            case "HmacSHA512":
            case "ARCFOUR":
            case "Blowfish":
            case "DES":
            case "DESede":
            case "RC2":
                return true;
            default:
                return false;
        }
    }

    private static boolean isAsymetricAlgorithm(String algo) {
        switch (algo) {
            case "DSA":
            case "RSA":
            case "RSASSA_PSS":
            case "EC":
            case "DH":
                return true;
            default:
                return false;
        }
    }
    
    public static SecretKey generateSymetricKey(String algo, int size) throws GGAPISecurityException {
        KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance(algo);
			keyGen.init(size);
			return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, e);
		}
    }

    public static KeyPair generateAsymetricKey(String algo, int size) throws GGAPISecurityException {
        KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance(algo);
			if (algo.equals("EC")) {
				if( size == 512 )
					keyGen.initialize(new ECGenParameterSpec("secp521r1"));
				else 
					keyGen.initialize(new ECGenParameterSpec("secp"+size+"r1"));
			} else {
				keyGen.initialize(size);
			}
			return keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException  e) {
			throw new GGAPISecurityException(GGAPIExceptionCode.GENERIC_SECURITY_ERROR, e);
		}
    }
}
